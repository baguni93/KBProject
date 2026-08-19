package org.scoula.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Integer transactionId;
    private Integer parentTransactionId;
    private Integer userId;
    private Integer receiveId;
    private String transactionType;
    private String sourceType;
    private String targetType;
    private String transactionStatus;
    private Integer amount;
    private Integer spendingCategoryId;
    private Integer settlementId;
    private Date createdAt;

    // 조인 정보 및 영수증 메모
    private String merchantName;
    private String memo;
    private String receiverName;
    private String senderName;
}
