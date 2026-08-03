package org.scoula.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountVerificationConfirmDTO {

    private Long verificationId;
    private String verificationCode;
}