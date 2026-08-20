<template>
  <main class="page-layout account-page">
    <PageHeader title="계좌 연결" custom-back @back="goBack" />

    <div class="page-content">
      <header class="title-section">
        <h1 class="text-26-bold">은행을 선택해 주세요</h1>
        <p class="text-15">연결할 계좌의 은행을 선택해 주세요.</p>
      </header>

      <section class="bank-section">
        <p v-if="loading" class="state-message text-13">
          은행 목록을 불러오고 있어요.
        </p>

        <p v-else-if="errorMessage" class="error-message text-13">
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

            <div v-else class="bank-logo fallback-logo text-15-bold">
              {{ bank.bankName.slice(0, 1) }}
            </div>

            <span class="text-13-bold">{{ bank.bankName }}</span>

            <span
              v-if="accountStore.accountForm.bankCode === bank.bankCode"
              class="selected-icon"
            >
              <i class="fa-solid fa-check"></i>
            </span>
          </button>
        </div>
      </section>

      <div class="bottom-btn-area single">
        <button
          class="bottom-btn"
          :disabled="!accountStore.accountForm.bankCode"
          type="button"
          @click="next"
        >
          다음
        </button>
      </div>
    </div>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getBanks } from '@/api/bankApi';
import { useAccountStore } from '@/stores/account';
import PageHeader from '@/components/common/PageHeader.vue';

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
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  background: var(--color-bg-page);
}

.title-section {
  flex-shrink: 0;
  margin-top: 38px;
}

.title-section h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.title-section p {
  margin: 14px 0 0;
  color: var(--color-text-sub);
  line-height: 1.5;
}

.bank-section {
  flex: 1;
  min-height: 0;
  margin-top: 38px;
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
  position: relative;
  display: flex;
  min-width: 0;
  height: 105px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 10px 6px;
  border: 1px solid var(--color-divider);
  border-radius: 16px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
  cursor: pointer;
  box-sizing: border-box;
}

.bank-item:active {
  background: var(--color-bg-screen);
}

.bank-item.selected {
  border-color: var(--color-primary);
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
  background: var(--color-primary);
  color: var(--color-text-main);
}

.bank-item span {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-align: center;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-icon {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex !important;
  width: 18px;
  height: 18px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-text-main);
  font-size: 9px;
}

.state-message,
.error-message {
  margin: 30px 0;
  line-height: 1.5;
  text-align: center;
}

.state-message {
  color: var(--color-text-sub);
}

.error-message {
  color: var(--color-error);
}
</style>
