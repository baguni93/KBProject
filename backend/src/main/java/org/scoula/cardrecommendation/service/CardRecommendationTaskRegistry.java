package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.CardRecommendationTaskState;
import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CardRecommendationTaskRegistry {

    private final ConcurrentMap<String, CardRecommendationTaskState> taskStates =
            new ConcurrentHashMap<>();

    public CardRecommendationTaskState get(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        return taskStates.get(createKey(userId, spendingAnalysisId));
    }

    public boolean begin(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        AtomicBoolean started = new AtomicBoolean(false);
        String key = createKey(userId, spendingAnalysisId);

        taskStates.compute(key, (ignored, current) -> {
            if (current != null
                    && current.getStatus()
                    == CardRecommendationTaskStatus.PROCESSING) {
                return current;
            }

            started.set(true);
            return new CardRecommendationTaskState(
                    CardRecommendationTaskStatus.PROCESSING,
                    null,
                    null,
                    "카드 혜택을 계산하고 있습니다."
            );
        });

        return started.get();
    }

    public void complete(
            Integer userId,
            Integer spendingAnalysisId,
            Integer recommendationCount,
            Boolean created,
            String message
    ) {
        taskStates.put(
                createKey(userId, spendingAnalysisId),
                new CardRecommendationTaskState(
                        CardRecommendationTaskStatus.COMPLETED,
                        recommendationCount,
                        created,
                        message == null || message.isBlank()
                                ? "카드 추천이 완료되었습니다."
                                : message
                )
        );
    }

    public void fail(
            Integer userId,
            Integer spendingAnalysisId,
            String message
    ) {
        taskStates.put(
                createKey(userId, spendingAnalysisId),
                new CardRecommendationTaskState(
                        CardRecommendationTaskStatus.FAILED,
                        null,
                        null,
                        message == null || message.isBlank()
                                ? "카드 추천 처리 중 오류가 발생했습니다."
                                : message
                )
        );
    }

    private String createKey(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        return userId + ":" + spendingAnalysisId;
    }
}
