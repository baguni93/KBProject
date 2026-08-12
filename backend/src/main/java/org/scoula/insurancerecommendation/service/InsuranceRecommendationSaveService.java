package org.scoula.insurancerecommendation.service;

import org.scoula.insurancerecommendation.domain.InsuranceRecommendationCandidateVO;

import java.util.List;

/* 추천 결과 DB 저장을 하나의 트랜잭션으로 처리하는 전용 Service. */
public interface InsuranceRecommendationSaveService {

    void saveRecommendations(
            Integer spendingAnalysisId,
            List<InsuranceRecommendationCandidateVO> candidates
    );
}
