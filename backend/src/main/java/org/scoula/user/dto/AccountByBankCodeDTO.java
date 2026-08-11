package org.scoula.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.user.domain.AccountVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountByBankCodeDTO {

    private int userId;
    private String bankCode;
    private String accountNumber;
    private String ownerName;
    private int balance;
    private String accountPassword;


    public static  AccountByBankCodeDTO of(AccountVO accountVO){

        return  accountVO == null ? null : AccountByBankCodeDTO
                .builder()
                .userId(accountVO.getUserId())
                .bankCode(accountVO.getBankCode())
                .accountNumber(accountVO.getAccountNumber())
                .ownerName(accountVO.getOwnerName())
                .balance(accountVO.getBalance())
                .accountPassword(accountVO.getAccountPassword())
                .build();
    }
}
