package org.scoula.cardrecommendation.domain;

import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatus;

public class CardRecommendationTaskState {

    private final CardRecommendationTaskStatus status;
    private final Integer recommendationCount;
    private final Boolean created;
    private final String message;

    public CardRecommendationTaskState(
            CardRecommendationTaskStatus status,
            Integer recommendationCount,
            Boolean created,
            String message
    ) {
        this.status = status;
        this.recommendationCount = recommendationCount;
        this.created = created;
        this.message = message;
    }

    public CardRecommendationTaskStatus getStatus() {
        return status;
    }

    public Integer getRecommendationCount() {
        return recommendationCount;
    }

    public Boolean getCreated() {
        return created;
    }

    public String getMessage() {
        return message;
    }
}
