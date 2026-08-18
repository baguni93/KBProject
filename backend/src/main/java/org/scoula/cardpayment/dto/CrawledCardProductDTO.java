package org.scoula.cardpayment.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * KB국민카드 목록/상세 페이지에서 수집한 카드 상품 한 건.
 *
 * cardProductId는 kb_card_product_tbl INSERT 후 MyBatis가 채우며,
 * 이후 benefits를 저장할 때 FK로 사용한다.
 */
public class CrawledCardProductDTO {
    private Integer cardProductId;
    private String cardName;
    private String cardType;
    private String cardDescription;
    private String cardImage;
    private String application;
    private Integer annualFee;

    private List<CrawledCardBenefitDTO> benefits = new ArrayList<>();

    public Integer getCardProductId() {
        return cardProductId;
    }

    public void setCardProductId(Integer cardProductId) {
        this.cardProductId = cardProductId;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardDescription() {
        return cardDescription;
    }

    public void setCardDescription(String cardDescription) {
        this.cardDescription = cardDescription;
    }

    public String getCardImage() {
        return cardImage;
    }

    public void setCardImage(String cardImage) {
        this.cardImage = cardImage;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Integer getAnnualFee() {
        return annualFee;
    }

    public void setAnnualFee(Integer annualFee) {
        this.annualFee = annualFee;
    }

    public List<CrawledCardBenefitDTO> getBenefits() {
        return benefits;
    }

    public void setBenefits(List<CrawledCardBenefitDTO> benefits) {
        this.benefits = benefits == null ? new ArrayList<>() : benefits;
    }
}
