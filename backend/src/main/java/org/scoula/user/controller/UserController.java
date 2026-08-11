package org.scoula.user.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.account.dto.AccountDTO;
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


    // USER-001 회원가입
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


    // USER-002 회원 기본정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoDTO> getUserInfo(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                userService.getUserInfo(userId)
        );
    }


    // USER-003 닉네임 수정
    @PatchMapping("/{userId}")
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


    // NICKNAME-001 닉네임 중복 확인
    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameCheckDTO> checkNickname(
            @RequestParam String nickname
    ) {
        return ResponseEntity.ok(
                userService.checkNickname(nickname)
        );
    }


    // USER-004 회원 이름 변경
    @PatchMapping("/{userId}/name")
    public ResponseEntity<Map<String, Object>> changeUserName(
            @PathVariable Long userId,
            @RequestBody UserNameChangeDTO changeDTO
    ) {
        userService.changeUserName(userId, changeDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "이름이 변경되었습니다.");

        return ResponseEntity.ok(response);
    }


    // USER-005 회원 휴대폰번호 변경
    @PatchMapping("/{userId}/phone")
    public ResponseEntity<Map<String, Object>> changePhoneNumber(
            @PathVariable Long userId,
            @RequestBody UserPhoneChangeDTO changeDTO
    ) {
        userService.changePhoneNumber(userId, changeDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "휴대폰번호가 변경되었습니다.");

        return ResponseEntity.ok(response);
    }


    // USER-006 회원탈퇴
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

    @GetMapping("/account/{bankCode}")
    public ResponseEntity<AccountByBankCodeDTO> getAccountByBankCode(@RequestParam int userId, @PathVariable String bankCode){

        return ResponseEntity.ok(userService.getAccountByBankCode(userId , bankCode));
    }
}