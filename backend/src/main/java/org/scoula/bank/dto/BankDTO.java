package org.scoula.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.bank.domain.BankVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDTO {

    private String bankCode;
    private String bankName;
    private String bankLogoName;
    private String bankLogoUrl;

    public static BankDTO of(BankVO bank) {
        String bankLogoUrl = bank.getBankLogoName() == null ? null : "/api/banks/logo/" + bank.getBankLogoName();

        return BankDTO.builder()
                .bankCode(bank.getBankCode())
                .bankName(bank.getBankName())
                .bankLogoName(bank.getBankLogoName())
                .bankLogoUrl(bankLogoUrl)
                .build();
    }
}