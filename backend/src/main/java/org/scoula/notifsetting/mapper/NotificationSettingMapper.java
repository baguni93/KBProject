package org.scoula.notifsetting.mapper;

import org.scoula.notifsetting.domain.NotificationSettingVO;

public interface NotificationSettingMapper {

    // 알림 설정 조회
    NotificationSettingVO findByUserId(Long userId);

    // 알림 설정 수정
    int update(NotificationSettingVO notificationSetting);

    // 알림 설정 저장
    int insert(NotificationSettingVO notificationSetting);
}