package org.scoula.transaction.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.transaction.dto.TransactionDTO;
import org.scoula.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Log4j2
public class TransactionController {

    private final TransactionService transactionService;

    // transaction-001: 거래 내역 목록 조회 (GET /api/transactions?userId=1&type=TRANSFER)
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getTransactionList(
            @RequestParam(value = "userId", defaultValue = "1") Integer userId,
            @RequestParam(value = "type", required = false) String type) {
        log.info("거래 내역 목록 조회 - 회원 ID: {}, 유형: {}", userId, type);
        List<TransactionDTO> list = transactionService.getTransactionList(userId, type);
        return ResponseEntity.ok(list);
    }

    // transaction-002: 상세 영수증 조회 (GET /api/transactions/{transactionId})
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransactionDetail(@PathVariable("transactionId") Integer transactionId) {
        log.info("상세 영수증 조회 - 거래 ID: {}", transactionId);
        TransactionDTO dto = transactionService.getTransactionById(transactionId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    // 영수증 메모 작성/수정 (PUT /api/transactions/{transactionId}/memo)
    @PutMapping("/{transactionId}/memo")
    public ResponseEntity<TransactionDTO> updateMemo(
            @PathVariable("transactionId") Integer transactionId,
            @RequestBody Map<String, String> body) {
        String memo = body.get("memo");
        log.info("영수증 메모 업데이트 - 거래 ID: {}, 메모: {}", transactionId, memo);
        boolean success = transactionService.updateMemo(transactionId, memo);
        if (success) {
            TransactionDTO updated = transactionService.getTransactionById(transactionId);
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
