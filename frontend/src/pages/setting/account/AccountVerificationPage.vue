<template>
  <div class="account-page">
    <main class="account-container">
      <button class="back-button" type="button" @click="goBack">&lt;</button>

      <header class="page-header">
        <h1>계좌 인증번호 입력</h1>

        <p>
          계좌 거래내역에 표시된<br />
          인증번호 4자리를 입력해 주세요.
        </p>
      </header>

      <section class="verification-section">
        <div class="verification-boxes" @click="focusInput">
          <div
              v-for="index in 4"
              :key="index"
              :class="{
              filled: verificationCode.length >= index,
              active: verificationCode.length === index - 1 && !errorMessage,
              error: !!errorMessage,
            }"
              class="verification-box"
          >
            {{ verificationCode[index - 1] || '' }}
          </div>

          <input
              ref="verificationInput"
              :value="verificationCode"
              class="hidden-input"
              inputmode="numeric"
              maxlength="4"
              type="text"
              @input="changeVerificationCode"
          />
        </div>

        <p v-if="accountStore.accountForm.developmentCode" class="development-code">
          개발용 인증번호: {{ accountStore.accountForm.developmentCode }}
        </p>

        <p v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </p>
      </section>

      <button
          class="confirm-button"
          :disabled="verificationCode.length !== 4 || loading"
          type="button"
          @click="confirmAndConnect"
      >
        {{ loading ? '계좌 연결 중...' : '확인' }}
      </button>
    </main>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { confirmAccountVerification, connectAccount } from '@/api/accountApi';
import { useAccountStore } from '@/stores/account';

const router = useRouter();
const accountStore = useAccountStore();

const verificationInput = ref(null);
const verificationCode = ref('');
const loading = ref(false);
const errorMessage = ref('');

// 입력창 포커스
const focusInput = async () => {
  await nextTick();
  verificationInput.value?.focus();
};

// 인증번호 입력
const changeVerificationCode = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 4);

  verificationCode.value = value;
  errorMessage.value = '';
  accountStore.setVerificationCode(value);

  if (event.target.value !== value) event.target.value = value;
};

// 계좌 인증 및 연결
const confirmAndConnect = async () => {
  const userId = accountStore.userId;
  const verificationId = accountStore.accountForm.verificationId;

  if (!userId || !verificationId) {
    router.replace('/setting/account/connect');
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    await confirmAccountVerification(userId, {
      verificationId,
      verificationCode: verificationCode.value,
    });

    await connectAccount(userId, { verificationId });

    router.replace('/setting/account/complete');
  } catch (error) {
    console.error(error);
    verificationCode.value = '';
    accountStore.setVerificationCode('');
    errorMessage.value = error.response?.data?.message || '인증번호가 일치하지 않습니다.';

    await focusInput();
  } finally {
    loading.value = false;
  }
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(() => {
  if (!accountStore.accountForm.verificationId) {
    router.replace('/setting/account/connect');
    return;
  }

  focusInput();
});
</script>

<style scoped>
.account-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.account-container {
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
  margin-bottom: 28px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

.page-header {
  margin: 0;
}

.page-header h1 {
  margin: 0 0 28px;
  color: #111111;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.35;
}

.page-header p {
  margin: 0;
  color: #777777;
  font-size: 20px;
  font-weight: 400;
  line-height: 1.35;
}

.verification-section {
  margin-top: 64px;
  text-align: center;
}

.verification-boxes {
  position: relative;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  width: 100%;
  cursor: text;
}

.verification-box {
  display: flex;
  aspect-ratio: 1 / 1;
  max-height: 72px;
  align-items: center;
  justify-content: center;
  border: 1px solid #dddddd;
  border-radius: 14px;
  background: #fafafa;
  color: #222222;
  font-size: 24px;
  font-weight: 700;
  transition:
      border-color 0.2s,
      background 0.2s,
      box-shadow 0.2s;
}

.verification-box.active {
  border-color: #ffbc2e;
  background: #fffaf0;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.verification-box.filled {
  border-color: #ffbc2e;
  background: #fff8e5;
}

.verification-box.error {
  border-color: #e53935;
  background: #fff7f7;
  box-shadow: none;
}

.hidden-input {
  position: absolute;
  width: 1px;
  height: 1px;
  border: 0;
  opacity: 0;
  pointer-events: none;
}

.development-code {
  margin: 18px 0 0;
  color: #777777;
  font-size: 13px;
  line-height: 1.5;
  text-align: left;
}

.error-message {
  margin: 18px 0 0;
  color: #e53935;
  font-size: 14px;
  line-height: 1.5;
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
  color: #999999;
  cursor: not-allowed;
}
</style>