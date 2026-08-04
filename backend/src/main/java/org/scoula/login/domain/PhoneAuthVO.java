package org.scoula.login.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PhoneAuthVO {

    private Long verificationId;
    private Long userId;

    private String userName;
    private LocalDate birthDate;
    private String carrierCode;
    private String phoneNumber;

    private String verificationCode;
    private String verificationPurpose;

    private LocalDateTime requestedAt;

    private String verifiedYn;
    private int failCount;
}