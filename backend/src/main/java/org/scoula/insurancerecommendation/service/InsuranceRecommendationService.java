package org.scoula.insurancerecommendation.service;

import org.scoula.insurancerecommendation.dto.*;

public interface InsuranceRecommendationService {

    /* 실제 거래를 분석해 추천 결과를 생성하거나, 이미 저장된 결과를 재사용한다. */
    InsuranceRecommendationCreateResponseDTO createOrReuse(
            Integer userId,
            Integer spendingAnalysisId
    );

    /* 비동기 시작 전에 소비분석 소유권과 12개월 여부만 검증할 때 사용한다. */
    void validateRecommendationAnalysis(
            Integer userId,
            Integer spendingAnalysisId
    );

    /* 전체 보험 둘러보기 목록 조회 */
    InsuranceProductListResponseDTO getInsuranceProducts(
            String category
    );

    /* 추천 여부와 무관한 보험상품 상세 조회 */
    InsuranceProductDetailResponseDTO getInsuranceProductDetail(
            Integer insuranceProductId
    );

    /* 저장된 보험추천 목록 조회 */
    InsuranceRecommendationListResponseDTO getRecommendations(
            Integer userId,
            Integer spendingAnalysisId
    );

    /* 추천 근거 상세 화면용 카테고리별 실제 거래 집계 */
    InsuranceRecommendationEvidenceResponseDTO getRecommendationEvidence(
            Integer userId,
            Integer spendingAnalysisId
    );

    /* 저장된 보험추천 한 건의 상품/보장/근거 거래 상세 조회 */
    InsuranceRecommendationDetailResponseDTO getRecommendationDetail(
            Integer userId,
            Integer insuranceRecommendationId
    );
}
