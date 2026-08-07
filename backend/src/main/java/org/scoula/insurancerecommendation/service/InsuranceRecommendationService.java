package org.scoula.insurancerecommendation.service;

import org.scoula.insurancerecommendation.domain.InsuranceRecommendationCandidateVO;

import java.util.List;

public interface InsuranceRecommendationService {

    List<InsuranceRecommendationCandidateVO> getRecommendationCandidates(
            Integer userId,
            Integer spendingAnalysisId
    );
}