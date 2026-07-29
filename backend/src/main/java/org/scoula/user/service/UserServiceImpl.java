package org.scoula.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.user.domain.UserVO;
import org.scoula.user.dto.*;
import org.scoula.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private static final long REJOIN_WAIT_HOURS = 24L;


    // 회원가입
    @Override
    @Transactional
    public Long signup(UserSignupDTO signupDTO) {

        validateSignupDTO(signupDTO);

        String phoneNumber =
                signupDTO.getPhoneNumber().trim();

        String nickname =
                signupDTO.getNickname().trim();

        /*
         * 같은 휴대폰번호 회원 확인
         *
         * ACTIVE:
         * 재가입 불가
         *
         * WITHDRAWN + 24시간 미경과:
         * 재가입 불가
         *
         * WITHDRAWN + 24시간 경과:
         * 기존 정보 익명화 후 새 회원 생성
         */
        UserVO existingUser =
                userMapper.findByPhoneNumber(phoneNumber);

        if (existingUser != null) {
            processExistingUserForSignup(existingUser);
        }

        // 기존 탈퇴 회원이 익명화된 이후 닉네임 중복 확인
        UserVO nicknameUser =
                userMapper.findByNickname(nickname);

        if (nicknameUser != null) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 닉네임입니다."
            );
        }

        validatePin(signupDTO.getPinPassword());

        UserVO user = UserVO.builder()
                .userName(
                        signupDTO.getUserName().trim()
                )
                .birthDate(
                        signupDTO.getBirthDate()
                )
                .phoneNumber(phoneNumber)
                .pinPassword(
                        passwordEncoder.encode(
                                signupDTO.getPinPassword()
                        )
                )
                .nickname(nickname)
                .userStatus("ACTIVE")
                .build();

        int userResult = userMapper.insert(user);

        if (userResult != 1) {
            throw new IllegalStateException(
                    "회원정보 저장 중 오류가 발생했습니다."
            );
        }

        int profileResult =
                userMapper.insertProfile(user);

        if (profileResult != 1) {
            throw new IllegalStateException(
                    "프로필 저장 중 오류가 발생했습니다."
            );
        }

        log.info(
                "회원가입 완료: userId={}",
                user.getUserId()
        );

        return user.getUserId();
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
    public boolean updateUser(
            Long userId,
            UserUpdateDTO updateDTO
    ) {
        UserVO user = getActiveUser(userId);

        String nickname = updateDTO.getNickname();

        if (nickname == null
                || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "닉네임을 입력해주세요."
            );
        }

        String trimmedNickname = nickname.trim();

        UserVO nicknameUser =
                userMapper.findByNickname(trimmedNickname);

        if (nicknameUser != null
                && !nicknameUser.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 닉네임입니다."
            );
        }

        user.setNickname(trimmedNickname);

        return userMapper.update(user) == 1;
    }


    // 닉네임 중복 확인
    @Override
    @Transactional(readOnly = true)
    public NicknameCheckDTO checkNickname(String nickname) {

        if (nickname == null
                || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "닉네임을 입력해주세요."
            );
        }

        String trimmedNickname = nickname.trim();

        UserVO user =
                userMapper.findByNickname(trimmedNickname);

        return NicknameCheckDTO.builder()
                .nickname(trimmedNickname)
                .available(user == null)
                .build();
    }


    // PIN 확인
    @Override
    @Transactional(readOnly = true)
    public boolean verifyPin(
            Long userId,
            PinVerifyDTO pinVerifyDTO
    ) {
        UserVO user = getActiveUser(userId);

        validatePin(
                pinVerifyDTO.getPinPassword()
        );

        return passwordEncoder.matches(
                pinVerifyDTO.getPinPassword(),
                user.getPinPassword()
        );
    }


    // PIN 변경
    @Override
    @Transactional
    public boolean changePin(
            Long userId,
            PinChangeDTO pinChangeDTO
    ) {
        UserVO user = getActiveUser(userId);

        validatePin(
                pinChangeDTO.getCurrentPinPassword()
        );

        validatePin(
                pinChangeDTO.getNewPinPassword()
        );

        if (!passwordEncoder.matches(
                pinChangeDTO.getCurrentPinPassword(),
                user.getPinPassword()
        )) {
            throw new IllegalArgumentException(
                    "현재 PIN이 일치하지 않습니다."
            );
        }

        if (pinChangeDTO.getNewPinPasswordConfirm() == null
                || !pinChangeDTO.getNewPinPassword()
                .equals(
                        pinChangeDTO
                                .getNewPinPasswordConfirm()
                )) {
            throw new IllegalArgumentException(
                    "새 PIN과 PIN 확인 값이 일치하지 않습니다."
            );
        }

        if (passwordEncoder.matches(
                pinChangeDTO.getNewPinPassword(),
                user.getPinPassword()
        )) {
            throw new IllegalArgumentException(
                    "현재 PIN과 다른 PIN을 입력해주세요."
            );
        }

        String encodedPinPassword =
                passwordEncoder.encode(
                        pinChangeDTO.getNewPinPassword()
                );

        return userMapper.updatePin(
                userId,
                encodedPinPassword
        ) == 1;
    }


    // 회원탈퇴
    @Override
    @Transactional
    public boolean withdraw(
            Long userId,
            UserWithdrawalDTO withdrawalDTO
    ) {
        UserVO user = getActiveUser(userId);

        if (withdrawalDTO == null) {
            throw new IllegalArgumentException(
                    "탈퇴 요청 정보가 필요합니다."
            );
        }

        validatePin(
                withdrawalDTO.getPinPassword()
        );

        if (!passwordEncoder.matches(
                withdrawalDTO.getPinPassword(),
                user.getPinPassword()
        )) {
            throw new IllegalArgumentException(
                    "PIN이 일치하지 않습니다."
            );
        }

        int result = userMapper.withdraw(userId);

        if (result != 1) {
            throw new IllegalStateException(
                    "회원 탈퇴 처리에 실패했습니다."
            );
        }

        log.info(
                "회원탈퇴 완료: userId={}",
                userId
        );

        return true;
    }


    /*
     * 재가입 요청 시 기존 회원 상태 처리
     */
    private void processExistingUserForSignup(
            UserVO existingUser
    ) {
        String userStatus =
                existingUser.getUserStatus();

        if ("ACTIVE".equals(userStatus)) {
            throw new IllegalArgumentException(
                    "이미 가입된 휴대폰번호입니다."
            );
        }

        if (!"WITHDRAWN".equals(userStatus)) {
            throw new IllegalStateException(
                    "현재 가입할 수 없는 회원 상태입니다."
            );
        }

        LocalDateTime withdrawnAt =
                existingUser.getWithdrawnAt();

        if (withdrawnAt == null) {
            throw new IllegalStateException(
                    "탈퇴 일시를 확인할 수 없습니다."
            );
        }

        LocalDateTime rejoinAvailableAt =
                withdrawnAt.plusHours(
                        REJOIN_WAIT_HOURS
                );

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(rejoinAvailableAt)) {
            Duration remaining =
                    Duration.between(
                            now,
                            rejoinAvailableAt
                    );

            long remainingHours =
                    remaining.toHours();

            long remainingMinutes =
                    remaining.minusHours(
                            remainingHours
                    ).toMinutes();

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


    /*
     * 24시간이 지난 탈퇴 회원의 개인정보 익명화
     */
    private void anonymizeWithdrawnUser(
            UserVO withdrawnUser
    ) {
        Long userId =
                withdrawnUser.getUserId();

        long timestamp =
                System.currentTimeMillis();

        String anonymousPhoneNumber =
                "withdrawn_"
                        + userId
                        + "_"
                        + timestamp;

        String anonymousNickname =
                "withdrawn_"
                        + userId
                        + "_"
                        + timestamp;

        int phoneResult =
                userMapper.anonymizePhoneNumber(
                        userId,
                        anonymousPhoneNumber
                );

        if (phoneResult != 1) {
            throw new IllegalStateException(
                    "탈퇴 회원 휴대폰번호 익명화에 실패했습니다."
            );
        }

        int nicknameResult =
                userMapper.anonymizeNickname(
                        userId,
                        anonymousNickname
                );

        if (nicknameResult != 1) {
            throw new IllegalStateException(
                    "탈퇴 회원 닉네임 익명화에 실패했습니다."
            );
        }

        log.info(
                "탈퇴 회원 익명화 완료: userId={}",
                userId
        );
    }


    // 활성 회원 조회
    private UserVO getActiveUser(Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "회원번호가 필요합니다."
            );
        }

        UserVO user =
                userMapper.findById(userId);

        if (user == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 회원입니다."
            );
        }

        if ("WITHDRAWN".equals(
                user.getUserStatus()
        )) {
            throw new IllegalStateException(
                    "탈퇴한 회원입니다."
            );
        }

        if (!"ACTIVE".equals(
                user.getUserStatus()
        )) {
            throw new IllegalStateException(
                    "이용할 수 없는 회원입니다."
            );
        }

        return user;
    }


    // 회원가입 요청 검증
    private void validateSignupDTO(
            UserSignupDTO signupDTO
    ) {
        if (signupDTO == null) {
            throw new IllegalArgumentException(
                    "회원가입 정보가 필요합니다."
            );
        }

        if (signupDTO.getUserName() == null
                || signupDTO.getUserName()
                .trim()
                .isEmpty()) {
            throw new IllegalArgumentException(
                    "이름을 입력해주세요."
            );
        }

        if (signupDTO.getBirthDate() == null) {
            throw new IllegalArgumentException(
                    "생년월일을 입력해주세요."
            );
        }

        if (signupDTO.getPhoneNumber() == null
                || signupDTO.getPhoneNumber()
                .trim()
                .isEmpty()) {
            throw new IllegalArgumentException(
                    "휴대폰번호를 입력해주세요."
            );
        }

        if (signupDTO.getNickname() == null
                || signupDTO.getNickname()
                .trim()
                .isEmpty()) {
            throw new IllegalArgumentException(
                    "닉네임을 입력해주세요."
            );
        }
    }


    // PIN 유효성 검사
    private void validatePin(
            String pinPassword
    ) {
        if (pinPassword == null
                || !pinPassword.matches("\\d{6}")) {
            throw new IllegalArgumentException(
                    "PIN은 숫자 6자리로 입력해주세요."
            );
        }
    }
}