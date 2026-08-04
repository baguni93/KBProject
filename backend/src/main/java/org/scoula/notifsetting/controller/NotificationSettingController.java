package org.scoula.notifsetting.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.notifsetting.dto.NotificationSettingDTO;
import org.scoula.notifsetting.dto.NotificationSettingUpdateDTO;
import org.scoula.notifsetting.service.NotificationSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/notification-settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    // NOTIF-001 알림 설정 조회
    @GetMapping
    public ResponseEntity<NotificationSettingDTO> getNotificationSetting(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationSettingService.getNotificationSetting(userId));
    }

    // NOTIF-002 알림 설정 수정
    @PatchMapping
    public ResponseEntity<Map<String, Object>> updateNotificationSetting(
            @PathVariable Long userId,
            @RequestBody NotificationSettingUpdateDTO updateDTO
    ) {

        boolean result = notificationSettingService.updateNotificationSetting(userId, updateDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", "알림 설정이 변경되었습니다.");

        return ResponseEntity.ok(response);
    }
}