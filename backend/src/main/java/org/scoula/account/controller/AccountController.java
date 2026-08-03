package org.scoula.account.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.account.dto.AccountConnectDTO;
import org.scoula.account.dto.AccountDTO;
import org.scoula.account.dto.AccountVerificationConfirmDTO;
import org.scoula.account.dto.AccountVerificationRequestDTO;
import org.scoula.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // ACCOUNT-001 연결 계좌 목록 조회
    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAccounts(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getAccounts(userId));
    }

    // ACCOUNT-002 계좌 인증번호 발급
    @PostMapping("/verification")
    public ResponseEntity<Map<String, Object>> requestVerification(@PathVariable Long userId, @RequestBody AccountVerificationRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.requestVerification(userId, requestDTO));
    }

    // ACCOUNT-003 계좌 인증번호 확인
    @PostMapping("/verification/confirm")
    public ResponseEntity<Map<String, Object>> confirmVerification(@PathVariable Long userId, @RequestBody AccountVerificationConfirmDTO confirmDTO) {
        boolean verified = accountService.confirmVerification(userId, confirmDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("verified", verified);
        response.put("message", "계좌 인증이 완료되었습니다.");

        return ResponseEntity.ok(response);
    }

    // ACCOUNT-004 계좌 연결
    @PostMapping
    public ResponseEntity<AccountDTO> connectAccount(@PathVariable Long userId, @RequestBody AccountConnectDTO connectDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.connectAccount(userId, connectDTO));
    }

    // ACCOUNT-005 대표계좌 설정
    @PatchMapping("/{linkedAccountId}/primary")
    public ResponseEntity<Map<String, Object>> setPrimaryAccount(@PathVariable Long userId, @PathVariable Long linkedAccountId) {
        boolean result = accountService.setPrimaryAccount(userId, linkedAccountId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", "대표계좌가 변경되었습니다.");

        return ResponseEntity.ok(response);
    }

    // ACCOUNT-006 계좌 연결 해제
    @DeleteMapping("/{linkedAccountId}")
    public ResponseEntity<Map<String, Object>> disconnectAccount(@PathVariable Long userId, @PathVariable Long linkedAccountId) {
        boolean result = accountService.disconnectAccount(userId, linkedAccountId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", "계좌 연결이 해제되었습니다.");

        return ResponseEntity.ok(response);
    }
}