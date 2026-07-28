package org.scoula.pointwallet.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletVO {

    private Integer walletId;
    private Integer userId;
    private Integer balance;
    private String walletStatus;
}