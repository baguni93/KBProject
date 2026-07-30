package org.scoula.wallet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.remittance.mapper.RemittanceMapper;
import org.scoula.wallet.dto.PaymentConfirmRequestDTO;
import org.scoula.wallet.dto.PaymentConfirmResponseDTO;
import org.scoula.wallet.dto.PaymentTokenDTO;
import org.scoula.wallet.dto.WalletDTO;
import org.scoula.wallet.mapper.WalletMapper;
import org.scoula.wallet.service.PaymentTokenStore;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Log4j2
public class PaymentController {

    private final PaymentTokenStore tokenStore;
    private final RemittanceMapper remittanceMapper;
    private final WalletMapper walletMapper;

    @PostMapping("/confirm")
    @Transactional
    public ResponseEntity<PaymentConfirmResponseDTO> confirmPayment(@RequestBody PaymentConfirmRequestDTO request) {
        log.info("가맹점 결제 승인 요청 - 토큰: {}, 금액: {}", request.getToken(), request.getAmount());

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("결제 금액이 바르지 않습니다.");
        }

        PaymentTokenDTO tokenDTO = tokenStore.validateAndConsumeToken(request.getToken());

        int updatedRows = remittanceMapper.subtractBalance(tokenDTO.getWalletId(), request.getAmount());
        if (updatedRows == 0) {
            throw new RuntimeException("지갑 잔액이 부족하여 결제를 완료할 수 없습니다.");
        }

        WalletDTO updatedWallet = walletMapper.getByWalletId(tokenDTO.getWalletId());

        String merchant = (request.getMerchantName() != null && !request.getMerchantName().isEmpty())
                ? request.getMerchantName() : "KB Pay 현장 가맹점";

        PaymentConfirmResponseDTO response = PaymentConfirmResponseDTO.builder()
                .status("SUCCESS")
                .message("결제가 성공적으로 완료되었습니다.")
                .remainingBalance(updatedWallet.getBalance())
                .merchantName(merchant)
                .build();

        return ResponseEntity.ok(response);
    }
}
