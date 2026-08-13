package org.scoula.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.card.domain.LinkedCardVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {

    private Long linkedCardId;
    private Long cardCode;
    private String cardCompanyCode;
    private String cardCompanyName;
    private String cardName;
    private String cardImageUrl;
    private String maskedCardNumber;
    private String representYn;

    public static CardDTO of(LinkedCardVO card) {

        String imageName = card.getCardImageName();
        String cardImageUrl = null;
        if (imageName != null && !imageName.trim().isEmpty()) {
            if (imageName.startsWith("http") || imageName.startsWith("/")) {
                cardImageUrl = imageName;
            } else {
                cardImageUrl = "/api/cards/image/" + imageName;
            }
        }

        return CardDTO.builder()
                .linkedCardId(card.getLinkedCardId())
                .cardCode(card.getCardCode())
                .cardCompanyCode(card.getCardCompanyCode())
                .cardCompanyName(card.getCardCompanyName())
                .cardName(card.getCardName())
                .cardImageUrl(cardImageUrl)
                .maskedCardNumber(maskCardNumber(card.getCardNum()))
                .representYn(card.getRepresentYn())
                .build();
    }

    // 카드번호 마스킹
    private static String maskCardNumber(String cardNum) {

        if (cardNum == null || cardNum.trim().isEmpty()) return null;

        String number = cardNum.replaceAll("[^0-9]", "");

        if (number.length() < 4) return "****";

        String lastFour = number.substring(number.length() - 4);

        return "**** **** **** " + lastFour;
    }
}