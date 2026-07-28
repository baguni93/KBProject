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
public class PaymentTokenDTO {
    private String token;             // 1회용 결제 토큰 값
    private String tokenType;         // 토큰 유형 (QR / BARCODE)
    private Integer walletId;         // 지갑 ID
    private Integer userId;           // 회원 ID
    private long expiresInSeconds;    // 유효 시간 (초, 기본 180초)
    private Date createdAt;           // 발급 일시
    private boolean used;             // 사용 완료 여부
}
