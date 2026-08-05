package org.scoula.cardrecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRecommendationBenefitDetailVO {
    private Integer cardRecommendationDetailId;
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
