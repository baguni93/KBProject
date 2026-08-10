package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.CardBenefitVO;
import org.scoula.cardrecommendation.domain.CardProductCalculationResult;
import org.scoula.cardrecommendation.domain.CardRecommendationCategoryVO;

import java.util.List;

/*
 * 카드 추천 계산 결과를 사람이 읽을 수 있는 AI 추천 설명문으로 만드는 서비스.
 *
 * 주의:
 * - 어떤 카드를 추천할지/몇 위인지 결정하지 않는다.
 * - 예상 할인액도 계산하지 않는다.
 * - 이미 계산이 끝난 카드 결과를 GPT에 설명하도록 요청하는 역할만 담당한다.
 */
public interface CardRecommendationNarrativeService {
    // 카드 한 장에 대한 전용 AI 추천문구를 생성한다.
    String createCardSummary(
            List<CardRecommendationCategoryVO> categories,
            CardProductCalculationResult cardResult,
            List<CardBenefitVO> benefits
    );
}
