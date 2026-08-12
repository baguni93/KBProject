package org.scoula.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.login.domain.RefreshTokenVO;
import org.scoula.login.mapper.LoginMapper;
import org.scoula.security.account.domain.CustomUser;
import org.scoula.security.account.dto.AuthResultDTO;
import org.scoula.security.account.dto.UserInfoDTO;
import org.scoula.security.util.JsonResponse;
import org.scoula.security.util.JwtProcessor;
import org.scoula.user.domain.UserVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Log4j2
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProcessor jwtProcessor;
    private final LoginMapper loginMapper;

    private AuthResultDTO makeAuthResult(CustomUser user) {
        UserVO userVO = user.getUser();
        String phoneNumber = userVO.getPhoneNumber();

        // Access Token 생성
        String accessToken = jwtProcessor.generateAccessToken(phoneNumber, userVO.getUserId());

        // Refresh Token 생성
        String refreshToken = jwtProcessor.generateRefreshToken(phoneNumber, userVO.getUserId());

        // 기존 Refresh Token 삭제
        loginMapper.deleteRefreshTokenByUserId(userVO.getUserId());

        // Refresh Token 저장
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusSeconds(jwtProcessor.getRefreshTokenValidSeconds());

        RefreshTokenVO refreshTokenVO = RefreshTokenVO.builder()
                .userId(userVO.getUserId())
                .refreshToken(refreshToken)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        int inserted = loginMapper.insertRefreshToken(refreshTokenVO);

        if (inserted != 1) {
            throw new IllegalStateException("리프레시 토큰 저장에 실패했습니다.");
        }

        // 인증 결과 생성
        return new AuthResultDTO(
                "Bearer",
                accessToken,
                refreshToken,
                jwtProcessor.getAccessTokenValidSeconds(),
                UserInfoDTO.of(userVO)
        );
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // 인증 결과 Principal
        CustomUser user = (CustomUser) authentication.getPrincipal();
        // PIN 로그인 실패 횟수 및 잠금 상태 초기화
        loginMapper.resetPinFailCount(user.getUser().getUserId());
        // 인증 성공 결과를 JSON으로 직접 응답
        AuthResultDTO result = makeAuthResult(user);
        JsonResponse.send(response, result);
    }
}
