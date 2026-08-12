package org.scoula.user.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountVO {

    private int userId;
    private String bankCode;
    private String accountNumber;
    private String ownerName;
    private int balance;
    private String accountPassword;

}
