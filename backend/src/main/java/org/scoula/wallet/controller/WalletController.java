package org.scoula.wallet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.wallet.dto.PaymentTokenDTO;
import org.scoula.wallet.dto.WalletChargeDTO;
import org.scoula.wallet.dto.WalletDTO;
import org.scoula.wallet.service.PaymentTokenStore;
import org.scoula.wallet.service.WalletService;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Log4j2
public class WalletController {

    private final WalletService walletService;
    private final PaymentTokenStore tokenStore;
    private final JwtProcessor jwtProcessor;

    private Integer resolveUserId(HttpServletRequest request, Integer paramUserId) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Long userId = jwtProcessor.getUserId(token);
                if (userId != null) {
                    return userId.intValue();
                }
            } catch (Exception e) {
                log.warn("토큰 추출 실패, 파라미터 사용: {}", e.getMessage());
            }
        }
        return (paramUserId != null && paramUserId > 0) ? paramUserId : 1;
    }

    // 회원 지갑 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletDTO> getWalletByUserId(
            HttpServletRequest request,
            @PathVariable("userId") Integer userId) {
        Integer targetUserId = resolveUserId(request, userId);
        log.info("지갑 잔액 조회 요청 - 회원 ID: {}, Resolved ID: {}", userId, targetUserId);
        WalletDTO walletDTO = walletService.getWalletByUserId(targetUserId);
        if (walletDTO == null && targetUserId != 1) {
            walletDTO = walletService.getWalletByUserId(1);
        }
        return ResponseEntity.ok(walletDTO);
    }

    // QR 결제 토큰
    @GetMapping("/me/qr-token")
    public ResponseEntity<PaymentTokenDTO> getQrToken(@RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        log.info("1회용 QR 결제 토큰 발급 요청 - 회원 ID: " + userId);
        WalletDTO wallet = walletService.getWalletByUserId(userId);
        Integer walletId = (wallet != null) ? wallet.getWalletId() : 1;
        PaymentTokenDTO tokenDTO = tokenStore.createToken(userId, walletId, "QR");
        return ResponseEntity.ok(tokenDTO);
    }

    // 바코드 결제 토큰
    @GetMapping("/me/barcode-token")
    public ResponseEntity<PaymentTokenDTO> getBarcodeToken(@RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        log.info("1회용 바코드 결제 토큰 발급 요청 - 회원 ID: " + userId);
        WalletDTO wallet = walletService.getWalletByUserId(userId);
        Integer walletId = (wallet != null) ? wallet.getWalletId() : 1;
        PaymentTokenDTO tokenDTO = tokenStore.createToken(userId, walletId, "BARCODE");
        return ResponseEntity.ok(tokenDTO);
    }

    // 지갑 충전
    @PostMapping("/charges")
    public ResponseEntity<WalletChargeDTO> chargeWallet(@RequestBody WalletChargeDTO chargeDTO) {
        log.info("지갑 충전 요청: {}", chargeDTO);
        WalletChargeDTO result = walletService.chargeWallet(chargeDTO);
        return ResponseEntity.ok(result);
    }

    // 충전 내역 상세
    @GetMapping("/charges/{chargeId}")
    public ResponseEntity<WalletChargeDTO> getChargeDetails(@PathVariable("chargeId") Integer chargeId) {
        log.info("충전 상세 조회 요청 - ID: " + chargeId);
        WalletChargeDTO result = walletService.getChargeDetails(chargeId);
        return ResponseEntity.ok(result);
    }

    // 자동 충전
    @PostMapping("/auto-charge")
    public ResponseEntity<WalletChargeDTO> autoChargeWallet(@RequestBody WalletChargeDTO chargeDTO) {
        log.info("부족금 자동 충전 요청: {}", chargeDTO);
        WalletChargeDTO result = walletService.autoChargeWallet(chargeDTO);
        return ResponseEntity.ok(result);
    }

    // 등록 카드 목록
    @GetMapping("/cards/user/{userId}")
    public ResponseEntity<java.util.List<org.scoula.wallet.dto.RegisteredCardDTO>> getUserCards(@PathVariable("userId") Integer userId) {
        log.info("회원 등록 카드 목록 DB 조회 - 회원 ID: " + userId);
        java.util.List<org.scoula.wallet.dto.RegisteredCardDTO> cards = walletService.getUserRegisteredCards(userId);
        return ResponseEntity.ok(cards);
    }
}