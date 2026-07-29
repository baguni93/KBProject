package org.scoula.user.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.user.dto.*;
import org.scoula.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    // 회원가입
    @PostMapping
    public ResponseEntity<Map<String, Object>> signup(
            @RequestBody UserSignupDTO signupDTO
    ) {
        Long userId =
                userService.signup(signupDTO);

        Map<String, Object> response =
                new HashMap<>();

        response.put("success", true);
        response.put(
                "message",
                "회원가입이 완료되었습니다."
        );
        response.put("userId", userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // 회원 기본정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDTO> getUserInfo(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                userService.getUserInfo(userId)
        );
    }


    // 회원정보 수정
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateDTO updateDTO
    ) {
        boolean result =
                userService.updateUser(
                        userId,
                        updateDTO
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put("success", result);
        response.put(
                "message",
                "회원정보가 수정되었습니다."
        );

        return ResponseEntity.ok(response);
    }


    // 닉네임 중복 확인
    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameCheckDTO> checkNickname(
            @RequestParam String nickname
    ) {
        return ResponseEntity.ok(
                userService.checkNickname(nickname)
        );
    }


    // PIN 확인
    @PostMapping("/{userId}/pin/verify")
    public ResponseEntity<Map<String, Object>> verifyPin(
            @PathVariable Long userId,
            @RequestBody PinVerifyDTO pinVerifyDTO
    ) {
        boolean verified =
                userService.verifyPin(
                        userId,
                        pinVerifyDTO
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put("verified", verified);
        response.put(
                "message",
                verified
                        ? "PIN 확인이 완료되었습니다."
                        : "PIN이 일치하지 않습니다."
        );

        return ResponseEntity.ok(response);
    }


    // PIN 변경
    @PutMapping("/{userId}/pin")
    public ResponseEntity<Map<String, Object>> changePin(
            @PathVariable Long userId,
            @RequestBody PinChangeDTO pinChangeDTO
    ) {
        boolean result =
                userService.changePin(
                        userId,
                        pinChangeDTO
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put("success", result);
        response.put(
                "message",
                "PIN이 변경되었습니다."
        );

        return ResponseEntity.ok(response);
    }


    // 회원탈퇴
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> withdraw(
            @PathVariable Long userId,
            @RequestBody UserWithdrawalDTO withdrawalDTO
    ) {
        userService.withdraw(
                userId,
                withdrawalDTO
        );

        Map<String, Object> response =
                new HashMap<>();

        response.put("success", true);
        response.put(
                "message",
                "회원 탈퇴가 완료되었습니다."
        );

        return ResponseEntity.ok(response);
    }
}