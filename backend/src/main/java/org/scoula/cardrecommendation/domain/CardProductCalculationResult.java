package org.scoula.cardrecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardProductCalculationResult {
    private CardProductVO product;
    private Integer recommendationRank;
    private Integer expectedBenefitAmount;
    private List<CardBenefitCalculationResult> benefitResults;
}
