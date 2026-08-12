import api from './index';

// 알림 설정 조회
export const getNotificationSetting = async () => {
    const { data } = await api.get('/api/users/notification-settings');
    return data;
};

// 알림 설정 수정
export const updateNotificationSetting = async (notificationSetting) => {
    const { data } = await api.patch('/api/users/notification-settings', notificationSetting);
    return data;
};