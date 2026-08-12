package org.scoula.cardpayment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.cardpayment.dto.CardAgreementDTO;
import org.scoula.cardpayment.dto.CardBinResponseDTO;
import org.scoula.cardpayment.dto.CardRegisterDTO;
import org.scoula.cardpayment.dto.CardStatusResponseDTO;
import org.scoula.cardpayment.dto.PrimaryCardResponseDTO;
import org.scoula.cardpayment.service.CardPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Log4j2
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CardPaymentController {

    private final CardPaymentService cardPaymentService;

    @GetMapping("/api/card-payment/bin/{binNumber}")
    public ResponseEntity<CardBinResponseDTO> getCardInfoByBin(@PathVariable("binNumber") String binNumber) {
        log.info("카드 BIN 자동 조회 요청 - BIN: {}", binNumber);
        CardBinResponseDTO responseDTO = cardPaymentService.getAutoFetchedCardInfo(binNumber);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/api/payments/cards/primary")
    public ResponseEntity<PrimaryCardResponseDTO> getPrimaryCard(
            HttpServletRequest request,
            @RequestParam(value = "userId", required = false, defaultValue = "1") Integer userId) {
        log.info("대표 카드 조회 요청 - UserID: {}", userId);
        PrimaryCardResponseDTO primaryCard = cardPaymentService.getPrimaryCard(userId);
        return ResponseEntity.ok(primaryCard);
    }

    @GetMapping("/api/payments/cards/status")
    public ResponseEntity<CardStatusResponseDTO> getCardStatus(
            HttpServletRequest request,
            @RequestParam(value = "userId", required = false, defaultValue = "1") Integer userId) {
        log.info("카드 상태 및 가이드 조회 요청 - UserID: {}", userId);
        CardStatusResponseDTO status = cardPaymentService.getCardStatus(userId);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/api/cards")
    public ResponseEntity<PrimaryCardResponseDTO> registerCard(
            HttpServletRequest request,
            @RequestBody CardRegisterDTO cardRegisterDTO) {
        log.info("카드 등록 요청: {}", cardRegisterDTO);
        PrimaryCardResponseDTO response = cardPaymentService.registerCard(cardRegisterDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/cards/agreements")
    public ResponseEntity<Boolean> saveAgreements(
            HttpServletRequest request,
            @RequestBody CardAgreementDTO cardAgreementDTO) {
        log.info("카드 약관 동의 저장 요청: {}", cardAgreementDTO);
        boolean result = cardPaymentService.saveCardAgreements(cardAgreementDTO);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/api/cards/{cardId}/primary")
    public ResponseEntity<Boolean> setPrimaryCard(
            HttpServletRequest request,
            @PathVariable("cardId") Integer cardId,
            @RequestParam(value = "userId", required = false, defaultValue = "1") Integer userId) {
        log.info("대표 카드 변경 요청 - CardID: {}, UserID: {}", cardId, userId);
        boolean result = cardPaymentService.setPrimaryCard(cardId, userId);
        return ResponseEntity.ok(result);
    }
}