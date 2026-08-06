package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.dto.CardRecommendationCreateResponseDTO;
import org.scoula.cardrecommendation.dto.CardRecommendationDetailResponseDTO;
import org.scoula.cardrecommendation.dto.CardRecommendationListResponseDTO;

public interface CardRecommendationService {

    CardRecommendationCreateResponseDTO createOrReuse(
            Integer userId,
            Integer spendingAnalysisId
    );

    void validateRecommendationAnalysis(
            Integer userId,
            Integer spendingAnalysisId
    );

    CardRecommendationListResponseDTO getRecommendations(
            Integer userId,
            Integer spendingAnalysisId,
            String cardType,
            String feeMode
    );

    CardRecommendationDetailResponseDTO getRecommendationDetail(
            Integer userId,
            Integer cardRecommendationId,
            String feeMode
    );
}
