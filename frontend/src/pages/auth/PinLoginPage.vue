<template>
  <div class="login-page animate-fade-in-up">
    <main class="login-container shadow-2xl">
      <button class="back-button" type="button" @click="goBack">
        <i class="bi bi-chevron-left fs-4 text-dark"></i>
      </button>

      <header class="login-header">
        <div class="login-icon pulse-glow">
          <span class="font-outfit">KB</span>
        </div>

        <h2 class="fw-extrabold text-dark font-outfit">KB Pay 간편 로그인</h2>
        <p class="text-secondary small font-outfit">등록하신 6자리 간편비밀번호를 입력해주세요.</p>
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
              class="pin-box shadow-sm"
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

        <button class="forgot-button font-outfit" type="button" @click="goPinReset">
          간편비밀번호를 잊으셨나요?
        </button>

        <p v-if="errorMessage" class="error-message fw-bold">
          {{ errorMessage }}
        </p>
      </section>

      <section class="security-area border-0 shadow-sm">
        <div class="security-icon">
          <i class="bi bi-shield-lock-fill"></i>
        </div>

        <div>
          <strong class="font-outfit">금융종단간 암호화 세션 보호</strong>
          <p>입력하시는 비밀번호는 이중 보안 처리되어 안전합니다.</p>
        </div>
      </section>

      <button
          class="login-button font-outfit shadow-md"
          :disabled="pinPassword.length !== 6 || loading"
          type="button"
          @click="login"
      >
        {{ loading ? '보안 인증 확인 중...' : '로그인' }}
      </button>

      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <span class="fw-bold font-outfit">안전하게 로그인하고 있습니다...</span>
      </div>
    </main>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo } from '@/api/userApi';
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

  if (!phoneNumber) {
    errorMessage.value = '휴대폰 본인인증 정보가 없습니다.';
    return;
  }

  if (pinPassword.value.length !== 6) {
    errorMessage.value = '간편비밀번호 6자리를 입력해 주세요.';
    await focusPinInput();
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    await authStore.login({
      phoneNumber,
      pinPassword: pinPassword.value,
    });

    const userInfo = await getUserInfo(authStore.userId);

    authStore.setUserName(userInfo.userName);

    sessionStorage.removeItem('pinLoginPhoneNumber');
    signupStore.reset();

    // router.replace('/wallet');
    await router.replace('/wallet');
  } catch (error) {
    console.error(error);

    pinPassword.value = '';
    errorMessage.value = error.response?.data?.message || '간편비밀번호가 일치하지 않습니다.';

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
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.login-container {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
  padding: 10px 28px 30px;
  background: #ffffff;
  box-sizing: border-box;
}

.back-button {
  align-self: flex-start;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.login-header {
  margin-top: 42px;
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
  background: linear-gradient(135deg, #ffbc00 0%, #ff9900 100%);
  box-shadow: 0 14px 28px rgba(255, 188, 0, 0.25);
  color: #111111;
  font-size: 26px;
  font-weight: 800;
}

.login-header h2 {
  margin: 0;
  color: #111111;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.login-header p {
  margin: 14px 0 0;
  color: #777777;
  font-size: 15px;
  line-height: 1.6;
}

.pin-section {
  margin-top: 54px;
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
  transition: .2s;
}

.pin-box.active {
  border-color: #ffbc2e;
  background: #fffaf0;
  box-shadow: 0 0 0 3px rgba(255,188,46,.12);
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
  margin-top: 24px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #666666;
  font-size: 13px;
  font-weight: 600;
  text-decoration: underline;
  cursor: pointer;
}

.error-message {
  min-height: 22px;
  margin: 18px 0 0;
  color: #e53935;
  font-size: 13px;
  line-height: 1.5;
}

.security-area {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 48px;
  padding: 18px;
  border-radius: 14px;
  background: #fff9e9;
}

.security-icon {
  display: flex;
  flex: none;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffbc2e;
  color: #ffffff;
  font-size: 18px;
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
  line-height: 1.5;
}

.login-button {
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
  font-size: 17px;
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
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: rgba(255,255,255,.86);
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
  animation: spin .8s linear infinite;
}

@media (max-width:360px) {
  .login-container {
    padding-right:20px;
    padding-left:20px;
  }

  .login-header h2 {
    font-size:23px;
  }

  .login-button {
    right:20px;
    left:20px;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>