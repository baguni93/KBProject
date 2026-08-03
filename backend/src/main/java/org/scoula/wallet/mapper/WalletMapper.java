package org.scoula.wallet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.wallet.dto.WalletChargeDTO;
import org.scoula.wallet.dto.WalletDTO;

@Mapper
public interface WalletMapper {

    WalletDTO getByUserId(Integer userId);

    WalletDTO getByWalletId(Integer walletId);

    int addBalance(@Param("walletId") Integer walletId, @Param("amount") Integer amount);

    int insertChargeTransaction(WalletChargeDTO chargeDTO);

    WalletChargeDTO getChargeById(Integer chargeId);

    // 박준우: 포인트 전환 처리 시 전자지갑 행 잠금 조회
    WalletDTO getByUserIdForUpdate(
            @Param("userId") Integer userId
    );

    // 박준우: 전자지갑 잔액 증가
    int addBalanceIfActive(
            @Param("walletId") Integer walletId,
            @Param("amount") Integer amount
    );

    java.util.List<org.scoula.wallet.dto.RegisteredCardDTO> getUserRegisteredCards(@Param("userId") Integer userId);
}