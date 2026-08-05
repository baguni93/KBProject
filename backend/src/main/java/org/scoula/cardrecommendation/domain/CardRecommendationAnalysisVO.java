package org.scoula.cardrecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRecommendationAnalysisVO {
    private Integer spendingAnalysisId;
    private Integer userId;
    private Integer analysisPeriod;
    private LocalDateTime createdAt;
    private String aiCardRecommendationSummary;
}
