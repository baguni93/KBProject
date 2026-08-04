<template>
  <div class="account-page">
    <main class="account-container">
      <button class="back-button" type="button" @click="goBack">&lt;</button>

      <header class="page-header">
        <div class="selected-bank">
          <img
              v-if="accountStore.accountForm.bankLogoUrl"
              :alt="accountStore.accountForm.bankName"
              :src="accountStore.accountForm.bankLogoUrl"
          />

          <strong>{{ accountStore.accountForm.bankName }}</strong>
        </div>

        <h1>계좌정보를 입력해 주세요</h1>
        <p>본인 명의의 계좌만 연결할 수 있어요.</p>
      </header>

      <form class="account-form" @submit.prevent="requestVerification">
        <label for="accountHolder">예금주</label>

        <input
            id="accountHolder"
            v-model.trim="accountHolder"
            maxlength="30"
            placeholder="예금주명을 입력해 주세요"
            type="text"
            @input="clearError"
        />

        <label for="accountNumber">계좌번호</label>

        <input
            id="accountNumber"
            :value="accountNumber"
            inputmode="numeric"
            maxlength="20"
            placeholder="'-' 없이 숫자만 입력해 주세요"
            type="text"
            @input="changeAccountNumber"
        />

        <p v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </p>

        <button
            class="next-button"
            :disabled="!canSubmit || loading"
            type="submit"
        >
          {{ loading ? '인증 요청 중...' : '인증번호 받기' }}
        </button>
      </form>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { requestAccountVerification } from '@/api/accountApi';
import { useAccountStore } from '@/stores/account';

const router = useRouter();
const accountStore = useAccountStore();

const accountHolder = ref(accountStore.accountForm.accountHolder);
const accountNumber = ref(accountStore.accountForm.accountNumber);
const loading = ref(false);
const errorMessage = ref('');

const canSubmit = computed(() => {
  return accountHolder.value.length > 0
      && accountNumber.value.length >= 8
      && accountStore.accountForm.bankCode;
});

// 계좌번호 입력
const changeAccountNumber = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 20);

  accountNumber.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) event.target.value = value;
};

// 오류 초기화
const clearError = () => {
  errorMessage.value = '';
};

// 인증번호 발급
const requestVerification = async () => {
  if (!canSubmit.value) return;

  const userId = accountStore.userId;

  if (!userId) {
    router.replace('/intro');
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    const requestData = {
      bankCode: accountStore.accountForm.bankCode,
      accountNumber: accountNumber.value,
      accountHolder: accountHolder.value,
    };

    const response = await requestAccountVerification(userId, requestData);

    accountStore.setAccountInfo(requestData);
    accountStore.setVerification(response);

    router.push('/setting/account/verification');
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '계좌 인증번호 발급에 실패했습니다.';
  } finally {
    loading.value = false;
  }
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(() => {
  if (!accountStore.accountForm.bankCode) router.replace('/setting/account/connect');
});
</script>

<style scoped>
.account-page {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  padding: 24px 0;
  background: #f4f4f4;
}

.account-container {
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
}

.page-header {
  margin-top: 46px;
}

.selected-bank {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
  padding: 9px 14px;
  border-radius: 20px;
  background: #fff7dc;
}

.selected-bank img {
  width: 26px;
  height: 26px;
  object-fit: contain;
}

.selected-bank strong {
  font-size: 14px;
}

.page-header h1 {
  margin: 0;
  color: #111111;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.8px;
}

.page-header p {
  margin: 17px 0 0;
  color: #777777;
  font-size: 16px;
}

.account-form {
  display: flex;
  flex: 1;
  flex-direction: column;
  margin-top: 52px;
}

.account-form label {
  margin: 0 0 10px;
  color: #333333;
  font-size: 14px;
  font-weight: 700;
}

.account-form input {
  width: 100%;
  height: 54px;
  margin-bottom: 28px;
  padding: 0 16px;
  border: 1px solid #dddddd;
  border-radius: 12px;
  font-size: 16px;
  outline: none;
}

.account-form input:focus {
  border-color: #ffbc2e;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.error-message {
  margin: 0;
  color: #e53935;
  font-size: 14px;
}

.next-button {
  width: 100%;
  height: 58px;
  margin-top: auto;
  border: 1px solid #cc9200;
  border-radius: 12px;
  background: #ffbc2e;
  color: #111111;
  font-size: 18px;
  font-weight: 800;
}

.next-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #aaaaaa;
}
</style>