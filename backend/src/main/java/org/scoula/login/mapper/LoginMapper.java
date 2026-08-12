package org.scoula.login.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.login.domain.PhoneAuthVO;
import org.scoula.login.domain.RefreshTokenVO;
import org.scoula.user.domain.UserVO;

import java.time.LocalDateTime;

public interface LoginMapper {

    // 같은 휴대폰번호의 기존 인증 요청 삭제
    int deletePhoneAuth(
            @Param("phoneNumber") String phoneNumber,
            @Param("verificationPurpose") String verificationPurpose
    );

    // 인증 요청 저장
    int insertPhoneAuth(PhoneAuthVO phoneAuth);

    // 최신 인증 요청 조회
    PhoneAuthVO findLatestPhoneAuth(
            @Param("phoneNumber") String phoneNumber,
            @Param("verificationPurpose") String verificationPurpose
    );

    // 인증번호 실패 횟수 증가
    int increaseFailCount(@Param("verificationId") Long verificationId);

    // 인증 완료 처리
    int completePhoneAuth(@Param("verificationId") Long verificationId);

    // 휴대폰번호 기준 가입 회원 수 확인
    int countUserByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    // 인증번호 재발급
    int updatePhoneAuthCode(
            @Param("verificationId") Long verificationId,
            @Param("verificationCode") String verificationCode,
            @Param("requestedAt") LocalDateTime requestedAt
    );

    // 로그인 회원 조회
    UserVO findUserByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    // PIN 로그인 실패 횟수 증가
    int increasePinFailCount(@Param("userId") Long userId);

    // PIN 로그인 5회 실패 시 잠금
    int lockPin(@Param("userId") Long userId);

    // PIN 로그인 성공 시 실패 횟수 및 잠금 상태 초기화
    int resetPinFailCount(@Param("userId") Long userId);

    // PIN 재설정용 인증 완료 정보 조회
    Long findVerifiedPinResetId(@Param("phoneNumber") String phoneNumber);

    // 이름·휴대폰번호 변경용 인증 완료 정보 조회
    PhoneAuthVO findVerifiedPhoneAuth(
            @Param("phoneNumber") String phoneNumber,
            @Param("verificationPurpose") String verificationPurpose
    );

    // PIN 재설정 완료 후 인증 정보 삭제
    int deleteVerificationById(@Param("verificationId") Long verificationId);

    // Refresh Token 저장
    int insertRefreshToken(RefreshTokenVO refreshToken);

    // Refresh Token 조회
    RefreshTokenVO findRefreshToken(@Param("refreshToken") String refreshToken);

    // 회원의 Refresh Token 삭제
    int deleteRefreshTokenByUserId(@Param("userId") Long userId);

    // 특정 Refresh Token 삭제
    int deleteRefreshToken(@Param("refreshToken") String refreshToken);
}