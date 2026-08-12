package org.scoula.cardpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardBinResponseDTO {
    private String binNumber;
    private String cardName;
    private String imageUrl;
}
