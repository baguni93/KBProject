package org.scoula.cardrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRecommendationCreateResponseDTO {
    private Integer spendingAnalysisId;
    private boolean created;
    private Integer recommendationCount;
    private String message;
}
