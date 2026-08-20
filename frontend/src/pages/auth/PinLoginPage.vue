<template>
  <div class="page-layout login-page">
    <!-- 공통 상단 헤더 -->
    <PageHeader custom-back @back="goBack" />

    <!-- 콘텐츠 -->
    <main class="page-content login-content">
      <!-- 메인 비주얼 -->
      <div class="login-visual">
        <div class="visual-glow"></div>

        <div class="visual-icon">
          <i class="fa-solid fa-lock"></i>
        </div>

        <span class="visual-dot dot-one"></span>
        <span class="visual-dot dot-two"></span>
        <span class="visual-dot dot-three"></span>
      </div>

      <!-- 제목 -->
      <header class="login-header">
        <h1 class="text-28-bold">간편비밀번호 로그인</h1>

        <p class="text-15">등록한 PIN 6자리를 입력해 주세요.</p>
      </header>

      <!-- PIN 입력 -->
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
            :disabled="pinLocked"
            class="hidden-pin-input"
            inputmode="numeric"
            maxlength="6"
            pattern="[0-9]*"
            type="password"
            autocomplete="off"
            @input="changePin"
          />
        </div>

        <p v-if="errorMessage" class="error-message text-13">
          {{ errorMessage }}
        </p>

        <button
          v-if="!pinLocked"
          class="forgot-button text-13-bold"
          type="button"
          @click="goPinReset"
        >
          간편비밀번호를 잊으셨나요?
        </button>
      </section>

      <!-- 보안 안내 -->
      <section class="security-card">
        <div class="security-icon">
          <i class="fa-solid fa-shield-halved"></i>
        </div>

        <div class="security-text">
          <strong class="text-15-bold"> 안전하게 보호하고 있어요 </strong>

          <p class="text-13">입력한 간편비밀번호는 안전하게 보호돼요.</p>
        </div>
      </section>
    </main>

    <!-- 하단 버튼 -->
    <div class="bottom-btn-area single">
      <button
        v-if="pinLocked"
        class="bottom-btn"
        type="button"
        @click="goPinReset"
      >
        본인인증 후 재설정
      </button>

      <button
        v-else
        class="bottom-btn"
        :disabled="pinPassword.length !== 6 || loading"
        type="button"
        @click="login"
      >
        {{ loading ? '로그인 중' : '로그인' }}
      </button>
    </div>

    <!-- 로딩 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>

      <span class="text-15-bold"> 안전하게 로그인하고 있어요. </span>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo } from '@/api/userApi';
import { useSignupStore } from '@/stores/signup';
import { useAuthStore } from '@/stores/auth';
import PageHeader from '@/components/common/PageHeader.vue';

const router = useRouter();
const signupStore = useSignupStore();
const authStore = useAuthStore();

const pinInput = ref(null);
const pinPassword = ref('');
const errorMessage = ref('');
const loading = ref(false);
const pinLocked = ref(false);

// PIN 입력창 포커스
const focusPinInput = async () => {
  if (loading.value || pinLocked.value) return;

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

    const userInfo = await getUserInfo();

    authStore.setUserName(userInfo.userName);

    sessionStorage.removeItem('pinLoginPhoneNumber');
    signupStore.reset();

    await router.replace('/wallet');
  } catch (error) {
    console.error(error);

    pinPassword.value = '';

    if (!error.response) {
      errorMessage.value =
        '서버에 연결할 수 없습니다. 네트워크 상태를 확인해주세요.';
    } else {
      const responseData = error.response.data;

      errorMessage.value =
        typeof responseData === 'string'
          ? responseData
          : responseData?.message ||
            '로그인에 실패했습니다. 다시 시도해주세요.';
    }

    if (errorMessage.value.includes('입력 가능 횟수를 초과'))
      pinLocked.value = true;

    if (!pinLocked.value) await focusPinInput();
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

// PIN 6자리 입력 시 자동 로그인
watch(pinPassword, (value) => {
  if (value.length !== 6 || loading.value || pinLocked.value) return;

  login();
});
</script>

<style scoped>
@import '@/components/common/common/common.css';
@import '@/components/common/common/layout.css';

.login-page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;

  background: linear-gradient(
    180deg,
    #fffdf8 0%,
    var(--color-bg-page) 42%,
    var(--color-bg-page) 100%
  );
}

.login-content {
  padding-top: 32px;
}

/* 메인 비주얼 */
.login-visual {
  position: relative;
  width: 104px;
  height: 104px;
  margin: 0 auto 28px;
}

