<template>
  <div class="notification-page">
    <main class="notification-container">
      <header class="page-header">
        <button
            class="back-button"
            type="button"
            aria-label="이전 화면"
            @click="goBack"
        >
          &lt;
        </button>

        <h1>알림 설정</h1>

        <div class="header-empty"></div>
      </header>

      <section class="title-section">
        <div class="title-text">
          <h2>
            원하는 알림만<br />
            선택해서 받아보세요
          </h2>

          <p>
            중요한 알림은 놓치지 않고,<br />
            불필요한 알림은 줄여드려요.
          </p>
        </div>

        <div class="bell-icon">
          🔔
        </div>
      </section>

      <section v-if="loading" class="state-area">
        <div class="loading-spinner"></div>
        <p>알림 설정을 불러오고 있어요.</p>
      </section>

      <section v-else-if="loadErrorMessage" class="state-area error">
        <p>{{ loadErrorMessage }}</p>

        <button type="button" @click="loadNotificationSetting">
          다시 시도
        </button>
      </section>

      <template v-else>
        <section class="setting-list">
          <article
              v-for="item in notificationItems"
              :key="item.key"
              class="setting-item"
          >
            <div class="item-icon">
              {{ item.icon }}
            </div>

            <div class="item-content">
              <strong>{{ item.title }}</strong>
              <p>{{ item.description }}</p>
            </div>

            <button
                :class="{ active: isEnabled(item.key) }"
                :disabled="savingKey === item.key"
                class="toggle-button"
                type="button"
                :aria-label="`${item.title} ${isEnabled(item.key) ? '끄기' : '켜기'}`"
                :aria-pressed="isEnabled(item.key)"
                @click="toggleNotification(item.key)"
            >
              <span class="toggle-circle"></span>
            </button>
          </article>
        </section>

        <section class="notice-area">
          <div class="notice-icon">
            i
          </div>

          <p>
            알림 설정은 서비스 내 알림 수신 여부에 사용돼요.<br />
            실제 알림 발송 기능은 별도 기능에서 처리돼요.
          </p>
        </section>

        <p v-if="saveMessage" class="save-message">
          {{ saveMessage }}
        </p>

        <p v-if="saveErrorMessage" class="save-error-message">
          {{ saveErrorMessage }}
        </p>
      </template>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getNotificationSetting, updateNotificationSetting } from '@/api/notificationSettingApi';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const notificationSetting = reactive({
  financeNotificationYn: 'N',
  friendNotificationYn: 'N',
  rewardNotificationYn: 'N',
  eventNotificationYn: 'N',
});

const loading = ref(false);
const savingKey = ref('');
const loadErrorMessage = ref('');
const saveMessage = ref('');
const saveErrorMessage = ref('');
let messageTimer = null;

// 알림 항목
const notificationItems = [
  {
    key: 'financeNotificationYn',
    icon: '🏦',
    title: '금융 알림',
    description: '입출금, 이체, 결제, 한도 변경 등 금융 거래 관련 알림을 받아요.',
  },
  {
    key: 'friendNotificationYn',
    icon: '🧑‍🤝‍🧑',
    title: '친구 요청 알림',
    description: '친구 요청과 수락 관련 알림을 받아요.',
  },
  {
    key: 'rewardNotificationYn',
    icon: '🎁',
    title: '리워드 알림',
    description: '포인트 적립, 사용, 만료 예정 등 리워드 관련 알림을 받아요.',
  },
  {
    key: 'eventNotificationYn',
    icon: '📢',
    title: '이벤트·혜택 알림',
    description: '이벤트, 제휴 혜택, 프로모션 등 다양한 소식을 받아요.',
  },
];

// 알림 활성화 여부
const isEnabled = (key) => {
  return notificationSetting[key] === 'Y';
};

