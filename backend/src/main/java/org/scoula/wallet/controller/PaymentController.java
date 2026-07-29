package org.scoula.wallet.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.remittance.mapper.RemittanceMapper;
import org.scoula.wallet.dto.PaymentTokenDTO;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentConfirmRequest {
        private String token;            // 스캔된 1회용 결제 토큰
        private Integer amount;          // 결제 금액
        private String merchantName;     // 가맹점 이름 (예: KB스타벅스점)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentConfirmResponse {
        private String status;
        private String message;
        private Integer remainingBalance;
        private String merchantName;
    }

    // POS 리더기 / 가맹점 결제 승인 API (POST /api/payments/confirm)
    @PostMapping("/confirm")
    @Transactional
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(@RequestBody PaymentConfirmRequest request) {
        log.info("가맹점 결제 승인 요청 - 토큰: {}, 금액: {}", request.getToken(), request.getAmount());

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("결제 금액이 바르지 않습니다.");
        }

        // 1. 토큰 검증 및 1회 사용 처리 (TTL 내 존재 확인 후 즉시 삭제)
        PaymentTokenDTO tokenDTO = tokenStore.validateAndConsumeToken(request.getToken());

        // 2. 지갑 잔액 차감 (차감 불가능 시 롤백)
        int updatedRows = remittanceMapper.subtractBalance(tokenDTO.getWalletId(), request.getAmount());
        if (updatedRows == 0) {
            throw new RuntimeException("지갑 잔액이 부족하여 결제를 완료할 수 없습니다.");
        }

        // 3. 변경된 지갑 잔액 조회
        var updatedWallet = walletMapper.getByWalletId(tokenDTO.getWalletId());

        String merchant = (request.getMerchantName() != null && !request.getMerchantName().isEmpty())
                ? request.getMerchantName() : "KB Pay 현장 가맹점";

        PaymentConfirmResponse response = PaymentConfirmResponse.builder()
                .status("SUCCESS")
                .message("결제가 성공적으로 완료되었습니다.")
                .remainingBalance(updatedWallet.getBalance())
                .merchantName(merchant)
                .build();

        return ResponseEntity.ok(response);
    }
}
