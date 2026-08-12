package org.scoula.profile.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.common.util.UploadFiles;
import org.scoula.profile.dto.ProfileDTO;
import org.scoula.profile.dto.ProfileImageDTO;
import org.scoula.profile.dto.ProfileUpdateDTO;
import org.scoula.profile.service.ProfileService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // PROFILE-001 프로필 조회
    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile(@AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getUser().getUserId();
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    // PROFILE-002 프로필 수정
    @PatchMapping
    public ResponseEntity<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody ProfileUpdateDTO updateDTO
    ) {
        Long userId = customUser.getUser().getUserId();
        boolean result = profileService.updateProfile(userId, updateDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", "프로필이 수정되었습니다.");

        return ResponseEntity.ok(response);
    }

    // PROFILE-003 프로필 이미지 등록 및 변경
    @PutMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileImageDTO> updateProfileImage(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam("image") MultipartFile image
    ) {
        Long userId = customUser.getUser().getUserId();
        return ResponseEntity.ok(profileService.updateProfileImage(userId, image));
    }

    // PROFILE-004 프로필 이미지 조회
    @GetMapping("/image")
    public void getProfileImage(
            @AuthenticationPrincipal CustomUser customUser,
            HttpServletResponse response
    ) {
        Long userId = customUser.getUser().getUserId();
        File imageFile = profileService.getProfileImage(userId);

        UploadFiles.downloadImage(response, imageFile);
    }

    // PROFILE-005 프로필 이미지 삭제
    @DeleteMapping("/image")
    public ResponseEntity<Map<String, Object>> deleteProfileImage(@AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getUser().getUserId();
        boolean result = profileService.deleteProfileImage(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", "프로필 이미지가 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }
}