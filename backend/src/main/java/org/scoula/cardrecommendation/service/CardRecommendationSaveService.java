package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.CardProductCalculationResult;

import java.util.List;

/*
 * 계산이 끝난 카드 추천 결과를 DB에 저장하는 전용 서비스.
 * 계산 로직과 DB 저장 트랜잭션을 분리하기 위해 별도 Service로 둔다.
 */
public interface CardRecommendationSaveService {
    void saveRecommendations(
            Integer spendingAnalysisId,
            List<CardProductCalculationResult> results
    );
}
