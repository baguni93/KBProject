package org.scoula.transaction.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.security.util.JwtProcessor;
import org.scoula.transaction.dto.TransactionDTO;
import org.scoula.transaction.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Log4j2
public class TransactionController {

    private final TransactionService transactionService;
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

    // 거래 내역 목록 조회
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getTransactionList(
            HttpServletRequest request,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "type", required = false) String type) {
        Integer resolvedUserId = resolveUserId(request, userId);
        List<TransactionDTO> list = transactionService.getTransactionList(resolvedUserId, type);
        return ResponseEntity.ok(list);
    }

    // 상세 영수증 조회
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransactionDetail(@PathVariable("transactionId") Integer transactionId) {
        TransactionDTO dto = transactionService.getTransactionById(transactionId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    // 영수증 메모 수정
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
