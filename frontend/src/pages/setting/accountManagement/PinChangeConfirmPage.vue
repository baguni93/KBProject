<template>
  <div class="page-layout pin-page">
    <PageHeader title="간편비밀번호 변경" custom-back @back="goBack" />

    <main class="page-content pin-container">
      <header class="pin-header">
        <div class="step-area">
          <span class="step complete"></span>
          <span class="step-line complete"></span>
          <span class="step complete"></span>
          <span class="step-line complete"></span>
          <span class="step active"></span>
        </div>

        <h1 class="text-26-bold">새 간편비밀번호 확인</h1>

        <p class="text-15">
          앞에서 입력한 숫자 6자리를<br />
          한 번 더 입력해 주세요.
        </p>
      </header>

      <section class="pin-section">
        <div
          :class="{ error: !!errorMessage }"
          class="pin-boxes"
          role="button"
          tabindex="0"
          @click="focusPinInput"
          @keydown.enter="focusPinInput"
        >
          <div
            v-for="index in 6"
            :key="index"
            :class="{
              filled: confirmPinPassword.length >= index,
              active: confirmPinPassword.length === index - 1 && !errorMessage,
            }"
            class="pin-box"
          >
            <span
              v-if="confirmPinPassword.length >= index"
              class="pin-dot"
            ></span>
          </div>

          <input
            ref="pinInput"
            :value="confirmPinPassword"
            class="hidden-pin-input"
            inputmode="numeric"
            maxlength="6"
            pattern="[0-9]*"
            type="password"
            autocomplete="new-password"
            @input="handlePinInput"
          />
        </div>

        <p v-if="errorMessage" class="error-message text-13">
          {{ errorMessage }}
        </p>

        <p v-else class="security-message">
          앞에서 입력한 간편비밀번호와 동일하게 입력해 주세요.
        </p>
      </section>
    </main>

    <div class="bottom-btn-area single">
      <button
        class="bottom-btn"
        :disabled="confirmPinPassword.length !== 6 || loading"
        type="button"
        @click="submitPinChange"
      >
        {{ loading ? '변경 중...' : '간편비밀번호 변경' }}
      </button>
    </div>

    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <span class="text-15-bold">간편비밀번호를 변경하고 있어요.</span>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { changePin } from '@/api/userApi';
import PageHeader from '@/components/common/PageHeader.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const pinInput = ref(null);
const confirmPinPassword = ref('');
const errorMessage = ref('');
const loading = ref(false);

// PIN 입력창 포커스
const focusPinInput = async () => {
  if (loading.value) return;

  await nextTick();
  pinInput.value?.focus();
};

// PIN 입력
const handlePinInput = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 6);

  confirmPinPassword.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) event.target.value = value;
};

// PIN 변경
const submitPinChange = async () => {
  const currentPinPassword = sessionStorage.getItem('pinChangeCurrentPin');
  const newPinPassword = sessionStorage.getItem('pinChangeNewPin');

  if (!authStore.userId || !currentPinPassword || !newPinPassword) {
    await router.replace('/setting/account-management/pin');
    return;
  }

  if (confirmPinPassword.value !== newPinPassword) {
    confirmPinPassword.value = '';
    errorMessage.value = '새 간편비밀번호가 일치하지 않습니다.';
    await focusPinInput();
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    await changePin({
      currentPinPassword,
      newPinPassword,
      newPinPasswordConfirm: confirmPinPassword.value,
    });

    sessionStorage.removeItem('pinChangeCurrentPin');
    sessionStorage.removeItem('pinChangeNewPin');

    await router.replace({
      path: '/setting/account-management/complete',
      query: { type: 'PIN_CHANGE' },
    });
  } catch (error) {
    console.error(error);

    confirmPinPassword.value = '';
    errorMessage.value =
      error.response?.data?.message || '간편비밀번호 변경에 실패했습니다.';

    await focusPinInput();
  } finally {
    loading.value = false;
  }
};

// 이전 화면
const goBack = () => {
  sessionStorage.removeItem('pinChangeNewPin');
  router.replace('/setting/account-management/pin/new');
};

onMounted(async () => {
  const currentPinPassword = sessionStorage.getItem('pinChangeCurrentPin');
  const newPinPassword = sessionStorage.getItem('pinChangeNewPin');

  if (!currentPinPassword || !newPinPassword) {
    await router.replace('/setting/account-management/pin');
    return;
  }

  await focusPinInput();
});
</script>

<style scoped>
@import '@/components/common/common/common.css';

.pin-page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  background: var(--color-bg-page);
}

.pin-container {
  overflow: hidden;
}

.pin-header {
  margin-top: 38px;
}

.step-area {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
}

.step {
  flex-shrink: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-border-main);
}

.step.complete {
  background: var(--color-primary);
}

.step.active {
  width: 44px;
  height: 12px;
  border-radius: 999px;
  background: var(--color-primary);
}

.step-line {
  width: 38px;
  height: 1px;
  margin: 0 8px;
  background: var(--color-border-main);
}

.step-line.complete {
  background: var(--color-primary);
}

.pin-header h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.pin-header p {
  margin: 16px 0 0;
  color: var(--color-text-sub);
  line-height: 1.6;
}

.pin-section {
  margin-top: 58px;
  text-align: center;
}

.pin-boxes {
  position: relative;
  display: grid;
  width: 100%;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 9px;
  cursor: text;
  outline: none;
}

.pin-box {
  display: flex;
  height: 54px;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border-main);
  border-radius: 12px;
  background: var(--color-bg-screen);
  box-sizing: border-box;
  transition:
    border-color 0.2s,
    background 0.2s,
    box-shadow 0.2s;
}

.pin-box.active {
  border-color: var(--color-primary);
  background: #fffaf0;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.pin-box.filled {
  border-color: var(--color-primary);
  background: #fff8e5;
}

.pin-boxes.error .pin-box {
  border-color: var(--color-error);
  background: var(--color-bg-page);
  box-shadow: 0 0 0 3px rgba(229, 57, 53, 0.08);
}

.pin-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: var(--color-text-main);
}

.hidden-pin-input {
  position: absolute;
  width: 1px;
  height: 1px;
  border: 0;
  opacity: 0;
  pointer-events: none;
}

.error-message {
  min-height: 40px;
  margin: 10px 0 0;
  color: var(--color-error);
  line-height: 1.4;
  text-align: center;
}

.security-message {
  min-height: 40px;
  margin: 18px 0 0;
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 400;
  line-height: 1.5;
  text-align: center;
}

.loading-overlay {
  position: absolute;
  z-index: 10;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.86);
  color: var(--color-text-main);
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 4px solid var(--color-bg-disabled);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@media (max-width: 360px) {
  .step-line {
    width: 30px;
  }

  .pin-boxes {
    gap: 6px;
  }

  .pin-box {
    height: 50px;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
