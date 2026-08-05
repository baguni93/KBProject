package org.scoula.cardrecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRecommendationDetailVO {
    private Integer cardRecommendationDetailId;
    private Integer cardRecommendationId;
    private Integer cardBenefitId;
    private Integer eligibleSpendingAmount;
    private Integer eligibleTransactionCount;
    private Integer eligibleMonthCount;
    private Integer expectedBenefitAmount;
}
