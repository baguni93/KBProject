package org.scoula.login.dto;

import lombok.Data;

@Data
public class PhoneAuthResendDTO {

    private String phoneNumber;
    private String verificationPurpose;
}