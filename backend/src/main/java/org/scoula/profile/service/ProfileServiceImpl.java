package org.scoula.profile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.UploadFiles;
import org.scoula.common.util.UploadPathName;
import org.scoula.profile.domain.ProfileVO;
import org.scoula.profile.dto.ProfileDTO;
import org.scoula.profile.dto.ProfileImageDTO;
import org.scoula.profile.dto.ProfileUpdateDTO;
import org.scoula.profile.mapper.ProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileMapper profileMapper;

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );


    // 프로필 조회
    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getProfile(Long userId) {

        ProfileVO profile = profileMapper.findByUserId(userId);

        if (profile == null) {
            throw new IllegalArgumentException("프로필을 찾을 수 없습니다.");
        }

        return ProfileDTO.of(profile);
    }


    // 프로필 수정
    @Override
    @Transactional
    public boolean updateProfile(Long userId, ProfileUpdateDTO updateDTO) {

        ProfileVO profile = profileMapper.findByUserId(userId);

        if (profile == null) {
            throw new IllegalArgumentException("프로필을 찾을 수 없습니다.");
        }

        validateNickname(userId, updateDTO.getNickname());

        String introduction = updateDTO.getIntroduction();

        if (introduction != null && introduction.length() > 300) {
            throw new IllegalArgumentException("자기소개는 300자 이하로 입력해주세요.");
        }

        profile.setNickname(updateDTO.getNickname().trim());
        profile.setIntroduction(introduction == null ? null : introduction.trim());

        return profileMapper.updateProfile(profile) > 0;
    }


    // 프로필 이미지 등록 및 변경
    @Override
    @Transactional
    public ProfileImageDTO updateProfileImage(Long userId, MultipartFile image) {

        ProfileVO profile = profileMapper.findByUserId(userId);

        if (profile == null) {
            throw new IllegalArgumentException("프로필을 찾을 수 없습니다.");
        }

        validateImage(image);

        String previousStoredName = profile.getStoredName();
        String storedPath;

        try {
            storedPath = UploadFiles.upload(UploadPathName.getProfilePath(), image);
        } catch (Exception e) {
            throw new RuntimeException("프로필 이미지 저장에 실패했습니다.", e);
        }

        String storedName = new File(storedPath).getName();
        String originalName = image.getOriginalFilename();

        int result = profileMapper.updateProfileImage(userId, originalName, storedName);

        if (result == 0) {
            deleteFile(storedName);
            throw new IllegalStateException("프로필 이미지 정보 저장에 실패했습니다.");
        }

        if (previousStoredName != null && !previousStoredName.trim().isEmpty()) {
            deleteFile(previousStoredName);
        }

        return ProfileImageDTO.builder()
                .userId(userId)
                .originalName(originalName)
                .storedName(storedName)
                .profileImageUrl("/api/users/" + userId + "/profile/image")
                .build();
    }


    // 프로필 이미지 파일 조회
    @Override
    @Transactional(readOnly = true)
    public File getProfileImage(Long userId) {
        ProfileVO profile = profileMapper.findByUserId(userId);

        if (profile == null) {
            throw new IllegalArgumentException("프로필을 찾을 수 없습니다.");
        }

        String storedName = profile.getStoredName();

        if (storedName == null || storedName.trim().isEmpty()) {
            storedName = "unknown.png";
        }

        File imageFile = new File(UploadPathName.getProfilePath(), storedName);

        if (!imageFile.exists() || !imageFile.isFile()) {
            throw new IllegalArgumentException("프로필 이미지 파일을 찾을 수 없습니다.");
        }

        return imageFile;
    }


    // 닉네임 검증
    private void validateNickname(Long userId, String nickname) {

        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        String trimmedNickname = nickname.trim();

        if (trimmedNickname.length() > 30) {
            throw new IllegalArgumentException("닉네임은 30자 이하로 입력해주세요.");
        }

        int count = profileMapper.countNicknameExceptUser(trimmedNickname, userId);

        if (count > 0) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }


    // 이미지 검증
    private void validateImage(MultipartFile image) {

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("프로필 이미지를 선택해주세요.");
        }

        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("프로필 이미지는 10MB 이하만 등록할 수 있습니다.");
        }

        String contentType = image.getContentType();

        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("JPG, PNG, GIF, WEBP 이미지만 등록할 수 있습니다.");
        }

        String originalName = image.getOriginalFilename();

        if (originalName == null || !originalName.contains(".")) {
            throw new IllegalArgumentException("올바른 이미지 파일이 아닙니다.");
        }
    }


    // 기존 이미지 파일 삭제
    private void deleteFile(String storedName) {

        File file = new File(UploadPathName.getProfilePath(), storedName);

        if (file.exists() && file.isFile()) {
            boolean deleted = file.delete();

            if (!deleted) {
                log.warn("기존 프로필 이미지 삭제 실패: {}", file.getPath());
            }
        }
    }
}