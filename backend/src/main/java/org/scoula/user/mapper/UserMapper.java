package org.scoula.user.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.user.domain.UserVO;

public interface UserMapper {

    // 회원번호 조회
    UserVO findById(Long userId);

    // 휴대폰번호 조회
    UserVO findByPhoneNumber(String phoneNumber);

    // 닉네임 조회
    UserVO findByNickname(String nickname);

    // 회원 저장
    int insert(UserVO user);

    // 프로필 저장
    int insertProfile(UserVO user);

    // 닉네임 수정
    int update(UserVO user);

    // PIN 변경
    int updatePin(
            @Param("userId") Long userId,
            @Param("pinPassword") String pinPassword
    );

    // 회원탈퇴
    int withdraw(Long userId);

    // 탈퇴 회원 휴대폰번호 익명화
    int anonymizePhoneNumber(
            @Param("userId") Long userId,
            @Param("anonymousPhoneNumber") String anonymousPhoneNumber
    );

    // 탈퇴 회원 닉네임 익명화
    int anonymizeNickname(
            @Param("userId") Long userId,
            @Param("anonymousNickname") String anonymousNickname
    );
}