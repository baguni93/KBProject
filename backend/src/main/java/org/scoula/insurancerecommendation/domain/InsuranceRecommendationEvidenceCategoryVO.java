package org.scoula.insurancerecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationEvidenceCategoryDTO;

import java.time.LocalDateTime;

/*
 * 추천 결과 전체의 "추천 근거 상세" 화면에서 사용할
 * 소비 카테고리별 실제 거래 집계 결과 VO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationEvidenceCategoryVO {

    private Integer spendingCategoryId;
    private String categoryName;
    private Integer transactionCount;
    private Integer totalAmount;
    private LocalDateTime latestTransactionAt;

    public InsuranceRecommendationEvidenceCategoryDTO toDTO() {
        return InsuranceRecommendationEvidenceCategoryDTO.builder()
                .spendingCategoryId(spendingCategoryId)
                .categoryName(categoryName)
                .transactionCount(transactionCount)
                .totalAmount(totalAmount)
                .latestTransactionAt(latestTransactionAt)
                .build();
    }
}
