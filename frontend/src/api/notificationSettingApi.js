import api from './index';

// 알림 설정 조회
export const getNotificationSetting = async (userId) => {
    const { data } = await api.get(
        `/api/users/${userId}/notification-settings`,
    );

    return data;
};

// 알림 설정 수정
export const updateNotificationSetting = async (
    userId,
    notificationSetting,
) => {
    const { data } = await api.patch(
        `/api/users/${userId}/notification-settings`,
        notificationSetting,
    );

    return data;
};