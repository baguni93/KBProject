package org.scoula.remittance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.remittance.dto.BankRemittanceInfoDTO;
import org.scoula.remittance.dto.RemittanceDTO;
import org.scoula.remittance.service.RemittanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/remittances")
@RequiredArgsConstructor
@Log4j2
public class RemittanceController {

    private final RemittanceService remittanceService;

    // remit-bank-001: 송금 가능 은행 및 최근 계좌 조회 (GET /api/remittances/banks)
    @GetMapping("/banks")
    public ResponseEntity<BankRemittanceInfoDTO> getBankRemittanceInfo(
            @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        log.info("최근 계좌 및 은행 목록 조회 - 회원 ID: {}", userId);
        BankRemittanceInfoDTO info = remittanceService.getBankRemittanceInfo(userId);
        return ResponseEntity.ok(info);
    }

    // remit-bank-002: 계좌 송금 예금주 실명 검증 (POST /api/remittances/bank-accounts/verify)
    @PostMapping("/bank-accounts/verify")
    public ResponseEntity<Map<String, Object>> verifyBankAccount(@RequestBody Map<String, String> body) {
        String bankCode = body.get("bankCode");
        String accountNumber = body.get("accountNumber");
        log.info("계좌 예금주 실명 검증 요청 - 은행: {}, 계좌: {}", bankCode, accountNumber);

        Map<String, Object> result = new HashMap<>();
        result.put("bankCode", bankCode);
        result.put("accountNumber", accountNumber);
        result.put("ownerName", "이KB");
        result.put("isValid", true);

        return ResponseEntity.ok(result);
    }

    // remit-friend-001-verify: 친구 송금 회원 실명 검증 (POST /api/remittances/friends/verify)
    @PostMapping("/friends/verify")
    public ResponseEntity<Map<String, Object>> verifyFriendAccount(@RequestBody Map<String, Object> body) {
        Object receiverIdObj = body.get("receiverId");
        log.info("친구 송금 수신자 실명 검증 요청 - 회원 ID: {}", receiverIdObj);

        Map<String, Object> result = new HashMap<>();
        result.put("receiverId", receiverIdObj);
        result.put("receiverName", "김국민");
        result.put("isValid", true);

        return ResponseEntity.ok(result);
    }

    // remit-friend-002: 친구 송금 (POST /api/remittances/friends)
    @PostMapping("/friends")
    public ResponseEntity<RemittanceDTO> sendMoneyToFriend(@RequestBody RemittanceDTO remittanceDTO) {
        remittanceDTO.setReceiverType("WALLET");
        log.info("친구 송금 요청 데이터: " + remittanceDTO);

        boolean result = remittanceService.sendMoney(remittanceDTO);
        if (result) {
            return ResponseEntity.ok(remittanceDTO);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    // remit-003: 송금 최종 실행 (POST /api/remittances)
    @PostMapping
    public ResponseEntity<RemittanceDTO> sendMoney(
            @ModelAttribute RemittanceDTO remittanceDTO,
            @RequestParam(value = "files", required = false) org.springframework.web.multipart.MultipartFile[] files) {
        System.out.println("========== [송금 컨트롤러 디버그] 수신된 files 배열: " + (files != null ? files.length + "개" : "null"));
        if (files != null && files.length > 0) {
            remittanceDTO.setFiles(java.util.Arrays.asList(files));
        }
        log.info("송금 최종 실행 요청 데이터: " + remittanceDTO + ", 첨부파일 개수: " + (files != null ? files.length : 0));

        boolean result = remittanceService.sendMoney(remittanceDTO);
        if (result) {
            return ResponseEntity.ok(remittanceDTO);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}