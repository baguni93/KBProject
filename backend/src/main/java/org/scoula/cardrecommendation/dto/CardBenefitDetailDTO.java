package org.scoula.cardrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardBenefitDetailDTO {
    private Integer cardBenefitId;
    private Integer spendingCategoryId;
    private String categoryName;
    private String benefitName;
    private Integer benefitAmount;
    private BigDecimal benefitRate;
    private Integer monthlyLimit;
    private Integer minimumSpendingAmount;
    private String benefitDescription;
    private Integer eligibleSpendingAmount;
    private Integer eligibleTransactionCount;
    private Integer eligibleMonthCount;
    private Integer expectedBenefitAmount;
}
