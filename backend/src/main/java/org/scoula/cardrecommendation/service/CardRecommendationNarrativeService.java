package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.CardProductCalculationResult;
import org.scoula.cardrecommendation.domain.CardRecommendationCategoryVO;

import java.util.List;

public interface CardRecommendationNarrativeService {
    String createSummary(
            List<CardRecommendationCategoryVO> categories,
            CardProductCalculationResult topCreditCard,
            CardProductCalculationResult topCheckCard
    );
}
