<template>
  <div class="complete-page">
    <main class="complete-container">
      <section class="complete-content">
        <div class="complete-icon">
          <span>✓</span>
        </div>

        <h1>계좌 연결이 완료되었어요!</h1>

        <p>
          연결한 계좌로 송금과 결제 서비스를<br />
          편리하게 이용할 수 있어요.
        </p>

        <section class="account-card">
          <img
              v-if="accountStore.accountForm.bankLogoUrl"
              :alt="accountStore.accountForm.bankName"
              :src="accountStore.accountForm.bankLogoUrl"
          />

          <div>
            <strong>{{ accountStore.accountForm.bankName }}</strong>
            <span>{{ maskedAccountNumber }}</span>
          </div>
        </section>
      </section>

      <button class="complete-button" type="button" @click="complete">
        {{ isInitialConnection ? '시작하기' : '확인' }}
      </button>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAccountStore } from '@/stores/account';
import { useSignupStore } from '@/stores/signup';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const accountStore = useAccountStore();
const signupStore = useSignupStore();
const authStore = useAuthStore();

// 회원가입 직후 첫 계좌 연결 여부
const isInitialConnection = computed(() => {
  return !!sessionStorage.getItem('signupUserId');
});

// 계좌번호 마스킹
const maskedAccountNumber = computed(() => {
  const accountNumber = accountStore.accountForm.accountNumber || '';

  if (accountNumber.length <= 4) return accountNumber;

  const middleLength = Math.max(accountNumber.length - 7, 0);

  return `${accountNumber.slice(0, 3)}-${'*'.repeat(middleLength)}-${accountNumber.slice(-4)}`;
});

// 계좌 연결 완료
const complete = async () => {
  const initialConnection = isInitialConnection.value;

  accountStore.resetAccountForm();

  if (initialConnection) {
    const phoneNumber = signupStore.phoneAuth.phoneNumber;
    const pinPassword = signupStore.pin;

    try {
      await authStore.login({
        phoneNumber,
        pinPassword,
      });

      sessionStorage.removeItem('signupUserId');
      signupStore.reset();

      await router.replace('/wallet');
    } catch (error) {
      console.error('회원가입 후 자동 로그인 실패', error);
      await router.replace('/intro');
    }

    return;
  }

  await router.replace('/setting/accounts');
};
</script>

<style scoped>
.complete-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.complete-container {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
  padding: 82px 28px 140px;
  background: #ffffff;
  box-sizing: border-box;
}

.complete-content {
  text-align: center;
}

.complete-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 110px;
  height: 110px;
  margin: 20px auto 42px;
  border-radius: 50%;
  background: linear-gradient(145deg, #ffc744, #ffb00f);
  box-shadow: 0 18px 34px rgba(255, 188, 46, 0.25);
  color: #ffffff;
  font-size: 52px;
  font-weight: 700;
}

.complete-content h1 {
  margin: 0 0 28px;
  color: #111111;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.35;
}

.complete-content > p {
  margin: 0;
  color: #777777;
  font-size: 20px;
  font-weight: 400;
  line-height: 1.35;
}

.account-card {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 56px;
  padding: 18px;
  border: 1px solid #f2e3b7;
  border-radius: 16px;
  background: #fff9ea;
  text-align: left;
}

.account-card img {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.account-card strong {
  display: block;
  color: #222222;
  font-size: 16px;
  font-weight: 700;
}

.account-card span {
  display: block;
  margin-top: 6px;
  color: #777777;
  font-size: 14px;
}

.complete-button {
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

.complete-button:active {
  background: #f2aa10;
}
</style>