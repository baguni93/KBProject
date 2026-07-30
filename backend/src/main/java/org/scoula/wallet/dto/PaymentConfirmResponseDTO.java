package org.scoula.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmResponseDTO {
    private String status;
    private String message;
    private Integer remainingBalance;
    private String merchantName;
}
