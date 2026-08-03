package org.scoula.notifsetting.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingVO {

    private Long notificationSettingId;
    private Long userId;
    private String financeNotificationYn;
    private String friendNotificationYn;
    private String rewardNotificationYn;
    private String eventNotificationYn;
    private LocalDateTime updatedAt;
}