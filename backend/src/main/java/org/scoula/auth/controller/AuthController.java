package org.scoula.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.auth.dto.PinVerifyRequestDTO;
import org.scoula.auth.dto.PinVerifyResponseDTO;
import org.scoula.auth.service.AuthService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthController {

    private final AuthService authService;

    // auth-001: 간편 PIN 6자리 인증
    @PostMapping("/pin/verify")
    public ResponseEntity<PinVerifyResponseDTO> verifyPin(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody PinVerifyRequestDTO requestDTO
    ) {
        Integer userId = customUser.getUser().getUserId().intValue();
        requestDTO.setUserId(userId);

        log.info("간편 PIN 6자리 인증 요청 - UserID: {}", userId);

        PinVerifyResponseDTO response = authService.verifyPin(requestDTO);

        if (response.isSuccess()) return ResponseEntity.ok(response);

        return ResponseEntity.badRequest().body(response);
    }
}