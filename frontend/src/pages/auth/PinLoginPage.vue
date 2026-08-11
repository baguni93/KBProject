<template>
  <div class="login-page animate-fade-in-up">
    <!-- 1. 상단 영역 (Header + 뒤로가기 버튼) -->
    <header class="signup-header">
      <button class="back-button" type="button" @click="goBack">&lt;</button>
    </header>

    <header class="login-header">
      <div class="login-icon pulse-glow">
        <span class="font-outfit">KB</span>
      </div>

      <h2 class="fw-extrabold text-dark font-outfit">KB Pay 간편 로그인</h2>
      <p class="text-secondary small font-outfit">
        등록하신 6자리 간편비밀번호를 입력해주세요.
      </p>
    </header>

    <!-- 2. 중앙 내용 영역 -->
    <main class="content-area">
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

        <button
          class="forgot-button font-outfit"
          type="button"
          @click="goPinReset"
        >
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
    </main>

    <!-- 3. 하단 버튼 영역 -->
    <div class="button-area">
      <button
        class="login-button font-outfit shadow-md"
        :disabled="pinPassword.length !== 6 || loading"
        type="button"
        @click="login"
      >
        {{ loading ? '보안 인증 확인 중...' : '로그인' }}
      </button>
    </div>

    <!-- 로딩 오버레이 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <span class="fw-bold font-outfit">안전하게 로그인하고 있습니다...</span>
    </div>
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

    await router.replace('/wallet');
  } catch (error) {
    console.error(error);

    pinPassword.value = '';

    const status = error.response?.status;

    if (!error.response) {
      errorMessage.value =
        '서버에 연결할 수 없습니다. 네트워크 상태를 확인해주세요.';
    } else if (status >= 500) {
      errorMessage.value = '간편비밀번호가 일치하지 않습니다.';
    } else {
      errorMessage.value =
        error.response?.data?.message ||
        '로그인에 실패했습니다. 다시 시도해주세요.';
    }

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
/* 1. 상단 헤더 영역 */
.signup-header {
  flex-shrink: 0;
}

.login-page {
  width: 100%;
  height: 100vh;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  padding: 36px 24px 70px;
  background: #ffffff;
}

/* 1. 상단 헤더 영역 (참고 코드와 완전 일치) */
.login-header {
  flex-shrink: 0;
  text-align: center;
}

.back-button {
  align-self: flex-start;
  margin-bottom: 16px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 26px;
  line-height: 1;
  cursor: pointer;
}

.login-icon {
  display: flex;
  width: 68px;
  height: 68px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  border-radius: 22px;
  background: linear-gradient(135deg, #ffbc00 0%, #ff9900 100%);
  box-shadow: 0 14px 28px rgba(255, 188, 0, 0.25);
  color: #111111;
  font-size: 26px;
  font-weight: 800;
}

.login-header h2 {
  margin: 0 0 10px;
  color: #111111;
  font-size: 26px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.login-header p {
  margin: 0;
  color: #777777;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}

/* 2. 중앙 내용 영역 */
.content-area {
  flex: 1;
  min-height: 0;
  margin-top: 24px;
  overflow-y: auto;
  box-sizing: border-box;
  padding-right: 2px;
}

.pin-section {
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
  height: 52px;
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
  margin-top: 20px;
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
  margin: 14px 0 0;
  color: #e53935;
  font-size: 13px;
  line-height: 1.5;
}

.security-area {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 32px;
  padding: 16px;
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
  font-size: 13.5px;
  font-weight: 700;
}

.security-area p {
  margin: 4px 0 0;
  color: #888888;
  font-size: 11px;
  line-height: 1.4;
}

/* 3. 하단 버튼 영역 */
.button-area {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 16px;
  background: #ffffff;
}

.login-button {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  background: #ffbc2e;
  color: #111111;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.login-button:active:not(:disabled) {
  background: #f3aa0b;
}

.login-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #aaaaaa;
  cursor: not-allowed;
}

/* 로딩 오버레이 */
.loading-overlay {
  position: absolute;
  inset: 0;
  z-index: 10;
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

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
