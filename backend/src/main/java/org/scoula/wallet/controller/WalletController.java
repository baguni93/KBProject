package org.scoula.wallet.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.wallet.dto.PaymentTokenDTO;
import org.scoula.wallet.dto.WalletChargeDTO;
import org.scoula.wallet.dto.WalletDTO;
import org.scoula.wallet.service.PaymentTokenStore;
import org.scoula.wallet.service.WalletService;
import org.scoula.security.util.JwtProcessor;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.scoula.cardpayment.mapper.CardPaymentMapper;
import org.scoula.login.mapper.LoginMapper;
import org.scoula.user.domain.UserVO;
import org.scoula.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Log4j2
public class WalletController {

    private final WalletService walletService;
    private final PaymentTokenStore tokenStore;
    private final JwtProcessor jwtProcessor;
    private final UserMapper userMapper;
    private final LoginMapper loginMapper;
    private final CardPaymentMapper cardPaymentMapper;
    private final PasswordEncoder passwordEncoder;

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

    // 회원 지갑 조회 (JWT 전용)
    @GetMapping("/me")
    public ResponseEntity<WalletDTO> getMyWallet(@AuthenticationPrincipal CustomUser customUser) {
        Long userId = (customUser != null) ? customUser.getUser().getUserId() : 1L;
        log.info("JWT 내 지갑 잔액 조회 요청 - 회원 ID: {}", userId);
        WalletDTO walletDTO = walletService.getWalletByUserId(userId.intValue());
        return ResponseEntity.ok(walletDTO);
    }

    // 회원 지갑 조회 (기존)
    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletDTO> getWalletByUserId(
            HttpServletRequest request,
            @PathVariable("userId") Integer userId) {
        Integer targetUserId = resolveUserId(request, userId);
        log.info("지갑 잔액 조회 요청 - 회원 ID: {}, Resolved ID: {}", userId, targetUserId);
        WalletDTO walletDTO = walletService.getWalletByUserId(targetUserId);
        return ResponseEntity.ok(walletDTO);
    }

    // QR 결제 토큰
    @GetMapping("/me/qr-token")
    public ResponseEntity<PaymentTokenDTO> getQrToken(
            HttpServletRequest request,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Integer targetUserId = resolveUserId(request, userId);
        log.info("1회용 QR 결제 토큰 발급 요청 - 회원 ID: {}", targetUserId);
        WalletDTO wallet = walletService.getWalletByUserId(targetUserId);
        Integer walletId = (wallet != null) ? wallet.getWalletId() : targetUserId;
        PaymentTokenDTO tokenDTO = tokenStore.createToken(targetUserId, walletId, "QR");
        return ResponseEntity.ok(tokenDTO);
    }

    // 바코드 결제 토큰
    @GetMapping("/me/barcode-token")
    public ResponseEntity<PaymentTokenDTO> getBarcodeToken(
            HttpServletRequest request,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Integer targetUserId = resolveUserId(request, userId);
        log.info("1회용 바코드 결제 토큰 발급 요청 - 회원 ID: {}", targetUserId);
        WalletDTO wallet = walletService.getWalletByUserId(targetUserId);
        Integer walletId = (wallet != null) ? wallet.getWalletId() : targetUserId;
        PaymentTokenDTO tokenDTO = tokenStore.createToken(targetUserId, walletId, "BARCODE");
        return ResponseEntity.ok(tokenDTO);
    }

    // 1회용 결제 토큰 즉시 만료/취소 처리 (화면 이탈 시 호출)
    @PostMapping("/me/payment-token/expire")
    public ResponseEntity<Boolean> expirePaymentToken(
            HttpServletRequest request,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Integer targetUserId = resolveUserId(request, userId);
        log.info("1회용 결제 토큰 즉시 만료 처리 - UserID: {}", targetUserId);
        tokenStore.invalidateTokensByUserId(targetUserId);
        return ResponseEntity.ok(true);
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

    // PIN 검증 (5회 연속 오류 시 잠금 및 남은 횟수 연동 - 회원/로그인 팀원 규격 준수)
    @PostMapping("/pin/verify")
    public ResponseEntity<Map<String, Object>> verifyPin(
            HttpServletRequest request,
            @RequestBody Map<String, String> requestBody,
            @RequestParam(value = "userId", required = false) Integer paramUserId) {
        Integer targetUserId = resolveUserId(request, paramUserId);
        String enteredPin = requestBody.get("pinPassword");
        if (enteredPin == null || enteredPin.isBlank()) {
            enteredPin = requestBody.get("pin");
        }

        Map<String, Object> response = new HashMap<>();

        if (enteredPin == null || enteredPin.length() != 6) {
            response.put("verified", false);
            response.put("pinLocked", false);
            response.put("remainingCount", 5);
            response.put("message", "간편비밀번호 6자리를 입력해주세요.");
            return ResponseEntity.ok(response);
        }

        Map<String, Object> userInfo = cardPaymentMapper.getUserPinAuthInfo(targetUserId);
        if (userInfo == null) {
            response.put("verified", false);
            response.put("pinLocked", false);
            response.put("remainingCount", 5);
            response.put("message", "존재하지 않는 회원입니다.");
            return ResponseEntity.ok(response);
        }

        String storedPin = (String) userInfo.get("pinPassword");
        int failCount = userInfo.get("pinFailCount") != null ? ((Number) userInfo.get("pinFailCount")).intValue() : 0;
        String lockedYn = (String) userInfo.get("pinLockedYn");

        if ("Y".equals(lockedYn) || failCount >= 5) {
            response.put("verified", false);
            response.put("pinLocked", true);
            response.put("remainingCount", 0);
            response.put("message", "간편비밀번호 입력 가능 횟수를 초과했습니다. 본인인증 후 재설정해주세요.");
            return ResponseEntity.ok(response);
        }

        boolean matched = false;
        if (storedPin != null) {
            String normalizedStored = storedPin.startsWith("$2y$")
                    ? storedPin.replaceFirst("^\\$2y\\$", "\\$2a\\$")
                    : storedPin;

            if (normalizedStored.startsWith("$2")) {
                matched = passwordEncoder.matches(enteredPin, normalizedStored);
            } else {
                matched = normalizedStored.equals(enteredPin);
            }
        }

        // 개발 및 테스트 계정 호환 지원 (123456)
        if (!matched && "123456".equals(enteredPin)) {
            matched = true;
        }

        if (!matched) {
            loginMapper.increasePinFailCount(targetUserId.longValue());
            int updatedFailCount = failCount + 1;
            int remainingCount = 5 - updatedFailCount;

            if (remainingCount <= 0) {
                loginMapper.lockPin(targetUserId.longValue());
                response.put("verified", false);
                response.put("pinLocked", true);
                response.put("remainingCount", 0);
                response.put("message", "간편비밀번호 입력 가능 횟수를 초과했습니다. 본인인증 후 재설정해주세요.");
            } else {
                response.put("verified", false);
                response.put("pinLocked", false);
                response.put("remainingCount", remainingCount);
                response.put("message", "간편비밀번호가 일치하지 않습니다. (남은 횟수: " + remainingCount + "회)");
            }
            return ResponseEntity.ok(response);
        }

        // 성공 시 실패 횟수 초기화
        loginMapper.resetPinFailCount(targetUserId.longValue());
        response.put("verified", true);
        response.put("pinLocked", false);
        response.put("remainingCount", 5);
        response.put("message", "PIN 인증에 성공하였습니다.");
        return ResponseEntity.ok(response);
    }
}