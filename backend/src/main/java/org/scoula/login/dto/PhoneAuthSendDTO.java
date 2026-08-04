package org.scoula.login.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PhoneAuthSendDTO {

    private String userName;
    private LocalDate birthDate;
    private String carrierCode;
    private String phoneNumber;

    // 입력하지 않으면 SIGN_UP으로 처리
    private String verificationPurpose;
}