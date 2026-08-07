package org.scoula.cardrecommendation.dto;

public enum CardRecommendationTaskStatus {
    IDLE, // 카드 추천을 시작하지 않은 단계
    PROCESSING, //카드 추천을 계산하고 있는 상태
    COMPLETED, // 카드 추천이 정상적으로 끝난 상태
    FAILED // 작업도중 예외가 발생한 상태
}
