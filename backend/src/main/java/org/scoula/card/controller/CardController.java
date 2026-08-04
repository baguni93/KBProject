package org.scoula.card.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.card.dto.CardAgreementDTO;
import org.scoula.card.dto.CardRegisterDTO;
import org.scoula.card.dto.CardStatusResponseDTO;
import org.scoula.card.dto.PrimaryCardResponseDTO;
import org.scoula.card.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
public class CardController {

    private final CardService cardService;

    // pay-001: 카드 결제 대기 (대표 카드가 설정된 상태의 결제 대기 정보 조회)
    @GetMapping("/api/payments/cards/primary")
    public ResponseEntity<PrimaryCardResponseDTO> getPrimaryCard(
            @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        log.info("대표 카드 조회 요청 - 회원 ID: {}", userId);
        PrimaryCardResponseDTO result = cardService.getPrimaryCard(userId);
        return ResponseEntity.ok(result);
    }

    // pay-002: 카드 미등록 (대표 카드 미지정 상태의 상태값 및 등록 가이드 조회)
    @GetMapping("/api/payments/cards/status")
    public ResponseEntity<CardStatusResponseDTO> getCardStatus(
            @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        log.info("카드 등록 상태 및 가이드 조회 요청 - 회원 ID: {}", userId);
        CardStatusResponseDTO result = cardService.getCardStatus(userId);
        return ResponseEntity.ok(result);
    }

    // card-001: 신용/체크 카드 정보 등록
    @PostMapping("/api/cards")
    public ResponseEntity<PrimaryCardResponseDTO> registerCard(
            @RequestBody CardRegisterDTO cardRegisterDTO) {
        log.info("신규 카드 등록 요청: {}", cardRegisterDTO);
        if (cardRegisterDTO.getUserId() == null) {
            cardRegisterDTO.setUserId(1); // 디폴트 유저 ID
        }
        PrimaryCardResponseDTO result = cardService.registerCard(cardRegisterDTO);
        return ResponseEntity.ok(result);
    }

    // card-002: 카드 결제 약관 동의
    @PostMapping("/api/cards/agreements")
    public ResponseEntity<Map<String, Object>> saveAgreements(
            @RequestBody CardAgreementDTO cardAgreementDTO) {
        log.info("카드 약관 동의 요청: {}", cardAgreementDTO);
        if (cardAgreementDTO.getUserId() == null) {
            cardAgreementDTO.setUserId(1);
        }
        boolean success = cardService.saveCardAgreements(cardAgreementDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "약관 동의가 정상 등록되었습니다." : "약관 동의 처리에 실패하였습니다.");

        return ResponseEntity.ok(response);
    }

    // card-003: 대표 카드 지정 완료
    @PatchMapping("/api/cards/{cardId}/primary")
    public ResponseEntity<Map<String, Object>> setPrimaryCard(
            @PathVariable("cardId") Integer cardId,
            @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        log.info("대표 카드 변경 요청 - CardID: {}, UserID: {}", cardId, userId);
        boolean success = cardService.setPrimaryCard(cardId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("cardId", cardId);
        response.put("message", success ? "대표 카드로 지정되었습니다." : "대표 카드 변경 실패");

        return ResponseEntity.ok(response);
    }
}
