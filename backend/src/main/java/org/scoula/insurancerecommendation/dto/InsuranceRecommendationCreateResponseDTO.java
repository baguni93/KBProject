package org.scoula.insurancerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* 보험추천 생성 또는 기존 결과 재사용 요청의 응답 DTO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationCreateResponseDTO {

    private Integer spendingAnalysisId;
    private boolean created;
    private Integer recommendationCount;
    private String message;
}
