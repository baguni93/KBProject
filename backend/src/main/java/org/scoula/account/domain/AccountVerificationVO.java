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
}