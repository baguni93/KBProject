package org.scoula.insurancerecommendation.service;

import org.scoula.insurancerecommendation.domain.InsuranceRecommendationTaskState;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationTaskStatus;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationTaskStatusDTO;
import org.scoula.insurancerecommendation.mapper.InsuranceRecommendationMapper;
import org.springframework.stereotype.Service;

@Service
public class InsuranceRecommendationAsyncServiceImpl
        implements InsuranceRecommendationAsyncService {

    private final InsuranceRecommendationService insuranceRecommendationService;
    private final InsuranceRecommendationMapper insuranceRecommendationMapper;
    private final InsuranceRecommendationTaskRegistry taskRegistry;
    private final InsuranceRecommendationAsyncWorker asyncWorker;

    public InsuranceRecommendationAsyncServiceImpl(
            InsuranceRecommendationService insuranceRecommendationService,
            InsuranceRecommendationMapper insuranceRecommendationMapper,
            InsuranceRecommendationTaskRegistry taskRegistry,
            InsuranceRecommendationAsyncWorker asyncWorker
    ) {
        this.insuranceRecommendationService = insuranceRecommendationService;
        this.insuranceRecommendationMapper = insuranceRecommendationMapper;
        this.taskRegistry = taskRegistry;
        this.asyncWorker = asyncWorker;
    }

    @Override
    public InsuranceRecommendationTaskStatusDTO start(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        insuranceRecommendationService.validateRecommendationAnalysis(
                userId,
                spendingAnalysisId
        );

        InsuranceRecommendationTaskState current =
                taskRegistry.get(userId, spendingAnalysisId);

        // 이미 실행 중이면 같은 작업을 중복 시작하지 않는다.
        if (current != null
                && current.getStatus()
                == InsuranceRecommendationTaskStatus.PROCESSING) {
            return current.toDTO(spendingAnalysisId);
        }

        /*
         * 새 DB에는 소비분석 단위의 보험추천 완료 summary가 없다.
         * 저장된 추천 행이 존재하면 서버 재시작 뒤에도 COMPLETED로 복원한다.
         */
        int existingCount = insuranceRecommendationMapper
                .countRecommendations(spendingAnalysisId);

        if (existingCount > 0) {
            taskRegistry.complete(
                    userId,
                    spendingAnalysisId,
                    existingCount,
                    false,
                    "기존 보험 추천 결과를 불러왔습니다."
            );

            return taskRegistry
                    .get(userId, spendingAnalysisId)
                    .toDTO(spendingAnalysisId);
        }

        boolean started = taskRegistry.begin(
                userId,
                spendingAnalysisId
        );

        InsuranceRecommendationTaskState processing =
                taskRegistry.get(userId, spendingAnalysisId);

        if (started) {
            try {
                asyncWorker.execute(userId, spendingAnalysisId);
            } catch (RuntimeException e) {
                taskRegistry.fail(
                        userId,
                        spendingAnalysisId,
                        "보험 추천 작업을 시작하지 못했습니다."
                );
                throw e;
            }
        }

        return processing.toDTO(spendingAnalysisId);
    }

    @Override
    public InsuranceRecommendationTaskStatusDTO getStatus(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        insuranceRecommendationService.validateRecommendationAnalysis(
                userId,
                spendingAnalysisId
        );

        InsuranceRecommendationTaskState state =
                taskRegistry.get(userId, spendingAnalysisId);

        if (state != null) {
            return state.toDTO(spendingAnalysisId);
        }

        int existingCount = insuranceRecommendationMapper
                .countRecommendations(spendingAnalysisId);

        if (existingCount > 0) {
            taskRegistry.complete(
                    userId,
                    spendingAnalysisId,
                    existingCount,
                    false,
                    "저장된 보험 추천 결과가 있습니다."
            );

            return taskRegistry
                    .get(userId, spendingAnalysisId)
                    .toDTO(spendingAnalysisId);
        }

        return new InsuranceRecommendationTaskStatusDTO(
                spendingAnalysisId,
                InsuranceRecommendationTaskStatus.IDLE.name(),
                0,
                null,
                "진행 중인 보험 추천 작업이 없습니다."
        );
    }
}
