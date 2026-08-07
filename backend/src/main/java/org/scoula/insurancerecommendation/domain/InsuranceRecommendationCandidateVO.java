package org.scoula.insurancerecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationCandidateVO {

    // 보험 상품 정보
    private Integer insuranceProductId;
    private String insuranceName;
    private String insuranceCategory;
    private String insuranceDescription;
    private Integer monthlyPremium;
    private String insuranceImage;
    private String applicationUrl;

    // 추천 근거가 된 소비 카테고리 정보
    private Integer spendingCategoryId;
    private String categoryName;
    private Integer parentCategoryId;

    // 보험-소비 카테고리 매핑 정보
    private String recommendationReason;
    private Integer priority;
}