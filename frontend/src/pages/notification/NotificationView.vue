<template>
  <div class="notification-page">
    <PageHeader title="알림" />

    <div class="notification-content">
      <div class="notification-header">
        <button class="read-all" @click="onclickReadAll">모두 읽음</button>
      </div>

      <div class="notification-list">
        <div
          v-for="notification in notifications"
          :key="notification.notificationId"
          class="notification-card"
          @click="onclickRead(notification)"
        >
          <!-- 프로필 -->
          <img
            :src="`/api/feeds/profile/${notification.sender.profileImageName}`"
            class="profile-img"
            alt="profile"
          />

          <!-- 내용 -->
          <div class="body">
            <div class="title">
              {{ notificationTitle(notification.notificationType) }}
            </div>

            <div class="message">
              <strong>
                {{ notification.sender.nickname }}
              </strong>

              {{ notificationMessage(notification.notificationType) }}
            </div>

            <div class="date">
              {{ formatRelativeDate(notification.createdAt) }}
            </div>
          </div>

          <!-- 안읽음 표시 -->
          <div v-if="notification.status === 'UNREAD'" class="dot"></div>

          <!-- 삭제 버튼 -->
          <button class="delete-btn" @click.stop="onclickDelete(notification)">
            ×
          </button>
        </div>
      </div>

      <EmptyList
        v-if="notifications.length === 0"
        desc="받은 알림이 없습니다."
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import { useNotificationStore } from '@/stores/notification';
import { useUserStore } from '@/stores/user';
import { formatRelativeDate } from '@/util/data';
import EmptyList from '@/components/common/EmptyList.vue';

const router = useRouter();

const userStore = useUserStore();
const userId = userStore.userId;

const notificationStore = useNotificationStore();

const notifications = computed(() => {
  return notificationStore.notifications.filter((x) => x.status !== 'READ');
});

// 제목
const notificationTitle = (type) => {
  const map = {
    LIKE: '좋아요 알림',

    COMMENT: '댓글 알림',

    SETTLEMENT_REQUEST: '정산 요청',

    SETTLEMENT_REMIND: '정산 요청',

    SETTLEMENT_COMPLETE: '정산 완료',

    SETTLEMENT_CANCEL: '정산 취소',
    SETTLEMENT_CANCEL_REFUND: '정산 취소',

    SETTLEMENT_PAYMENT: '정산 결제 완료',

    FRIEND_REQUEST: '친구 요청',

    FRIEND_ACCEPT: '친구 수락',
  };

  return map[type] ?? '새로운 알림';
};

// 내용
const notificationMessage = (type) => {
  const map = {
    LIKE: '님이 피드에 좋아요를 눌렀습니다.',

    COMMENT: '님이 피드에 댓글을 남겼습니다.',

    SETTLEMENT_REQUEST: '님이 정산 요청을 보냈습니다.',

    SETTLEMENT_REMIND: '님의 정산 요청 시간 얼마 남지 않았어요',

    SETTLEMENT_COMPLETE: '님의 정산이 완료되었습니다.',

    SETTLEMENT_CANCEL: '님의 정산 요청이 취소되었습니다.',

    SETTLEMENT_CANCEL_REFUND:
      '님의 정산 요청이 취소되어 지갑으로 반환되었습니다.',

    SETTLEMENT_PAYMENT: '님이 정산금을 지불했습니다.',

    FRIEND_REQUEST: '님이 친구 요청을 보냈습니다.',

    FRIEND_ACCEPT: '님과 친구가 되었습니다.',
  };

  return map[type] ?? '새로운 알림이 있습니다.';
};

// 알림 클릭
const onclickRead = async (notification) => {
  try {
    // 읽음 처리
    await notificationStore.read({
      notificationId: notification.notificationId,

      userId,
    });

    const type = notification.notificationType;

    let routerPath = null;

    switch (type) {
      // 좋아요 / 댓글
      case 'LIKE':
        routerPath = `/mypage`;
      case 'COMMENT':
        // 피드 상세 이동
        routerPath = `/mypage`;

        break;

      // 친구 요청
      case 'FRIEND_REQUEST':
        routerPath = '/friends?tab=request';

        break;

      // 친구 수락
      case 'FRIEND_ACCEPT':
        routerPath = '/friends?tab=list';

        break;

      // 정산
      case 'SETTLEMENT_REQUEST':
      case 'SETTLEMENT_CANCEL_REFUND':
      case 'SETTLEMENT_PAYMENT':
      case 'SETTLEMENT_REMIND':
      case 'SETTLEMENT_COMPLETE':
        routerPath = '/settlement';

        break;

      // 정산 취소
      case 'SETTLEMENT_CANCEL':
        router.push({
          path: '/mypage',

          query: {
            tab: 'wallet',
          },
        });

        return;

      default:
        return;
    }

    if (routerPath) {
      router.push(routerPath);
    }
  } catch (error) {
    console.log(error);
  }
};

// 전체 읽음
const onclickReadAll = () => {
  notificationStore.readAll({
    userId,
  });
};

const onclickDelete = (notification) => {
  notificationStore.read({
    notificationId: notification.notificationId,
    userId,
  });
};

onMounted(() => {
  notificationStore.getList({
    userId,
  });
});
</script>

<style scoped>
.notification-page {
  width: 100%;
  min-height: 100%;
  padding: 0 20px 30px;
  background: #ffffff;
}

.notification-content {
  margin-top: 20px;
}

/* 모두 읽음 */
.notification-header {
  display: flex;

  justify-content: flex-end;

  margin-bottom: 18px;
}

.read-all {
  border: none;

  background: transparent;

  color: #666;

  font-size: 14px;

  cursor: pointer;
}

/* 리스트 */
.notification-list {
  display: flex;

  flex-direction: column;

  gap: 14px;
}

/* 카드 */
.notification-card {
  display: flex;

  align-items: center;

  position: relative;

  padding: 16px;

  background: white;

  border-radius: 16px;

  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);

  cursor: pointer;

  transition: 0.2s;
}

.notification-card:hover {
  transform: translateY(-2px);
}

/* 프로필 */
.profile-img {
  width: 52px;

  height: 52px;

  border-radius: 50%;

  object-fit: cover;

  margin-right: 14px;
}

.body {
  flex: 1;
}

.title {
  font-size: 15px;

  font-weight: 700;
}

.message {
  margin-top: 5px;

  font-size: 14px;

  color: #555;
}

.message strong {
  color: #222;
}

.date {
  margin-top: 8px;

  font-size: 12px;

  color: #999;
}

/* 안읽음 점 */
.dot {
  position: absolute;

  top: 16px;

  right: 16px;

  width: 8px;

  height: 8px;

  border-radius: 50%;

  background: #ffcc00;
}

.delete-btn {
  position: absolute;

  right: 16px;
  bottom: 14px;

  border: none;

  background: transparent;

  color: #999;

  font-size: 20px;

  line-height: 1;

  cursor: pointer;

  padding: 0;

  transition: 0.2s;
}

.delete-btn:hover {
  color: #ff5555;
  transform: scale(1.1);
}

.empty {
  text-align: center;

  color: #999;

  padding: 30px 0;

  font-size: 14px;
}
</style>
