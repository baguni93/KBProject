package org.scoula.cardpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactionResponseDTO {
    private Long cardTransactionId;
    private Integer linkedCardId;
    private String status; // PENDING, SUCCESS, FAILED
    private Date createdAt;
    private Integer transactionId; // financial_transaction_tbl FK
    private String merchantName;
    private Integer amount;
    private Long updatedWalletBalance;
    private String message;
}
