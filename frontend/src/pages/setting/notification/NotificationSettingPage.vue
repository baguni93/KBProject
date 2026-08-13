<template>
  <div class="page-layout notification-page">
    <PageHeader title="알림 설정" custom-back @back="goBack" />

    <main class="page-content notification-container">
      <section class="title-section">
        <div class="title-text">
          <h2 class="text-26-bold">
            원하는 알림만<br />
            선택해서 받아보세요
          </h2>

          <p class="text-15">
            중요한 알림은 놓치지 않고,<br />
            불필요한 알림은 줄여드려요.
          </p>
        </div>

        <div class="bell-icon">
          <i class="fa-solid fa-bell"></i>
        </div>
      </section>

      <section v-if="loading" class="state-area">
        <div class="loading-spinner"></div>
        <p class="text-13">알림 설정을 불러오고 있어요.</p>
      </section>

      <section v-else-if="loadErrorMessage" class="state-area error">
        <p class="text-13">{{ loadErrorMessage }}</p>

        <button class="content-btn primary retry-button" type="button" @click="loadNotificationSetting">
          다시 시도
        </button>
      </section>

      <template v-else>
        <section class="setting-list">
          <article v-for="item in notificationItems" :key="item.key" class="setting-item">
            <div :class="`item-icon ${item.iconClass}`">
              <i :class="item.icon"></i>
            </div>

            <div class="item-content">
              <strong class="text-15-bold">{{ item.title }}</strong>
              <p class="text-13">{{ item.description }}</p>
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
            <i class="fa-solid fa-circle-info"></i>
          </div>

          <p class="notice-text">
            알림 설정은 서비스 내 알림 수신 여부에 사용돼요.<br />
            실제 알림 발송 기능은 별도 기능에서 처리돼요.
          </p>
        </section>

        <Transition name="toast">
          <div v-if="saveMessage" class="save-message text-13" role="status">
            <div class="save-icon">
              <i class="fa-solid fa-check"></i>
            </div>

            <span>{{ saveMessage }}</span>
          </div>
        </Transition>

        <Transition name="toast">
          <div v-if="saveErrorMessage" class="save-error-message text-13" role="alert">
            <i class="fa-solid fa-circle-exclamation"></i>
            <span>{{ saveErrorMessage }}</span>
          </div>
        </Transition>
      </template>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getNotificationSetting, updateNotificationSetting } from '@/api/notificationSettingApi';
import PageHeader from '@/components/common/PageHeader.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const notificationSetting = reactive({ financeNotificationYn: 'N', friendNotificationYn: 'N', rewardNotificationYn: 'N', eventNotificationYn: 'N' });
const loading = ref(false);
const savingKey = ref('');
const loadErrorMessage = ref('');
const saveMessage = ref('');
const saveErrorMessage = ref('');
let messageTimer = null;

// 알림 항목
const notificationItems = [
  { key: 'financeNotificationYn', icon: 'fa-solid fa-building-columns', iconClass: 'finance', title: '금융 알림', description: '입출금, 이체, 결제, 한도 변경 등 금융 거래 관련 알림을 받아요.' },
  { key: 'friendNotificationYn', icon: 'fa-solid fa-user-group', iconClass: 'friend', title: '친구 요청 알림', description: '친구 요청과 수락 관련 알림을 받아요.' },
  { key: 'rewardNotificationYn', icon: 'fa-solid fa-gift', iconClass: 'reward', title: '리워드 알림', description: '포인트 적립, 사용, 만료 예정 등 리워드 관련 알림을 받아요.' },
  { key: 'eventNotificationYn', icon: 'fa-solid fa-bullhorn', iconClass: 'event', title: '이벤트·혜택 알림', description: '이벤트, 제휴 혜택, 프로모션 등 다양한 소식을 받아요.' },
];

// 알림 활성화 여부
const isEnabled = (key) => notificationSetting[key] === 'Y';

