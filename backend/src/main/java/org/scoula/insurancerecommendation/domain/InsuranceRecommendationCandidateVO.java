package org.scoula.insurancerecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationItemDTO;

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

    // 보험-소비 카테고리 매핑에서 가져오는 규칙 기반 추천 이유
    private String recommendationReason;
    private Integer priority;

    // 추천 후보가 확정된 뒤 AI가 생성하는 상품별 맞춤 설명
    private String aiRecommendationSummary;

    /*
     * 팀 공통 규칙에 따라 VO -> DTO 변환은 VO 내부에서 담당한다.
     * 이 VO는 아직 DB에 저장되기 전의 "추천 후보"이므로
     * insuranceRecommendationId는 null 상태로 반환된다.
     */
    public InsuranceRecommendationItemDTO toDTO() {
        return InsuranceRecommendationItemDTO.builder()
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
