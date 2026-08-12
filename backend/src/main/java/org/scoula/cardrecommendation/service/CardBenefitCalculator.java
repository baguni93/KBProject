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
 * 카드 상품별 "예상 할인액"을 계산하는 순수 계산 컴포넌트.
 *
 * [전체 흐름]
 * 1) 카드 상품별 혜택 목록을 묶는다.
 * 2) 실제 12개월 거래를 월 단위로 집계한다.
 * 3) 각 혜택이 적용되는 소비 카테고리 거래만 찾는다.
 * 4) 전월실적 조건을 만족하는 달만 할인 계산에 포함한다.
 * 5) 할인율/정액 할인과 월 한도를 적용한다.
 * 6) 혜택별 예상 할인액을 합산해 카드 한 장의 예상 할인액을 만든다.
 *
 * 이 클래스는 추천 순위를 정하거나 DB에 저장하지 않는다.
 * "거래 + 카드 혜택 -> 계산 결과"만 담당한다.
 */
@Component
public class CardBenefitCalculator {

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

        List<CardProductCalculationResult> results = new ArrayList<>();

        for (CardProductVO product : products) {
            List<CardBenefitCalculationResult> benefitResults =
                    benefitsByProduct
                            .getOrDefault(product.getCardProductId(), List.of())
                            .stream()
                            .map(benefit -> calculateBenefit(
                                    benefit,
                                    safeTransactions,
                                    monthlyTotalAmount,
                                    benefitStartAt,
                                    benefitEndAt
                            ))
                            .collect(Collectors.toList());

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
    private CardBenefitCalculationResult calculateBenefit(
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

        long eligibleSpendingAmount = 0L;
        long eligibleTransactionCount = 0L;
        int eligibleMonthCount = 0;
        long totalDiscount = 0L;

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

            eligibleMonthCount++;
            eligibleTransactionCount += eligibleTransactions.size();
            eligibleSpendingAmount += eligibleTransactions.stream()
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

            totalDiscount += monthlyDiscount;
        }

        return CardBenefitCalculationResult.builder()
                .cardBenefitId(benefit.getCardBenefitId())
                .eligibleSpendingAmount(Math.toIntExact(eligibleSpendingAmount))
                .eligibleTransactionCount(Math.toIntExact(eligibleTransactionCount))
                .eligibleMonthCount(eligibleMonthCount)
                .expectedBenefitAmount(Math.toIntExact(totalDiscount))
                .build();
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
}