// 알림 설정 조회
const loadNotificationSetting = async () => {
  if (!authStore.userId) {
    await router.replace('/intro');
    return;
  }

  try {
    loading.value = true;
    loadErrorMessage.value = '';

    const data = await getNotificationSetting();

    notificationSetting.financeNotificationYn = data.financeNotificationYn || 'N';
    notificationSetting.friendNotificationYn = data.friendNotificationYn || 'N';
    notificationSetting.rewardNotificationYn = data.rewardNotificationYn || 'N';
    notificationSetting.eventNotificationYn = data.eventNotificationYn || 'N';
  } catch (error) {
    console.error(error);
    loadErrorMessage.value = error.response?.data?.message || error.error || '알림 설정을 불러오지 못했습니다.';
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

    await updateNotificationSetting({
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
    saveErrorMessage.value = error.response?.data?.message || error.error || '알림 설정 변경에 실패했습니다.';
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

// 설정 메인 화면
const goBack = () => {
  router.push('/setting');
};

onMounted(loadNotificationSetting);
</script>

<style scoped>
@import "@/components/common/common/common.css";

.notification-page {
  background: var(--color-bg-page);
}

.notification-container {
  position: relative;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.notification-container::-webkit-scrollbar {
  display: none;
}

/* 상단 */
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
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.title-text p {
  margin: 14px 0 0;
  color: var(--color-text-muted);
  line-height: 1.55;
}

.bell-icon {
  display: flex;
  flex: none;
  width: 72px;
  height: 72px;
  align-items: center;
  justify-content: center;
  margin-left: 18px;
  border-radius: 24px;
  background: #fff8e5;
  box-shadow: 0 10px 24px rgba(255, 188, 46, 0.13);
  color: var(--color-primary-active);
  font-size: 30px;
}

/* 알림 목록 */
.setting-list {
  flex-shrink: 0;
  margin-top: 40px;
  border: 1px solid var(--color-divider);
  border-radius: 18px;
  background: var(--color-bg-page);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.035);
  overflow: hidden;
}

.setting-item {
  display: flex;
  min-height: 94px;
  align-items: center;
  gap: 13px;
  padding: 16px;
  border-bottom: 1px solid var(--color-divider);
  box-sizing: border-box;
}

.setting-item:last-child {
  border-bottom: 0;
}

/* 알림 아이콘 */
.item-icon {
  display: flex;
  flex: none;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  border-radius: 13px;
  font-size: 17px;
}

.item-icon.finance {
  background: #fff6dc;
  color: #d99800;
}

.item-icon.friend {
  background: #eef5ff;
  color: #4f7fc8;
}

.item-icon.reward {
  background: #fff0f3;
  color: #dc647c;
}

.item-icon.event {
  background: #f3efff;
  color: #8067c7;
}

.item-content {
  min-width: 0;
  flex: 1;
}

.item-content strong {
  display: block;
  color: var(--color-text-main);
}

.item-content p {
  margin: 5px 0 0;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 400;
  line-height: 1.45;
  letter-spacing: -0.2px;
  word-break: keep-all;
}

/* 토글 */
.toggle-button {
  position: relative;
  flex: none;
  width: 52px;
  height: 30px;
  padding: 0;
  border: 0;
  border-radius: 18px;
  background: var(--color-border-main);
  cursor: pointer;
  transition: background-color 0.2s ease, opacity 0.2s ease;
}

.toggle-button.active {
  background: var(--color-primary);
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
  background: var(--color-bg-page);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.16);
  transition: transform 0.2s ease;
}

.toggle-button.active .toggle-circle {
  transform: translateX(22px);
}

/* 안내 */
.notice-area {
  display: flex;
  flex-shrink: 0;
  align-items: flex-start;
  gap: 11px;
  margin-top: 20px;
  padding: 14px 15px;
  border-radius: 13px;
  background: var(--color-bg-screen);
}

.notice-icon {
  display: flex;
  flex: none;
  width: 26px;
  height: 26px;
  align-items: center;
  justify-content: center;
  color: var(--color-text-sub);
  font-size: 15px;
}

.notice-area p {
  margin: 1px 0 0;
  color: var(--color-text-muted);
  line-height: 1.55;
}

.notice-text {
  margin: 1px 0 0;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 400;
  line-height: 1.55;
}

/* 상태 */
.state-area {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: var(--color-text-sub);
  text-align: center;
}

.state-area p {
  margin: 0;
}

.state-area.error {
  color: var(--color-error);
}

.retry-button {
  width: auto;
  min-width: 100px;
  padding: 0 18px;
}

.loading-spinner {
  width: 34px;
  height: 34px;
  border: 4px solid var(--color-bg-disabled);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* 저장 성공 토스트 */
.save-message {
  position: fixed;
  z-index: 20;
  right: 24px;
  bottom: 34px;
  left: 24px;
  display: flex;
  max-width: 374px;
  min-height: 44px;
  align-items: center;
  justify-content: flex-start;
  gap: 9px;
  margin: 0 auto;
  padding: 9px 14px;
  border: 1px solid rgba(31, 166, 75, 0.22);
  border-radius: 12px;
  background: rgba(31, 166, 75, 0.13);
  box-shadow: 0 8px 24px rgba(31, 166, 75, 0.12);
  color: #157a38;
  box-sizing: border-box;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.save-icon {
  display: flex;
  width: 22px;
  height: 22px;
  flex: none;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(31, 166, 75, 0.16);
  color: var(--color-success);
  font-size: 11px;
}

/* 저장 실패 토스트 */
.save-error-message {
  position: fixed;
  z-index: 20;
  right: 24px;
  bottom: 34px;
  left: 24px;
  display: flex;
  max-width: 374px;
  min-height: 44px;
  align-items: center;
  justify-content: flex-start;
  gap: 9px;
  margin: 0 auto;
  padding: 9px 18px;
  border: 1px solid rgba(229, 57, 53, 0.2);
  border-radius: 12px;
  background: rgba(229, 57, 53, 0.1);
  box-shadow: 0 8px 24px rgba(229, 57, 53, 0.1);
  color: var(--color-error);
  box-sizing: border-box;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

/* 토스트 애니메이션 */
.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 360px) {
  .title-section {
    align-items: flex-start;
  }

  .bell-icon {
    width: 62px;
    height: 62px;
    margin-left: 12px;
    border-radius: 20px;
    font-size: 26px;
  }

  .setting-item {
    padding-right: 13px;
    padding-left: 13px;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>