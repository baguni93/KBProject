package org.scoula.card.dto;

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
    private String statusCode; // e.g. "NO_CARD", "PRIMARY_CARD_SET", "NEED_PRIMARY_CARD"
}
