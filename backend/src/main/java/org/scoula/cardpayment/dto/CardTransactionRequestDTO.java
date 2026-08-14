package org.scoula.cardpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactionRequestDTO {
    private Integer linkedCardId;
    private Integer userId;
}
