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
public class CardBenefitVO {
    private Integer cardBenefitId;
    private Integer cardProductId;
    private Integer spendingCategoryId;
    private String benefitName;
    private Integer benefitAmount;
    private BigDecimal benefitRate;
    private Integer monthlyLimit;
    private Integer minimumSpendingAmount;
    private String benefitDescription;
}
