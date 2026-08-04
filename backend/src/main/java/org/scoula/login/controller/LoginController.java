package org.scoula.login.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.login.dto.*;
import org.scoula.login.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;


    /**
     * AUTH-001 인증번호 발급
     */
    @PostMapping("/phone/send")
    public ResponseEntity<?> sendPhoneAuthCode(
            @RequestBody PhoneAuthSendDTO sendDTO) {

        String verificationCode =
                loginService.sendPhoneAuthCode(
                        sendDTO
                );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "message",
                "개발용 인증번호가 생성되었습니다."
        );

        /*
         * 개발용으로만 응답에 인증번호 포함
         */
        response.put(
                "verificationCode",
                verificationCode
        );

        response.put(
                "expiresIn",
                180
        );

        return ResponseEntity.ok(response);
    }


    /**
     * AUTH-002 인증번호 재발급
     */
    @PostMapping("/phone/resend")
    public ResponseEntity<Map<String, Object>> resendPhoneAuthCode(
            @RequestBody PhoneAuthResendDTO resendDTO
    ) {
        String verificationCode =
                loginService.resendPhoneAuthCode(resendDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "개발용 인증번호가 재생성되었습니다.");
        response.put("verificationCode", verificationCode);
        response.put("expiresIn", 180);

        return ResponseEntity.ok(response);
    }


    /**
     * AUTH-003 인증번호 확인
     */
    @PostMapping("/phone/verify")
    public ResponseEntity<Map<String, Object>>
    verifyPhoneAuthCode(
            @RequestBody
            PhoneAuthVerifyDTO verifyDTO
    ) {

        loginService.verifyPhoneAuthCode(
                verifyDTO
        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "message",
                "휴대폰 인증이 완료되었습니다."
        );

        response.put(
                "verified",
                true
        );

        return ResponseEntity.ok(response);
    }


    /**
     * AUTH-004 가입 여부 확인
     */
    @PostMapping("/signup/check")
    public ResponseEntity<Map<String, Object>>
    checkSignupStatus(
            @RequestBody
            SignupCheckDTO checkDTO
    ) {

        String memberStatus =
                loginService.checkSignupStatus(
                        checkDTO
                );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "memberStatus",
                memberStatus
        );

        response.put(
                "existingMember",
                "EXISTING".equals(memberStatus)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * AUTH-005 로그인
     *
     * POST /api/login
     */
    @PostMapping
    public ResponseEntity<TokenDTO> login(
            @RequestBody
            PinLoginDTO loginDTO
    ) {

        TokenDTO tokenDTO =
                loginService.login(
                        loginDTO
                );

        return ResponseEntity.ok(
                tokenDTO
        );
    }


    /**
     * AUTH-006 로그아웃
     *
     * POST /api/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logoutWrongPath(
            @RequestBody
            LogoutDTO logoutDTO
    ) {

        loginService.logout(
                logoutDTO
        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "message",
                "로그아웃되었습니다."
        );

        return ResponseEntity.ok(
                response
        );
    }
}