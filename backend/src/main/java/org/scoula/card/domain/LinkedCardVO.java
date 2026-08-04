package org.scoula.card.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedCardVO {

    private Long linkedCardId;
    private Long userId;
    private Long cardId;
    private String cardCompanyCode;
    private String cardCompanyName;
    private String cardName;
    private String cardImageName;
    private String cardNum;
    private String representYn;
}