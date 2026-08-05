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
public class CardRecommendationListResponseDTO {
    private Integer spendingAnalysisId;
    private Integer analysisPeriod;
    private String cardType;
    private String feeMode;
    private String aiCardRecommendationSummary;
    private Integer recommendationCount;
    private List<CardRecommendationItemDTO> recommendations;
}
