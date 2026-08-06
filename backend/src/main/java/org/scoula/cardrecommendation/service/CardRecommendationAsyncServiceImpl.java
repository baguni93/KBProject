package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.CardRecommendationTaskState;
import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatus;
import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatusDTO;
import org.scoula.cardrecommendation.mapper.CardRecommendationMapper;
import org.springframework.stereotype.Service;

@Service
public class CardRecommendationAsyncServiceImpl
        implements CardRecommendationAsyncService {

    private final CardRecommendationService cardRecommendationService;
    private final CardRecommendationMapper cardRecommendationMapper;
    private final CardRecommendationTaskRegistry taskRegistry;
    private final CardRecommendationAsyncWorker asyncWorker;

    public CardRecommendationAsyncServiceImpl(
            CardRecommendationService cardRecommendationService,
            CardRecommendationMapper cardRecommendationMapper,
            CardRecommendationTaskRegistry taskRegistry,
            CardRecommendationAsyncWorker asyncWorker
    ) {
        this.cardRecommendationService = cardRecommendationService;
        this.cardRecommendationMapper = cardRecommendationMapper;
        this.taskRegistry = taskRegistry;
        this.asyncWorker = asyncWorker;
    }

    @Override
    public CardRecommendationTaskStatusDTO start(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        cardRecommendationService.validateRecommendationAnalysis(
                userId,
                spendingAnalysisId
        );

        CardRecommendationTaskState current =
                taskRegistry.get(userId, spendingAnalysisId);

        if (current != null
                && current.getStatus()
                == CardRecommendationTaskStatus.PROCESSING) {
            return toDTO(spendingAnalysisId, current);
        }

        int existingCount = cardRecommendationMapper
                .countRecommendations(spendingAnalysisId);

        if (existingCount > 0) {
            taskRegistry.complete(
                    userId,
                    spendingAnalysisId,
                    existingCount,
                    false,
                    "기존 카드 추천 결과를 불러왔습니다."
            );
            return toDTO(
                    spendingAnalysisId,
                    taskRegistry.get(userId, spendingAnalysisId)
            );
        }

        boolean started = taskRegistry.begin(userId, spendingAnalysisId);
        CardRecommendationTaskState processing =
                taskRegistry.get(userId, spendingAnalysisId);

        if (started) {
            try {
                asyncWorker.execute(userId, spendingAnalysisId);
            } catch (RuntimeException e) {
                taskRegistry.fail(
                        userId,
                        spendingAnalysisId,
                        "카드 추천 작업을 시작하지 못했습니다."
                );
                throw e;
            }
        }

        return toDTO(spendingAnalysisId, processing);
    }

    @Override
    public CardRecommendationTaskStatusDTO getStatus(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        cardRecommendationService.validateRecommendationAnalysis(
                userId,
                spendingAnalysisId
        );

        CardRecommendationTaskState state =
                taskRegistry.get(userId, spendingAnalysisId);

        if (state != null) {
            return toDTO(spendingAnalysisId, state);
        }

        int existingCount = cardRecommendationMapper
                .countRecommendations(spendingAnalysisId);

        if (existingCount > 0) {
            taskRegistry.complete(
                    userId,
                    spendingAnalysisId,
                    existingCount,
                    false,
                    "저장된 카드 추천 결과가 있습니다."
            );
            return toDTO(
                    spendingAnalysisId,
                    taskRegistry.get(userId, spendingAnalysisId)
            );
        }

        return new CardRecommendationTaskStatusDTO(
                spendingAnalysisId,
                CardRecommendationTaskStatus.IDLE.name(),
                0,
                null,
                "진행 중인 카드 추천 작업이 없습니다."
        );
    }

    private CardRecommendationTaskStatusDTO toDTO(
            Integer spendingAnalysisId,
            CardRecommendationTaskState state
    ) {
        return new CardRecommendationTaskStatusDTO(
                spendingAnalysisId,
                state.getStatus().name(),
                state.getRecommendationCount(),
                state.getCreated(),
                state.getMessage()
        );
    }
}
