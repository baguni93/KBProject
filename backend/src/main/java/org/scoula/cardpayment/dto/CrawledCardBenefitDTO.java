package org.scoula.cardpayment.dto;

import java.math.BigDecimal;

/**
 * KB국민카드 상품 페이지에서 수집한 추천 계산용 혜택 한 건.
 *
 * 현재 card_benefit_tbl과 CardBenefitCalculator가 표현할 수 있는
 * 정률 할인 또는 정액 할인만 이 DTO로 변환한다.
 */
public class CrawledCardBenefitDTO {
    private Integer cardProductId;
    private Integer spendingCategoryId;
    private String categoryName;
    private String benefitName;
    private Integer benefitAmount;
    private BigDecimal benefitRate;
    private Integer monthlyLimit;
    private Integer minimumSpendingAmount;
    private String benefitDescription;

    public Integer getCardProductId() {
        return cardProductId;
    }

    public void setCardProductId(Integer cardProductId) {
        this.cardProductId = cardProductId;
    }

    public Integer getSpendingCategoryId() {
        return spendingCategoryId;
    }

    public void setSpendingCategoryId(Integer spendingCategoryId) {
        this.spendingCategoryId = spendingCategoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getBenefitName() {
        return benefitName;
    }

    public void setBenefitName(String benefitName) {
        this.benefitName = benefitName;
    }

    public Integer getBenefitAmount() {
        return benefitAmount;
    }

    public void setBenefitAmount(Integer benefitAmount) {
        this.benefitAmount = benefitAmount;
    }

    public BigDecimal getBenefitRate() {
        return benefitRate;
    }

    public void setBenefitRate(BigDecimal benefitRate) {
        this.benefitRate = benefitRate;
    }

    public Integer getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(Integer monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public Integer getMinimumSpendingAmount() {
        return minimumSpendingAmount;
    }

    public void setMinimumSpendingAmount(Integer minimumSpendingAmount) {
        this.minimumSpendingAmount = minimumSpendingAmount;
    }

    public String getBenefitDescription() {
        return benefitDescription;
    }

    public void setBenefitDescription(String benefitDescription) {
        this.benefitDescription = benefitDescription;
    }
}
