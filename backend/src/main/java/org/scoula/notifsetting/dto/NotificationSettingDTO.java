package org.scoula.notifsetting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.notifsetting.domain.NotificationSettingVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingDTO {

    private String financeNotificationYn;
    private String friendNotificationYn;
    private String rewardNotificationYn;
    private String eventNotificationYn;

    public static NotificationSettingDTO of(NotificationSettingVO notificationSetting) {
        return NotificationSettingDTO.builder()
                .financeNotificationYn(notificationSetting.getFinanceNotificationYn())
                .friendNotificationYn(notificationSetting.getFriendNotificationYn())
                .rewardNotificationYn(notificationSetting.getRewardNotificationYn())
                .eventNotificationYn(notificationSetting.getEventNotificationYn())
                .build();
    }
}