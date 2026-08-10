package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.CardProductCalculationResult;

import java.util.List;

public interface CardRecommendationSaveService {
    void saveRecommendations(
            Integer spendingAnalysisId,
            List<CardProductCalculationResult> results
    );
}
