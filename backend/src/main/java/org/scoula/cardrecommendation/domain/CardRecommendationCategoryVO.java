package org.scoula.cardrecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRecommendationCategoryVO {
    private Integer spendingCategoryId;
    private String categoryName;
    private Integer spendingAmount;
    private BigDecimal spendingRatio;
    private Integer transactionCount;
}
