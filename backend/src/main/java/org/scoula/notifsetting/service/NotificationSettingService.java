package org.scoula.notifsetting.service;

import org.scoula.notifsetting.dto.NotificationSettingDTO;
import org.scoula.notifsetting.dto.NotificationSettingUpdateDTO;

public interface NotificationSettingService {

    // 알림 설정 조회
    NotificationSettingDTO getNotificationSetting(Long userId);

    // 알림 설정 수정
    boolean updateNotificationSetting(Long userId, NotificationSettingUpdateDTO updateDTO);
}