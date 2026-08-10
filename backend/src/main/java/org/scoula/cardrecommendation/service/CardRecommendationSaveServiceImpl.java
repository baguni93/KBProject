package org.scoula.cardrecommendation.service;

import lombok.RequiredArgsConstructor;
import org.scoula.cardrecommendation.domain.*;
import org.scoula.cardrecommendation.mapper.CardRecommendationMapper;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/*
 * 카드 추천 결과 저장 담당.
 *
 * 한 카드에 대해
 * 1) card_recommendation_tbl에 순위/예상혜택/AI문구 저장
 * 2) card_recommendation_benefit_result_tbl 등에 혜택별 계산 결과 저장
 * 을 하나의 트랜잭션으로 묶는다.
 *
 * 저장 중 오류가 발생하면 중간 데이터만 남지 않도록 전체 롤백된다.
 */
@Service
@RequiredArgsConstructor
public class CardRecommendationSaveServiceImpl
        implements CardRecommendationSaveService {

    private final CardRecommendationMapper cardRecommendationMapper;

    /*
     * 모든 카드 계산 결과를 순서대로 저장한다.
     * aiRecommendationSummary는 TOP 3 카드에는 생성된 값이 들어가고,
     * AI 문구를 만들지 않은 카드에는 null일 수 있다.
     */
    @Override
    @Transactional
    public void saveRecommendations(
            Integer spendingAnalysisId,
            List<CardProductCalculationResult> results
    ) {
        try {
            for (CardProductCalculationResult result : results) {
                CardRecommendationVO recommendation =
                        CardRecommendationVO.builder()
                                .spendingAnalysisId(spendingAnalysisId)
                                .cardProductId(
                                        result.getProduct().getCardProductId()
                                )
                                .recommendationRank(
                                        result.getRecommendationRank()
                                )
                                .expectedBenefitAmount(
                                        result.getExpectedBenefitAmount()
                                )
                                .aiRecommendationSummary(
                                        result.getAiRecommendationSummary()
                                )
                                .build();

                int inserted = cardRecommendationMapper
                        .insertCardRecommendation(recommendation);

                if (inserted != 1
                        || recommendation.getCardRecommendationId() == null) {
                    throw new CustomException(
                            ErrorCode.CARD_RECOMMENDATION_SAVE_FAILED
                    );
                }

                List<CardRecommendationDetailVO> details =
                        result.getBenefitResults().stream()
                                .map(benefitResult ->
                                        CardRecommendationDetailVO.builder()
                                                .cardRecommendationId(
                                                        recommendation
                                                                .getCardRecommendationId()
                                                )
                                                .cardBenefitId(
                                                        benefitResult
                                                                .getCardBenefitId()
                                                )
                                                .eligibleSpendingAmount(
                                                        benefitResult
                                                                .getEligibleSpendingAmount()
                                                )
                                                .eligibleTransactionCount(
                                                        benefitResult
                                                                .getEligibleTransactionCount()
                                                )
                                                .eligibleMonthCount(
                                                        benefitResult
                                                                .getEligibleMonthCount()
                                                )
                                                .expectedBenefitAmount(
                                                        benefitResult
                                                                .getExpectedBenefitAmount()
                                                )
                                                .build()
                                )
                                .collect(Collectors.toList());

                if (!details.isEmpty()) {
                    int detailInserted = cardRecommendationMapper
                            .insertCardRecommendationDetails(details);

                    if (detailInserted != details.size()) {
                        throw new CustomException(
                                ErrorCode.CARD_RECOMMENDATION_SAVE_FAILED
                        );
                    }
                }
            }

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(
                    ErrorCode.CARD_RECOMMENDATION_SAVE_FAILED
            );
        }
    }
}
