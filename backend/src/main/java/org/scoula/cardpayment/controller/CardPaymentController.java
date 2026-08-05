package org.scoula.cardpayment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.cardpayment.dto.CardAgreementDTO;
import org.scoula.cardpayment.dto.CardRegisterDTO;
import org.scoula.cardpayment.dto.CardStatusResponseDTO;
import org.scoula.cardpayment.dto.PrimaryCardResponseDTO;
import org.scoula.cardpayment.service.CardPaymentService;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class CardPaymentController {

    private final CardPaymentService cardPaymentService;
    private final JwtProcessor jwtProcessor;

    // 토큰에서 userId 추출 (토큰이 없거나 파싱 실패 시 파라미터/기본값 사용)
    private Integer resolveUserId(HttpServletRequest request, Integer paramUserId) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Long userId = jwtProcessor.getUserId(token);
                if (userId != null) {
                    log.info("JWT 토큰에서 추출된 회원 ID: {}", userId);
                    return userId.intValue();
                }
            } catch (Exception e) {
                log.warn("토큰에서 userId 추출 실패, 기본값 사용: {}", e.getMessage());
            }
        }
        return paramUserId != null ? paramUserId : 1;
    }

    // pay-001: 카드 결제 대기 (대표 카드가 설정된 상태의 결제 대기 정보 조회)
    @GetMapping("/api/payments/cards/primary")
    public ResponseEntity<PrimaryCardResponseDTO> getPrimaryCard(
            HttpServletRequest request,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Integer resolvedUserId = resolveUserId(request, userId);
        log.info("대표 카드 조회 요청 - 회원 ID: {}", resolvedUserId);
        PrimaryCardResponseDTO result = cardPaymentService.getPrimaryCard(resolvedUserId);
        return ResponseEntity.ok(result);
    }

    // pay-002: 카드 미등록 (대표 카드 미지정 상태의 상태값 및 등록 가이드 조회)
    @GetMapping("/api/payments/cards/status")
    public ResponseEntity<CardStatusResponseDTO> getCardStatus(
            HttpServletRequest request,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Integer resolvedUserId = resolveUserId(request, userId);
        log.info("카드 등록 상태 및 가이드 조회 요청 - 회원 ID: {}", resolvedUserId);
        CardStatusResponseDTO result = cardPaymentService.getCardStatus(resolvedUserId);
        return ResponseEntity.ok(result);
    }

    // card-001: 신용/체크 카드 정보 등록
    @PostMapping("/api/cards")
    public ResponseEntity<PrimaryCardResponseDTO> registerCard(
            HttpServletRequest request,
            @RequestBody CardRegisterDTO cardRegisterDTO) {
        Integer resolvedUserId = resolveUserId(request, cardRegisterDTO.getUserId());
        cardRegisterDTO.setUserId(resolvedUserId);
        log.info("신규 카드 등록 요청 - 회원 ID: {}", resolvedUserId);
        PrimaryCardResponseDTO result = cardPaymentService.registerCard(cardRegisterDTO);
        return ResponseEntity.ok(result);
    }

    // card-002: 카드 결제 약관 동의
    @PostMapping("/api/cards/agreements")
    public ResponseEntity<Map<String, Object>> saveAgreements(
            HttpServletRequest request,
            @RequestBody CardAgreementDTO cardAgreementDTO) {
        Integer resolvedUserId = resolveUserId(request, cardAgreementDTO.getUserId());
        cardAgreementDTO.setUserId(resolvedUserId);
        log.info("카드 약관 동의 요청 - 회원 ID: {}", resolvedUserId);
        boolean success = cardPaymentService.saveCardAgreements(cardAgreementDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "약관 동의가 정상 등록되었습니다." : "약관 동의 처리에 실패하였습니다.");

        return ResponseEntity.ok(response);
    }

    // card-003: 대표 카드 지정 완료
    @PatchMapping("/api/cards/{cardId}/primary")
    public ResponseEntity<Map<String, Object>> setPrimaryCard(
            HttpServletRequest request,
            @PathVariable("cardId") Integer cardId,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Integer resolvedUserId = resolveUserId(request, userId);
        log.info("대표 카드 변경 요청 - CardID: {}, UserID: {}", cardId, resolvedUserId);
        boolean success = cardPaymentService.setPrimaryCard(cardId, resolvedUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("cardId", cardId);
        response.put("message", success ? "대표 카드로 지정되었습니다." : "대표 카드 변경 실패");

        return ResponseEntity.ok(response);
    }
}
