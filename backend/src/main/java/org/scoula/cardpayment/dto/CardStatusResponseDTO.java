package org.scoula.cardpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardStatusResponseDTO {
    private Integer userId;
    private Boolean hasRegisteredCard;
    private Boolean hasPrimaryCard;
    private Integer registeredCardCount;
    private Integer primaryCardId;
    private String guideMessage;
    private String statusCode;
}
