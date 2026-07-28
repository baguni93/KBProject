package org.scoula.wallet.service;

import lombok.RequiredArgsConstructor;
import org.scoula.wallet.dto.WalletChargeDTO;
import org.scoula.wallet.dto.WalletDTO;
import org.scoula.wallet.mapper.WalletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;

    @Override
    public WalletDTO getWalletByUserId(Integer userId) {
        return walletMapper.getByUserId(userId);
    }

    @Override
    @Transactional
    public WalletChargeDTO chargeWallet(WalletChargeDTO chargeDTO) {
        if (chargeDTO.getAmount() == null || chargeDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("충전 금액은 0원보다 커야 합니다.");
        }

        // 1. 지갑 정보 확인
        WalletDTO wallet = null;
        if (chargeDTO.getWalletId() != null) {
            wallet = walletMapper.getByWalletId(chargeDTO.getWalletId());
        } else if (chargeDTO.getUserId() != null) {
            wallet = walletMapper.getByUserId(chargeDTO.getUserId());
        }

        if (wallet == null) {
            throw new RuntimeException("지갑 정보를 찾을 수 없습니다.");
        }

        // 2. 지갑 잔액 증가
        walletMapper.addBalance(wallet.getWalletId(), chargeDTO.getAmount());

        // 3. 충전 거래 내역 기록
        chargeDTO.setWalletId(wallet.getWalletId());
        chargeDTO.setUserId(wallet.getUserId());
        chargeDTO.setStatus("SUCCESS");
        if (chargeDTO.getMemo() == null || chargeDTO.getMemo().isEmpty()) {
            chargeDTO.setMemo("연동 계좌 지갑 머니 충전");
        }

        walletMapper.insertChargeTransaction(chargeDTO);

        // 4. 변경된 최신 잔액 조회
        WalletDTO updatedWallet = walletMapper.getByWalletId(wallet.getWalletId());
        chargeDTO.setUpdatedBalance(updatedWallet.getBalance());
        chargeDTO.setCreatedAt(new Date());

        return chargeDTO;
    }

    @Override
    public WalletChargeDTO getChargeDetails(Integer chargeId) {
        WalletChargeDTO charge = walletMapper.getChargeById(chargeId);
        if (charge == null) {
            throw new RuntimeException("해당 충전 건을 찾을 수 없습니다. (ID: " + chargeId + ")");
        }
        return charge;
    }

    @Override
    @Transactional
    public WalletChargeDTO autoChargeWallet(WalletChargeDTO chargeDTO) {
        if (chargeDTO.getMemo() == null) {
            chargeDTO.setMemo("송금 잔액 부족 자동 충전");
        }
        return chargeWallet(chargeDTO);
    }
}