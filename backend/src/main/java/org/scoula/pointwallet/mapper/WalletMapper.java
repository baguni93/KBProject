package org.scoula.pointwallet.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.pointwallet.domain.WalletVO;

public interface WalletMapper {

    // 포인트 전환 처리 전 전자지갑 row Lock 조회
    WalletVO selectWalletForUpdate(
            @Param("userId") Integer userId
    );

    // 전자지갑 잔액 증가
    int increaseWalletBalance(
            @Param("walletId") Integer walletId,
            @Param("amount") Integer amount
    );

    // 변경된 전자지갑 조회
    WalletVO selectWalletByUserId(
            @Param("userId") Integer userId
    );
}