<template>
  <div class="pin-page">
    <main class="pin-container">
      <button
          class="back-button"
          type="button"
          @click="goBack"
      >
        &lt;
      </button>

      <header class="pin-header">
        <div class="step-area">
          <span class="step complete"></span>
          <span class="step-line active"></span>
          <span class="step active"></span>
        </div>

        <h1>새 간편비밀번호 확인</h1>

        <p>
          앞에서 입력한 간편비밀번호를<br />
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
              active:
                confirmPinPassword.length === index - 1
                && !errorMessage,
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
              @input="changePin"
          />
        </div>

        <p v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </p>

        <p v-else class="guide-message">
          앞에서 입력한 간편비밀번호를 다시 입력해주세요.
        </p>
      </section>

      <button
          class="confirm-button"
          type="button"
          :disabled="
          confirmPinPassword.length !== 6
          || loading
        "
          @click="resetPinPassword"
      >
        {{ loading ? '변경 중...' : '간편비밀번호 변경' }}
      </button>

      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner"></div>

        <span>간편비밀번호를 변경하고 있어요.</span>
      </div>
    </main>
  </div>
</template>

<script setup>
import {
  nextTick,
  onMounted,
  ref,
} from 'vue';
import { useRouter } from 'vue-router';
import { resetPin } from '@/api/userApi';

const router = useRouter();

const pinInput = ref(null);
const confirmPinPassword = ref('');
const errorMessage = ref('');
const loading = ref(false);

// PIN 입력창 포커스
const focusPinInput = async () => {
  if (loading.value) {
    return;
  }

  await nextTick();
  pinInput.value?.focus();
};

// PIN 입력
const changePin = (event) => {
  const value =
      event.target.value
          .replace(/[^0-9]/g, '')
          .slice(0, 6);

  confirmPinPassword.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) {
    event.target.value = value;
  }
};

// PIN 재설정
const resetPinPassword = async () => {
  const phoneNumber =
      sessionStorage.getItem(
          'pinResetPhoneNumber',
      );

  const newPinPassword =
      sessionStorage.getItem(
          'pinResetNewPin',
      );

  if (!phoneNumber || !newPinPassword) {
    await router.replace('/intro');
    return;
  }

  if (
      confirmPinPassword.value
      !== newPinPassword
  ) {
    confirmPinPassword.value = '';

    errorMessage.value =
        '간편비밀번호가 일치하지 않습니다.';

    await focusPinInput();
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    await resetPin({
      phoneNumber,
      newPinPassword,
      newPinPasswordConfirm:
      confirmPinPassword.value,
    });

    sessionStorage.removeItem(
        'pinResetNewPin',
    );

    sessionStorage.setItem(
        'pinResetCompleted',
        'true',
    );

    await router.replace(
        '/auth/pin-reset-complete',
    );
  } catch (error) {
    console.error(error);

    confirmPinPassword.value = '';

    errorMessage.value =
        error.response?.data?.message
        || '간편비밀번호 변경에 실패했습니다.';
  } finally {
    loading.value = false;

    if (errorMessage.value) {
      await focusPinInput();
    }
  }
};

// 이전 화면
const goBack = async () => {
  sessionStorage.removeItem(
      'pinResetNewPin',
  );

  await router.replace(
      '/auth/pin-reset',
  );
};

onMounted(() => {
  focusPinInput();
});
</script>

<style scoped>
.pin-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.pin-container {
  position: relative;
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  padding: 10px 28px 140px;
  background: #ffffff;
  box-sizing: border-box;
  overflow: hidden;
}

.back-button {
  align-self: flex-start;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 27px;
  line-height: 1;
  cursor: pointer;
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
  background: #dddddd;
}

.step.complete {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #dddddd;
}

.step.active {
  width: 44px;
  height: 12px;
  border-radius: 999px;
  background: #ffbc2e;
}

.step-line {
  width: 38px;
  height: 1px;
  margin: 0 8px;
  background: #dddddd;
}

.step-line.active {
  background: #dddddd;
}

.pin-header h1 {
  margin: 0;
  color: #111111;
  font-size: 25px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.pin-header p {
  margin: 16px 0 0;
  color: #777777;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.6;
}

.pin-section {
  margin-top: 58px;
  text-align: center;
}

.pin-boxes {
  position: relative;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 9px;
  width: 100%;
  cursor: text;
  outline: none;
}

.pin-box {
  display: flex;
  height: 54px;
  align-items: center;
  justify-content: center;
  border: 1px solid #dddddd;
  border-radius: 12px;
  background: #fafafa;
  box-sizing: border-box;
  transition:
      border-color 0.2s,
      background 0.2s,
      box-shadow 0.2s;
}

.pin-box.active {
  border-color: #ffbc2e;
  background: #fffaf0;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.pin-box.filled {
  border-color: #ffbc2e;
  background: #fff8e5;
}

.pin-boxes.error .pin-box {
  border-color: #e53935;
  background: #fff7f7;
  box-shadow: none;
}

.pin-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #222222;
}

.hidden-pin-input {
  position: absolute;
  width: 1px;
  height: 1px;
  border: 0;
  opacity: 0;
  pointer-events: none;
}

.error-message,
.guide-message {
  min-height: 40px;
  margin: 18px 0 0;
  font-size: 13px;
  line-height: 1.5;
  text-align: center;
}

.error-message {
  color: #e53935;
}

.guide-message {
  color: #999999;
}

.confirm-button {
  position: absolute;
  right: 28px;
  bottom: 58px;
  left: 28px;
  width: auto;
  height: 58px;
  margin: 0;
  border: 1px solid #cc9200;
  border-radius: 10px;
  background: #ffbc2e;
  color: #111111;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
}

.confirm-button:active:not(:disabled) {
  background: #f2aa10;
}

.confirm-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #aaaaaa;
  cursor: not-allowed;
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
  color: #333333;
  font-size: 15px;
  font-weight: 700;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 4px solid #eeeeee;
  border-top-color: #ffbc2e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@media (max-width: 360px) {
  .pin-container {
    padding-right: 20px;
    padding-left: 20px;
  }

  .confirm-button {
    right: 20px;
    left: 20px;
  }

  .step-line {
    width: 30px;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>