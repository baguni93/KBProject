package org.scoula.pointwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointTransactionDTO {

    private Integer pointTransactionId;
    private Integer pointWalletId;

    // EARN, USE, EXPIRE, CANCEL
    private String transactionType;

    private Integer pointAmount;

    // DEPOSIT, ATTENDANCE, RANDOM_BOX, CONVERSION, EVENT
    private String reasonType;

    // SQL로 직접 형변환
    private String createdAt;
}