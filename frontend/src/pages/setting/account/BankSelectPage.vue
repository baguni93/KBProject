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
            :class="{
              selected: accountStore.accountForm.bankCode === bank.bankCode,
            }"
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
    errorMessage.value =
      error.response?.data?.message || '은행 목록을 불러오지 못했습니다.';
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

onMounted(() => {
  accountStore.resetAccountForm();
  loadBanks();
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
  font-weight: 400;
  line-height: 1.35;
}

.bank-section {
  flex: 1;
  min-height: 0;
  margin-top: 54px;
  padding-bottom: 8px;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.bank-section::-webkit-scrollbar {
  display: none;
}

.bank-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
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
  padding: 10px 6px;
  border: 1px solid #eeeeee;
  border-radius: 16px;
  background: #ffffff;
  color: #333333;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  box-sizing: border-box;
}

.bank-item:active {
  background: #fafafa;
}

.bank-item.selected {
  border-color: #ffbc2e;
  background: #fff9e9;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.bank-logo {
  display: block;
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
  color: #111111;
  font-weight: 800;
}

.bank-item span {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.state-message,
.error-message {
  margin: 30px 0;
  font-size: 14px;
  line-height: 1.5;
  text-align: center;
}

.state-message {
  color: #777777;
}

.error-message {
  color: #e53935;
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
