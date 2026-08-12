package org.scoula.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountVerificationVO {

    private Long verificationId;
    private Long userId;
    private String bankCode;
    private String accountNumber;
    private String accountHolder;
    private String verificationCode;
    private String verifiedYn;
    private LocalDateTime requestedAt;

    private Integer failCount; // 계좌 인증번호 입력 실패 횟수
    private Integer resendCount; // 계좌 인증번호 재발급 횟수
    private LocalDateTime lockedUntil; // 계좌 인증 잠금 해제 일시
}