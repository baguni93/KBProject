package org.scoula.insurancerecommendation.service;

import org.scoula.insurancerecommendation.domain.InsuranceRecommendationCandidateVO;

/*
 * 추천 대상으로 확정된 보험 상품 한 건의 AI 맞춤 설명을 생성한다.
 * 규칙 기반 recommendationReason과는 별도 역할이다.
 */
public interface InsuranceRecommendationNarrativeService {

    String createInsuranceSummary(
            InsuranceRecommendationCandidateVO candidate
    );
}
