package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.dto.CardRecommendationCreateResponseDTO;
import org.scoula.cardrecommendation.dto.CardRecommendationDetailResponseDTO;
import org.scoula.cardrecommendation.dto.CardRecommendationListResponseDTO;

/*
 * 카드 추천 기능의 핵심 비즈니스 서비스 인터페이스.
 *
 * - 추천 생성/기존 결과 재사용
 * - 추천 목록 조회
 * - 카드별 상세 조회
 * - 12개월 분석 검증
 * 을 담당한다.
 */
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
