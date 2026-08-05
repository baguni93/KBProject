package org.scoula.cardrecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRecommendationVO {
    private Integer cardRecommendationId;
    private Integer spendingAnalysisId;
    private Integer cardProductId;
    private Integer recommendationRank;
    private Integer expectedBenefitAmount;
    private LocalDateTime createdAt;
}
