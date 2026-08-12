package org.scoula.insurancerecommendation.service;

import org.scoula.insurancerecommendation.domain.InsuranceRecommendationCandidateVO;

import java.util.List;

/*
 * 보험 추천 메인 화면 상단의 "AI 추천 요약"을 생성한다.
 *
 * 중요:
 * - 상품별 추천 이유는 AI를 사용하지 않는다.
 * - 전체 추천 결과에 대해 딱 한 번만 AI를 호출한다.
 */
public interface InsuranceRecommendationNarrativeService {

    String createInsuranceSummary(
            List<InsuranceRecommendationCandidateVO> candidates
    );
}
