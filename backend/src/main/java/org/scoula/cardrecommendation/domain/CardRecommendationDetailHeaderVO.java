package org.scoula.cardrecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRecommendationDetailHeaderVO {
    private Integer cardRecommendationId;
    private Integer spendingAnalysisId;
    private Integer analysisPeriod;
    private Integer cardProductId;
    private Integer recommendationRank;
    private Integer expectedBenefitAmount;
    private String cardName;
    private String cardType;
    private String cardDescription;
    private String cardImage;
    private String application;
    private Integer annualFee;
    private String aiRecommendationSummary;
}
