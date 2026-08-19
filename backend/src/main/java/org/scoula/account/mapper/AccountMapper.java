package org.scoula.account.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.account.domain.AccountVerificationVO;
import org.scoula.account.domain.LinkedAccountVO;

import java.util.List;

public interface AccountMapper {

    // 연결 계좌 목록 조회
    List<LinkedAccountVO> findAccountsByUserId(Long userId);

    // 연결 계좌 단건 조회
    LinkedAccountVO findAccountById(@Param("userId") Long userId, @Param("linkedAccountId") Long linkedAccountId);

    // 연결된 계좌 개수 조회
    int countConnectedAccounts(Long userId);

    // 중복 연결 계좌 조회
    int countConnectedAccount(@Param("userId") Long userId, @Param("bankCode") String bankCode, @Param("accountNumber") String accountNumber);

    // 동일 계좌 연결 이력 조회
    LinkedAccountVO findAccountByAccountInfo(@Param("userId") Long userId, @Param("bankCode") String bankCode, @Param("accountNumber") String accountNumber);

    // 연결 해제 계좌 재연결
    int reconnectAccount(@Param("userId") Long userId, @Param("linkedAccountId") Long linkedAccountId, @Param("primaryYn") String primaryYn);

    // 은행 존재 여부 조회
    int countActiveBank(String bankCode);

    // 계좌 인증 정보 저장
    int insertVerification(AccountVerificationVO verification);

    // 계좌 인증 정보 조회
    AccountVerificationVO findVerificationById(@Param("verificationId") Long verificationId, @Param("userId") Long userId);

    // 동일 계좌의 최근 인증 요청 조회
    AccountVerificationVO findLatestVerification(
            @Param("userId") Long userId,
            @Param("bankCode") String bankCode,
            @Param("accountNumber") String accountNumber
    );

    // 계좌 인증 성공 처리
    int verifyAccount(@Param("verificationId") Long verificationId, @Param("userId") Long userId);

    // 계좌 인증 실패 횟수 증가
    int increaseVerificationFailCount(@Param("verificationId") Long verificationId, @Param("userId") Long userId);

    // 계좌 인증번호 재발급
    int resendVerificationCode(@Param("verificationId") Long verificationId, @Param("userId") Long userId, @Param("verificationCode") String verificationCode);

    // 계좌 인증 5분 잠금
    int lockVerification(@Param("verificationId") Long verificationId, @Param("userId") Long userId);

    // 계좌 인증 정보 삭제
    int deleteVerification(@Param("verificationId") Long verificationId, @Param("userId") Long userId);

    // 연결 계좌 저장
    int insertAccount(LinkedAccountVO account);

    // 기존 대표계좌 해제
    int clearPrimaryAccount(Long userId);

    // 대표계좌 설정
    int setPrimaryAccount(@Param("userId") Long userId, @Param("linkedAccountId") Long linkedAccountId);

    // 계좌 연결 해제
    int disconnectAccount(@Param("userId") Long userId, @Param("linkedAccountId") Long linkedAccountId);

    // 다른 연결 계좌 한 건 조회
    LinkedAccountVO findAnotherConnectedAccount(@Param("userId") Long userId, @Param("linkedAccountId") Long linkedAccountId);
}