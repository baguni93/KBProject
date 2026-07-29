package org.scoula.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletChargeDTO {
    private Integer chargeId;         // 충전 건 ID / 거래 번호
    private Integer walletId;         // 지갑 ID
    private Integer userId;           // 회원 ID
    private Integer amount;           // 충전 요청 금액
    private String bankCode;          // 연동 출금 은행 코드
    private String accountNumber;     // 연동 출금 계좌 번호
    private String memo;              // 충전 메모
    private Integer updatedBalance;   // 충전 후 잔액
    private String status;            // 처리 상태 (SUCCESS, FAILED)
    private Date createdAt;           // 충전 일시
}
