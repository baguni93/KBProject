package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatusDTO;

public interface CardRecommendationAsyncService {

    CardRecommendationTaskStatusDTO start(
            Integer userId,
            Integer spendingAnalysisId
    );

    CardRecommendationTaskStatusDTO getStatus(
            Integer userId,
            Integer spendingAnalysisId
    );
}
