package org.scoula.cardrecommendation.domain;

import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatus;


// 카드 추천시 비동기 작업을 현재상태를 메모리에 저장하기 위한 객체.
public class CardRecommendationTaskState {


    private final CardRecommendationTaskStatus status; // 현재 상태 (4가지)
    private final Integer recommendationCount; // 완료된 카드 추천 결과 개수
    private final Boolean created; // 이번 요청에서 추천결과를 새로 만들었는지?
    private final String message; // 프론트에 보여줄 상태 설명

    // 생성자. 객체의 필드를 수정하는 방식이 아니라, 상태 변경시, 새로운 객체를 만들어 저장하는 방식
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
