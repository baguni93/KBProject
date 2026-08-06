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

    // 사용자 인증 토큰 처리
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
        return paramUserId != null ? paramUserId : 1;
    }

    // 대표 카드 조회
    @GetMapping("/api/payments/cards/primary")
    public ResponseEntity<PrimaryCardResponseDTO> getPrimaryCard(
            HttpServletRequest request,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Integer resolvedUserId = resolveUserId(request, userId);
        PrimaryCardResponseDTO result = cardPaymentService.getPrimaryCard(resolvedUserId);
        return ResponseEntity.ok(result);
    }

    // 카드 등록 상태 조회
    @GetMapping("/api/payments/cards/status")
    public ResponseEntity<CardStatusResponseDTO> getCardStatus(
            HttpServletRequest request,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Integer resolvedUserId = resolveUserId(request, userId);
        CardStatusResponseDTO result = cardPaymentService.getCardStatus(resolvedUserId);
        return ResponseEntity.ok(result);
    }

    // 카드 등록
    @PostMapping("/api/cards")
    public ResponseEntity<PrimaryCardResponseDTO> registerCard(
            HttpServletRequest request,
            @RequestBody CardRegisterDTO cardRegisterDTO) {
        Integer resolvedUserId = resolveUserId(request, cardRegisterDTO.getUserId());
        cardRegisterDTO.setUserId(resolvedUserId);
        PrimaryCardResponseDTO result = cardPaymentService.registerCard(cardRegisterDTO);
        return ResponseEntity.ok(result);
    }

    // 카드 약관 동의
    @PostMapping("/api/cards/agreements")
    public ResponseEntity<Map<String, Object>> saveAgreements(
            HttpServletRequest request,
            @RequestBody CardAgreementDTO cardAgreementDTO) {
        Integer resolvedUserId = resolveUserId(request, cardAgreementDTO.getUserId());
        cardAgreementDTO.setUserId(resolvedUserId);
        boolean success = cardPaymentService.saveCardAgreements(cardAgreementDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "약관 동의가 등록되었습니다." : "약관 동의 실패");

        return ResponseEntity.ok(response);
    }

    // 대표 카드 지정
    @PatchMapping("/api/cards/{cardId}/primary")
    public ResponseEntity<Map<String, Object>> setPrimaryCard(
            HttpServletRequest request,
            @PathVariable("cardId") Integer cardId,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Integer resolvedUserId = resolveUserId(request, userId);
        boolean success = cardPaymentService.setPrimaryCard(cardId, resolvedUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("cardId", cardId);
        response.put("message", success ? "대표 카드로 지정되었습니다." : "대표 카드 지정 실패");

        return ResponseEntity.ok(response);
    }
}
