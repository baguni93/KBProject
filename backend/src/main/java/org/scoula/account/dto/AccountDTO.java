package org.scoula.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.account.domain.LinkedAccountVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private Long linkedAccountId;
    private String bankCode;
    private String bankName;
    private String bankLogoUrl;
    private String accountNumber;
    private String accountHolder;
    private String primaryYn;
    private String connectionStatus;

    public static AccountDTO of(LinkedAccountVO account) {
        String bankLogoUrl = account.getBankLogoName() == null ? null : "/api/banks/logo/" + account.getBankLogoName();

        return AccountDTO.builder()
                .linkedAccountId(account.getLinkedAccountId())
                .bankCode(account.getBankCode())
                .bankName(account.getBankName())
                .bankLogoUrl(bankLogoUrl)
                .accountNumber(account.getAccountNumber())
                .accountHolder(account.getAccountHolder())
                .primaryYn(account.getPrimaryYn())
                .connectionStatus(account.getConnectionStatus())
                .build();
    }
}