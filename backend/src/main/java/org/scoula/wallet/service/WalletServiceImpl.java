package org.scoula.wallet.service;

import lombok.RequiredArgsConstructor;
import org.scoula.wallet.dto.WalletChargeDTO;
import org.scoula.wallet.dto.WalletDTO;
import org.scoula.wallet.mapper.WalletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@lombok.extern.log4j.Log4j2
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;

    @Override
    @Transactional
    public WalletDTO getWalletByUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            userId = 1;
        }
        WalletDTO wallet = walletMapper.getByUserId(userId);
        if (wallet == null) {
            try {
                walletMapper.insertWallet(userId);
                wallet = walletMapper.getByUserId(userId);
                log.info("신규 유저 전자지갑(wallet_tbl) 자동 개설 완료: userId={}", userId);
            } catch (Exception e) {
                log.warn("신규 지갑 생성 예외: {}", e.getMessage());
            }
            if (wallet == null) {
                wallet = WalletDTO.builder()
                        .userId(userId)
                        .balance(0)
                        .walletStatus("ACTIVE")
                        .build();
            }
        }
        return wallet;
    }

    @Override
    @Transactional
    public WalletChargeDTO chargeWallet(WalletChargeDTO chargeDTO) {
        if (chargeDTO.getAmount() == null || chargeDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("충전 금액은 0원보다 커야 합니다.");
        }

        Integer targetUserId = chargeDTO.getUserId();
        if (targetUserId == null || targetUserId <= 0) {
            targetUserId = 1;
        }

        // 1. 지갑 정보 확인 (없으면 자동 생성)
        WalletDTO wallet = walletMapper.getByUserId(targetUserId);
        if (wallet == null && chargeDTO.getWalletId() != null) {
            wallet = walletMapper.getByWalletId(chargeDTO.getWalletId());
        }
        if (wallet == null) {
            try {
                walletMapper.insertWallet(targetUserId);
                wallet = walletMapper.getByUserId(targetUserId);
            } catch (Exception e) {
                log.warn("신규 지갑 생성 예외: {}", e.getMessage());
            }
        }

        if (wallet == null) {
            throw new RuntimeException("지갑 정보를 찾을 수 없습니다.");
        }

        // 2. 대표계좌 잔액 차감 및 지갑 잔액 증가
        try {
            walletMapper.subtractPrimaryAccountBalance(targetUserId, chargeDTO.getAmount());
        } catch (Exception e) {
            log.warn("충전 시 대표계좌 출금 예외: {}", e.getMessage());
        }
        walletMapper.addBalance(wallet.getWalletId(), chargeDTO.getAmount());

        // 3. 충전 거래 내역 기록
        chargeDTO.setWalletId(wallet.getWalletId());
        chargeDTO.setUserId(targetUserId);
        chargeDTO.setStatus("SUCCESS");
        if (chargeDTO.getMemo() == null || chargeDTO.getMemo().isEmpty()) {
            chargeDTO.setMemo("연동 계좌 지갑 머니 충전");
        }

        try {
            walletMapper.insertChargeTransaction(chargeDTO);
        } catch (Exception e) {
            log.warn("충전 거래내역 저장 예외 (충전 계속 진행): {}", e.getMessage());
        }

        // 4. 변경된 최신 잔액 조회
        WalletDTO updatedWallet = walletMapper.getByWalletId(wallet.getWalletId());
        chargeDTO.setUpdatedBalance(updatedWallet != null ? updatedWallet.getBalance() : chargeDTO.getAmount());
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

    @Override
    public java.util.List<org.scoula.wallet.dto.RegisteredCardDTO> getUserRegisteredCards(Integer userId) {
        return walletMapper.getUserRegisteredCards(userId);
    }
}