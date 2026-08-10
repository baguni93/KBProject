package org.scoula.insurancerecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationTransactionDTO;

import java.time.LocalDateTime;

/* 추천 근거가 된 실제 금융거래 조회 결과용 VO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationTransactionVO {

    private Integer transactionId;
    private String merchantName;
    private Integer amount;
    private LocalDateTime createdAt;
    private Integer spendingCategoryId;
    private String categoryName;
    private Integer parentCategoryId;
    private String parentCategoryName;

    public InsuranceRecommendationTransactionDTO toDTO() {
        return InsuranceRecommendationTransactionDTO.builder()
                .transactionId(transactionId)
                .merchantName(merchantName)
                .amount(amount)
                .createdAt(createdAt)
                .spendingCategoryId(spendingCategoryId)
                .categoryName(categoryName)
                .parentCategoryId(parentCategoryId)
                .parentCategoryName(parentCategoryName)
                .build();
    }
}