// 알림 설정 조회
const loadNotificationSetting = async () => {
  if (!authStore.userId) {
    await router.replace('/intro');
    return;
  }

  try {
    loading.value = true;
    loadErrorMessage.value = '';

    const data = await getNotificationSetting(authStore.userId);

    notificationSetting.financeNotificationYn = data.financeNotificationYn || 'N';
    notificationSetting.friendNotificationYn = data.friendNotificationYn || 'N';
    notificationSetting.rewardNotificationYn = data.rewardNotificationYn || 'N';
    notificationSetting.eventNotificationYn = data.eventNotificationYn || 'N';
  } catch (error) {
    console.error(error);

    loadErrorMessage.value =
        error.response?.data?.message
        || error.error
        || '알림 설정을 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 알림 설정 토글
const toggleNotification = async (key) => {
  if (savingKey.value) return;

  const previousValue = notificationSetting[key];
  const changedValue = previousValue === 'Y' ? 'N' : 'Y';

  notificationSetting[key] = changedValue;

  try {
    savingKey.value = key;
    saveErrorMessage.value = '';

    await updateNotificationSetting(authStore.userId, {
      financeNotificationYn: notificationSetting.financeNotificationYn,
      friendNotificationYn: notificationSetting.friendNotificationYn,
      rewardNotificationYn: notificationSetting.rewardNotificationYn,
      eventNotificationYn: notificationSetting.eventNotificationYn,
    });

    showSaveMessage('알림 설정이 변경되었습니다.');
  } catch (error) {
    console.error(error);

    // 저장 실패 시 이전 값으로 복구
    notificationSetting[key] = previousValue;

    saveErrorMessage.value =
        error.response?.data?.message
        || error.error
        || '알림 설정 변경에 실패했습니다.';
  } finally {
    savingKey.value = '';
  }
};

// 저장 완료 메시지
const showSaveMessage = (message) => {
  saveMessage.value = message;

  if (messageTimer) window.clearTimeout(messageTimer);

  messageTimer = window.setTimeout(() => {
    saveMessage.value = '';
  }, 1800);
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(loadNotificationSetting);
</script>

<style scoped>
.notification-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.notification-container {
  position: relative;
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  padding: 10px 28px 30px;
  background: #ffffff;
  box-sizing: border-box;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.notification-container::-webkit-scrollbar {
  display: none;
}

.page-header {
  display: grid;
  grid-template-columns: 38px 1fr 38px;
  min-height: 44px;
  align-items: center;
  flex-shrink: 0;
}

.page-header h1 {
  margin: 0;
  color: #222222;
  font-size: 17px;
  font-weight: 700;
  text-align: center;
}

.back-button {
  justify-self: start;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 27px;
  line-height: 1;
  cursor: pointer;
}

.header-empty {
  width: 38px;
}

.title-section {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  margin-top: 38px;
}

.title-text {
  min-width: 0;
}

.title-text h2 {
  margin: 0;
  color: #111111;
  font-size: 25px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.title-text p {
  margin: 14px 0 0;
  color: #888888;
  font-size: 13px;
  line-height: 1.55;
}

.bell-icon {
  display: flex;
  flex: none;
  width: 82px;
  height: 82px;
  align-items: center;
  justify-content: center;
  margin-left: 18px;
  border-radius: 28px;
  background: #fff8e5;
  box-shadow: 0 12px 26px rgba(255, 188, 46, 0.14);
  font-size: 42px;
  transform: rotate(-8deg);
}

.setting-list {
  flex-shrink: 0;
  margin-top: 44px;
  border: 1px solid #e5e5e5;
  border-radius: 16px;
  background: #ffffff;
  overflow: hidden;
}

.setting-item {
  display: flex;
  min-height: 88px;
  align-items: center;
  gap: 13px;
  padding: 15px 16px;
  border-bottom: 1px solid #eeeeee;
}

.setting-item:last-child {
  border-bottom: 0;
}

.item-icon {
  display: flex;
  flex: none;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;
  font-size: 25px;
}

.item-content {
  min-width: 0;
  flex: 1;
}

.item-content strong {
  display: block;
  color: #222222;
  font-size: 15px;
  font-weight: 800;
}

.item-content p {
  margin: 6px 0 0;
  color: #888888;
  font-size: 11px;
  line-height: 1.45;
  word-break: keep-all;
}

.toggle-button {
  position: relative;
  flex: none;
  width: 52px;
  height: 30px;
  padding: 0;
  border: 0;
  border-radius: 18px;
  background: #d8d9de;
  cursor: pointer;
  transition: background 0.2s;
}

.toggle-button.active {
  background: #ffb800;
}

.toggle-button:disabled {
  cursor: wait;
  opacity: 0.65;
}

.toggle-circle {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.16);
  transition: transform 0.2s;
}

.toggle-button.active .toggle-circle {
  transform: translateX(22px);
}

.notice-area {
  display: flex;
  flex-shrink: 0;
  align-items: flex-start;
  gap: 12px;
  margin-top: 20px;
  padding: 14px 15px;
  border-radius: 13px;
  background: #faf8f4;
}

.notice-icon {
  display: flex;
  flex: none;
  width: 26px;
  height: 26px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #f3ead8;
  color: #777777;
  font-size: 13px;
  font-weight: 700;
}

.notice-area p {
  margin: 0;
  color: #999999;
  font-size: 10px;
  line-height: 1.55;
}

.state-area {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: #777777;
  font-size: 13px;
  text-align: center;
}

.state-area p {
  margin: 0;
}

.state-area.error {
  color: #e53935;
}

.state-area button {
  height: 40px;
  padding: 0 18px;
  border: 1px solid #cc9200;
  border-radius: 10px;
  background: #ffbc2e;
  color: #111111;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.loading-spinner {
  width: 34px;
  height: 34px;
  border: 4px solid #eeeeee;
  border-top-color: #ffbc2e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.save-message,
.save-error-message {
  position: fixed;
  z-index: 20;
  right: 28px;
  bottom: 30px;
  left: 28px;
  max-width: 374px;
  margin: 0 auto;
  padding: 14px 16px;
  border-radius: 12px;
  box-sizing: border-box;
  font-size: 12px;
  text-align: center;
}

.save-message {
  background: rgba(34, 34, 34, 0.92);
  color: #ffffff;
}

.save-error-message {
  background: #fff0f0;
  color: #e53935;
}

@media (max-width: 360px) {
  .notification-container {
    padding-right: 20px;
    padding-left: 20px;
  }

  .title-section {
    align-items: flex-start;
  }

  .bell-icon {
    width: 68px;
    height: 68px;
    margin-left: 12px;
    border-radius: 23px;
    font-size: 34px;
  }

  .setting-item {
    padding-right: 13px;
    padding-left: 13px;
  }

  .save-message,
  .save-error-message {
    right: 20px;
    left: 20px;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>