package org.scoula.remittance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.remittance.dto.BankDTO;
import org.scoula.remittance.dto.RecentAccountDTO;
import org.scoula.remittance.dto.RemittanceDTO;

import java.util.List;

@Mapper
public interface RemittanceMapper {

    int getWalletBalance(@Param("walletId") Integer walletId);

    int subtractBalance(@Param("walletId") Integer walletId, @Param("amount") Integer amount);

    int addBalance(@Param("walletId") Integer walletId, @Param("amount") Integer amount);

    int insertRemittance(RemittanceDTO remittanceDTO);

    int insertChargeTransaction(@Param("walletId") Integer walletId, @Param("amount") Integer amount);

    int addAccountBalance(@Param("bankCode") String bankCode,
                          @Param("accountNumber") String accountNumber,
                          @Param("amount") Integer amount);

    int subtractPrimaryAccountBalance(@Param("userId") Integer userId, @Param("amount") Integer amount);

    int insertFeed(@Param("userId") Integer userId,
                   @Param("transactionId") Integer transactionId,
                   @Param("feedType") String feedType,
                   @Param("content") String content,
                   @Param("visibility") String visibility);

    List<BankDTO> getBankList();

    List<RecentAccountDTO> getRecentAccounts(@Param("userId") Integer userId, @Param("limit") int limit);

    String getAccountOwnerName(@Param("bankCode") String bankCode, @Param("accountNumber") String accountNumber);

    Integer getUserIdByAccount(@Param("bankCode") String bankCode, @Param("accountNumber") String accountNumber);

    String getUserNicknameOrName(@Param("userId") Integer userId);

    int insertFeedImage(@Param("feedId") int feedId, @Param("imageName") String imageName);

    int insertReceiptMemo(@Param("transactionId") Integer transactionId, @Param("memo") String memo);
}