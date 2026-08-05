package org.scoula.cardrecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardProductVO {
    private Integer cardProductId;
    private String cardName;
    private String cardType;
    private String cardDescription;
    private String cardImage;
    private String application;
    private Integer annualFee;
}
