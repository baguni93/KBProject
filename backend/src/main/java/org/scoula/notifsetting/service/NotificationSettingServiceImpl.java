package org.scoula.notifsetting.service;

import lombok.RequiredArgsConstructor;
import org.scoula.notifsetting.domain.NotificationSettingVO;
import org.scoula.notifsetting.dto.NotificationSettingDTO;
import org.scoula.notifsetting.dto.NotificationSettingUpdateDTO;
import org.scoula.notifsetting.mapper.NotificationSettingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingServiceImpl implements NotificationSettingService {

    private final NotificationSettingMapper notificationSettingMapper;

    // 알림 설정 조회
    @Override
    @Transactional(readOnly = true)
    public NotificationSettingDTO getNotificationSetting(Long userId) {
        NotificationSettingVO notificationSetting = notificationSettingMapper.findByUserId(userId);

        if (notificationSetting == null) {
            throw new IllegalArgumentException("알림 설정을 찾을 수 없습니다.");
        }

        return NotificationSettingDTO.of(notificationSetting);
    }

    // 알림 설정 수정
    @Override
    @Transactional
    public boolean updateNotificationSetting(Long userId, NotificationSettingUpdateDTO updateDTO) {
        NotificationSettingVO notificationSetting = notificationSettingMapper.findByUserId(userId);

        if (notificationSetting == null) {
            throw new IllegalArgumentException("알림 설정을 찾을 수 없습니다.");
        }

        validateNotificationSetting(updateDTO);

        notificationSetting.setFinanceNotificationYn(updateDTO.getFinanceNotificationYn());
        notificationSetting.setFriendNotificationYn(updateDTO.getFriendNotificationYn());
        notificationSetting.setRewardNotificationYn(updateDTO.getRewardNotificationYn());
        notificationSetting.setEventNotificationYn(updateDTO.getEventNotificationYn());

        return notificationSettingMapper.update(notificationSetting) > 0;
    }

    // 알림 설정값 검증
    private void validateNotificationSetting(NotificationSettingUpdateDTO updateDTO) {
        validateYn(updateDTO.getFinanceNotificationYn(), "금융 알림");
        validateYn(updateDTO.getFriendNotificationYn(), "친구 요청 알림");
        validateYn(updateDTO.getRewardNotificationYn(), "리워드 알림");
        validateYn(updateDTO.getEventNotificationYn(), "이벤트 혜택 알림");
    }

    // Y/N 값 검증
    private void validateYn(String value, String fieldName) {
        if (!"Y".equals(value) && !"N".equals(value)) {
            throw new IllegalArgumentException(fieldName + " 설정값은 Y 또는 N이어야 합니다.");
        }
    }
}