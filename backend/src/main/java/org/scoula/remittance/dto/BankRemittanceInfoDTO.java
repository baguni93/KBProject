package org.scoula.remittance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankRemittanceInfoDTO {
    private List<BankDTO> banks;
    private List<RecentAccountDTO> recentAccounts;
}
