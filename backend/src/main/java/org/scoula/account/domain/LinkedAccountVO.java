package org.scoula.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedAccountVO {

    private Long linkedAccountId;
    private Long userId;
    private String bankCode;
    private String bankName;
    private String bankLogoName;
    private String accountNumber;
    private String accountHolder;
    private String primaryYn;
    private String connectionStatus;
}