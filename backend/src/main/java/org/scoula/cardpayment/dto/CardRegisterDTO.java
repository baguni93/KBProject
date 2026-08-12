package org.scoula.cardpayment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardRegisterDTO {
    private Integer cardCode;
    private Integer cardId;
    private Integer userId;
    private Integer accountId;
    private String cardNum;
    private String expiryDate;
    private String cvv;
    private String cardPassword;
    private String representYn;
    private String cardName;
    private String cardAlias;
    private String cardImgUrl;
    private String cardCompanyCode;
    private String cardImageName;
}
