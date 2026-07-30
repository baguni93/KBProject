package org.scoula.remittance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.remittance.dto.BankDTO;
import org.scoula.remittance.dto.RecentAccountDTO;
import org.scoula.remittance.dto.RemittanceDTO;

import java.util.List;

@Mapper
public interface RemittanceMapper {

    int subtractBalance(@Param("walletId") Integer walletId, @Param("amount") Integer amount);

    int addBalance(@Param("walletId") Integer walletId, @Param("amount") Integer amount);

    int insertRemittance(RemittanceDTO remittanceDTO);

    int addAccountBalance(@Param("bankCode") String bankCode,
                          @Param("accountNumber") String accountNumber,
                          @Param("amount") Integer amount);

    int insertReceiptMemo(@Param("transactionId") Integer transactionId, @Param("memo") String memo);

    List<BankDTO> getBankList();

    List<RecentAccountDTO> getRecentAccounts(@Param("userId") Integer userId, @Param("limit") int limit);
}