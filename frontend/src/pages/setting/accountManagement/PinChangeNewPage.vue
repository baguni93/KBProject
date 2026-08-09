<template>
  <div class="pin-page">
    <main class="pin-container">
      <button class="back-button" type="button" @click="goBack">
        &lt;
      </button>

      <header class="pin-header">
        <div class="step-area">
          <span class="step complete"></span>
          <span class="step-line"></span>
          <span class="step active"></span>
          <span class="step-line"></span>
          <span class="step"></span>
        </div>

        <h1>새 간편비밀번호 설정</h1>

        <p>
          새로 사용할 숫자 6자리를<br />
          입력해 주세요.
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
              filled: pinPassword.length >= index,
              active: pinPassword.length === index - 1 && !errorMessage,
            }"
              class="pin-box"
          >
            <span v-if="pinPassword.length >= index" class="pin-dot"></span>
          </div>

          <input
              ref="pinInput"
              :value="pinPassword"
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

        <p v-else class="security-message">
          생년월일, 반복되거나 연속된 숫자는 사용할 수 없어요.
        </p>
      </section>

      <button
          class="next-button"
          :disabled="pinPassword.length !== 6 || loading"
          type="button"
          @click="next"
      >
        {{ loading ? '확인 중...' : '다음' }}
      </button>
    </main>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo } from '@/api/userApi';
import { useAuthStore } from '@/stores/auth';
import { validatePin } from '@/util/pinValidation';

const router = useRouter();
const authStore = useAuthStore();

const pinInput = ref(null);
const pinPassword = ref('');
const birthDate = ref('');
const errorMessage = ref('');
const loading = ref(false);

// PIN 입력창 포커스
const focusPinInput = async () => {
  if (loading.value) return;

  await nextTick();
  pinInput.value?.focus();
};

// PIN 입력
const changePin = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 6);

  pinPassword.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) event.target.value = value;
};

// 회원 생년월일 조회
const loadUserBirthDate = async () => {
  if (!authStore.userId) {
    await router.replace('/intro');
    return;
  }

  try {
    loading.value = true;

    const data = await getUserInfo(authStore.userId);

    birthDate.value = data.birthDate || '';
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '회원정보를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 새 PIN 검증
const next = async () => {
  if (pinPassword.value.length !== 6 || loading.value) return;

  const currentPin = sessionStorage.getItem('pinChangeCurrentPin');

  if (!currentPin) {
    await router.replace('/setting/account-management/pin');
    return;
  }

  if (pinPassword.value === currentPin) {
    pinPassword.value = '';
    errorMessage.value = '현재 간편비밀번호와 다른 번호를 입력해 주세요.';
    await focusPinInput();
    return;
  }

  const result = validatePin(pinPassword.value, birthDate.value);

  if (!result.valid) {
    pinPassword.value = '';
    errorMessage.value = result.message;
    await focusPinInput();
    return;
  }

  sessionStorage.setItem('pinChangeNewPin', pinPassword.value);
  await router.push('/setting/account-management/pin/confirm');
};

// 이전 화면
const goBack = () => {
  sessionStorage.removeItem('pinChangeNewPin');
  router.back();
};

onMounted(async () => {
  const currentPin = sessionStorage.getItem('pinChangeCurrentPin');

  if (!currentPin) {
    await router.replace('/setting/account-management/pin');
    return;
  }

  sessionStorage.removeItem('pinChangeNewPin');
  await loadUserBirthDate();
  await focusPinInput();
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

.step.active {
  width: 44px;
  height: 12px;
  border-radius: 999px;
  background: #ffbc2e;
}

.step.complete {
  background: #dddddd;
}

.step-line {
  width: 38px;
  height: 1px;
  margin: 0 8px;
  background: #dddddd;
}

.step-line.active {
  background: #ffbc2e;
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
.security-message {
  min-height: 40px;
  margin: 18px 0 0;
  font-size: 13px;
  line-height: 1.5;
  text-align: center;
}

.error-message {
  color: #e53935;
}

.security-message {
  color: #999999;
}

.next-button {
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

.next-button:active:not(:disabled) {
  background: #f2aa10;
}

.next-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #aaaaaa;
  cursor: not-allowed;
}

@media (max-width: 360px) {
  .pin-container {
    padding-right: 20px;
    padding-left: 20px;
  }

  .next-button {
    right: 20px;
    left: 20px;
  }

  .step-line {
    width: 30px;
  }
}
</style>