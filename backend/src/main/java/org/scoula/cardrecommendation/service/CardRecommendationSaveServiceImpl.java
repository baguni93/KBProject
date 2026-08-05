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

@Service
@RequiredArgsConstructor
public class CardRecommendationSaveServiceImpl
        implements CardRecommendationSaveService {

    private final CardRecommendationMapper cardRecommendationMapper;

    @Override
    @Transactional
    public void saveRecommendations(
            Integer userId,
            Integer spendingAnalysisId,
            String aiSummary,
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

            int updated = cardRecommendationMapper
                    .updateCardRecommendationSummary(
                            userId,
                            spendingAnalysisId,
                            aiSummary
                    );

            if (updated != 1) {
                throw new CustomException(
                        ErrorCode.CARD_RECOMMENDATION_SAVE_FAILED
                );
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
