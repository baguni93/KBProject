<template>
  <header class="top-header">
    <div class="actions">
      <button class="icon-btn" @click="goNotification">
        <i class="fa-solid fa-bell"></i>

        <!-- 새 알림 표시 -->
        <span
          v-if="notificationStore.hasUnread"
          class="notification-dot"
        ></span>
      </button>

      <button class="icon-btn" @click="goSetting">
        <i class="fa-solid fa-gear"></i>
      </button>
    </div>
  </header>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useNotificationStore } from '@/stores/notification';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const userId = authStore.userId;
const router = useRouter();

const notificationStore = useNotificationStore();

const goNotification = () => {
  router.push('/notification');
};

const goSetting = () => {
  router.push('/setting');
};

onMounted(async () => {
  await notificationStore.getList({ userId });
});
</script>

<style scoped>
.top-header {
  height: 56px;

  display: flex;
  align-items: center;
  justify-content: space-between;

  padding: 0 20px;
}

.actions {
  display: flex;
  align-items: center;

  gap: 14px;

  margin-left: auto;
}

.icon-btn {
  width: 36px;
  height: 36px;

  display: flex;

  align-items: center;
  justify-content: center;

  position: relative;

  border: none;

  background: transparent;

  font-size: 20px;

  cursor: pointer;
}

.icon-btn:hover {
  background: #f5f5f5;

  border-radius: 50%;
}

/* 새 알림 점 */
.notification-dot {
  position: absolute;

  top: 5px;

  right: 5px;

  width: 8px;

  height: 8px;

  background: #ff3b30;

  border-radius: 50%;
}
</style>
