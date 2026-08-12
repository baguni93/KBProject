package org.scoula.account.service;

import org.scoula.account.dto.AccountConnectDTO;
import org.scoula.account.dto.AccountDTO;
import org.scoula.account.dto.AccountVerificationConfirmDTO;
import org.scoula.account.dto.AccountVerificationRequestDTO;

import java.util.List;
import java.util.Map;

public interface AccountService {

    // 연결 계좌 목록 조회
    List<AccountDTO> getAccounts(Long userId);

    // 계좌 인증번호 발급
    Map<String, Object> requestVerification(Long userId, AccountVerificationRequestDTO requestDTO);

    // 계좌 인증번호 재발급
    Map<String, Object> resendVerification(Long userId, Long verificationId);

    // 계좌 인증번호 확인
    boolean confirmVerification(Long userId, AccountVerificationConfirmDTO confirmDTO);

    // 계좌 연결
    AccountDTO connectAccount(Long userId, AccountConnectDTO connectDTO);

    // 대표계좌 설정
    boolean setPrimaryAccount(Long userId, Long linkedAccountId);

    // 계좌 연결 해제
    boolean disconnectAccount(Long userId, Long linkedAccountId);
}