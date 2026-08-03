package org.scoula.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountVerificationRequestDTO {

    private String bankCode;
    private String accountNumber;
    private String accountHolder;
}