.visual-glow {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(255, 188, 46, 0.11);

  opacity: 0;
  transform: scale(0.78);
  animation:
    glow-enter 0.5s ease 0.05s forwards,
    glow-breathe 3s ease-in-out 1s infinite;
}

.visual-icon {
  position: absolute;
  inset: 14px;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 26px;
  background: linear-gradient(
    135deg,
    #ffca52 0%,
    var(--color-primary) 65%,
    #f3a711 100%
  );
  box-shadow: 0 12px 26px rgba(255, 188, 46, 0.22);
  color: #ffffff;

  opacity: 0;
  transform: scale(0.62);
  animation: icon-pop 0.55s cubic-bezier(0.34, 1.56, 0.64, 1) 0.12s forwards;
}

.visual-icon i {
  font-size: 27px;

  opacity: 0;
  transform: scale(0.6);
  animation: lock-enter 0.28s ease 0.4s forwards;
}

/* 작은 장식 */
.visual-dot {
  position: absolute;
  z-index: 3;
  border-radius: 50%;

  opacity: 0;
  transform: scale(0);
}

.dot-one {
  top: 5px;
  right: 10px;
  width: 8px;
  height: 8px;
  background: #8f81f5;

  animation:
    dot-pop 0.35s ease 0.42s forwards,
    dot-float 3.2s ease-in-out 1s infinite;
}

.dot-two {
  bottom: 8px;
  left: 4px;
  width: 7px;
  height: 7px;
  background: #6fd0bd;

  animation:
    dot-pop 0.35s ease 0.5s forwards,
    dot-float 3.6s ease-in-out 1.1s infinite reverse;
}

.dot-three {
  right: 3px;
  bottom: 18px;
  width: 6px;
  height: 6px;
  background: #ff9aa7;

  animation:
    dot-pop 0.35s ease 0.58s forwards,
    dot-float 3.4s ease-in-out 1.15s infinite;
}

/* 제목 */
.login-header {
  text-align: center;
}

.login-header h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.6px;
}

.login-header p {
  margin: 14px 0 0;
  color: var(--color-text-sub);
  line-height: 1.6;
}

/* PIN */
.pin-section {
  margin-top: 42px;
  text-align: center;
}

.pin-boxes {
  position: relative;
  display: grid;
  width: 100%;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 8px;
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
  background: #fafafa;
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
  background: #fff7f7;
  box-shadow: none;
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

/* 오류 */
.error-message {
  min-height: 20px;
  margin: 14px 0 0;
  color: var(--color-error);
  line-height: 1.5;
}

/* 비밀번호 찾기 */
.forgot-button {
  margin-top: 18px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-text-sub);
  cursor: pointer;
  text-decoration: underline;
  text-decoration-thickness: 1px;
  text-underline-offset: 4px;
}

.forgot-button:active {
  color: var(--color-text-main);
}

/* 보안 안내 */
.security-card {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 14px;
  margin-top: 40px;
  padding: 16px;
  border: 1px solid rgba(255, 188, 46, 0.16);
  border-radius: 16px;
  background: linear-gradient(110deg, #fff9ec 0%, #fffdf8 75%, #faf8ff 100%);
  box-sizing: border-box;
  text-align: left;
}

.security-icon {
  display: flex;
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(255, 188, 46, 0.12);
  color: var(--color-primary-border);
}

.security-icon i {
  font-size: 17px;
}

.security-text {
  min-width: 0;
}

.security-text strong {
  display: block;
  color: var(--color-text-main);
}

.security-text p {
  margin: 5px 0 0;
  color: var(--color-text-sub);
  line-height: 1.45;
}

/* 로딩 */
.loading-overlay {
  position: absolute;
  inset: 0;
  z-index: 100;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.88);
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

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes glow-enter {
  from {
    opacity: 0;
    transform: scale(0.78);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes glow-breathe {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }

  50% {
    opacity: 0.72;
    transform: scale(1.06);
  }
}

@keyframes icon-pop {
  0% {
    opacity: 0;
    transform: scale(0.62);
  }

  70% {
    opacity: 1;
    transform: scale(1.07);
  }

  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes lock-enter {
  from {
    opacity: 0;
    transform: scale(0.6);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes dot-pop {
  0% {
    opacity: 0;
    transform: scale(0);
  }

  70% {
    opacity: 1;
    transform: scale(1.3);
  }

  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes dot-float {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-4px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .visual-glow,
  .visual-icon,
  .visual-icon i,
  .visual-dot {
    opacity: 1;
    transform: none;
    animation: none;
  }
}
</style>
