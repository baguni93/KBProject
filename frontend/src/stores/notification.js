import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import notificationApi from '@/api/notificationApi';

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref([]);

  // 읽지 않은 알림 개수
  const unreadCount = computed(() => {
    return notifications.value.filter((x) => x.status === 'UNREAD').length;
  });

  // 안읽은 알림 존재 여부
  const hasUnread = computed(() => {
    return unreadCount.value > 0;
  });

  // 기존 알림 조회
  const getList = async (params) => {
    try {
      notifications.value = await notificationApi.getList(params);

      console.log(notifications.value);
    } catch (e) {
      console.log(e);
    }
  };

  // WebSocket 실시간 알림 추가
  const addNotification = (notification) => {
    const exists = notifications.value.some(
      (x) => x.notificationId === notification.notificationId,
    );

    if (!exists) {
      notifications.value.unshift(notification);
    }
  };

  // 읽음 처리
  const read = async (params) => {
    try {
      await notificationApi.read(params);

      const target = notifications.value.find(
        (x) => x.notificationId === params.notificationId,
      );

      if (target) {
        target.status = 'READ';
      }
    } catch (e) {
      console.log(e);
    }
  };

  // 전체 읽음
  const readAll = async (params) => {
    try {
      await notificationApi.readAll(params);

      notifications.value.forEach((x) => {
        x.status = 'READ';
      });
    } catch (e) {
      console.log(e);
    }
  };

  return {
    notifications,

    unreadCount,

    hasUnread,

    getList,

    addNotification,

    read,

    readAll,
  };
});
