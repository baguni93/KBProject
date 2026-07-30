package org.scoula.remittance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentAccountDTO {
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String ownerName;
    private Date lastTransferAt;
}
