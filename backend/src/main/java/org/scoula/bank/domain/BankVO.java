package org.scoula.bank.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankVO {

    private String bankCode;
    private String bankName;
    private String bankLogoName;
    private String useYn;
}