package org.scoula.analysis.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 미분류 결제 거래 DB 조회 결과
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnclassifiedTransactionVO {

    private Integer transactionId;
    private String merchantName;
    private Integer amount;
    private LocalDateTime createdAt;
}