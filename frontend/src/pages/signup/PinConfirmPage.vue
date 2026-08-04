<template>
  <div class="pin-page">
    <main class="pin-container">
      <button class="back-button" type="button" @click="goBack">
        &lt;
      </button>

      <header class="pin-header">
        <h1>간편비밀번호를<br />한 번 더 입력해주세요.</h1>

        <p>비밀번호 확인을 위해<br />동일한 숫자 6자리를 입력해주세요.</p>
      </header>

      <PinInput :model-value="confirmPin" />

      <p v-if="errorMessage" class="error-message">
        {{ errorMessage }}
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

        <button class="key-button delete-button" type="button" @click="deleteNumber">
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

const router = useRouter();
const signupStore = useSignupStore();
const confirmPin = ref('');
const errorMessage = ref('');
const keypadNumbers = ['1', '2', '3', '4', '5', '6', '7', '8', '9'];

// 숫자 입력
const inputNumber = (number) => {
  if (confirmPin.value.length >= 6) return;
  confirmPin.value += number;
};

// 숫자 삭제
const deleteNumber = () => {
  confirmPin.value = confirmPin.value.slice(0, -1);
  errorMessage.value = '';
};

// 이전 화면
const goBack = () => {
  router.back();
};

watch(confirmPin, (value) => {
  if (value.length !== 6) return;

  if (value !== signupStore.pin) {
    errorMessage.value = '간편비밀번호가 일치하지 않습니다.';
    confirmPin.value = '';
    return;
  }

  signupStore.setPinConfirmed(true);
  router.push('/signup/nickname');
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

.error-message {
  margin: 24px 0 0;
  color: #d32f2f;
  font-size: 14px;
  text-align: center;
}

.keypad {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  row-gap: 18px;
  margin-top: auto;
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
}

.key-button:active {
  background: #f3f3f3;
}

.delete-button {
  font-size: 28px;
}
</style>