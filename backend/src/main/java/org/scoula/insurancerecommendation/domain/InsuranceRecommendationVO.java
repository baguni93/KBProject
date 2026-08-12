package org.scoula.insurancerecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * kb_insurance_recommendation_tbl 한 행과 대응하는 저장용 VO.
 * 규칙 기반 추천 이유와 상품별 AI 추천 요약을 함께 저장한다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationVO {

    private Integer insuranceRecommendationId;
    private Integer spendingAnalysisId;
    private Integer insuranceProductId;
    private String recommendationReason;
    private String aiRecommendationSummary;
    private LocalDateTime createdAt;
}
