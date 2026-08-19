package org.scoula.login.service;

import org.scoula.login.dto.*;

public interface LoginService {

    // AUTH-001 인증번호 발급
    String sendPhoneAuthCode(PhoneAuthSendDTO sendDTO);

    // AUTH-002 인증번호 재발급
    String resendPhoneAuthCode(PhoneAuthResendDTO resendDTO);

    // AUTH-003 인증번호 확인
    void verifyPhoneAuthCode(PhoneAuthVerifyDTO verifyDTO);

    // AUTH-004 가입 여부 확인
    SignupStatusResponseDTO checkSignupStatus(SignupCheckDTO checkDTO);

    // AUTH-005 로그인
    TokenDTO login(PinLoginDTO loginDTO);

    // AUTH-006 로그아웃
    void logout(LogoutDTO logoutDTO);

    // AUTH-007 토큰 재발급
    TokenDTO refreshToken(RefreshTokenRequestDTO requestDTO);

    TokenDTO reissueTokenAfterPhoneChange(Long userId, String newPhoneNumber);
}