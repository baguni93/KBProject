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

        String verificationCode = String.format("%04d", secureRandom.nextInt(10000));

        AccountVerificationVO verification = AccountVerificationVO.builder()
                .userId(userId)
                .bankCode(requestDTO.getBankCode())
                .accountNumber(requestDTO.getAccountNumber().trim())
                .accountHolder(userName)
                .verificationCode(verificationCode)
                .verifiedYn("N")
                .requestedAt(LocalDateTime.now())
                .build();

        accountMapper.insertVerification(verification);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("verificationId", verification.getVerificationId());
        response.put("verificationCode", verificationCode);
        response.put("message", "계좌 인증번호가 발급되었습니다.");

        return response;
    }

    // 계좌 인증번호 확인
    @Override
    @Transactional
    public boolean confirmVerification(
            Long userId,
            AccountVerificationConfirmDTO confirmDTO
    ) {

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

        if (!verification.getVerificationCode().equals(confirmDTO.getVerificationCode())) {
//            throw new IllegalArgumentException("계좌 인증번호가 일치하지 않습니다.");
            throw new CustomException(ErrorCode.VERIFICATION_ACCOUNT_CODE_NOT_INCORRECT);
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

        AccountVerificationVO verification = accountMapper.findVerificationById(
                connectDTO.getVerificationId(),
                userId
        );

        if (verification == null) {
            throw new IllegalArgumentException("계좌 인증 요청을 찾을 수 없습니다.");
        }

        if (!"Y".equals(verification.getVerifiedYn())) {
            throw new IllegalArgumentException("계좌 인증이 완료되지 않았습니다.");
        }

        int duplicateCount = accountMapper.countConnectedAccount(
                userId,
                verification.getBankCode(),
                verification.getAccountNumber()
        );

        if (duplicateCount > 0) {
            throw new IllegalArgumentException("이미 연결된 계좌입니다.");
        }

        boolean firstAccount = accountMapper.countConnectedAccounts(userId) == 0;

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

        LinkedAccountVO connectedAccount = accountMapper.findAccountById(
                userId,
                account.getLinkedAccountId()
        );

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
