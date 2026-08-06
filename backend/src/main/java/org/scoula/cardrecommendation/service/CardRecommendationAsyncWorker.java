package org.scoula.cardrecommendation.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scoula.cardrecommendation.dto.CardRecommendationCreateResponseDTO;
import org.scoula.exception.CustomException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CardRecommendationAsyncWorker {

    private static final Logger log =
            LogManager.getLogger(CardRecommendationAsyncWorker.class);

    private final CardRecommendationService cardRecommendationService;
    private final CardRecommendationTaskRegistry taskRegistry;

    public CardRecommendationAsyncWorker(
            CardRecommendationService cardRecommendationService,
            CardRecommendationTaskRegistry taskRegistry
    ) {
        this.cardRecommendationService = cardRecommendationService;
        this.taskRegistry = taskRegistry;
    }

    @Async
    public void execute(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        try {
            CardRecommendationCreateResponseDTO result =
                    cardRecommendationService.createOrReuse(
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
                    "비동기 카드 추천 실패 userId={}, analysisId={}, code={}",
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
                    "비동기 카드 추천 처리 중 예외 userId={}, analysisId={}",
                    userId,
                    spendingAnalysisId,
                    e
            );
            taskRegistry.fail(
                    userId,
                    spendingAnalysisId,
                    "카드 추천 처리 중 오류가 발생했습니다."
            );
        }
    }
}
