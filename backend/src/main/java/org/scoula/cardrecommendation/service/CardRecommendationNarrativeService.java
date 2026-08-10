package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.CardBenefitVO;
import org.scoula.cardrecommendation.domain.CardProductCalculationResult;
import org.scoula.cardrecommendation.domain.CardRecommendationCategoryVO;

import java.util.List;

public interface CardRecommendationNarrativeService {
    String createCardSummary(
            List<CardRecommendationCategoryVO> categories,
            CardProductCalculationResult cardResult,
            List<CardBenefitVO> benefits
    );
}
