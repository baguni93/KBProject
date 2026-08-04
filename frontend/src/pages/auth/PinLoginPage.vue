<template>
  <div class="login-page">
    <main class="login-container">
      <button class="back-button" type="button" @click="goBack">&lt;</button>

      <header class="login-header">
        <div class="login-icon">
          <span>₩</span>
        </div>

        <h1>간편 로그인</h1>
        <p>간편비밀번호를 입력해 주세요.</p>
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
              autocomplete="off"
              @input="changePin"
          />
        </div>

        <button class="forgot-button" type="button" @click="goPinReset">
          간편비밀번호를 잊으셨나요?
        </button>

        <p v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </p>
      </section>

      <section class="security-area">
        <div class="security-icon">✓</div>

        <div>
          <strong>안전하게 보호되고 있어요</strong>
          <p>입력한 간편비밀번호는 화면에 표시되지 않아요.</p>
        </div>
      </section>

      <button
          class="login-button"
          :disabled="pinPassword.length !== 6 || loading"
          type="button"
          @click="login"
      >
        {{ loading ? '로그인 중...' : '로그인' }}
      </button>

      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <span>로그인하고 있어요.</span>
      </div>
    </main>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import loginApi from '@/api/loginApi';
import { useSignupStore } from '@/stores/signup';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const signupStore = useSignupStore();
const authStore = useAuthStore();

const pinInput = ref(null);
const pinPassword = ref('');
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

// 로그인
const login = async () => {
  const phoneNumber = sessionStorage.getItem('pinLoginPhoneNumber');

  if (pinPassword.value.length !== 6) {
    errorMessage.value = '간편비밀번호 6자리를 입력해 주세요.';
    await focusPinInput();
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    await authStore.login({ phoneNumber, pinPassword: pinPassword.value });

    sessionStorage.removeItem('pinLoginPhoneNumber');

    signupStore.reset();

    router.replace('/');
  } catch (error) {
    console.error(error);
    pinPassword.value = '';
    errorMessage.value = '간편비밀번호가 일치하지 않습니다.';

    await focusPinInput();
  } finally {
    loading.value = false;
  }
};

// PIN 재설정 본인인증 시작
const goPinReset = () => {
  signupStore.setVerificationPurpose('PIN_RESET');
  router.push('/signup/check');
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(() => {
  focusPinInput();
});

// PIN 6자리 입력 시 로그인
watch(pinPassword, (value) => {
  if (value.length !== 6 || loading.value) return;

  login();
});
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  padding: 24px 0;
  background: #f4f4f4;
  overflow: auto;
}

.login-container {
  position: relative;
  display: flex;
  flex: none;
  flex-direction: column;
  width: 390px;
  height: 844px;
  padding: 26px 28px 30px;
  background: #ffffff;
  overflow: hidden;
}

.back-button {
  align-self: flex-start;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.login-header {
  margin-top: 58px;
  text-align: center;
}

.login-icon {
  display: flex;
  width: 68px;
  height: 68px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  border-radius: 22px;
  background: linear-gradient(145deg, #ffc744, #ffb00f);
  box-shadow: 0 14px 28px rgba(255, 181, 22, 0.25);
  color: #ffffff;
  font-size: 34px;
  font-weight: 800;
}

.login-header h1 {
  margin: 0;
  color: #111111;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.6px;
}

.login-header p {
  margin: 16px 0 0;
  color: #777777;
  font-size: 16px;
  font-weight: 500;
  line-height: 1.55;
}

.pin-section {
  margin-top: 52px;
  text-align: center;
}

.pin-boxes {
  position: relative;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
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
  transition: 0.2s;
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

.forgot-button {
  margin-top: 25px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #444444;
  font-size: 14px;
  font-weight: 600;
  text-decoration: underline;
  cursor: pointer;
}

.error-message {
  margin: 18px 0 0;
  color: #e53935;
  font-size: 14px;
  line-height: 1.5;
}

.security-area {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-top: 46px;
  padding: 16px;
  border-radius: 14px;
  background: #fff9ea;
}

.security-icon {
  display: flex;
  flex: none;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffbc2e;
  color: #ffffff;
  font-size: 18px;
  font-weight: 800;
}

.security-area strong {
  display: block;
  color: #222222;
  font-size: 14px;
  font-weight: 700;
}

.security-area p {
  margin: 5px 0 0;
  color: #888888;
  font-size: 11px;
  line-height: 1.4;
}

.login-button {
  width: 100%;
  height: 58px;
  margin-top: auto;
  border: 1px solid #cc9200;
  border-radius: 12px;
  background: #ffbc2e;
  color: #111111;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
}

.login-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #aaaaaa;
  cursor: not-allowed;
}

.loading-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.84);
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

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>