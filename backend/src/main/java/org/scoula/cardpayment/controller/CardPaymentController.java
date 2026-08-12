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

    // 1단계: 결제 대기(PENDING) 생성 API
    @PostMapping("/api/cards/payments/request")
    public ResponseEntity<org.scoula.cardpayment.dto.CardTransactionResponseDTO> createPendingTransaction(
            @RequestBody(required = false) org.scoula.cardpayment.dto.CardTransactionRequestDTO requestDTO) {
        if (requestDTO == null) {
            requestDTO = org.scoula.cardpayment.dto.CardTransactionRequestDTO.builder().linkedCardId(1).build();
        }
        log.info("결제 대기(PENDING) 레코드 생성 요청 - LinkedCardID: {}", requestDTO.getLinkedCardId());
        org.scoula.cardpayment.dto.CardTransactionResponseDTO response = cardPaymentService.createPendingTransaction(requestDTO);
        return ResponseEntity.ok(response);
    }

    // 2~3단계: 결제 승인 요청 API (Talent API / Postman 테스트용)
    @PostMapping("/api/cards/payments/approve")
    public ResponseEntity<org.scoula.cardpayment.dto.CardTransactionResponseDTO> approveTransaction(
            @RequestBody org.scoula.cardpayment.dto.CardTransactionApproveDTO approveDTO) {
        log.info("결제 승인 테스트 요청: {}", approveDTO);
        org.scoula.cardpayment.dto.CardTransactionResponseDTO response = cardPaymentService.approveTransaction(approveDTO);
        return ResponseEntity.ok(response);
    }

    // 결제 상태 단건 조회 API
    @GetMapping("/api/cards/payments/transactions/{cardTransactionId}")
    public ResponseEntity<org.scoula.cardpayment.dto.CardTransactionResponseDTO> getTransactionStatus(
            @PathVariable("cardTransactionId") Long cardTransactionId) {
        log.info("결제 상태 조회 요청 - ID: {}", cardTransactionId);
        org.scoula.cardpayment.dto.CardTransactionResponseDTO response = cardPaymentService.getTransactionStatus(cardTransactionId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    // 결제 취소/만료 FAILED 처리 API
    @PostMapping("/api/cards/payments/cancel")
    public ResponseEntity<Boolean> cancelTransaction(
            @RequestBody org.scoula.cardpayment.dto.CardTransactionApproveDTO approveDTO) {
        Long cardTxId = approveDTO.getCardTransactionId();
        log.info("결제 취소/만료 FAILED 요청 - ID: {}", cardTxId);
        boolean result = cardPaymentService.cancelTransaction(cardTxId);
        return ResponseEntity.ok(result);
    }
}