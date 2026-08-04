package org.scoula.login.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PhoneAuthVerifyDTO {

    private String phoneNumber;
    private String verificationCode;

    // 입력하지 않으면 SIGN_UP으로 처리
    private String verificationPurpose;
}