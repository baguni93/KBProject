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
        시작하기
      </button>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAccountStore } from '@/stores/account';

const router = useRouter();
const accountStore = useAccountStore();

const maskedAccountNumber = computed(() => {
  const accountNumber = accountStore.accountForm.accountNumber;

  if (accountNumber.length <= 4) return accountNumber;

  return `${accountNumber.slice(0, 3)}-${'*'.repeat(accountNumber.length - 7)}-${accountNumber.slice(-4)}`;
});

// 계좌 연결 완료
const complete = () => {
  sessionStorage.removeItem('signupUserId');
  accountStore.resetAccountForm();
  router.replace('/');
};
</script>

<style scoped>
.complete-page {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  padding: 24px 0;
  background: #f4f4f4;
}

.complete-container {
  display: flex;
  flex: none;
  flex-direction: column;
  width: 390px;
  height: 844px;
  padding: 74px 28px 30px;
  background: #ffffff;
  overflow: hidden;
}

.complete-content {
  text-align: center;
}

.complete-icon {
  display: flex;
  width: 108px;
  height: 108px;
  align-items: center;
  justify-content: center;
  margin: 56px auto 46px;
  border-radius: 50%;
  background: #ffbc2e;
  box-shadow: 0 20px 40px rgba(255, 188, 46, 0.28);
  color: #ffffff;
  font-size: 54px;
}

.complete-content h1 {
  margin: 0;
  color: #111111;
  font-size: 27px;
  font-weight: 800;
}

.complete-content > p {
  margin: 20px 0 0;
  color: #777777;
  font-size: 16px;
  line-height: 1.65;
}

.account-card {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 52px;
  padding: 20px;
  border: 1px solid #f1e2b5;
  border-radius: 18px;
  background: #fff9e9;
  text-align: left;
}

.account-card img {
  width: 46px;
  height: 46px;
  object-fit: contain;
}

.account-card strong {
  display: block;
  color: #222222;
  font-size: 15px;
}

.account-card span {
  display: block;
  margin-top: 7px;
  color: #777777;
  font-size: 13px;
}

.complete-button {
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
</style>