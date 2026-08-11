package org.scoula.user.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.user.domain.AccountVO;
import org.scoula.user.domain.BankVO;
import org.scoula.user.domain.UserVO;

import java.util.List;

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

    // 회원 이름 변경
    int updateUserName(
            @Param("userId") Long userId,
            @Param("userName") String userName
    );

    // 회원 휴대폰번호 변경
    int updatePhoneNumber(
            @Param("userId") Long userId,
            @Param("phoneNumber") String phoneNumber
    );

    // PIN 변경
    int updatePin(
            @Param("userId") Long userId,
            @Param("pinPassword") String pinPassword
    );

    // PIN 재설정
    int updatePinByPhoneNumber(
            @Param("phoneNumber") String phoneNumber,
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

    void insertAccount(@Param("accountList") List<AccountVO> accountList);

    List<BankVO> getBanks();

    AccountVO getAccountByBackCode(@Param("userId") int userId, @Param("bankCode") String bankCode);
}