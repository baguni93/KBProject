package org.scoula.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.account.domain.AccountVerificationVO;
import org.scoula.account.domain.LinkedAccountVO;
import org.scoula.account.dto.AccountConnectDTO;
import org.scoula.account.dto.AccountDTO;
import org.scoula.account.dto.AccountVerificationConfirmDTO;
import org.scoula.account.dto.AccountVerificationRequestDTO;
import org.scoula.account.mapper.AccountMapper;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.user.domain.UserVO;
import org.scoula.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final UserMapper userMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    // 연결 계좌 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<AccountDTO> getAccounts(Long userId) {
        return accountMapper.findAccountsByUserId(userId)
                .stream()
                .map(AccountDTO::of)
                .collect(Collectors.toList());
    }

    // 계좌 인증번호 발급
    @Override
    @Transactional
    public Map<String, Object> requestVerification(
            Long userId,
            AccountVerificationRequestDTO requestDTO
    ) {

        validateVerificationRequest(requestDTO);

        UserVO user = userMapper.findById(userId);

        if (user == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }

        String userName = user.getUserName().trim();
        String accountHolder = requestDTO.getAccountHolder().trim();

        if (!userName.equals(accountHolder)) {
            throw new IllegalArgumentException("예금주명이 회원 실명과 일치하지 않습니다.");
        }

        if (accountMapper.countActiveBank(requestDTO.getBankCode()) == 0) {
            throw new IllegalArgumentException("존재하지 않거나 사용할 수 없는 은행입니다.");
        }

        int duplicateCount = accountMapper.countConnectedAccount(
                userId,
                requestDTO.getBankCode(),
                requestDTO.getAccountNumber().trim()
        );

        if (duplicateCount > 0) {
            throw new IllegalArgumentException("이미 연결된 계좌입니다.");
        }

        // 동일 계좌의 최근 인증 잠금 상태 확인
        AccountVerificationVO latestVerification = accountMapper.findLatestVerification(
                userId,
                requestDTO.getBankCode(),
                requestDTO.getAccountNumber().trim()
        );

        if (latestVerification != null
                && latestVerification.getLockedUntil() != null
                && LocalDateTime.now().isBefore(latestVerification.getLockedUntil())) {
            throw new IllegalArgumentException("계좌 인증 시도 횟수를 초과했습니다. 5분 후 다시 시도해주세요.");
        }

        String verificationCode = String.format("%04d", secureRandom.nextInt(10000));

        AccountVerificationVO verification = AccountVerificationVO.builder()
                .userId(userId)
                .bankCode(requestDTO.getBankCode())
                .accountNumber(requestDTO.getAccountNumber().trim())
                .accountHolder(userName)
                .verificationCode(verificationCode)
                .verifiedYn("N")
                .requestedAt(LocalDateTime.now())
                .failCount(0)
                .resendCount(0)
                .lockedUntil(null)
                .build();

        accountMapper.insertVerification(verification);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("verificationId", verification.getVerificationId());
        response.put("verificationCode", verificationCode);
        response.put("message", "계좌 인증번호가 발급되었습니다.");

        return response;
    }

    // 계좌 인증번호 재발급
    @Override
    @Transactional
    public Map<String, Object> resendVerification(Long userId, Long verificationId) {

        AccountVerificationVO verification = accountMapper.findVerificationById(verificationId, userId);

        if (verification == null) {
            throw new IllegalArgumentException("계좌 인증 요청을 찾을 수 없습니다.");
        }

        if ("Y".equals(verification.getVerifiedYn())) {
            throw new IllegalArgumentException("이미 완료된 계좌 인증입니다.");
        }

        if (verification.getLockedUntil() != null
                && LocalDateTime.now().isBefore(verification.getLockedUntil())) {
            throw new IllegalArgumentException("계좌 인증 시도 횟수를 초과했습니다. 5분 후 다시 시도해주세요.");
        }

        int failCount = verification.getFailCount() == null ? 0 : verification.getFailCount();
        int resendCount = verification.getResendCount() == null ? 0 : verification.getResendCount();

        if (failCount < 5) {
            throw new IllegalArgumentException("인증번호 입력 가능 횟수가 남아있습니다.");
        }

        if (resendCount >= 1) {
            throw new IllegalArgumentException("인증번호 재발급 가능 횟수를 초과했습니다.");
        }

        String verificationCode = String.format("%04d", secureRandom.nextInt(10000));

        int updated = accountMapper.resendVerificationCode(verificationId, userId, verificationCode);

        if (updated != 1) {
            throw new IllegalStateException("계좌 인증번호 재발급에 실패했습니다.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("verificationId", verificationId);
        response.put("verificationCode", verificationCode);
        response.put("message", "계좌 인증번호가 재발급되었습니다.");

        return response;
    }

    // 계좌 인증번호 확인
    @Override
    @Transactional(noRollbackFor = {IllegalArgumentException.class , CustomException.class } )
    public boolean confirmVerification(Long userId, AccountVerificationConfirmDTO confirmDTO) {

        if (confirmDTO.getVerificationId() == null) {
//            throw new IllegalArgumentException("계좌 인증번호 식별값이 필요합니다.");
            throw new CustomException(ErrorCode.VERIFICATION_ACCOUNT_CODE_NOT_FOUND);
        }

        if (confirmDTO.getVerificationCode() == null
                || !confirmDTO.getVerificationCode().matches("\\d{4}")) {
//            throw new IllegalArgumentException("인증번호는 숫자 4자리로 입력해주세요.");

            throw new CustomException(ErrorCode.VERIFICATION_ACCOUNT_CODE_EMPTY);
        }

        AccountVerificationVO verification = accountMapper.findVerificationById(
                confirmDTO.getVerificationId(),
                userId
        );

        if (verification == null) {
//            throw new IllegalArgumentException("계좌 인증 요청을 찾을 수 없습니다.");
            throw new CustomException(ErrorCode.VERIFICATION_ACCOUNT_CODE_NOT_REQUEST);
        }

        if ("Y".equals(verification.getVerifiedYn())) return true;

        // 계좌 인증 잠금 확인
        if (verification.getLockedUntil() != null
                && LocalDateTime.now().isBefore(verification.getLockedUntil())) {
            throw new IllegalArgumentException("계좌 인증 시도 횟수를 초과했습니다. 5분 후 다시 시도해주세요.");
        }

        int failCount = verification.getFailCount() == null ? 0 : verification.getFailCount();
        int resendCount = verification.getResendCount() == null ? 0 : verification.getResendCount();

        // 이미 첫 번째 5회 실패가 끝난 경우
        if (failCount >= 5 && resendCount == 0) {
            throw new IllegalArgumentException("계좌 인증번호 입력 가능 횟수를 초과했습니다. 인증번호를 다시 받아주세요.");
        }

        // 재발급 후 5회 실패로 잠금된 요청
        if (failCount >= 5 && resendCount >= 1) {
            throw new IllegalArgumentException("계좌 인증 시도 횟수를 초과했습니다. 5분 후 다시 시도해주세요.");
        }

        if (!verification.getVerificationCode().equals(confirmDTO.getVerificationCode())) {
            accountMapper.increaseVerificationFailCount(confirmDTO.getVerificationId(), userId);

            int newFailCount = failCount + 1;
            int remainingCount = 5 - newFailCount;

            // 첫 번째 인증번호 5회 실패
            if (remainingCount <= 0 && resendCount == 0) {
                throw new IllegalArgumentException("계좌 인증번호 입력 가능 횟수를 초과했습니다. 인증번호를 다시 받아주세요.");
            }

            // 재발급 인증번호도 5회 실패
            if (remainingCount <= 0) {
                accountMapper.lockVerification(confirmDTO.getVerificationId(), userId);
                throw new IllegalArgumentException("계좌 인증 시도 횟수를 초과했습니다. 5분 후 다시 시도해주세요.");
            }

//            throw new IllegalArgumentException("계좌 인증번호가 일치하지 않습니다. 남은 횟수: " + remainingCount);

            throw new CustomException(
                    ErrorCode.VERIFICATION_ACCOUNT_CODE_NOT_INCORRECT,
                    "계좌 인증번호가 일치하지 않습니다. \n남은 횟수: " + remainingCount
            );
        }

        return accountMapper.verifyAccount(confirmDTO.getVerificationId(), userId) > 0;
    }

    // 계좌 연결
    @Override
    @Transactional
    public AccountDTO connectAccount(Long userId, AccountConnectDTO connectDTO) {

        if (connectDTO.getVerificationId() == null) {
            throw new IllegalArgumentException("계좌 인증 정보가 필요합니다.");
        }

        AccountVerificationVO verification = accountMapper.findVerificationById(connectDTO.getVerificationId(), userId);

        if (verification == null) {
            throw new IllegalArgumentException("계좌 인증 요청을 찾을 수 없습니다.");
        }

        if (!"Y".equals(verification.getVerifiedYn())) {
            throw new IllegalArgumentException("계좌 인증이 완료되지 않았습니다.");
        }

        int duplicateCount = accountMapper.countConnectedAccount(userId, verification.getBankCode(), verification.getAccountNumber());

        if (duplicateCount > 0) {
            throw new IllegalArgumentException("이미 연결된 계좌입니다.");
        }

        boolean firstAccount = accountMapper.countConnectedAccounts(userId) == 0;
        LinkedAccountVO existingAccount = accountMapper.findAccountByAccountInfo(userId, verification.getBankCode(), verification.getAccountNumber());

        // 이전에 연결 해제한 동일 계좌가 있으면 기존 행을 재사용
        if (existingAccount != null) {
            int result = accountMapper.reconnectAccount(userId, existingAccount.getLinkedAccountId(), firstAccount ? "Y" : "N");

            if (result == 0) {
                throw new IllegalStateException("계좌 재연결에 실패했습니다.");
            }

            accountMapper.deleteVerification(connectDTO.getVerificationId(), userId);
            LinkedAccountVO connectedAccount = accountMapper.findAccountById(userId, existingAccount.getLinkedAccountId());

            return AccountDTO.of(connectedAccount);
        }

        LinkedAccountVO account = LinkedAccountVO.builder()
                .userId(userId)
                .bankCode(verification.getBankCode())
                .accountNumber(verification.getAccountNumber())
                .accountHolder(verification.getAccountHolder())
                .primaryYn(firstAccount ? "Y" : "N")
                .connectionStatus("CONNECTED")
                .build();

        int result = accountMapper.insertAccount(account);

        if (result == 0) {
            throw new IllegalStateException("계좌 연결에 실패했습니다.");
        }

        accountMapper.deleteVerification(connectDTO.getVerificationId(), userId);
        LinkedAccountVO connectedAccount = accountMapper.findAccountById(userId, account.getLinkedAccountId());

        return AccountDTO.of(connectedAccount);
    }

    // 대표계좌 설정
    @Override
    @Transactional
    public boolean setPrimaryAccount(Long userId, Long linkedAccountId) {

        LinkedAccountVO account = accountMapper.findAccountById(userId, linkedAccountId);

        if (account == null) {
            throw new IllegalArgumentException("연결된 계좌를 찾을 수 없습니다.");
        }

        if ("Y".equals(account.getPrimaryYn())) return true;

        accountMapper.clearPrimaryAccount(userId);

        return accountMapper.setPrimaryAccount(userId, linkedAccountId) > 0;
    }

    // 계좌 연결 해제
    @Override
    @Transactional
    public boolean disconnectAccount(Long userId, Long linkedAccountId) {

        LinkedAccountVO account = accountMapper.findAccountById(userId, linkedAccountId);

        if (account == null) {
            throw new IllegalArgumentException("연결된 계좌를 찾을 수 없습니다.");
        }

        int connectedCount = accountMapper.countConnectedAccounts(userId);

        if (connectedCount <= 1) {
            throw new IllegalArgumentException("마지막 계좌는 연결 해제할 수 없습니다.");
        }

        LinkedAccountVO nextPrimaryAccount = null;

        if ("Y".equals(account.getPrimaryYn())) {
            nextPrimaryAccount = accountMapper.findAnotherConnectedAccount(
                    userId,
                    linkedAccountId
            );

            if (nextPrimaryAccount == null) {
                throw new IllegalStateException("대표계좌로 설정할 다른 계좌가 없습니다.");
            }
        }

        int result = accountMapper.disconnectAccount(userId, linkedAccountId);

        if (result == 0) return false;

        if (nextPrimaryAccount != null) {
            accountMapper.setPrimaryAccount(
                    userId,
                    nextPrimaryAccount.getLinkedAccountId()
            );
        }

        return true;
    }

    // 계좌 인증 요청값 검증
    private void validateVerificationRequest(AccountVerificationRequestDTO requestDTO) {

        if (requestDTO == null) {
            throw new IllegalArgumentException("계좌 인증 요청 정보가 필요합니다.");
        }

        if (requestDTO.getBankCode() == null || requestDTO.getBankCode().trim().isEmpty()) {
            throw new IllegalArgumentException("은행을 선택해주세요.");
        }

        if (requestDTO.getAccountNumber() == null || requestDTO.getAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("계좌번호를 입력해주세요.");
        }

        if (!requestDTO.getAccountNumber().trim().matches("[0-9-]+")) {
            throw new IllegalArgumentException("계좌번호는 숫자와 하이픈만 입력할 수 있습니다.");
        }

        if (requestDTO.getAccountHolder() == null || requestDTO.getAccountHolder().trim().isEmpty()) {
            throw new IllegalArgumentException("예금주명을 입력해주세요.");
        }

        if (requestDTO.getAccountHolder().trim().length() > 50) {
            throw new IllegalArgumentException("예금주명은 50자 이하로 입력해주세요.");
        }
    }
}
