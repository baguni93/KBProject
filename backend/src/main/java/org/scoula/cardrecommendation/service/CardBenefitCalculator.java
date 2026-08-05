package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CardBenefitCalculator {

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
