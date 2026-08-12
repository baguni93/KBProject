package org.scoula.security.filter;

import lombok.extern.log4j.Log4j2;
import org.scoula.login.domain.PhoneAuthVO;
import org.scoula.login.mapper.LoginMapper;
import org.scoula.security.account.dto.LoginDTO;
import org.scoula.security.handler.LoginFailureHandler;
import org.scoula.security.handler.LoginSuccessHandler;
import org.scoula.user.domain.UserVO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Log4j2
public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final int LOGIN_AUTH_EXPIRE_MINUTES = 10;

    private final LoginMapper loginMapper;

    public JwtUsernamePasswordAuthenticationFilter(
            AuthenticationManager authenticationManager,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler,
            LoginMapper loginMapper) {

        super(authenticationManager);
        this.loginMapper = loginMapper;

        setFilterProcessesUrl("/api/login");
        setAuthenticationSuccessHandler(loginSuccessHandler);
        setAuthenticationFailureHandler(loginFailureHandler);
    }

    // 로그인 요청 URL인 경우 로그인 작업 처리
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        // 요청 BODY의 JSON에서 phoneNumber, pinPassword → LoginDTO
        LoginDTO login = LoginDTO.of(request);

        String phoneNumber = login.getPhoneNumber().replaceAll("[^0-9]", "");

        // 로그인 실패 처리에서 사용할 휴대폰번호 저장
        request.setAttribute("loginPhoneNumber", phoneNumber);

        // 로그인 전 휴대폰 본인인증 완료 여부 확인
        PhoneAuthVO phoneAuth = loginMapper.findLatestPhoneAuth(phoneNumber, "SIGN_UP");

        if (phoneAuth == null) {
            throw new BadCredentialsException("휴대폰 본인인증이 필요합니다.");
        }

        if (!"Y".equals(phoneAuth.getVerifiedYn())) {
            throw new BadCredentialsException("휴대폰 본인인증이 완료되지 않았습니다.");
        }

        LocalDateTime expiresAt = phoneAuth.getRequestedAt().plusMinutes(LOGIN_AUTH_EXPIRE_MINUTES);

        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new BadCredentialsException("휴대폰 본인인증 유효시간이 만료되었습니다.");
        }

        // PIN 잠금 상태 확인
        UserVO user = loginMapper.findUserByPhoneNumber(phoneNumber);

        if (user != null && "Y".equals(user.getPinLockedYn())) {
            throw new BadCredentialsException("간편비밀번호 입력 가능 횟수를 초과했습니다. 본인인증 후 재설정해주세요.");
        }

        // 인증 토큰 구성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(phoneNumber, login.getPinPassword());

        // AuthenticationManager에게 인증 요청
        return getAuthenticationManager().authenticate(authenticationToken);
    }
}