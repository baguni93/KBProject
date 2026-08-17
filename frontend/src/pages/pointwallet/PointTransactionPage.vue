<template>
  <main class="transaction-page">
    <PageHeader title="포인트 이용내역" :showBack="true" :customBack="true" @back="goToPointWallet" />

    <div class="transaction-content">
      <div class="transaction-tabs">
        <button
            v-for="tab in transactionTypeOptions"
            :key="tab.value"
            type="button"
            class="transaction-tab"
            :class="{ active: selectedType === tab.value }"
            @click="selectType(tab.value)"
        >
          {{ tab.label }}
        </button>

        <div class="transaction-indicator" :style="indicatorStyle"></div>
      </div>

      <section class="transaction-section">
        <div class="transaction-title-row">
          <h2 class="text-18-bold">포인트 이용내역</h2>
          <span class="result-count text-13">{{ transactions.length }}건</span>
        </div>

        <PointTransactionList
            :selected-type="selectedType"
            :transactions="transactions"
            :loading="loading"
            loading-text="이용내역을 불러오는 중이에요."
            empty-text="해당 조건의 이용내역이 없어요."
        />

        <p v-if="message" class="transaction-message text-13">{{ message }}</p>
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import PointTransactionList from '@/components/common/PointTransactionList.vue';
import pointWalletApi from '@/api/pointWalletApi';
import { getApiErrorMessage, transactionTypeOptions } from '@/util/pointWallet';

const router = useRouter();
const selectedType = ref('ALL');
const transactions = ref([]);
const loading = ref(false);
const message = ref('');

const selectedTabIndex = computed(() => {
  const index = transactionTypeOptions.findIndex((tab) => tab.value === selectedType.value);
  return index < 0 ? 0 : index;
});

const indicatorStyle = computed(() => ({
  width: `${100 / transactionTypeOptions.length}%`,
  transform: `translateX(${selectedTabIndex.value * 100}%)`,
}));

const goToPointWallet = () => router.push('/point-wallet');

const loadTransactions = async () => {
  loading.value = true;
  message.value = '';

  try {
    const data = await pointWalletApi.getTransactions(selectedType.value);
    transactions.value = (data ?? []).filter((transaction) => ['EARN', 'USE'].includes(transaction.transactionType));
  } catch (error) {
    message.value = getApiErrorMessage(error, '포인트 이용내역을 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
};

const selectType = async (type) => {
  if (selectedType.value === type) return;
  selectedType.value = type;
  await loadTransactions();
};

onMounted(loadTransactions);
</script>

<style scoped>
.transaction-page {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: var(--color-text-main);
  background: var(--color-bg-screen);
}

.transaction-page :deep(.page-header) {
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

.transaction-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  scrollbar-width: none;
}

.transaction-content::-webkit-scrollbar {
  display: none;
}

.transaction-tabs {
  position: sticky;
  top: 0;
  z-index: 20;
  height: 52px;
  display: flex;
  border-bottom: 1px solid var(--color-divider);
  background: var(--color-bg-page);
}

.transaction-tab {
  flex: 1;
  border: 0;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s ease, font-weight 0.2s ease;
}

.transaction-tab.active {
  color: var(--color-text-main);
  font-weight: 600;
}

.transaction-indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  border-radius: 3px 3px 0 0;
  background: var(--color-primary);
  transition: transform 0.25s ease;
}

.transaction-section {
  padding: 20px 24px 40px;
}

.transaction-title-row {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.transaction-title-row h2 {
  margin: 0;
}

.result-count {
  flex-shrink: 0;
  color: var(--color-text-muted);
}

.transaction-message {
  margin: 12px 4px 0;
  color: var(--color-error);
  line-height: 1.45;
  word-break: keep-all;
}

@media (max-width: 360px) {
  .transaction-page :deep(.page-header) {
    padding-right: 18px;
    padding-left: 18px;
  }

  .transaction-section {
    padding-right: 18px;
    padding-left: 18px;
  }
}
</style>
