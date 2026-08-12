package org.scoula.login.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.login.dto.LogoutDTO;
import org.scoula.login.dto.RefreshTokenRequestDTO;
import org.scoula.login.dto.TokenDTO;
import org.scoula.login.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
public class TokenController {

    private final LoginService loginService;

    // AUTH-006 로그아웃
    @PostMapping("/api/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody LogoutDTO logoutDTO) {
        loginService.logout(logoutDTO);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "로그아웃되었습니다.");

        return ResponseEntity.ok(response);
    }

    // AUTH-007 토큰 재발급
    @PostMapping("/api/refresh")
    public ResponseEntity<TokenDTO> refreshToken(@RequestBody RefreshTokenRequestDTO requestDTO) {
        TokenDTO tokenDTO = loginService.refreshToken(requestDTO);
        return ResponseEntity.ok(tokenDTO);
    }
}