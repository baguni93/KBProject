package org.scoula.cardpayment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactionDetailVO {
    private Long cardTransactionId;
    private Integer linkedCardId;
    private String status; // PENDING, SUCCESS, FAILED
    private Date createdAt;
    private Integer transactionId;
    private Integer amount;
    private String merchantName;
}
