package org.scoula.cardrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRecommendationItemDTO {
    private Integer cardRecommendationId;
    private Integer cardProductId;
    private String cardName;
    private String cardType;
    private String cardDescription;
    private String cardImage;
    private String application;
    private Integer annualFee;
    private Integer expectedBenefitAmount;
    private Integer displayBenefitAmount;
    private Integer recommendationRank;
    private String aiRecommendationSummary;
}
