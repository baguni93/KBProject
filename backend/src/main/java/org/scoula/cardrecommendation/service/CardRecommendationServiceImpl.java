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

/*
 * 카드 추천 기능의 전체 흐름을 조립하는 핵심 Service.
 *
 * [추천 생성 흐름]
 * 1) 해당 소비분석이 현재 사용자 소유인지 + 12개월 분석인지 검증
 * 2) 이미 card_recommendation_tbl에 결과가 있으면 기존 결과 재사용
 * 3) 카드 상품/혜택/최근 12개월 거래 조회
 * 4) CardBenefitCalculator로 카드별 실제 예상 혜택 계산
 * 5) 신용카드와 체크카드를 각각 별도로 순위 산정
 * 6) 각 유형별 TOP 3 결과만 최종 저장 대상으로 필터링
 * 7) TOP 3 카드만 NarrativeService로 AI 추천문구 생성
 * 8) SaveService를 통해 TOP 3 결과와 AI 문구 DB 저장
 *
 * [조회 흐름]
 * - 목록: CREDIT/CHECK + 연회비 계산 방식에 따라 정렬 후 최대 3개 반환
 * - 상세: 선택한 cardRecommendationId의 카드/혜택/AI 설명 반환
 */
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

    /*
     * 카드 추천 생성의 핵심 메서드.
     * AsyncWorker에서도 결국 이 메서드를 호출한다.
     * 이미 저장된 추천 결과가 있으면 재계산/GPT 호출 없이 기존 결과를 재사용한다.
     */
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

        // 실제 거래에 카드 혜택 규칙을 대입해 모든 카드의 예상 할인액을 계산한다.
        List<CardProductCalculationResult> results =
                cardBenefitCalculator.calculate(
                        products,
                        benefits,
                        transactions,
                        benefitStartAt,
                        benefitEndAt
                );

        // CREDIT / CHECK를 서로 섞지 않고 카드 유형별로 각각 순위를 부여한다.
        assignRecommendationRanks(results);

        List<CardRecommendationCategoryVO> categories =
                cardRecommendationMapper.selectAnalysisCategories(
                        spendingAnalysisId
                );

        /*
         * CREDIT / CHECK 각각 3위까지만 최종 추천 결과로 확정한다.
         * 전체 카드는 예상 혜택 계산과 순위 산정에 사용하지만,
         * DB에는 화면에서 실제 사용하는 TOP 3 결과만 저장한다.
         */
        List<CardProductCalculationResult> topResults = results.stream()
                .filter(Objects::nonNull)
                .filter(result -> result.getProduct() != null)
                .filter(result -> result.getRecommendationRank() != null)
                .filter(result -> result.getRecommendationRank() <= DISPLAY_LIMIT)
                .collect(Collectors.toList());

        // 최종 저장 대상(TOP 3)에 대해서만 카드별 AI 추천문구를 생성한다.
        generateTopRecommendationSummaries(
                categories,
                topResults,
                benefits
        );

        /*
         * 실제 저장은 별도 빈에서 하나의 트랜잭션으로 처리한다.
         * DB에는 CREDIT 1~3위 + CHECK 1~3위, 최대 6건만 저장한다.
         */
        saveService.saveRecommendations(
                spendingAnalysisId,
                topResults
        );

        log.info(
                "카드 추천 생성 완료 userId={}, analysisId={}, count={}",
                userId,
                spendingAnalysisId,
                topResults.size()
        );

        return CardRecommendationCreateResponseDTO.builder()
                .spendingAnalysisId(spendingAnalysisId)
                .created(true)
                .recommendationCount(topResults.size())
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

    /*
     * 카드추천 메인 목록 조회.
     * cardType으로 신용/체크를 나누고 feeMode에 따라 정렬 기준을 바꾼 뒤
     * 화면에는 최대 DISPLAY_LIMIT(3)개만 반환한다.
     */
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

    /*
     * 카드 한 장의 상세 정보 조회.
     * 저장된 카드별 AI 문구와 혜택별 실제 계산 결과를 함께 반환한다.
     * feeMode에 따라 화면에 보여줄 혜택금액/순위를 다시 계산한다.
     */
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

    /*
     * 카드 유형별 원본 추천 순위를 만든다.
     * 예상 할인액이 큰 카드 우선, 동률이면 연회비가 낮은 카드,
     * 다시 동률이면 cardProductId가 작은 카드 순으로 정렬한다.
     */
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

    /*
     * 최종 TOP 3 결과 각각에 대해 NarrativeService를 호출한다.
     * 호출 전에 topResults로 이미 필터링되어 있으며,
     * CREDIT 3장 + CHECK 3장이 모두 존재하면 최대 6회 호출된다.
     */
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

    /*
     * 프론트에 보여줄 순위용 정렬 기준.
     * MAX_BENEFIT: 예상 할인액 기준
     * NET_BENEFIT: 예상 할인액 - 연회비 기준
     */
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
