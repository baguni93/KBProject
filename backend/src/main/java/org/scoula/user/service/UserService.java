package org.scoula.user.service;

import org.scoula.user.dto.*;

public interface UserService {

    Long signup(UserSignupDTO signupDTO); // 회원가입

    UserInfoDTO getUserInfo(Long userId); // 회원 기본정보 조회

    boolean updateUser(Long userId, UserUpdateDTO updateDTO); // 회원정보 수정

    NicknameCheckDTO checkNickname(String nickname); // 닉네임 중복 확인

    boolean verifyPin(Long userId, PinVerifyDTO pinVerifyDTO); // PIN 확인

    boolean changePin(Long userId, PinChangeDTO pinChangeDTO); // PIN 변경

    boolean withdraw(Long userId, UserWithdrawalDTO withdrawalDTO); // 회원 탈퇴
}