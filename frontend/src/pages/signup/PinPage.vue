<template>
  <div class="page-layout pin-page">
    <!-- 공통 상단 헤더 -->
    <PageHeader
        custom-back
        @back="goBack"
    />

    <!-- 콘텐츠 -->
    <main class="page-content pin-content">
      <!-- 회원가입 진행 단계 -->
      <div class="signup-progress" aria-label="회원가입 진행 단계">
        <span class="progress-step active"></span>
        <span class="progress-line"></span>
        <span class="progress-step"></span>
        <span class="progress-line"></span>
        <span class="progress-step"></span>
      </div>

      <!-- 제목 -->
      <header class="pin-header">
        <h1 class="text-30-bold">
          간편비밀번호 설정
        </h1>

        <p class="text-15">
          로그인에 사용할 PIN 6자리를 입력해 주세요.
        </p>
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
                filled: pin.length >= index,
                active: pin.length === index - 1 && !errorMessage,
              }"
              class="pin-box"
          >
            <span
                v-if="pin.length >= index"
                class="pin-dot"
            ></span>
          </div>

          <input
              ref="pinInput"
              :value="pin"
              class="hidden-pin-input"
              type="password"
              inputmode="numeric"
              maxlength="6"
              pattern="[0-9]*"
              autocomplete="off"
              @input="changePin"
          />
        </div>

        <p v-if="errorMessage" class="error-message text-13">
          {{ errorMessage }}
        </p>

        <p v-else class="guide-message text-13">
          생년월일이나 연속된 숫자는 사용할 수 없어요.
        </p>
      </section>
    </main>

    <!-- 공통 하단 버튼 -->
    <div class="bottom-btn-area single">
      <button
          class="bottom-btn"
          type="button"
          :disabled="pin.length !== 6"
          @click="next"
      >
        다음
      </button>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useSignupStore } from '@/stores/signup';
import { validatePin } from '@/util/pinValidation';
import PageHeader from '@/components/common/PageHeader.vue';

const router = useRouter();
const signupStore = useSignupStore();

const pinInput = ref(null);
const pin = ref('');
const errorMessage = ref('');

// PIN 입력창 포커스
const focusPinInput = async () => {
  await nextTick();
  pinInput.value?.focus();
};

// PIN 입력
const changePin = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 6);

  pin.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) event.target.value = value;
};

// 이전 화면
const goBack = () => {
  signupStore.setPin('');
  router.back();
};

// 다음 화면
const next = async () => {
  if (pin.value.length !== 6) {
    await focusPinInput();
    return;
  }

  const result = validatePin(pin.value, signupStore.phoneAuth.birthDate);

  if (!result.valid) {
    errorMessage.value = result.message;
    pin.value = '';
    signupStore.setPin('');

    await focusPinInput();
    return;
  }

  errorMessage.value = '';
  signupStore.setPin(pin.value);

  await router.push('/signup/pin-confirm');
};

onMounted(() => {
  focusPinInput();
});
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.pin-page {
  background: var(--color-bg-page);
}

.pin-content {
  padding-top: 30px;
}

/* 회원가입 진행 단계 */
.signup-progress {
  display: flex;
  align-items: center;
  align-self: flex-start;
  margin-bottom: 40px;
}

.progress-step {
  flex-shrink: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-border-main);
}

.progress-step.active {
  width: 44px;
  height: 12px;
  border-radius: 999px;
  background: var(--color-primary);
}

.progress-line {
  width: 38px;
  height: 1px;
  margin: 0 8px;
  background: var(--color-border-main);
}

/* 제목 */
.pin-header {
  text-align: left;
}

.pin-header h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.pin-header p {
  margin: 14px 0 0;
  color: var(--color-text-sub);
  line-height: 1.6;
}

/* PIN 입력 */
.pin-section {
  margin-top: 52px;
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

/* 안내 문구 */
.error-message,
.guide-message {
  min-height: 20px;
  margin: 16px 0 0;
  line-height: 1.5;
  text-align: center;
}

.error-message {
  color: var(--color-error);
}

.guide-message {
  color: var(--color-text-muted);
}
</style>