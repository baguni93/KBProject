package org.scoula.insurancerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationItemDTO {

    // 저장된 추천 결과를 상세 화면으로 연결하기 위한 PK
    private Integer insuranceRecommendationId;

    private Integer insuranceProductId;
    private String insuranceName;
    private String insuranceCategory;
    private String insuranceDescription;
    private Integer monthlyPremium;
    private String insuranceImage;
    private String applicationUrl;

    // 추천 근거가 된 보험 매핑 카테고리
    private Integer spendingCategoryId;
    private String categoryName;

    // 규칙 기반 추천 근거와 AI 맞춤 설명은 서로 다른 역할이다.
    private String recommendationReason;
    private String aiRecommendationSummary;

    private Integer priority;
}
