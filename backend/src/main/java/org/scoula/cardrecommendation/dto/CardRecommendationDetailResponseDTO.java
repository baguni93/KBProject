package org.scoula.cardrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRecommendationDetailResponseDTO {
    private Integer cardRecommendationId;
    private Integer spendingAnalysisId;
    private Integer cardProductId;
    private String cardName;
    private String cardType;
    private String cardDescription;
    private String cardImage;
    private String application;
    private Integer annualFee;
    private Integer expectedBenefitAmount;
    private Integer netBenefitAmount;
    private Integer displayBenefitAmount;
    private Integer recommendationRank;
    private String feeMode;
    private String aiCardRecommendationSummary;
    private List<CardBenefitDetailDTO> benefits;
}
