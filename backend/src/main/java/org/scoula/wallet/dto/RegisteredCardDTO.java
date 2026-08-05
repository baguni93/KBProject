package org.scoula.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisteredCardDTO {
    private Integer cardId;
    private Integer accountId;
    private Integer userId;
    private String cardNum;
    private String expiryDate;
    private String representYn;
    private String cardName;
    private String holderName;
}
