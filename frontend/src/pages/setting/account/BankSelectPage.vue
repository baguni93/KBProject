<template>
  <div class="account-page">
    <main class="account-container">
      <button class="back-button" type="button" @click="goBack">&lt;</button>

      <header class="page-header">
        <h1>은행을 선택해 주세요</h1>
        <p>연결할 계좌의 은행을 선택해 주세요.</p>
      </header>

      <section class="bank-section">
        <p v-if="loading" class="state-message">은행 목록을 불러오고 있어요.</p>

        <p v-else-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </p>

        <div v-else class="bank-grid">
          <button
              v-for="bank in accountStore.banks"
              :key="bank.bankCode"
              :class="{ selected: accountStore.accountForm.bankCode === bank.bankCode }"
              class="bank-item"
              type="button"
              @click="selectBank(bank)"
          >
            <img
                v-if="bank.bankLogoUrl"
                :alt="bank.bankName"
                :src="bank.bankLogoUrl"
                class="bank-logo"
            />

            <div v-else class="bank-logo fallback-logo">
              {{ bank.bankName.slice(0, 1) }}
            </div>

            <span>{{ bank.bankName }}</span>
          </button>
        </div>
      </section>

      <button
          class="next-button"
          :disabled="!accountStore.accountForm.bankCode"
          type="button"
          @click="next"
      >
        다음
      </button>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getBanks } from '@/api/bankApi';
import { useAccountStore } from '@/stores/account';

const router = useRouter();
const accountStore = useAccountStore();

const loading = ref(false);
const errorMessage = ref('');

// 은행 목록 조회
const loadBanks = async () => {
  try {
    loading.value = true;
    errorMessage.value = '';

    const banks = await getBanks();
    accountStore.setBanks(banks);
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '은행 목록을 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 은행 선택
const selectBank = (bank) => {
  accountStore.setBank(bank);
};

// 계좌정보 입력 화면 이동
const next = () => {
  if (!accountStore.accountForm.bankCode) return;
  router.push('/setting/account/info');
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(loadBanks);
</script>

<style scoped>
.account-page {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  padding: 24px 0;
  background: #f4f4f4;
  overflow: auto;
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
  margin-top: 48px;
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

.bank-section {
  min-height: 0;
  margin-top: 42px;
  overflow-y: auto;
}

.bank-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px 12px;
}

.bank-item {
  display: flex;
  min-width: 0;
  height: 105px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: 1px solid #eeeeee;
  border-radius: 16px;
  background: #ffffff;
  color: #333333;
  font-size: 13px;
  font-weight: 600;
}

.bank-item.selected {
  border-color: #ffbc2e;
  background: #fff9e9;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.bank-logo {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.fallback-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffbc2e;
  font-weight: 800;
}

.state-message,
.error-message {
  margin: 30px 0;
  font-size: 14px;
  text-align: center;
}

.state-message {
  color: #777777;
}

.error-message {
  color: #e53935;
}

.next-button {
  width: 100%;
  height: 58px;
  flex: none;
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