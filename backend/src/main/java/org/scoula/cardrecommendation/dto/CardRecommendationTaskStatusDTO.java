package org.scoula.cardrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRecommendationTaskStatusDTO {

    private Integer spendingAnalysisId;
    private String status;
    private Integer recommendationCount;
    private Boolean created;
    private String message;
}
