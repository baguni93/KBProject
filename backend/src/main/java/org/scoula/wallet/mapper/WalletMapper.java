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
}