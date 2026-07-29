package org.scoula.remittance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.remittance.dto.RemittanceDTO;

@Mapper
public interface RemittanceMapper {

    // 내 지갑 잔액 차감
    int subtractBalance(@Param("walletId") Integer walletId, @Param("amount") Integer amount);

    // 상대방 지갑 잔액 증가
    int addBalance(@Param("walletId") Integer walletId, @Param("amount") Integer amount);

    // 송금 내역 기록 저장
    int insertRemittance(RemittanceDTO remittanceDTO);

    // 계좌번호 및 은행코드로 계좌 잔액 증가
    int addAccountBalance(@Param("bankCode") String bankCode,
                          @Param("accountNumber") String accountNumber,
                          @Param("amount") Integer amount);

    // 영수증 메모 저장
    int insertReceiptMemo(@Param("transactionId") Integer transactionId, @Param("memo") String memo);

}