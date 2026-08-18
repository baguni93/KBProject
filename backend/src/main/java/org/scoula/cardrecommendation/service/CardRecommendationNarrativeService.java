package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

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

    /*
     * 카드 상품별 "예상 할인액"을 계산하는 순수 계산 컴포넌트.
     *
     * [전체 흐름]
     * 1) 카드 상품별 혜택 목록을 묶는다.
     * 2) 실제 12개월 거래를 월 단위로 집계한다.
     * 3) 각 혜택이 적용되는 소비 카테고리 거래만 찾는다.
     * 4) 전월실적 조건을 만족하는 달만 할인 계산에 포함한다.
     * 5) 할인율/정액 할인과 월 한도를 적용한다.
     * 6) 같은 카테고리 혜택은 월별 최댓값 하나만 선택해 중복 할인을 막는다.
     * 7) 카테고리별 선택 결과를 합산해 카드 한 장의 예상 할인액을 만든다.
     *
     * 이 클래스는 추천 순위를 정하거나 DB에 저장하지 않는다.
     * "거래 + 카드 혜택 -> 계산 결과"만 담당한다.
     */
    @Component
    class CardBenefitCalculator {

        /*
         * 모든 카드 상품에 대해 예상 혜택을 계산한다.
         * 반환값 CardProductCalculationResult는 이후 Service에서
         * 신용/체크카드별 순위 산정과 AI 추천문구 생성에 사용된다.
         */
        public List<CardProductCalculationResult> calculate(
                List<CardProductVO> products,
                List<CardBenefitVO> benefits,
                List<CardRecommendationTransactionVO> transactions,
                LocalDateTime benefitStartAt,
                LocalDateTime benefitEndAt
        ) {
            if (products == null || products.isEmpty()) {
                return List.of();
            }

            List<CardBenefitVO> safeBenefits =
                    benefits == null ? List.of() : benefits;
            List<CardRecommendationTransactionVO> safeTransactions =
                    transactions == null ? List.of() : transactions;

            Map<Integer, List<CardBenefitVO>> benefitsByProduct =
                    safeBenefits.stream()
                            .filter(Objects::nonNull)
                            .filter(benefit -> benefit.getCardProductId() != null)
                            .collect(Collectors.groupingBy(CardBenefitVO::getCardProductId));

            Map<YearMonth, Integer> monthlyTotalAmount =
                    createMonthlyTotalAmount(safeTransactions);
            Map<Integer, List<CardRecommendationTransactionVO>> transactionsByCategory =
                    createTransactionsByCategory(safeTransactions);

            List<CardProductCalculationResult> results = new ArrayList<>();

            for (CardProductVO product : products) {
                List<BenefitMonthlyCalculation> monthlyCalculations =
                        benefitsByProduct
                                .getOrDefault(product.getCardProductId(), List.of())
                                .stream()
                                .map(benefit -> calculateBenefit(
                                        benefit,
                                        transactionsByCategory.getOrDefault(
                                                benefit.getSpendingCategoryId(),
                                                List.of()
                                        ),
                                        monthlyTotalAmount,
                                        benefitStartAt,
                                        benefitEndAt
                                ))
                                .collect(Collectors.toList());

                List<CardBenefitCalculationResult> benefitResults =
                        selectBestMonthlyBenefits(monthlyCalculations);

                int expectedBenefitAmount =
                        benefitResults.stream()
                                .map(CardBenefitCalculationResult::getExpectedBenefitAmount)
                                .filter(Objects::nonNull)
                                .mapToInt(Integer::intValue)
                                .sum();

                results.add(
                        CardProductCalculationResult.builder()
                                .product(product)
                                .expectedBenefitAmount(expectedBenefitAmount)
                                .benefitResults(benefitResults)
                                .build()
                );
            }

            return results;
        }

        /*
         * 카드 혜택 한 건에 대해 실제 받을 수 있었던 예상 할인액을 계산한다.
         * 같은 혜택이라도 월별 전월실적과 월 할인한도를 각각 적용한다.
         */
        private BenefitMonthlyCalculation calculateBenefit(
                CardBenefitVO benefit,
                List<CardRecommendationTransactionVO> transactions,
                Map<YearMonth, Integer> monthlyTotalAmount,
                LocalDateTime benefitStartAt,
                LocalDateTime benefitEndAt
        ) {
            Map<YearMonth, List<CardRecommendationTransactionVO>> matchedByMonth =
                    transactions.stream()
                            .filter(transaction -> isInsideBenefitPeriod(
                                    transaction,
                                    benefitStartAt,
                                    benefitEndAt
                            ))
                            .filter(transaction -> matchesCategory(benefit, transaction))
                            .collect(Collectors.groupingBy(transaction ->
                                    YearMonth.from(transaction.getCreatedAt())
                            ));

            Map<YearMonth, MonthlyBenefitResult> resultsByMonth =
                    new LinkedHashMap<>();

            for (Map.Entry<YearMonth, List<CardRecommendationTransactionVO>> entry
                    : matchedByMonth.entrySet()) {

                YearMonth benefitMonth = entry.getKey();
                int previousMonthAmount =
                        monthlyTotalAmount.getOrDefault(
                                benefitMonth.minusMonths(1),
                                0
                        );

                if (!isPerformanceSatisfied(
                        benefit.getMinimumSpendingAmount(),
                        previousMonthAmount
                )) {
                    continue;
                }

                List<CardRecommendationTransactionVO> eligibleTransactions =
                        entry.getValue().stream()
                                .filter(transaction -> transaction.getAmount() != null)
                                .filter(transaction -> transaction.getAmount() > 0)
                                .collect(Collectors.toList());

                if (eligibleTransactions.isEmpty()) {
                    continue;
                }

                long eligibleSpendingAmount = eligibleTransactions.stream()
                        .map(CardRecommendationTransactionVO::getAmount)
                        .mapToLong(Integer::longValue)
                        .sum();

                long monthlyDiscount = eligibleTransactions.stream()
                        .mapToLong(transaction -> calculateTransactionDiscount(
                                transaction.getAmount(),
                                benefit
                        ))
                        .sum();

                if (benefit.getMonthlyLimit() != null) {
                    monthlyDiscount = Math.min(
                            monthlyDiscount,
                            benefit.getMonthlyLimit()
                    );
                }

                resultsByMonth.put(
                        benefitMonth,
                        new MonthlyBenefitResult(
                                Math.toIntExact(eligibleSpendingAmount),
                                eligibleTransactions.size(),
                                Math.toIntExact(monthlyDiscount)
                        )
                );
            }

            return new BenefitMonthlyCalculation(benefit, resultsByMonth);
        }

        /*
         * 같은 카드/카테고리의 여러 혜택이 동일 거래에 겹쳐 적용되지 않도록
         * 각 월에 계산된 할인액이 가장 큰 혜택 한 건만 선택한다.
         * 선택된 월의 금액만 혜택 상세 결과에도 합산한다.
         */
        private List<CardBenefitCalculationResult> selectBestMonthlyBenefits(
                List<BenefitMonthlyCalculation> calculations
        ) {
            Map<Integer, List<BenefitMonthlyCalculation>> byCategory =
                    calculations.stream()
                            .filter(Objects::nonNull)
                            .filter(calculation -> calculation.benefit != null)
                            .filter(calculation ->
                                    calculation.benefit.getSpendingCategoryId() != null)
                            .filter(calculation ->
                                    calculation.benefit.getCardBenefitId() != null)
                            .collect(Collectors.groupingBy(
                                    calculation ->
                                            calculation.benefit.getSpendingCategoryId(),
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

            Map<Integer, MutableBenefitTotal> selectedTotals =
                    new LinkedHashMap<>();

            for (List<BenefitMonthlyCalculation> sameCategory : byCategory.values()) {
                Set<YearMonth> months = sameCategory.stream()
                        .flatMap(calculation -> calculation.resultsByMonth.keySet().stream())
                        .collect(Collectors.toCollection(TreeSet::new));

                for (YearMonth month : months) {
                    BenefitMonthlyCalculation selected = null;
                    MonthlyBenefitResult selectedResult = null;

                    for (BenefitMonthlyCalculation candidate : sameCategory) {
                        MonthlyBenefitResult candidateResult =
                                candidate.resultsByMonth.get(month);
                        if (candidateResult == null) {
                            continue;
                        }

                        if (selectedResult == null
                                || candidateResult.discountAmount
                                > selectedResult.discountAmount
                                || (candidateResult.discountAmount
                                == selectedResult.discountAmount
                                && benefitId(candidate.benefit)
                                < benefitId(selected.benefit))) {
                            selected = candidate;
                            selectedResult = candidateResult;
                        }
                    }

                    if (selected == null || selectedResult == null) {
                        continue;
                    }

                    CardBenefitVO selectedBenefit = selected.benefit;
                    int selectedBenefitId = benefitId(selectedBenefit);

                    selectedTotals
                            .computeIfAbsent(
                                    selectedBenefitId,
                                    ignored -> new MutableBenefitTotal(selectedBenefit)
                            )
                            .add(selectedResult);
                }
            }

            return selectedTotals.values().stream()
                    .sorted(Comparator.comparingInt(total ->
                            benefitId(total.benefit)))
                    .map(MutableBenefitTotal::toResult)
                    .collect(Collectors.toList());
        }

        private int benefitId(CardBenefitVO benefit) {
            return benefit == null || benefit.getCardBenefitId() == null
                    ? Integer.MAX_VALUE
                    : benefit.getCardBenefitId();
        }

        /*
         * 전월실적 확인을 위해 사용자의 전체 거래금액을 월별로 합산한다.
         * 예: 7월 혜택 적용 여부를 판단할 때 6월 총 사용금액을 조회한다.
         */
        private Map<YearMonth, Integer> createMonthlyTotalAmount(
                List<CardRecommendationTransactionVO> transactions
        ) {
            return transactions.stream()
                    .filter(Objects::nonNull)
                    .filter(transaction -> transaction.getCreatedAt() != null)
                    .filter(transaction -> transaction.getAmount() != null)
                    .filter(transaction -> transaction.getAmount() > 0)
                    .collect(Collectors.groupingBy(
                            transaction -> YearMonth.from(transaction.getCreatedAt()),
                            Collectors.collectingAndThen(
                                    Collectors.summingLong(transaction ->
                                            transaction.getAmount().longValue()
                                    ),
                                    Math::toIntExact
                            )
                    ));
        }

        /*
         * 혜택 하나를 계산할 때마다 12개월 거래 전체를 다시 훑지 않도록
         * 거래를 자신의 카테고리와 부모 카테고리 양쪽에 한 번만 색인한다.
         */
        private Map<Integer, List<CardRecommendationTransactionVO>>
        createTransactionsByCategory(
                List<CardRecommendationTransactionVO> transactions
        ) {
            Map<Integer, List<CardRecommendationTransactionVO>> index =
                    new HashMap<>();

            for (CardRecommendationTransactionVO transaction : transactions) {
                if (transaction == null) {
                    continue;
                }

                Integer categoryId = transaction.getSpendingCategoryId();
                if (categoryId != null) {
                    index.computeIfAbsent(categoryId, ignored -> new ArrayList<>())
                            .add(transaction);
                }

                Integer parentCategoryId = transaction.getParentCategoryId();
                if (parentCategoryId != null
                        && !Objects.equals(parentCategoryId, categoryId)) {
                    index.computeIfAbsent(
                                    parentCategoryId,
                                    ignored -> new ArrayList<>()
                            )
                            .add(transaction);
                }
            }

            return index;
        }

        private boolean isInsideBenefitPeriod(
                CardRecommendationTransactionVO transaction,
                LocalDateTime benefitStartAt,
                LocalDateTime benefitEndAt
        ) {
            if (transaction == null || transaction.getCreatedAt() == null) {
                return false;
            }

            return !transaction.getCreatedAt().isBefore(benefitStartAt)
                    && transaction.getCreatedAt().isBefore(benefitEndAt);
        }

        /*
         * 카드 혜택 카테고리와 거래 카테고리가 일치하는지 확인한다.
         * 거래가 하위 카테고리인 경우 부모 카테고리 혜택도 적용될 수 있도록
         * spendingCategoryId와 parentCategoryId를 모두 비교한다.
         */
        private boolean matchesCategory(
                CardBenefitVO benefit,
                CardRecommendationTransactionVO transaction
        ) {
            if (benefit.getSpendingCategoryId() == null
                    || transaction.getSpendingCategoryId() == null) {
                return false;
            }

            return Objects.equals(
                    benefit.getSpendingCategoryId(),
                    transaction.getSpendingCategoryId()
            ) || Objects.equals(
                    benefit.getSpendingCategoryId(),
                    transaction.getParentCategoryId()
            );
        }

        private boolean isPerformanceSatisfied(
                Integer minimumSpendingAmount,
                int previousMonthAmount
        ) {
            return minimumSpendingAmount == null
                    || minimumSpendingAmount <= 0
                    || previousMonthAmount >= minimumSpendingAmount;
        }

        /*
         * 거래 한 건의 할인액 계산.
         * benefitRate가 있으면 퍼센트 할인,
         * benefitAmount가 함께 있으면 건별 최대 할인액처럼 사용한다.
         * benefitRate가 없고 benefitAmount만 있으면 정액 할인으로 계산한다.
         */
        private int calculateTransactionDiscount(
                int transactionAmount,
                CardBenefitVO benefit
        ) {
            BigDecimal benefitRate = benefit.getBenefitRate();
            Integer benefitAmount = benefit.getBenefitAmount();

            if (benefitRate != null) {
                int rateDiscount = BigDecimal.valueOf(transactionAmount)
                        .multiply(benefitRate)
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                        .intValueExact();

                if (benefitAmount != null) {
                    return Math.min(rateDiscount, benefitAmount);
                }

                return rateDiscount;
            }

            if (benefitAmount != null) {
                return Math.min(transactionAmount, benefitAmount);
            }

            return 0;
        }

        private static final class BenefitMonthlyCalculation {
            private final CardBenefitVO benefit;
            private final Map<YearMonth, MonthlyBenefitResult> resultsByMonth;

            private BenefitMonthlyCalculation(
                    CardBenefitVO benefit,
                    Map<YearMonth, MonthlyBenefitResult> resultsByMonth
            ) {
                this.benefit = benefit;
                this.resultsByMonth = resultsByMonth;
            }
        }

        private static final class MonthlyBenefitResult {
            private final int eligibleSpendingAmount;
            private final int eligibleTransactionCount;
            private final int discountAmount;

            private MonthlyBenefitResult(
                    int eligibleSpendingAmount,
                    int eligibleTransactionCount,
                    int discountAmount
            ) {
                this.eligibleSpendingAmount = eligibleSpendingAmount;
                this.eligibleTransactionCount = eligibleTransactionCount;
                this.discountAmount = discountAmount;
            }
        }

        private static final class MutableBenefitTotal {
            private final CardBenefitVO benefit;
            private long eligibleSpendingAmount;
            private long eligibleTransactionCount;
            private int eligibleMonthCount;
            private long expectedBenefitAmount;

            private MutableBenefitTotal(CardBenefitVO benefit) {
                this.benefit = benefit;
            }

            private void add(MonthlyBenefitResult result) {
                eligibleSpendingAmount += result.eligibleSpendingAmount;
                eligibleTransactionCount += result.eligibleTransactionCount;
                eligibleMonthCount++;
                expectedBenefitAmount += result.discountAmount;
            }

            private CardBenefitCalculationResult toResult() {
                return CardBenefitCalculationResult.builder()
                        .cardBenefitId(benefit.getCardBenefitId())
                        .eligibleSpendingAmount(
                                Math.toIntExact(eligibleSpendingAmount)
                        )
                        .eligibleTransactionCount(
                                Math.toIntExact(eligibleTransactionCount)
                        )
                        .eligibleMonthCount(eligibleMonthCount)
                        .expectedBenefitAmount(
                                Math.toIntExact(expectedBenefitAmount)
                        )
                        .build();
            }
        }
    }
}
