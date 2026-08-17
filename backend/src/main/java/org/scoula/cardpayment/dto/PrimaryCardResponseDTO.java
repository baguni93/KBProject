package org.scoula.cardpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrimaryCardResponseDTO {
    private Integer cardId;
    private Integer accountId;
    private Integer userId;
    private String cardNum;
    private String expiryDate;
    private String representYn;
    private String cardName;
    private String cardImgFileName;
    private String holderName;
    private String paymentStatus;
    private String message;
}
