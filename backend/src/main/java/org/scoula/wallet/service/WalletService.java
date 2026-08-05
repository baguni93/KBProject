package org.scoula.wallet.service;

import org.scoula.wallet.dto.WalletChargeDTO;
import org.scoula.wallet.dto.WalletDTO;

public interface WalletService {

    // 회원 번호(userId)로 해당 회원의 지갑 정보 조회
    WalletDTO getWalletByUserId(Integer userId);

    // 지갑 머니 수동 충전 신청 (POST /api/wallets/charges)
    WalletChargeDTO chargeWallet(WalletChargeDTO chargeDTO);

    // 지갑 머니 충전 내역 상세 조회 (GET /api/wallets/charges/{chargeId})
    WalletChargeDTO getChargeDetails(Integer chargeId);

    // 부족금 자동 충전 처리 (POST /api/wallets/auto-charge)
    WalletChargeDTO autoChargeWallet(WalletChargeDTO chargeDTO);

    // 회원 등록 실물 카드 목록 조회
    java.util.List<org.scoula.wallet.dto.RegisteredCardDTO> getUserRegisteredCards(Integer userId);
}
