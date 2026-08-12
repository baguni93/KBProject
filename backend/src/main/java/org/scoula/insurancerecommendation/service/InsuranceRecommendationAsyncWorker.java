package org.scoula.insurancerecommendation.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scoula.exception.CustomException;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationCreateResponseDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/* 실제 보험추천 계산/저장을 별도 스레드에서 실행하는 Worker. */
@Service
public class InsuranceRecommendationAsyncWorker {

    private static final Logger log =
            LogManager.getLogger(InsuranceRecommendationAsyncWorker.class);

    private final InsuranceRecommendationService insuranceRecommendationService;
    private final InsuranceRecommendationTaskRegistry taskRegistry;

    public InsuranceRecommendationAsyncWorker(
            InsuranceRecommendationService insuranceRecommendationService,
            InsuranceRecommendationTaskRegistry taskRegistry
    ) {
        this.insuranceRecommendationService = insuranceRecommendationService;
        this.taskRegistry = taskRegistry;
    }

    @Async
    public void execute(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        try {
            /*
             * 실제 추천 계산 + DB 저장은 기존 Service의 idempotent 메서드를 사용한다.
             * 이미 결과가 저장돼 있다면 createOrReuse가 자동으로 재사용한다.
             */
            InsuranceRecommendationCreateResponseDTO result =
                    insuranceRecommendationService.createOrReuse(
                            userId,
                            spendingAnalysisId
                    );

            taskRegistry.complete(
                    userId,
                    spendingAnalysisId,
                    result.getRecommendationCount(),
                    result.isCreated(),
                    result.getMessage()
            );

        } catch (CustomException e) {
            log.warn(
                    "비동기 보험 추천 실패 userId={}, analysisId={}, code={}",
                    userId,
                    spendingAnalysisId,
                    e.getErrorCode().getCode(),
                    e
            );

            taskRegistry.fail(
                    userId,
                    spendingAnalysisId,
                    e.getMessage()
            );

        } catch (Exception e) {
            log.error(
                    "비동기 보험 추천 처리 중 예외 userId={}, analysisId={}",
                    userId,
                    spendingAnalysisId,
                    e
            );

            taskRegistry.fail(
                    userId,
                    spendingAnalysisId,
                    "보험 추천 처리 중 오류가 발생했습니다."
            );
        }
    }
}
