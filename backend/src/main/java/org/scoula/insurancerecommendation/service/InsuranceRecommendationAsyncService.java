package org.scoula.insurancerecommendation.service;

import org.scoula.insurancerecommendation.dto.InsuranceRecommendationTaskStatusDTO;

public interface InsuranceRecommendationAsyncService {

    InsuranceRecommendationTaskStatusDTO start(
            Integer userId,
            Integer spendingAnalysisId
    );

    InsuranceRecommendationTaskStatusDTO getStatus(
            Integer userId,
            Integer spendingAnalysisId
    );
}
