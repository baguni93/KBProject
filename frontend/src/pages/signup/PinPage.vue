<template>
  <div class="pin-page">
    <main class="pin-container">
      <button class="back-button" type="button" @click="goBack">
        &lt;
      </button>

      <header class="pin-header">
        <h1>
          간편비밀번호를<br />
          설정해주세요.
        </h1>

        <p>
          로그인과 서비스 이용 시 사용할<br />
          숫자 6자리를 입력해주세요.
        </p>
      </header>

      <PinInput :model-value="pin" />

      <p v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </p>

      <p v-else class="guide-message">
        생년월일, 연속된 숫자는 사용할 수 없어요.
      </p>

      <section class="keypad">
        <button
            v-for="number in keypadNumbers"
            :key="number"
            class="key-button"
            type="button"
            @click="inputNumber(number)"
        >
          {{ number }}
        </button>

        <div class="key-empty"></div>

        <button class="key-button" type="button" @click="inputNumber('0')">
          0
        </button>

        <button
            class="key-button delete-button"
            type="button"
            @click="deleteNumber"
        >
          ←
        </button>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import PinInput from '@/components/auth/PinInput.vue';
import { useSignupStore } from '@/stores/signup';
import { validatePin } from '@/util/pinValidation';

const router = useRouter();
const signupStore = useSignupStore();

const pin = ref('');
const errorMessage = ref('');
const keypadNumbers = ['1', '2', '3', '4', '5', '6', '7', '8', '9'];

// 숫자 입력
const inputNumber = (number) => {
  if (pin.value.length >= 6) return;

  pin.value += number;
  errorMessage.value = '';
};

// 숫자 삭제
const deleteNumber = () => {
  pin.value = pin.value.slice(0, -1);
  errorMessage.value = '';
};

// 이전 화면
const goBack = () => {
  signupStore.setPin('');
  router.back();
};

// PIN 6자리 입력 완료
watch(pin, (value) => {
  if (value.length !== 6) return;

  const result = validatePin(value, signupStore.phoneAuth.birthDate);

  if (!result.valid) {
    errorMessage.value = result.message;
    pin.value = '';
    signupStore.setPin('');
    return;
  }

  errorMessage.value = '';
  signupStore.setPin(value);
  router.push('/signup/pin-confirm');
});
</script>

<style scoped>
.pin-page {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  padding: 24px 0;
  background: #f4f4f4;
  overflow: auto;
}

.pin-container {
  display: flex;
  flex: none;
  flex-direction: column;
  width: 390px;
  height: 844px;
  padding: 26px 28px 32px;
  background: #ffffff;
  overflow: hidden;
}

.back-button {
  align-self: flex-start;
  margin-bottom: 38px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.pin-header {
  margin-bottom: 58px;
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
  font-size: 17px;
  font-weight: 500;
  line-height: 1.55;
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

.keypad {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  margin-top: auto;
  row-gap: 18px;
}

.key-button,
.key-empty {
  height: 68px;
}

.key-button {
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: #222222;
  font-size: 26px;
  font-weight: 500;
  cursor: pointer;
}

.key-button:active {
  background: #f3f3f3;
}

.delete-button {
  font-size: 28px;
}
</style>