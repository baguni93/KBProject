package org.scoula.cardpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactionApproveDTO {
    private Long cardTransactionId;
    private Integer userId;
    private Integer receiveId; // 수신자(가맹점 유저) ID
    private String merchantName; // 예: "스타벅스"
    private Integer amount;       // 예: 10000
    private Integer merchantAccountId; // 가맹점 더미 계좌 ID
    private Integer spendingCategoryId; // AI/가맹점 매핑으로 결정된 소비 카테고리 ID
    private Integer transactionId; // 생성된 financial_transaction_tbl PK
}
