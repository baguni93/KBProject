package org.scoula.user.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.login.dto.TokenDTO;
import org.scoula.login.service.LoginService;
import org.scoula.security.account.domain.CustomUser;
import org.scoula.user.dto.*;
import org.scoula.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LoginService loginService;

    // USER-001 회원가입
    @PostMapping
    public ResponseEntity<Map<String, Object>> signup(@RequestBody UserSignupDTO signupDTO) {
        Long userId = userService.signup(signupDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "회원가입이 완료되었습니다.");
        response.put("userId", userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // USER-002 회원 기본정보 조회
    @GetMapping
    public ResponseEntity<UserInfoDTO> getUserInfo(@AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getUser().getUserId();

        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    // USER-003 닉네임 수정
    @PatchMapping
    public ResponseEntity<Map<String, Object>> updateUser(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody UserUpdateDTO updateDTO
    ) {
        Long userId = customUser.getUser().getUserId();
        boolean result = userService.updateUser(userId, updateDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", "회원정보가 수정되었습니다.");

        return ResponseEntity.ok(response);
    }

    // NICKNAME-001 닉네임 중복 확인
    @GetMapping("/nickname/check")
    public ResponseEntity<NicknameCheckDTO> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.checkNickname(nickname));
    }

    // USER-004 회원 이름 변경
    @PatchMapping("/name")
    public ResponseEntity<Map<String, Object>> changeUserName(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody UserNameChangeDTO changeDTO
    ) {
        Long userId = customUser.getUser().getUserId();

        userService.changeUserName(userId, changeDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "이름이 변경되었습니다.");

        return ResponseEntity.ok(response);
    }

    // USER-005 회원 휴대폰번호 변경
    @PatchMapping("/phone")
    public ResponseEntity<TokenDTO> changePhoneNumber(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody UserPhoneChangeDTO changeDTO
    ) {
        Long userId = customUser.getUser().getUserId();

        userService.changePhoneNumber(userId, changeDTO);

        TokenDTO tokenDTO = loginService.reissueTokenAfterPhoneChange(userId, changeDTO.getNewPhoneNumber().trim());

        return ResponseEntity.ok(tokenDTO);
    }

    // USER-005-1 휴대폰번호 변경 가능 여부 확인
    @GetMapping("/phone/check")
    public ResponseEntity<Map<String, Object>> checkPhoneNumber(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam String phoneNumber
    ) {
        Long userId = customUser.getUser().getUserId();
        boolean available = userService.checkPhoneNumber(userId, phoneNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("available", available);
        response.put("message", "사용 가능한 휴대폰번호입니다.");

        return ResponseEntity.ok(response);
    }

    // USER-006 회원탈퇴
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> withdraw(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody UserWithdrawalDTO withdrawalDTO
    ) {
        Long userId = customUser.getUser().getUserId();

        userService.withdraw(userId, withdrawalDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "회원 탈퇴가 완료되었습니다.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{bankCode}")
    public ResponseEntity<AccountByBankCodeDTO> getAccountByBankCode(@RequestParam int userId, @PathVariable String bankCode){

        return ResponseEntity.ok(userService.getAccountByBankCode(userId , bankCode));
    }
}