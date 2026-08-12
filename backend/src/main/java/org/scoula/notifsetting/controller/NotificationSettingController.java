package org.scoula.notifsetting.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.notifsetting.dto.NotificationSettingDTO;
import org.scoula.notifsetting.dto.NotificationSettingUpdateDTO;
import org.scoula.notifsetting.service.NotificationSettingService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users/notification-settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    // NOTIF-001 알림 설정 조회
    @GetMapping
    public ResponseEntity<NotificationSettingDTO> getNotificationSetting(@AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getUser().getUserId();
        return ResponseEntity.ok(notificationSettingService.getNotificationSetting(userId));
    }

    // NOTIF-002 알림 설정 수정
    @PatchMapping
    public ResponseEntity<Map<String, Object>> updateNotificationSetting(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody NotificationSettingUpdateDTO updateDTO
    ) {
        Long userId = customUser.getUser().getUserId();
        boolean result = notificationSettingService.updateNotificationSetting(userId, updateDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", "알림 설정이 변경되었습니다.");

        return ResponseEntity.ok(response);
    }
}