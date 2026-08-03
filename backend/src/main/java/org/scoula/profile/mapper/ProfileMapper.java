package org.scoula.profile.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.profile.domain.ProfileVO;

public interface ProfileMapper {

    // 회원 프로필 조회
    ProfileVO findByUserId(Long userId);

    // 다른 회원의 닉네임 사용 여부 조회
    int countNicknameExceptUser(
            @Param("nickname") String nickname,
            @Param("userId") Long userId
    );

    // 프로필 수정
    int updateProfile(ProfileVO profile);

    // 프로필 이미지 수정
    int updateProfileImage(
            @Param("userId") Long userId,
            @Param("originalName") String originalName,
            @Param("storedName") String storedName
    );
}