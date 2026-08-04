package org.scoula.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.user.dto.PinChangeDTO;
import org.scoula.user.dto.PinResetDTO;
import org.scoula.user.dto.PinVerifyDTO;
import org.scoula.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
public class PinController {

    private final UserService userService;

    // PIN-001 PIN 확인
    @PostMapping("/api/users/{userId}/pin/verify")
    public ResponseEntity<Map<String, Object>> verifyPin(
            @PathVariable Long userId,
            @RequestBody PinVerifyDTO pinVerifyDTO
    ) {
        boolean verified = userService.verifyPin(userId, pinVerifyDTO);
        Map<String, Object> response = new HashMap<>();

        response.put("verified", verified);
        response.put(
                "message",
                verified
                        ? "PIN 확인이 완료되었습니다."
                        : "PIN이 일치하지 않습니다."
        );

        return ResponseEntity.ok(response);
    }

    // PIN-002 PIN 변경
    @PatchMapping("/api/users/{userId}/pin")
    public ResponseEntity<Map<String, Object>> changePin(
            @PathVariable Long userId,
            @RequestBody PinChangeDTO pinChangeDTO
    ) {
        boolean result = userService.changePin(userId, pinChangeDTO);
        Map<String, Object> response = new HashMap<>();

        response.put("success", result);
        response.put("message", "PIN이 변경되었습니다.");

        return ResponseEntity.ok(response);
    }

    // PIN-003 간편비밀번호 재설정
    @PatchMapping("/api/auth/pin/reset")
    public ResponseEntity<Map<String, Object>> resetPin(
            @RequestBody PinResetDTO pinResetDTO
    ) {
        userService.resetPin(pinResetDTO);
        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("message", "간편비밀번호가 재설정되었습니다.");

        return ResponseEntity.ok(response);
    }
}