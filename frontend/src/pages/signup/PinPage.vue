<template>
  <div class="pin-page">
    <main class="pin-container">
      <button class="back-button" type="button" @click="goBack">
        &lt;
      </button>

      <div class="signup-progress" aria-label="회원가입 진행 단계">
        <span class="progress-step active"></span>
        <span class="progress-line"></span>
        <span class="progress-step"></span>
        <span class="progress-line"></span>
        <span class="progress-step"></span>
      </div>

      <header class="pin-header">
        <h1>간편비밀번호 설정</h1>

        <p>숫자 6자리를 입력해주세요.</p>
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

        <p v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </p>

        <p v-else class="guide-message">
          생년월일, 연속된 숫자는 사용할 수 없어요.
        </p>
      </section>

      <button
          class="next-button"
          type="button"
          :disabled="pin.length !== 6"
          @click="next"
      >
        다음
      </button>
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
import { useSignupStore } from '@/stores/signup';
import { validatePin } from '@/util/pinValidation';

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
  const value =
      event.target.value
          .replace(/[^0-9]/g, '')
          .slice(0, 6);

  pin.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) {
    event.target.value = value;
  }
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

  const result = validatePin(
      pin.value,
      signupStore.phoneAuth.birthDate,
  );

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
.pin-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.pin-container {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
  padding: 26px 28px 140px;
  background: #ffffff;
  box-sizing: border-box;
}

.back-button {
  align-self: flex-start;
  margin-bottom: 34px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

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
  background: #dddddd;
}

.progress-step.active {
  width: 44px;
  height: 12px;
  border-radius: 999px;
  background: #ffbc2e;
}

.progress-line {
  width: 38px;
  height: 1px;
  margin: 0 8px;
  background: #dddddd;
}

.pin-header {
  text-align: left;
}

.pin-header h1 {
  margin: 0 0 20px;
  color: #111111;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.4;
}

.pin-header p {
  margin: 0;
  color: #777777;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.35;
}

.pin-section {
  margin-top: 58px;
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

.error-message,
.guide-message {
  min-height: 42px;
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

.next-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #aaaaaa;
  cursor: not-allowed;
}
</style>