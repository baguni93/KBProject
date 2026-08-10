package org.scoula.insurancerecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationItemDTO;

/*
 * 저장된 보험 추천 목록 조회 결과용 VO.
 * 추천 결과 테이블 + 보험 상품 + 대표 매핑 카테고리를 함께 담는다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationListItemVO {

    private Integer insuranceRecommendationId;
    private Integer spendingAnalysisId;
    private Integer insuranceProductId;

    private String insuranceName;
    private String insuranceCategory;
    private String insuranceDescription;
    private Integer monthlyPremium;
    private String insuranceImage;
    private String applicationUrl;

    private Integer spendingCategoryId;
    private String categoryName;

    // 규칙 기반 추천 이유
    private String recommendationReason;

    // 해당 보험 상품 전용 AI 추천 설명
    private String aiRecommendationSummary;

    private Integer priority;

    // 팀 규칙: VO -> DTO 변환은 VO 내부에서 수행한다.
    public InsuranceRecommendationItemDTO toDTO() {
        return InsuranceRecommendationItemDTO.builder()
                .insuranceRecommendationId(insuranceRecommendationId)
                .insuranceProductId(insuranceProductId)
                .insuranceName(insuranceName)
                .insuranceCategory(insuranceCategory)
                .insuranceDescription(insuranceDescription)
                .monthlyPremium(monthlyPremium)
                .insuranceImage(insuranceImage)
                .applicationUrl(applicationUrl)
                .spendingCategoryId(spendingCategoryId)
                .categoryName(categoryName)
                .recommendationReason(recommendationReason)
                .aiRecommendationSummary(aiRecommendationSummary)
                .priority(priority)
                .build();
    }
}
