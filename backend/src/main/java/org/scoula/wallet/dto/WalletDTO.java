package org.scoula.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor     
@Builder
public class WalletDTO {

    private Integer walletId;      // 지갑 번호 (PK)
    private Integer userId;        // 회원 번호 (FK)
    private Integer balance;    // 지갑 잔액
    private String walletStatus;// 지갑 상태 (ACTIVE, CLOSED)

}
