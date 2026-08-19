package org.scoula.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.login.domain.PhoneAuthVO;
import org.scoula.login.mapper.LoginMapper;
import org.scoula.notifsetting.domain.NotificationSettingVO;
import org.scoula.notifsetting.mapper.NotificationSettingMapper;
import org.scoula.pointwallet.service.PointWalletService;
import org.scoula.user.domain.AccountVO;
import org.scoula.user.domain.BankVO;
import org.scoula.user.domain.UserVO;
import org.scoula.user.dto.*;
import org.scoula.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final LoginMapper loginMapper;
    private final NotificationSettingMapper notificationSettingMapper;
    private final PointWalletService pointWalletService;
    private final org.scoula.wallet.mapper.WalletMapper walletMapper;
    private final PasswordEncoder passwordEncoder;

    // private static final long REJOIN_WAIT_HOURS = 24L;
    private static final long REJOIN_WAIT_MINUTES = 1L;

    // 닉네임 형식
    private static final String NICKNAME_PATTERN = "^[가-힣a-z0-9_]{1,15}$";

    // 회원가입
    @Override
    @Transactional
    public Long signup(UserSignupDTO signupDTO) {

        validateSignupDTO(signupDTO);

        String phoneNumber = signupDTO.getPhoneNumber().trim();
        String nickname = signupDTO.getNickname().trim();

        // 같은 휴대폰번호 회원 확인
        // ACTIVE: 재가입 불가
        // WITHDRAWN + 24시간 미경과: 재가입 불가
        // WITHDRAWN + 24시간 경과: 기존 정보 익명화 후 새 회원 생성
        UserVO existingUser = userMapper.findByPhoneNumber(phoneNumber);

        if (existingUser != null) processExistingUserForSignup(existingUser);

        // 기존 탈퇴 회원이 익명화된 이후 닉네임 중복 확인
        UserVO nicknameUser = userMapper.findByNickname(nickname);

        if (nicknameUser != null) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 회원가입 PIN 정책 검사
        validatePinPolicy(signupDTO.getPinPassword(), signupDTO.getBirthDate());

        UserVO user = UserVO.builder()
                .userName(signupDTO.getUserName().trim())
                .birthDate(signupDTO.getBirthDate())
                .phoneNumber(phoneNumber)
                .pinPassword(passwordEncoder.encode(signupDTO.getPinPassword()))
                .nickname(nickname)
                .userStatus("ACTIVE")
                .build();


        int userResult = userMapper.insert(user);

        if (userResult != 1) {
            throw new IllegalStateException("회원정보 저장 중 오류가 발생했습니다.");
        }

        // 기본 회원 권한 저장
        int authResult = userMapper.insertAuth(user.getUserId(), "ROLE_USER");

        if (authResult != 1) {
            throw new IllegalStateException("회원 권한 저장 중 오류가 발생했습니다.");
        }

        int profileResult = userMapper.insertProfile(user);

        if (profileResult != 1) {
            throw new IllegalStateException("프로필 저장 중 오류가 발생했습니다.");
        }

        //박우진 추가 회원 가입 시 은행 마다 계좌 더미를 만들어준다.
        Random random = new Random();
        List<BankVO> list = userMapper.getBanks();
        List<AccountVO> accountList = new ArrayList<>();
        for(var bank : list){

            String randomAccountNumber = generateRandomAccountNumber(bank.getBankCode(), random);

            var accountVo = AccountVO.builder().
                    userId(user.getUserId().intValue()).
                    bankCode(bank.getBankCode()).
                    accountNumber(randomAccountNumber).
                    ownerName(user.getUserName()).
                    balance(100000).
                    accountPassword("1234").
                    build();
            accountList.add(accountVo);

        }
        if (!accountList.isEmpty()) {
            userMapper.insertAccount(accountList);
        }


        // 기본 알림 설정 생성
        NotificationSettingVO notificationSetting = NotificationSettingVO.builder()
                .userId(user.getUserId())
                .financeNotificationYn("Y")
                .friendNotificationYn("Y")
                .rewardNotificationYn("Y")
                .eventNotificationYn("Y")
                .build();

        int notificationSettingResult = notificationSettingMapper.insert(notificationSetting);

        if (notificationSettingResult != 1) {
            throw new IllegalStateException("알림 설정 저장 중 오류가 발생했습니다.");
        }

        // 회원가입 시 전자지갑(페이머니) 및 포인트 지갑 자동 생성
        try {
            walletMapper.insertWallet(user.getUserId().intValue());
        } catch (Exception wErr) {
            log.warn("회원가입 시 전자지갑 생성 예외: {}", wErr.getMessage());
        }
        pointWalletService.createWallet(user.getUserId().intValue());

        log.info("회원가입 완료: userId={}", user.getUserId());

        return user.getUserId();
    }


    //박우진 추가 , 은행 따른 계좌 받아오기
    @Override
    public AccountByBankCodeDTO getAccountByBankCode(int userId, String bankCode) {
        AccountVO vo = userMapper.getAccountByBackCode(userId, bankCode);

        return AccountByBankCodeDTO.of(vo);
    }


    // 회원 기본정보 조회
    @Override
    @Transactional(readOnly = true)
    public UserInfoDTO getUserInfo(Long userId) {

        UserVO user = getActiveUser(userId);

        return UserInfoDTO.of(user);
    }

    // 회원정보 수정
    @Override
    @Transactional
    public boolean updateUser(Long userId, UserUpdateDTO updateDTO) {

        UserVO user = getActiveUser(userId);
        String nickname = updateDTO.getNickname();

        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        String trimmedNickname = nickname.trim();
        UserVO nicknameUser = userMapper.findByNickname(trimmedNickname);

        if (nicknameUser != null && !nicknameUser.getUserId().equals(userId)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        validateNickname(trimmedNickname);
        user.setNickname(trimmedNickname);

        return userMapper.update(user) == 1;
    }

    // 회원 이름 변경
    @Override
    @Transactional
    public void changeUserName(Long userId, UserNameChangeDTO changeDTO) {
        UserVO user = getActiveUser(userId);

        if (changeDTO == null) {
            throw new IllegalArgumentException("이름 변경 정보가 필요합니다.");
        }

        if (changeDTO.getPhoneNumber() == null || changeDTO.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("휴대폰번호가 필요합니다.");
        }

        if (changeDTO.getNewUserName() == null || changeDTO.getNewUserName().trim().isEmpty()) {
            throw new IllegalArgumentException("변경할 이름을 입력해주세요.");
        }

        String phoneNumber = changeDTO.getPhoneNumber().trim();
        String newUserName = changeDTO.getNewUserName().trim();

        if (!phoneNumber.equals(user.getPhoneNumber())) {
            throw new IllegalArgumentException("현재 회원의 휴대폰번호와 일치하지 않습니다.");
        }

        if (newUserName.length() > 50) {
            throw new IllegalArgumentException("이름은 50자 이하로 입력해주세요.");
        }

        if (newUserName.equals(user.getUserName())) {
            throw new IllegalArgumentException("현재 이름과 다른 이름을 입력해주세요.");
        }

        PhoneAuthVO verification = loginMapper.findVerifiedPhoneAuth(phoneNumber, "NAME_CHANGE");

        if (verification == null) {
            throw new IllegalArgumentException("이름 변경을 위한 휴대폰 본인인증이 필요합니다.");
        }

        if (verification.getBirthDate() == null || !verification.getBirthDate().equals(user.getBirthDate())) {
            throw new IllegalArgumentException("본인인증 정보가 회원정보와 일치하지 않습니다.");
        }

        if (verification.getPhoneNumber() == null || !verification.getPhoneNumber().equals(user.getPhoneNumber())) {
            throw new IllegalArgumentException("본인인증 정보가 회원정보와 일치하지 않습니다.");
        }

        if (verification.getUserName() == null || !verification.getUserName().trim().equals(newUserName)) {
            throw new IllegalArgumentException("인증한 이름과 변경할 이름이 일치하지 않습니다.");
        }

        int updateResult = userMapper.updateUserName(userId, newUserName);

        if (updateResult != 1) {
            throw new IllegalStateException("이름 변경에 실패했습니다.");
        }

        int deleteResult = loginMapper.deleteVerificationById(verification.getVerificationId());

        if (deleteResult != 1) {
            throw new IllegalStateException("본인인증 정보 처리에 실패했습니다.");
        }

        log.info("회원 이름 변경 완료: userId={}", userId);
    }


    // 회원 휴대폰번호 변경
    @Override
    @Transactional
    public void changePhoneNumber(Long userId, UserPhoneChangeDTO changeDTO) {
        UserVO user = getActiveUser(userId);

        if (changeDTO == null) throw new IllegalArgumentException("휴대폰번호 변경 정보가 필요합니다.");

        if (changeDTO.getNewPhoneNumber() == null || changeDTO.getNewPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("새 휴대폰번호를 입력해주세요.");
        }

        String newPhoneNumber = changeDTO.getNewPhoneNumber().trim();

        if (!newPhoneNumber.matches("^01[016789]\\d{7,8}$")) throw new IllegalArgumentException("올바른 휴대폰번호 형식이 아닙니다.");

        if (newPhoneNumber.equals(user.getPhoneNumber())) {
            throw new IllegalArgumentException("현재 휴대폰번호와 다른 번호를 입력해주세요.");
        }

        UserVO existingUser = userMapper.findByPhoneNumber(newPhoneNumber);

        if (existingUser != null && "ACTIVE".equals(existingUser.getUserStatus())) {
            throw new IllegalArgumentException("이미 가입된 휴대폰번호입니다.");
        }

        PhoneAuthVO verification = loginMapper.findVerifiedPhoneAuth(newPhoneNumber, "PHONE_CHANGE");

        if (verification == null) {
            throw new IllegalArgumentException("휴대폰번호 변경을 위한 본인인증이 필요합니다.");
        }

        if (verification.getUserName() == null || !verification.getUserName().trim().equals(user.getUserName())) {
            throw new IllegalArgumentException("본인인증 정보가 회원정보와 일치하지 않습니다.");
        }

        if (verification.getBirthDate() == null || !verification.getBirthDate().equals(user.getBirthDate())) {
            throw new IllegalArgumentException("본인인증 정보가 회원정보와 일치하지 않습니다.");
        }

        int updateResult = userMapper.updatePhoneNumber(userId, newPhoneNumber);

        if (updateResult != 1) throw new IllegalStateException("휴대폰번호 변경에 실패했습니다.");

        int deleteResult = loginMapper.deleteVerificationById(verification.getVerificationId());

        if (deleteResult != 1) throw new IllegalStateException("본인인증 정보 처리에 실패했습니다.");

        log.info("회원 휴대폰번호 변경 완료: userId={}", userId);
    }

    // 휴대폰번호 변경 가능 여부 확인
    @Override
    @Transactional(readOnly = true)
    public boolean checkPhoneNumber(Long userId, String phoneNumber) {
        UserVO user = getActiveUser(userId);

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) throw new IllegalArgumentException("새 휴대폰번호를 입력해주세요.");

        String newPhoneNumber = phoneNumber.trim().replaceAll("[^0-9]", "");

        if (!newPhoneNumber.matches("^01[016789]\\d{7,8}$")) throw new IllegalArgumentException("휴대폰번호를 확인해주세요.");
        if (newPhoneNumber.equals(user.getPhoneNumber())) throw new IllegalArgumentException("현재 휴대폰번호와 다른 번호를 입력해주세요.");

        UserVO existingUser = userMapper.findByPhoneNumber(newPhoneNumber);

        if (existingUser != null && "ACTIVE".equals(existingUser.getUserStatus())) throw new IllegalArgumentException("이미 가입된 휴대폰번호입니다.");

        return true;
    }


    // 닉네임 중복 확인
    @Override
    @Transactional(readOnly = true)
    public NicknameCheckDTO checkNickname(String nickname) {

        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        String trimmedNickname = nickname.trim();

        validateNickname(trimmedNickname);

        UserVO user = userMapper.findByNickname(trimmedNickname);

        return NicknameCheckDTO.builder()
                .nickname(trimmedNickname)
                .available(user == null)
                .build();
    }

    // PIN 확인
    @Override
    @Transactional(readOnly = true)
    public boolean verifyPin(Long userId, PinVerifyDTO pinVerifyDTO) {

        UserVO user = getActiveUser(userId);

        validatePin(pinVerifyDTO.getPinPassword());

        return passwordEncoder.matches(
                pinVerifyDTO.getPinPassword(),
                user.getPinPassword()
        );
    }

    // PIN 변경
    @Override
    @Transactional
    public boolean changePin(Long userId, PinChangeDTO pinChangeDTO) {

        UserVO user = getActiveUser(userId);

        if (pinChangeDTO == null) {
            throw new IllegalArgumentException("PIN 변경 정보가 필요합니다.");
        }

        // 현재 PIN은 형식만 검사
        validatePin(pinChangeDTO.getCurrentPinPassword());

        // 새 PIN은 전체 정책 검사
        validatePinPolicy(pinChangeDTO.getNewPinPassword(), user.getBirthDate());

        if (!passwordEncoder.matches(
                pinChangeDTO.getCurrentPinPassword(),
                user.getPinPassword())) {
            throw new IllegalArgumentException("현재 PIN이 일치하지 않습니다.");
        }

        if (pinChangeDTO.getNewPinPasswordConfirm() == null
                || !pinChangeDTO.getNewPinPassword()
                .equals(pinChangeDTO.getNewPinPasswordConfirm())) {
            throw new IllegalArgumentException("새 PIN과 PIN 확인 값이 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(
                pinChangeDTO.getNewPinPassword(),
                user.getPinPassword())) {
            throw new IllegalArgumentException("현재 PIN과 다른 PIN을 입력해주세요.");
        }

        String encodedPinPassword = passwordEncoder.encode(pinChangeDTO.getNewPinPassword());

        return userMapper.updatePin(userId, encodedPinPassword) == 1;
    }

    // PIN 재설정
    @Override
    @Transactional
    public void resetPin(PinResetDTO pinResetDTO) {

        // PIN 재설정 요청값 검증
        validatePinResetDTO(pinResetDTO);

        String phoneNumber = pinResetDTO.getPhoneNumber().trim();

        // PIN_RESET 목적의 휴대폰 인증 완료 여부 확인
        Long verificationId = loginMapper.findVerifiedPinResetId(phoneNumber);

        if (verificationId == null) {
            throw new IllegalArgumentException("PIN 재설정을 위한 휴대폰 본인인증이 필요합니다.");
        }

        // 재설정 대상 회원 확인
        UserVO user = userMapper.findByPhoneNumber(phoneNumber);

        if (user == null) {
            throw new IllegalArgumentException("가입된 회원 정보를 찾을 수 없습니다.");
        }

        if ("WITHDRAWN".equals(user.getUserStatus())) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }

        if (!"ACTIVE".equals(user.getUserStatus())) {
            throw new IllegalStateException("PIN을 재설정할 수 없는 회원 상태입니다.");
        }

        // 새 PIN 정책 검사
        validatePinPolicy(pinResetDTO.getNewPinPassword(), user.getBirthDate());

        // 새로운 PIN 암호화
        String encodedPinPassword = passwordEncoder.encode(pinResetDTO.getNewPinPassword());

        // 휴대폰번호를 기준으로 PIN 변경
        int updateResult = userMapper.updatePinByPhoneNumber(phoneNumber, encodedPinPassword);

        if (updateResult != 1) {
            throw new IllegalStateException("PIN 재설정 처리에 실패했습니다.");
        }

        // PIN 재설정 완료 후 로그인 실패 횟수 및 잠금 상태 초기화
        loginMapper.resetPinFailCount(user.getUserId());

        // 사용한 PIN_RESET 인증 기록 삭제
        int deleteResult = loginMapper.deleteVerificationById(verificationId);

        if (deleteResult != 1) {
            throw new IllegalStateException("휴대폰 인증 정보 처리에 실패했습니다.");
        }

        log.info("PIN 재설정 완료: userId={}", user.getUserId());
    }

    // 회원탈퇴
    @Override
    @Transactional
    public void withdraw(Long userId, UserWithdrawalDTO withdrawalDTO) {

        UserVO user = getActiveUser(userId);

        if (withdrawalDTO == null) {
            throw new IllegalArgumentException("탈퇴 요청 정보가 필요합니다.");
        }

        validatePin(withdrawalDTO.getPinPassword());

        if (!passwordEncoder.matches(
                withdrawalDTO.getPinPassword(),
                user.getPinPassword())) {
            throw new IllegalArgumentException("PIN이 일치하지 않습니다.");
        }

        // 회원 상태 변경
        int result = userMapper.withdraw(userId);

        if (result != 1) {
            throw new IllegalStateException("회원 탈퇴 처리에 실패했습니다.");
        }

        // 닉네임 즉시 익명화
        long timestamp = System.currentTimeMillis();
        String anonymousNickname =
                "wd" + userId + "_" + (timestamp % 100000);

        int nicknameResult =
                userMapper.anonymizeNickname(userId, anonymousNickname);

        if (nicknameResult != 1) {
            throw new IllegalStateException("탈퇴 회원 닉네임 익명화에 실패했습니다.");
        }

        // Refresh Token 즉시 삭제
        loginMapper.deleteRefreshTokenByUserId(userId);

        log.info("회원탈퇴 완료: userId={}", userId);
    }

    // 재가입 요청 시 기존 회원 상태 처리
    private void processExistingUserForSignup(UserVO existingUser) {

        String userStatus = existingUser.getUserStatus();

        if ("ACTIVE".equals(userStatus)) {
            throw new IllegalArgumentException("이미 가입된 휴대폰번호입니다.");
        }

        if (!"WITHDRAWN".equals(userStatus)) {
            throw new IllegalStateException("현재 가입할 수 없는 회원 상태입니다.");
        }

        LocalDateTime withdrawnAt = existingUser.getWithdrawnAt();

        if (withdrawnAt == null) {
            throw new IllegalStateException("탈퇴 일시를 확인할 수 없습니다.");
        }

        // LocalDateTime rejoinAvailableAt = withdrawnAt.plusHours(REJOIN_WAIT_HOURS);

        LocalDateTime rejoinAvailableAt = withdrawnAt.plusMinutes(REJOIN_WAIT_MINUTES);
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(rejoinAvailableAt)) {

            Duration remaining = Duration.between(now, rejoinAvailableAt);
            long remainingHours = remaining.toHours();
            long remainingMinutes = remaining.minusHours(remainingHours).toMinutes();

            throw new IllegalStateException(
                    "탈퇴 후 24시간이 지나야 재가입할 수 있습니다. "
                            + "남은 시간: "
                            + remainingHours
                            + "시간 "
                            + remainingMinutes
                            + "분"
            );
        }

        anonymizeWithdrawnUser(existingUser);
    }

    // 24시간이 지난 탈퇴 회원의 개인정보 익명화
    private void anonymizeWithdrawnUser(UserVO withdrawnUser) {

        Long userId = withdrawnUser.getUserId();
        long timestamp = System.currentTimeMillis();

        String anonymousPhoneNumber =
                "wd" + userId + "_" + (timestamp % 100000);

        int phoneResult =
                userMapper.anonymizePhoneNumber(userId, anonymousPhoneNumber);

        if (phoneResult != 1) {
            throw new IllegalStateException(
                    "탈퇴 회원 휴대폰번호 익명화에 실패했습니다."
            );
        }

        log.info("탈퇴 회원 휴대폰번호 익명화 완료: userId={}", userId);
    }

    // 활성 회원 조회
    private UserVO getActiveUser(Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException("회원번호가 필요합니다.");
        }

        UserVO user = userMapper.findById(userId);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        if ("WITHDRAWN".equals(user.getUserStatus())) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }

        if (!"ACTIVE".equals(user.getUserStatus())) {
            throw new IllegalStateException("이용할 수 없는 회원입니다.");
        }

        return user;
    }

    // 회원가입 요청 검증
    private void validateSignupDTO(UserSignupDTO signupDTO) {

        if (signupDTO == null) {
            throw new IllegalArgumentException("회원가입 정보가 필요합니다.");
        }

        if (signupDTO.getUserName() == null || signupDTO.getUserName().trim().isEmpty()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        if (signupDTO.getBirthDate() == null) {
            throw new IllegalArgumentException("생년월일을 입력해주세요.");
        }

        if (signupDTO.getPhoneNumber() == null || signupDTO.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("휴대폰번호를 입력해주세요.");
        }

        validateNickname(signupDTO.getNickname());
    }

    // PIN 재설정 요청 검증
    private void validatePinResetDTO(PinResetDTO pinResetDTO) {

        if (pinResetDTO == null) {
            throw new IllegalArgumentException("PIN 재설정 정보가 필요합니다.");
        }

        if (pinResetDTO.getPhoneNumber() == null || pinResetDTO.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("휴대폰번호를 입력해주세요.");
        }

        String phoneNumber = pinResetDTO.getPhoneNumber().trim();

        if (!phoneNumber.matches("^01[016789]\\d{7,8}$")) {
            throw new IllegalArgumentException("올바른 휴대폰번호 형식이 아닙니다.");
        }

        validatePin(pinResetDTO.getNewPinPassword());

        if (pinResetDTO.getNewPinPasswordConfirm() == null) {
            throw new IllegalArgumentException("새 PIN 확인 값을 입력해주세요.");
        }

        if (!pinResetDTO.getNewPinPassword().equals(pinResetDTO.getNewPinPasswordConfirm())) {
            throw new IllegalArgumentException("새 PIN과 PIN 확인 값이 일치하지 않습니다.");
        }
    }

    // 닉네임 유효성 검사
    private void validateNickname(String nickname) {

        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        String trimmedNickname = nickname.trim();

        if (!trimmedNickname.matches(NICKNAME_PATTERN)) {
            throw new IllegalArgumentException(
                    "닉네임은 한글, 영어 소문자, 숫자, _만 사용하여 15자 이내로 입력해주세요."
            );
        }
    }

    // PIN 정책 검사
    private void validatePinPolicy(String pinPassword, LocalDate birthDate) {

        // 숫자 6자리 형식 확인
        validatePin(pinPassword);

        if (birthDate == null) {
            throw new IllegalArgumentException("생년월일 정보를 확인할 수 없습니다.");
        }

        // 생년월일을 YYMMDD 형식으로 변환
        String birthDatePin = birthDate.format(DateTimeFormatter.ofPattern("yyMMdd"));

        // 생년월일과 정확히 같은 PIN 차단
        if (pinPassword.equals(birthDatePin)) {
            throw new IllegalArgumentException("생년월일은 PIN으로 사용할 수 없습니다.");
        }

        // 동일 숫자 4회 이상 연속 차단
        if (pinPassword.matches(".*(\\d)\\1{3,}.*")) {
            throw new IllegalArgumentException("동일한 숫자를 4자리 이상 연속으로 사용할 수 없습니다.");
        }

        // 오름차순 또는 내림차순 숫자 4자리 이상 차단
        if (hasSequentialNumbers(pinPassword)) {
            throw new IllegalArgumentException("연속된 숫자를 4자리 이상 사용할 수 없습니다.");
        }
    }

    // 연속 숫자 검사
    private boolean hasSequentialNumbers(String pinPassword) {

        for (int start = 0; start <= pinPassword.length() - 4; start++) {

            boolean ascending = true;
            boolean descending = true;

            // 시작 위치부터 4자리 구간 확인
            for (int offset = 1; offset < 4; offset++) {

                int previous = pinPassword.charAt(start + offset - 1) - '0';
                int current = pinPassword.charAt(start + offset) - '0';

                if (current - previous != 1) ascending = false;
                if (current - previous != -1) descending = false;
            }

            if (ascending || descending) {
                return true;
            }
        }

        return false;
    }

    // PIN 유효성 검사
    private void validatePin(String pinPassword) {

        if (pinPassword == null || !pinPassword.matches("\\d{6}")) {
            throw new IllegalArgumentException("PIN은 숫자 6자리로 입력해주세요.");
        }
    }

    private String generateRandomAccountNumber(String bankCode, Random random) {
        StringBuilder sb = new StringBuilder();
        int length = 12; // 기본 자릿수

        // 은행별 일반적인 계좌 자리수 및 접두사 반영 예시
        switch (bankCode) {
            case "090": // 카카오뱅크: 보통 3333-XX-XXXXXXX 형태 (총 12~13자리)
                sb.append("3333");
                length = 12;
                break;
            case "092": // 토스뱅크: 보통 1000-XXXX-XXXX 형태
                sb.append("1000");
                length = 12;
                break;
            case "089": // 케이뱅크: 보통 100-XXX-XXXXXX 형태
                sb.append("100");
                length = 12;
                break;
            default: // 그 외 일반 은행 (국민, 신한, 우리 등 10~14자리 랜덤)
                length = 12;
                break;
        }

        // 나머지 부족한 자릿수를 무작위 숫자로 채움
        while (sb.length() < length) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }
}