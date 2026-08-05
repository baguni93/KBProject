package org.scoula.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRegisterDTO {
    private Integer cardId;
    private Integer userId;
    private Integer accountId;
    private String cardNum;
    private String expiryDate;
    private String cvv;
    private String cardPassword;
    private String representYn; // 'Y' or 'N'
    private String cardName;
}
