package org.scoula.cardrecommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.cardrecommendation.domain.*;
import org.scoula.cardrecommendation.dto.*;
import org.scoula.cardrecommendation.mapper.CardRecommendationMapper;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CardRecommendationServiceImpl
        implements CardRecommendationService {

    private static final int REQUIRED_ANALYSIS_PERIOD = 12;
    private static final int DISPLAY_LIMIT = 3;

    private final CardRecommendationMapper cardRecommendationMapper;
    private final CardBenefitCalculator cardBenefitCalculator;
    private final CardRecommendationNarrativeService narrativeService;
    private final CardRecommendationSaveService saveService;

    @Override
    public CardRecommendationCreateResponseDTO createOrReuse(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        CardRecommendationAnalysisVO analysis =
                getValidatedAnalysis(userId, spendingAnalysisId);

        int existingCount = cardRecommendationMapper
                .countRecommendations(spendingAnalysisId);

        if (existingCount > 0) {
            return CardRecommendationCreateResponseDTO.builder()
                    .spendingAnalysisId(spendingAnalysisId)
                    .created(false)
                    .recommendationCount(existingCount)
                    .message("기존 카드 추천 결과를 불러왔습니다.")
                    .build();
        }

        List<CardProductVO> products =
                cardRecommendationMapper.selectCardProducts();

        if (products == null || products.isEmpty()) {
            throw new CustomException(
                    ErrorCode.CARD_RECOMMENDATION_PRODUCT_NOT_FOUND
            );
        }

        List<CardBenefitVO> benefits =
                cardRecommendationMapper.selectCardBenefits();

        LocalDate analysisEndDate =
                analysis.getCreatedAt().toLocalDate();
        LocalDate analysisStartDate =
                analysisEndDate.minusMonths(REQUIRED_ANALYSIS_PERIOD);

        LocalDateTime benefitStartAt =
                analysisStartDate.atStartOfDay();
        LocalDateTime benefitEndAt =
                analysisEndDate.plusDays(1).atStartOfDay();
        LocalDateTime transactionSearchStartAt =
                analysisStartDate
                        .withDayOfMonth(1)
                        .minusMonths(1)
                        .atStartOfDay();

        List<CardRecommendationTransactionVO> transactions =
                cardRecommendationMapper.selectRecommendationTransactions(
                        userId,
                        transactionSearchStartAt,
                        benefitEndAt
                );

        List<CardProductCalculationResult> results =
                cardBenefitCalculator.calculate(
                        products,
                        benefits,
                        transactions,
                        benefitStartAt,
                        benefitEndAt
                );

        assignRecommendationRanks(results);

        List<CardRecommendationCategoryVO> categories =
                cardRecommendationMapper.selectAnalysisCategories(
                        spendingAnalysisId
                );

        generateTopRecommendationSummaries(
                categories,
                results,
                benefits
        );

        /*
         * 실제 저장은 별도 빈에서 하나의 트랜잭션으로 처리한다.
         * 거래 계산과 카드별 AI 문장 생성이 모두 끝난 뒤 저장을 시작한다.
         */
        saveService.saveRecommendations(
                spendingAnalysisId,
                results
        );

        log.info(
                "카드 추천 생성 완료 userId={}, analysisId={}, count={}",
                userId,
                spendingAnalysisId,
                results.size()
        );

        return CardRecommendationCreateResponseDTO.builder()
                .spendingAnalysisId(spendingAnalysisId)
                .created(true)
                .recommendationCount(results.size())
                .message("카드 추천 결과를 생성했습니다.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public void validateRecommendationAnalysis(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        getValidatedAnalysis(userId, spendingAnalysisId);
    }

    @Override
    @Transactional(readOnly = true)
    public CardRecommendationListResponseDTO getRecommendations(
            Integer userId,
            Integer spendingAnalysisId,
            String cardTypeValue,
            String feeModeValue
    ) {
        CardRecommendationAnalysisVO analysis =
                getValidatedAnalysis(userId, spendingAnalysisId);
        CardType cardType = CardType.from(cardTypeValue);
        FeeMode feeMode = FeeMode.from(feeModeValue);

        List<CardRecommendationListItemVO> rows =
                cardRecommendationMapper.selectRecommendationList(
                        userId,
                        spendingAnalysisId,
                        cardType.name()
                );

        if (rows == null || rows.isEmpty()) {
            throw new CustomException(
                    ErrorCode.CARD_RECOMMENDATION_NOT_FOUND
            );
        }

        Comparator<CardRecommendationListItemVO> comparator =
                createDisplayComparator(feeMode);

        List<CardRecommendationListItemVO> sortedRows =
                rows.stream()
                        .sorted(comparator)
                        .limit(DISPLAY_LIMIT)
                        .collect(Collectors.toList());

        List<CardRecommendationItemDTO> items = new ArrayList<>();
        for (int index = 0; index < sortedRows.size(); index++) {
            CardRecommendationListItemVO row = sortedRows.get(index);
            items.add(
                    CardRecommendationItemDTO.builder()
                            .cardRecommendationId(
                                    row.getCardRecommendationId()
                            )
                            .cardProductId(row.getCardProductId())
                            .cardName(row.getCardName())
                            .cardType(row.getCardType())
                            .cardDescription(row.getCardDescription())
                            .cardImage(row.getCardImage())
                            .application(row.getApplication())
                            .annualFee(nullSafe(row.getAnnualFee()))
                            .expectedBenefitAmount(
                                    nullSafe(row.getExpectedBenefitAmount())
                            )
                            .displayBenefitAmount(
                                    calculateDisplayBenefit(row, feeMode)
                            )
                            .recommendationRank(index + 1)
                            .aiRecommendationSummary(
                                    row.getAiRecommendationSummary()
                            )
                            .build()
            );
        }

        return CardRecommendationListResponseDTO.builder()
                .spendingAnalysisId(spendingAnalysisId)
                .analysisPeriod(analysis.getAnalysisPeriod())
                .cardType(cardType.name())
                .feeMode(feeMode.name())
                .recommendationCount(items.size())
                .recommendations(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CardRecommendationDetailResponseDTO getRecommendationDetail(
            Integer userId,
            Integer cardRecommendationId,
            String feeModeValue
    ) {
        if (cardRecommendationId == null || cardRecommendationId <= 0) {
            throw new CustomException(
                    ErrorCode.CARD_RECOMMENDATION_NOT_FOUND
            );
        }

        FeeMode feeMode = FeeMode.from(feeModeValue);
        CardRecommendationDetailHeaderVO header =
                cardRecommendationMapper.selectRecommendationDetailHeader(
                        userId,
                        cardRecommendationId
                );

        if (header == null) {
            throw new CustomException(
                    ErrorCode.CARD_RECOMMENDATION_NOT_FOUND
            );
        }

        if (!Objects.equals(
                header.getAnalysisPeriod(),
                REQUIRED_ANALYSIS_PERIOD
        )) {
            throw new CustomException(
                    ErrorCode.CARD_RECOMMENDATION_REQUIRES_12_MONTH_ANALYSIS
            );
        }

        List<CardRecommendationBenefitDetailVO> detailRows =
                cardRecommendationMapper.selectRecommendationBenefitDetails(
                        cardRecommendationId
                );

        if (detailRows == null) {
            detailRows = List.of();
        }

        List<CardBenefitDetailDTO> benefits = detailRows.stream()
                .map(row -> CardBenefitDetailDTO.builder()
                        .cardBenefitId(row.getCardBenefitId())
                        .spendingCategoryId(row.getSpendingCategoryId())
                        .categoryName(row.getCategoryName())
                        .benefitName(row.getBenefitName())
                        .benefitAmount(row.getBenefitAmount())
                        .benefitRate(row.getBenefitRate())
                        .monthlyLimit(row.getMonthlyLimit())
                        .minimumSpendingAmount(
                                row.getMinimumSpendingAmount()
                        )
                        .benefitDescription(row.getBenefitDescription())
                        .eligibleSpendingAmount(
                                row.getEligibleSpendingAmount()
                        )
                        .eligibleTransactionCount(
                                row.getEligibleTransactionCount()
                        )
                        .eligibleMonthCount(row.getEligibleMonthCount())
                        .expectedBenefitAmount(
                                row.getExpectedBenefitAmount()
                        )
                        .build())
                .collect(Collectors.toList());

        int expectedBenefit = nullSafe(
                header.getExpectedBenefitAmount()
        );
        int annualFee = nullSafe(header.getAnnualFee());
        int netBenefit = expectedBenefit - annualFee;
        int displayRank = calculateDisplayRank(
                userId,
                header,
                feeMode
        );

        return CardRecommendationDetailResponseDTO.builder()
                .cardRecommendationId(header.getCardRecommendationId())
                .spendingAnalysisId(header.getSpendingAnalysisId())
                .cardProductId(header.getCardProductId())
                .cardName(header.getCardName())
                .cardType(header.getCardType())
                .cardDescription(header.getCardDescription())
                .cardImage(header.getCardImage())
                .application(header.getApplication())
                .annualFee(annualFee)
                .expectedBenefitAmount(expectedBenefit)
                .netBenefitAmount(netBenefit)
                .displayBenefitAmount(
                        feeMode == FeeMode.NET_BENEFIT
                                ? netBenefit
                                : expectedBenefit
                )
                .recommendationRank(displayRank)
                .feeMode(feeMode.name())
                .aiRecommendationSummary(
                        header.getAiRecommendationSummary()
                )
                .benefits(benefits)
                .build();
    }

    private int calculateDisplayRank(
            Integer userId,
            CardRecommendationDetailHeaderVO header,
            FeeMode feeMode
    ) {
        List<CardRecommendationListItemVO> sameTypeRows =
                cardRecommendationMapper.selectRecommendationList(
                        userId,
                        header.getSpendingAnalysisId(),
                        header.getCardType()
                );

        if (sameTypeRows == null || sameTypeRows.isEmpty()) {
            return nullSafe(header.getRecommendationRank());
        }

        List<CardRecommendationListItemVO> sortedRows =
                sameTypeRows.stream()
                        .sorted(createDisplayComparator(feeMode))
                        .collect(Collectors.toList());

        for (int index = 0; index < sortedRows.size(); index++) {
            if (Objects.equals(
                    sortedRows.get(index).getCardRecommendationId(),
                    header.getCardRecommendationId()
            )) {
                return index + 1;
            }
        }

        return nullSafe(header.getRecommendationRank());
    }

    private CardRecommendationAnalysisVO getValidatedAnalysis(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        if (spendingAnalysisId == null || spendingAnalysisId <= 0) {
            throw new CustomException(
                    ErrorCode.CARD_RECOMMENDATION_ANALYSIS_NOT_FOUND
            );
        }

        CardRecommendationAnalysisVO analysis =
                cardRecommendationMapper.selectRecommendationAnalysis(
                        userId,
                        spendingAnalysisId
                );

        if (analysis == null) {
            throw new CustomException(
                    ErrorCode.CARD_RECOMMENDATION_ANALYSIS_NOT_FOUND
            );
        }

        if (!Objects.equals(
                analysis.getAnalysisPeriod(),
                REQUIRED_ANALYSIS_PERIOD
        )) {
            throw new CustomException(
                    ErrorCode.CARD_RECOMMENDATION_REQUIRES_12_MONTH_ANALYSIS
            );
        }

        if (analysis.getCreatedAt() == null) {
            throw new CustomException(
                    ErrorCode.CARD_RECOMMENDATION_ANALYSIS_NOT_FOUND
            );
        }

        return analysis;
    }

    private void assignRecommendationRanks(
            List<CardProductCalculationResult> results
    ) {
        for (CardType type : CardType.values()) {
            List<CardProductCalculationResult> sameType = results.stream()
                    .filter(result -> result.getProduct() != null)
                    .filter(result -> type.name().equals(
                            result.getProduct().getCardType()
                    ))
                    .sorted(createCalculationComparator())
                    .collect(Collectors.toList());

            for (int index = 0; index < sameType.size(); index++) {
                sameType.get(index).setRecommendationRank(index + 1);
            }
        }
    }

    private void generateTopRecommendationSummaries(
            List<CardRecommendationCategoryVO> categories,
            List<CardProductCalculationResult> results,
            List<CardBenefitVO> benefits
    ) {
        results.stream()
                .filter(Objects::nonNull)
                .filter(result -> result.getProduct() != null)
                .filter(result -> result.getRecommendationRank() != null)
                .filter(result -> result.getRecommendationRank() <= DISPLAY_LIMIT)
                .forEach(result -> result.setAiRecommendationSummary(
                        narrativeService.createCardSummary(
                                categories,
                                result,
                                benefits
                        )
                ));
    }

    private Comparator<CardProductCalculationResult>
    createCalculationComparator() {
        return Comparator
                .comparingInt((CardProductCalculationResult result) ->
                        nullSafe(result.getExpectedBenefitAmount())
                )
                .reversed()
                .thenComparingInt(result ->
                        nullSafe(result.getProduct().getAnnualFee())
                )
                .thenComparingInt(result ->
                        nullSafe(result.getProduct().getCardProductId())
                );
    }

    private Comparator<CardRecommendationListItemVO>
    createDisplayComparator(FeeMode feeMode) {
        if (feeMode == FeeMode.NET_BENEFIT) {
            return Comparator
                    .comparingInt((CardRecommendationListItemVO row) ->
                            calculateDisplayBenefit(row, feeMode)
                    )
                    .reversed()
                    .thenComparing(
                            Comparator.comparingInt(
                                    (CardRecommendationListItemVO row) ->
                                            nullSafe(
                                                    row.getExpectedBenefitAmount()
                                            )
                            ).reversed()
                    )
                    .thenComparingInt(row ->
                            nullSafe(row.getCardProductId())
                    );
        }

        return Comparator
                .comparingInt((CardRecommendationListItemVO row) ->
                        nullSafe(row.getExpectedBenefitAmount())
                )
                .reversed()
                .thenComparingInt(row ->
                        nullSafe(row.getAnnualFee())
                )
                .thenComparingInt(row ->
                        nullSafe(row.getCardProductId())
                );
    }

    private int calculateDisplayBenefit(
            CardRecommendationListItemVO row,
            FeeMode feeMode
    ) {
        int expectedBenefit = nullSafe(
                row.getExpectedBenefitAmount()
        );

        if (feeMode == FeeMode.NET_BENEFIT) {
            return expectedBenefit - nullSafe(row.getAnnualFee());
        }

        return expectedBenefit;
    }

    private int nullSafe(Integer value) {
        return value == null ? 0 : value;
    }
}
