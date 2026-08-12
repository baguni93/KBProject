package org.scoula.insurancerecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.insurancerecommendation.dto.InsuranceCoverageDTO;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationDetailResponseDTO;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationTransactionDTO;

import java.time.LocalDateTime;
import java.util.List;

/*
 * 보험 추천 상세 조회의 헤더 정보용 VO.
 * 추천 결과 소유자 검증에 필요한 분석기간/생성시각과 상품 정보를 함께 가진다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationDetailHeaderVO {

    private Integer insuranceRecommendationId;
    private Integer spendingAnalysisId;
    private Integer analysisPeriod;
    private LocalDateTime analysisCreatedAt;

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

    // 선택한 보험 상품 자체의 AI 추천 설명
    private String aiRecommendationSummary;

    public InsuranceRecommendationDetailResponseDTO toDTO(
            String analysisStartDate,
            String analysisEndDate,
            List<InsuranceCoverageDTO> coverages,
            List<InsuranceRecommendationTransactionDTO> evidenceTransactions
    ) {
        return InsuranceRecommendationDetailResponseDTO.builder()
                .insuranceRecommendationId(insuranceRecommendationId)
                .spendingAnalysisId(spendingAnalysisId)
                .analysisPeriod(analysisPeriod)
                .analysisStartDate(analysisStartDate)
                .analysisEndDate(analysisEndDate)
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
                .coverages(coverages)
                .evidenceTransactions(evidenceTransactions)
                .build();
    }
}
