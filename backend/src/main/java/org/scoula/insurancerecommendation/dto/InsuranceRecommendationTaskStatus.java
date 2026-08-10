package org.scoula.insurancerecommendation.dto;

/* 보험추천 비동기 작업 상태. */
public enum InsuranceRecommendationTaskStatus {
    IDLE,
    PROCESSING,
    COMPLETED,
    FAILED
}
