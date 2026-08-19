package org.scoula.remittance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.remittance.dto.BankRemittanceInfoDTO;
import org.scoula.remittance.dto.RemittanceDTO;
import org.scoula.remittance.service.RemittanceService;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/remittances")
@RequiredArgsConstructor
@Log4j2
public class RemittanceController {

    private final RemittanceService remittanceService;
    private final org.scoula.remittance.mapper.RemittanceMapper remittanceMapper;
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

    // 은행 및 최근 계좌 목록 조회
    @GetMapping("/banks")
    public ResponseEntity<BankRemittanceInfoDTO> getBankRemittanceInfo(
            HttpServletRequest request,
            @RequestParam(value = "userId", required = false) Integer userId) {
        Integer resolvedUserId = resolveUserId(request, userId);
        BankRemittanceInfoDTO info = remittanceService.getBankRemittanceInfo(resolvedUserId);
        return ResponseEntity.ok(info);
    }

    // 계좌 예금주 실명 검증
    @PostMapping("/bank-accounts/verify")
    public ResponseEntity<Map<String, Object>> verifyBankAccount(@RequestBody Map<String, String> body) {
        String bankCode = body.get("bankCode");
        String accountNumber = body.get("accountNumber");

        String ownerName = null;
        try {
            ownerName = remittanceMapper.getAccountOwnerName(bankCode, accountNumber);
        } catch (Exception e) {
            log.warn("계좌 예금주 조회 예외: {}", e.getMessage());
        }

        if (ownerName == null || ownerName.trim().isEmpty()) {
            ownerName = "수취인";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("bankCode", bankCode);
        result.put("accountNumber", accountNumber);
        result.put("ownerName", ownerName);
        result.put("isValid", true);

        return ResponseEntity.ok(result);
    }

    // 친구 송금 수신자 검증
    @PostMapping("/friends/verify")
    public ResponseEntity<Map<String, Object>> verifyFriendAccount(@RequestBody Map<String, Object> body) {
        Object receiverIdObj = body.get("receiverId");
        Integer receiverId = null;
        if (receiverIdObj instanceof Number) {
            receiverId = ((Number) receiverIdObj).intValue();
        } else if (receiverIdObj instanceof String) {
            try {
                receiverId = Integer.parseInt((String) receiverIdObj);
            } catch (Exception ignored) {}
        }

        String receiverName = null;
        if (receiverId != null) {
            try {
                receiverName = remittanceMapper.getUserNicknameOrName(receiverId);
            } catch (Exception e) {
                log.warn("수신자 닉네임 조회 예외: {}", e.getMessage());
            }
        }

        if (receiverName == null || receiverName.trim().isEmpty()) {
            receiverName = "친구";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("receiverId", receiverId);
        result.put("receiverName", receiverName);
        result.put("isValid", true);

        return ResponseEntity.ok(result);
    }

    // 친구 송금
    @PostMapping("/friends")
    public ResponseEntity<RemittanceDTO> sendMoneyToFriend(
            HttpServletRequest request,
            @RequestBody RemittanceDTO remittanceDTO) {
        Integer resolvedUserId = resolveUserId(request, remittanceDTO.getWalletId());
        remittanceDTO.setWalletId(resolvedUserId);
        remittanceDTO.setReceiverType("WALLET");

        boolean result = remittanceService.sendMoney(remittanceDTO);
        if (result) {
            return ResponseEntity.ok(remittanceDTO);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    // 송금 실행
    @PostMapping
    public ResponseEntity<RemittanceDTO> sendMoney(
            HttpServletRequest request,
            @ModelAttribute RemittanceDTO remittanceDTO,
            @RequestParam(value = "files", required = false) MultipartFile... files) {
        Integer resolvedUserId = resolveUserId(request, remittanceDTO.getWalletId());
        remittanceDTO.setWalletId(resolvedUserId);

        if (files != null && files.length > 0) {
            remittanceDTO.setFiles(java.util.Arrays.asList(files));
        }

        boolean result = remittanceService.sendMoney(remittanceDTO);
        if (result) {
            return ResponseEntity.ok(remittanceDTO);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    // 정산 환불
    @PostMapping("/refund")
    public ResponseEntity<Boolean> refundSettlement(
            @RequestParam Integer settlementId,
            @RequestParam Integer requesterUserId,
            @RequestParam Integer memberUserId,
            @RequestParam Integer amount) {
        boolean result = remittanceService.refundSettlement(settlementId,requesterUserId, memberUserId, amount);
        if (result) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    // 결제 내역 영수증 피드 및 사진 업로드 (내 담당 파트)
    @PostMapping("/receipt-feed")
    public ResponseEntity<Map<String, Object>> saveReceiptFeed(
            HttpServletRequest request,
            @RequestParam("userId") Integer userId,
            @RequestParam("targetId") Integer targetId,
            @RequestParam(value = "feedType", defaultValue = "PAYMENT") String feedType,
            @RequestParam(value = "content", defaultValue = "") String content,
            @RequestParam(value = "visibility", defaultValue = "PUBLIC") String visibility,
            @RequestParam(value = "files", required = false) java.util.List<MultipartFile> files) {
        Integer resolvedUserId = resolveUserId(request, userId);
        Map<String, Object> result = remittanceService.saveReceiptFeed(resolvedUserId, targetId, feedType, content, visibility, files);
        return ResponseEntity.ok(result);
    }
}