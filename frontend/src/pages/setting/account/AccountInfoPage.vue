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

        <div class="readonly-field">
          <input
            id="accountHolder"
            :value="accountHolder"
            class="readonly-input"
            placeholder="회원 실명을 불러오고 있어요"
            type="text"
            readonly
          />

          <span v-if="userLoading" class="field-loading"> 조회 중 </span>
        </div>

        <p class="field-guide">
          로그인한 회원의 실명으로만 계좌를 연결할 수 있어요.
        </p>

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
          :disabled="!canSubmit || loading || userLoading"
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
import { getAccountByBankCode } from '@/api/userApi';
import { getUserInfo } from '@/api/userApi';
import { useAccountStore } from '@/stores/account';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const accountStore = useAccountStore();
const authStore = useAuthStore();

const accountHolder = ref('');
const accountNumber = ref(accountStore.accountForm.accountNumber || '');
const loading = ref(false);
const userLoading = ref(false);
const errorMessage = ref('');

// 인증 요청 가능 여부
const canSubmit = computed(() => {
  return (
    accountHolder.value.length > 0 &&
    accountNumber.value.length >= 8 &&
    !!accountStore.accountForm.bankCode
  );
});

// 로그인 회원 실명 조회
const loadAccountHolder = async () => {
  const userId = authStore.userId || accountStore.userId;

  if (!userId) {
    await router.replace('/intro');
    return;
  }

  try {
    userLoading.value = true;
    errorMessage.value = '';

    const userInfo = await getUserInfo(userId);

    accountHolder.value = userInfo.userName || '';

    if (!accountHolder.value) {
      errorMessage.value = '회원 실명을 확인할 수 없습니다.';
      return;
    }

    authStore.setUserName(accountHolder.value);
  } catch (error) {
    console.error(error);

    errorMessage.value =
      error.response?.data?.message || '회원 실명을 불러오지 못했습니다.';
  } finally {
    userLoading.value = false;
  }
};

// 계좌번호 입력
const changeAccountNumber = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 20);

  accountNumber.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) {
    event.target.value = value;
  }
};

// 인증번호 발급
const requestVerification = async () => {
  if (!canSubmit.value || loading.value || userLoading.value) return;

  const userId = authStore.userId || accountStore.userId;

  if (!userId) {
    await router.replace('/intro');
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

    await router.push('/setting/account/verification');
  } catch (error) {
    console.error(error);

    errorMessage.value =
      error.response?.data?.message || '계좌 인증번호 발급에 실패했습니다.';
  } finally {
    loading.value = false;
  }
};

//박우진 추가

const accountInfo = async () => {
  console.log(authStore.userId);
  const response = await getAccountByBankCode(
    authStore.userId,
    accountStore.accountForm.bankCode,
  );

  accountNumber.value = response.accountNumber;
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(async () => {
  if (!accountStore.accountForm.bankCode) {
    await router.replace('/setting/account/connect');
    return;
  }
  await accountInfo();
  await loadAccountHolder();
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
  display: block;
  width: 26px;
  height: 26px;
  object-fit: contain;
}

.selected-bank strong {
  color: #222222;
  font-size: 14px;
  font-weight: 800;
}

.page-header h1 {
  margin: 0 0 28px;
  color: #111111;
  font-size: 30px;
  font-weight: 700;
}

.page-header p {
  margin: 0;
  color: #777777;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.35;
}

.account-form {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  margin-top: 54px;
}

.account-form label {
  margin: 0 0 10px;
  color: #333333;
  font-size: 15px;
  font-weight: 700;
}

.account-form input {
  width: 100%;
  height: 54px;
  padding: 0 16px;
  border: 1px solid #dddddd;
  border-radius: 10px;
  background: #ffffff;
  color: #222222;
  font-size: 16px;
  outline: none;
  box-sizing: border-box;
}

.account-form input::placeholder {
  color: #aaaaaa;
}

.account-form input:focus {
  border-color: #ffbc2e;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.readonly-field {
  position: relative;
}

.readonly-input {
  padding-right: 70px !important;
  border-color: #eeeeee !important;
  background: #f7f7f7 !important;
  color: #555555 !important;
  cursor: default;
}

.readonly-input:focus {
  border-color: #eeeeee !important;
  box-shadow: none !important;
}

.field-loading {
  position: absolute;
  top: 50%;
  right: 16px;
  color: #999999;
  font-size: 12px;
  transform: translateY(-50%);
}

.field-guide {
  margin: 9px 0 28px;
  color: #999999;
  font-size: 12px;
  line-height: 1.5;
}

.account-form label[for='accountNumber'] {
  margin-top: 0;
}

#accountNumber {
  margin-bottom: 0;
}

.error-message {
  margin: 14px 0 0;
  color: #e53935;
  font-size: 14px;
  line-height: 1.5;
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

.next-button:active:not(:disabled) {
  background: #f2aa10;
}

.next-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #999999;
  cursor: not-allowed;
}
</style>
