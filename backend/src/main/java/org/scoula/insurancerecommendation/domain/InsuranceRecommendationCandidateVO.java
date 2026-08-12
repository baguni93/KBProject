package org.scoula.insurancerecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationItemDTO;

import java.time.LocalDateTime;

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

    // 보험-소비 카테고리 매핑 우선순위
    private Integer priority;

    /*
     * 추천 근거 문구를 만들 때 사용할 대표 결제 1건.
     * AI가 아니라 실제 financial_transaction_tbl 데이터에서 가져온다.
     */
    private Integer evidenceTransactionId;
    private String evidenceMerchantName;
    private LocalDateTime evidenceCreatedAt;

    // 실제 결제 근거를 바탕으로 Service에서 생성하는 규칙 기반 추천 이유
    private String recommendationReason;

    /*
     * 추천 페이지 상단 "AI 추천 요약" 전용 값.
     * 상품마다 생성하지 않고 전체 추천 결과에 대해 1회만 생성하며,
     * 저장 시 첫 추천 행에만 보관한다.
     */
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
