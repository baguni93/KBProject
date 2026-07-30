package org.scoula.remittance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemittanceDTO {

    private Integer walletId;
    private Integer receiverId;
    private Integer amount;
    private String memo;
    private String status;

    private String receiverType;
    private String bankCode;
    private String accountNumber;

    private Integer transactionId;

    // 피드 연동용 필드
    private String feedType;    // REMITTANCE
    private String content;     // 피드 내용
    private String visibility;  // PUBLIC, FRIENDS, PRIVATE
}
