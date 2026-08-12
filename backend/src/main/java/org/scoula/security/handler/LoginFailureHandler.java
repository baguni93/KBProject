package org.scoula.security.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.login.mapper.LoginMapper;
import org.scoula.security.util.JsonResponse;
import org.scoula.user.domain.UserVO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Log4j2
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private static final int MAX_FAIL_COUNT = 5;
    private final LoginMapper loginMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
//        JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, "사용자 ID 또는 비밀번호가 일치하지 않습니다.");
//    }

        String message = exception.getMessage();
        String phoneNumber = (String) request.getAttribute("loginPhoneNumber");

        // 휴대폰 본인인증 관련 실패는 PIN 실패 횟수에 포함하지 않음
        if (message != null && (
                message.equals("휴대폰 본인인증이 필요합니다.")
                        || message.equals("휴대폰 본인인증이 완료되지 않았습니다.")
                        || message.equals("휴대폰 본인인증 유효시간이 만료되었습니다.")
        )) {
            JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, message);
            return;
        }

        // 이미 PIN 잠금 상태인 경우
        if (message != null && message.contains("간편비밀번호 입력 가능 횟수를 초과")) {
            JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, message);
            return;
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다.");
            return;
        }

        UserVO user = loginMapper.findUserByPhoneNumber(phoneNumber);

        if (user == null) {
            JsonResponse.sendError(response, HttpStatus.UNAUTHORIZED, "가입되지 않은 휴대폰번호입니다.");
            return;
        }

        int currentFailCount = user.getPinFailCount() == null ? 0 : user.getPinFailCount();

        // PIN 로그인 실패 횟수 증가
        loginMapper.increasePinFailCount(user.getUserId());

        int newFailCount = currentFailCount + 1;
        int remainingCount = MAX_FAIL_COUNT - newFailCount;

        // PIN 5회 실패 시 잠금
        if (remainingCount <= 0) {
            loginMapper.lockPin(user.getUserId());

            JsonResponse.sendError(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "간편비밀번호 입력 가능 횟수를 초과했습니다. 본인인증 후 재설정해주세요."
            );
            return;
        }

        JsonResponse.sendError(
                response,
                HttpStatus.UNAUTHORIZED,
                "간편비밀번호가 일치하지 않습니다. 남은 횟수: " + remainingCount
        );
    }
}