package org.scoula.login.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.login.domain.PhoneAuthVO;
import org.scoula.login.domain.RefreshTokenVO;
import org.scoula.login.dto.*;
import org.scoula.login.mapper.LoginMapper;
import org.scoula.security.util.JwtProcessor;
import org.scoula.user.domain.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Log4j2
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private static final int AUTH_EXPIRE_MINUTES = 3;
    private static final int SIGNUP_EXPIRE_MINUTES = 10;
    private static final int MAX_FAIL_COUNT = 5;

    private final LoginMapper loginMapper;
    private final JwtProcessor jwtProcessor;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();


    // AUTH-001 개발용 인증번호 최초 발급
    @Override
    @Transactional
    public String sendPhoneAuthCode(PhoneAuthSendDTO sendDTO) {

        validateSendRequest(sendDTO);

        String phoneNumber = normalizePhoneNumber(sendDTO.getPhoneNumber());
        String verificationPurpose = getVerificationPurpose(sendDTO.getVerificationPurpose());

        // 같은 번호와 인증 목적의 이전 요청 삭제
        loginMapper.deletePhoneAuth(phoneNumber, verificationPurpose);

        String verificationCode = createVerificationCode();
        PhoneAuthVO phoneAuth = new PhoneAuthVO();

        phoneAuth.setUserName(sendDTO.getUserName().trim());
        phoneAuth.setBirthDate(sendDTO.getBirthDate());
        phoneAuth.setCarrierCode(sendDTO.getCarrierCode().trim().toUpperCase());
        phoneAuth.setPhoneNumber(phoneNumber);
        phoneAuth.setVerificationCode(verificationCode);
        phoneAuth.setVerificationPurpose(verificationPurpose);
        phoneAuth.setRequestedAt(LocalDateTime.now());
        phoneAuth.setVerifiedYn("N");
        phoneAuth.setFailCount(0);

        int inserted = loginMapper.insertPhoneAuth(phoneAuth);

        if (inserted != 1) {
            throw new IllegalStateException("인증번호 저장에 실패했습니다.");
        }

        log.info("======================================");
        log.info("[개발용 휴대폰 인증]");
        log.info("휴대폰번호 : {}", phoneNumber);
        log.info("인증번호 : {}", verificationCode);
        log.info("유효시간 : {}분", AUTH_EXPIRE_MINUTES);
        log.info("======================================");

        // 개발 단계에서만 인증번호 반환
        return verificationCode;
    }


    // AUTH-002 개발용 인증번호 재발급
    @Override
    @Transactional
    public String resendPhoneAuthCode(PhoneAuthResendDTO resendDTO) {

        if (resendDTO == null || isBlank(resendDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("휴대폰번호를 입력해주세요.");
        }

        String phoneNumber = normalizePhoneNumber(resendDTO.getPhoneNumber());
        String verificationPurpose = getVerificationPurpose(resendDTO.getVerificationPurpose());

        PhoneAuthVO phoneAuth =
                loginMapper.findLatestPhoneAuth(phoneNumber, verificationPurpose);

        if (phoneAuth == null) {
            throw new IllegalArgumentException(
                    "기존 인증번호 발급 내역이 없습니다. 인증번호를 먼저 요청해주세요."
            );
        }

        String verificationCode = createVerificationCode();

        int updated = loginMapper.updatePhoneAuthCode(
                phoneAuth.getVerificationId(),
                verificationCode,
                LocalDateTime.now()
        );

        if (updated != 1) {
            throw new IllegalStateException("인증번호 재발급에 실패했습니다.");
        }

        log.info("======================================");
        log.info("[개발용 인증번호 재발급]");
        log.info("휴대폰번호 : {}", phoneNumber);
        log.info("인증번호 : {}", verificationCode);
        log.info("유효시간 : {}분", AUTH_EXPIRE_MINUTES);
        log.info("======================================");

        return verificationCode;
    }


    // AUTH-003 인증번호 확인
    @Override
    @Transactional
    public void verifyPhoneAuthCode(PhoneAuthVerifyDTO verifyDTO) {

        validateVerifyRequest(verifyDTO);

        String phoneNumber = normalizePhoneNumber(verifyDTO.getPhoneNumber());
        String verificationPurpose = getVerificationPurpose(verifyDTO.getVerificationPurpose());

        PhoneAuthVO phoneAuth =
                loginMapper.findLatestPhoneAuth(phoneNumber, verificationPurpose);

        if (phoneAuth == null) {
            throw new IllegalArgumentException("인증번호 발급 내역이 없습니다.");
        }

        if ("Y".equals(phoneAuth.getVerifiedYn())) {
            throw new IllegalArgumentException("이미 인증이 완료되었습니다.");
        }

        if (phoneAuth.getFailCount() >= MAX_FAIL_COUNT) {
            throw new IllegalArgumentException(
                    "인증번호 입력 가능 횟수를 초과했습니다. 다시 발급해주세요."
            );
        }

        LocalDateTime expiresAt =
                phoneAuth.getRequestedAt().plusMinutes(AUTH_EXPIRE_MINUTES);

        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new IllegalArgumentException(
                    "인증번호가 만료되었습니다. 다시 발급해주세요."
            );
        }

        if (!phoneAuth.getVerificationCode().equals(verifyDTO.getVerificationCode())) {

            loginMapper.increaseFailCount(phoneAuth.getVerificationId());

            int remainingCount = MAX_FAIL_COUNT - phoneAuth.getFailCount() - 1;

            if (remainingCount <= 0) {
                throw new IllegalArgumentException(
                        "인증번호 입력 가능 횟수를 초과했습니다. 다시 발급해주세요."
                );
            }

            throw new IllegalArgumentException(
                    "인증번호가 일치하지 않습니다. 남은 횟수: " + remainingCount
            );
        }

        int updated = loginMapper.completePhoneAuth(phoneAuth.getVerificationId());

        if (updated != 1) {
            throw new IllegalStateException("휴대폰 인증 처리에 실패했습니다.");
        }
    }


    // AUTH-004 가입 여부 확인
    @Override
    @Transactional(readOnly = true)
    public String checkSignupStatus(SignupCheckDTO checkDTO) {

        if (checkDTO == null || isBlank(checkDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("휴대폰번호를 입력해주세요.");
        }

        String phoneNumber = normalizePhoneNumber(checkDTO.getPhoneNumber());

        PhoneAuthVO phoneAuth =
                loginMapper.findLatestPhoneAuth(phoneNumber, "SIGN_UP");

        if (phoneAuth == null) {
            throw new IllegalArgumentException("휴대폰 인증 내역이 없습니다.");
        }

        if (!"Y".equals(phoneAuth.getVerifiedYn())) {
            throw new IllegalArgumentException("휴대폰 인증이 완료되지 않았습니다.");
        }

        LocalDateTime signupExpiresAt =
                phoneAuth.getRequestedAt().plusMinutes(SIGNUP_EXPIRE_MINUTES);

        if (LocalDateTime.now().isAfter(signupExpiresAt)) {
            throw new IllegalArgumentException(
                    "휴대폰 인증 유효시간이 만료되었습니다. 다시 인증해주세요."
            );
        }

        int userCount = loginMapper.countUserByPhoneNumber(phoneNumber);

        if (userCount > 0) {
            return "EXISTING";
        }

        return "NEW";
    }

    // AUTH-005 PIN 로그인
    @Override
    @Transactional
    public TokenDTO login(PinLoginDTO loginDTO) {

        validateLoginRequest(loginDTO);

        String phoneNumber = normalizePhoneNumber(loginDTO.getPhoneNumber());
        UserVO user = loginMapper.findUserByPhoneNumber(phoneNumber);

        if (user == null) {
            throw new IllegalArgumentException("가입되지 않은 휴대폰번호입니다.");
        }

        if (!"ACTIVE".equals(user.getUserStatus())) {
            throw new IllegalArgumentException("로그인할 수 없는 회원 상태입니다.");
        }

        if (isBlank(user.getPinPassword())) {
            throw new IllegalStateException("등록된 간편비밀번호가 없습니다.");
        }

        boolean matched =
                passwordEncoder.matches(loginDTO.getPinPassword(), user.getPinPassword());

        if (!matched) {
            throw new IllegalArgumentException("간편비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProcessor.generateAccessToken(phoneNumber, user.getUserId());
        String refreshToken = jwtProcessor.generateRefreshToken(phoneNumber, user.getUserId());

        // 회원당 Refresh Token 1개만 유지한다.
        // 새 로그인 시 기존 토큰을 삭제한다.
        loginMapper.deleteRefreshTokenByUserId(user.getUserId());

        LocalDateTime issuedAt = LocalDateTime.now();

        LocalDateTime expiresAt =
                issuedAt.plusSeconds(jwtProcessor.getRefreshTokenValidSeconds());

        RefreshTokenVO refreshTokenVO = RefreshTokenVO.builder()
                .userId(user.getUserId())
                .refreshToken(refreshToken)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        int inserted = loginMapper.insertRefreshToken(refreshTokenVO);

        if (inserted != 1) {
            throw new IllegalStateException("리프레시 토큰 저장에 실패했습니다.");
        }

        log.info("로그인 성공 - userId: {}", user.getUserId());

        return TokenDTO.builder()
                .userId(user.getUserId())
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtProcessor.getAccessTokenValidSeconds())
                .build();
    }


    // AUTH-006 로그아웃
    @Override
    @Transactional
    public void logout(LogoutDTO logoutDTO) {

        if (logoutDTO == null || isBlank(logoutDTO.getRefreshToken())) {
            throw new IllegalArgumentException("리프레시 토큰을 입력해주세요.");
        }

        String refreshToken = logoutDTO.getRefreshToken().trim();
        int deleted = loginMapper.deleteRefreshToken(refreshToken);

        // 토큰이 이미 삭제된 경우에도 로그아웃 요청은 성공으로 처리한다.
        log.info("로그아웃 완료 - 삭제된 토큰 수: {}", deleted);
    }


    // AUTH-007 토큰 재발급
    @Override
    @Transactional
    public TokenDTO refreshToken(RefreshTokenRequestDTO requestDTO) {

        if (requestDTO == null || isBlank(requestDTO.getRefreshToken())) {
            throw new IllegalArgumentException("리프레시 토큰을 입력해주세요.");
        }

        String oldRefreshToken = requestDTO.getRefreshToken().trim();

        // JWT 서명 및 만료 여부 검증
        try {
            jwtProcessor.validateToken(oldRefreshToken);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "유효하지 않거나 만료된 리프레시 토큰입니다."
            );
        }

        String tokenType = jwtProcessor.getTokenType(oldRefreshToken);

        if (!"REFRESH".equals(tokenType)) {
            throw new IllegalArgumentException("리프레시 토큰이 아닙니다.");
        }

        // DB에 저장된 Refresh Token인지 확인
        RefreshTokenVO savedToken = loginMapper.findRefreshToken(oldRefreshToken);

        if (savedToken == null) {
            throw new IllegalArgumentException(
                    "로그아웃되었거나 등록되지 않은 토큰입니다."
            );
        }

        // DB의 만료 일시도 별도로 확인
        if (savedToken.getExpiresAt() == null
                || savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {

            loginMapper.deleteRefreshToken(oldRefreshToken);

            throw new IllegalArgumentException("만료된 리프레시 토큰입니다.");
        }

        String phoneNumber = jwtProcessor.getUsername(oldRefreshToken);
        UserVO user = loginMapper.findUserByPhoneNumber(phoneNumber);

        if (user == null) {
            throw new IllegalArgumentException("토큰에 해당하는 회원이 없습니다.");
        }

        if (!savedToken.getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("토큰의 회원정보가 일치하지 않습니다.");
        }

        if (!"ACTIVE".equals(user.getUserStatus())) {
            throw new IllegalArgumentException("로그인할 수 없는 회원 상태입니다.");
        }

        String newAccessToken = jwtProcessor.generateAccessToken(phoneNumber, user.getUserId());
        String newRefreshToken = jwtProcessor.generateRefreshToken(phoneNumber, user.getUserId());

        // 기존 토큰 삭제 후 새 토큰 저장
        // Refresh Token Rotation 방식
        loginMapper.deleteRefreshToken(oldRefreshToken);

        LocalDateTime newIssuedAt = LocalDateTime.now();

        LocalDateTime newExpiresAt =
                newIssuedAt.plusSeconds(jwtProcessor.getRefreshTokenValidSeconds());

        RefreshTokenVO newTokenVO = RefreshTokenVO.builder()
                .userId(user.getUserId())
                .refreshToken(newRefreshToken)
                .issuedAt(newIssuedAt)
                .expiresAt(newExpiresAt)
                .build();

        int inserted = loginMapper.insertRefreshToken(newTokenVO);

        if (inserted != 1) {
            throw new IllegalStateException("리프레시 토큰 저장에 실패했습니다.");
        }

        log.info("토큰 재발급 완료 - userId: {}", user.getUserId());

        return TokenDTO.builder()
                .userId(user.getUserId())
                .tokenType("Bearer")
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresIn(jwtProcessor.getAccessTokenValidSeconds())
                .build();
    }


    // 인증번호 발급 요청 검증
    private void validateSendRequest(PhoneAuthSendDTO sendDTO) {

        if (sendDTO == null) {
            throw new IllegalArgumentException("인증 요청 정보가 없습니다.");
        }

        if (isBlank(sendDTO.getUserName())) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        if (sendDTO.getBirthDate() == null) {
            throw new IllegalArgumentException("생년월일을 입력해주세요.");
        }

        if (isBlank(sendDTO.getCarrierCode())) {
            throw new IllegalArgumentException("통신사를 선택해주세요.");
        }

        if (isBlank(sendDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("휴대폰번호를 입력해주세요.");
        }
    }


    // 인증번호 확인 요청 검증
    private void validateVerifyRequest(PhoneAuthVerifyDTO verifyDTO) {

        if (verifyDTO == null) {
            throw new IllegalArgumentException("인증 요청 정보가 없습니다.");
        }

        if (isBlank(verifyDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("휴대폰번호를 입력해주세요.");
        }

        if (isBlank(verifyDTO.getVerificationCode())) {
            throw new IllegalArgumentException("인증번호를 입력해주세요.");
        }

        if (!verifyDTO.getVerificationCode().matches("\\d{6}")) {
            throw new IllegalArgumentException("인증번호는 숫자 6자리여야 합니다.");
        }
    }


    // 로그인 요청 검증
    private void validateLoginRequest(PinLoginDTO loginDTO) {

        if (loginDTO == null) {
            throw new IllegalArgumentException("로그인 정보를 입력해주세요.");
        }

        if (isBlank(loginDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("휴대폰번호를 입력해주세요.");
        }

        if (isBlank(loginDTO.getPinPassword())) {
            throw new IllegalArgumentException("간편비밀번호를 입력해주세요.");
        }

        if (!loginDTO.getPinPassword().matches("\\d{6}")) {
            throw new IllegalArgumentException("간편비밀번호는 숫자 6자리여야 합니다.");
        }
    }


    // 100000~999999 범위의 인증번호 생성
    private String createVerificationCode() {
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }


    // 휴대폰번호에서 숫자만 추출
    private String normalizePhoneNumber(String phoneNumber) {

        if (phoneNumber == null) {
            throw new IllegalArgumentException("휴대폰번호를 입력해주세요.");
        }

        String normalized = phoneNumber.replaceAll("[^0-9]", "");

        if (!normalized.matches("^01[016789][0-9]{7,8}$")) {
            throw new IllegalArgumentException("올바른 휴대폰번호 형식이 아닙니다.");
        }

        return normalized;
    }


    // 인증 목적 기본값 처리
    private String getVerificationPurpose(String verificationPurpose) {

        if (isBlank(verificationPurpose)) {
            return "SIGN_UP";
        }

        return verificationPurpose.trim().toUpperCase();
    }


    // null 또는 빈 문자열 검사
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}