package org.scoula.profile.service;

import org.scoula.profile.dto.ProfileDTO;
import org.scoula.profile.dto.ProfileImageDTO;
import org.scoula.profile.dto.ProfileUpdateDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface ProfileService {

    // 프로필 조회
    ProfileDTO getProfile(Long userId);

    // 프로필 수정
    boolean updateProfile(Long userId, ProfileUpdateDTO updateDTO);

    // 프로필 이미지 등록 및 변경
    ProfileImageDTO updateProfileImage(Long userId, MultipartFile image);

    // 프로필 이미지 파일 조회
    File getProfileImage(Long userId);
}