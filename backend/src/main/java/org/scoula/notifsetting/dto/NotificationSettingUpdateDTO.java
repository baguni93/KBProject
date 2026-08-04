package org.scoula.notifsetting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingUpdateDTO {

    private String financeNotificationYn;
    private String friendNotificationYn;
    private String rewardNotificationYn;
    private String eventNotificationYn;
}