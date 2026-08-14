<template>
  <div class="page-layout transaction-page">
    <PageHeader title="포인트 이용내역" />

    <main class="page-content">
      <CommonTabBar
        :tabs="transactionTypeOptions"
        :model-value="selectedType"
        @update:model-value="selectType"
      />

      <section class="kb-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">
            포인트 이용내역
          </h2>

          <span class="result-count text-13">
            {{ transactions.length }}건
          </span>
        </div>

        <PointTransactionList
          :transactions="transactions"
          :loading="loading"
          loading-text="이용내역을 불러오는 중이에요."
          empty-text="해당 조건의 이용내역이 없어요."
        />
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import CommonTabBar from '@/components/common/CommonTabBar.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import PointTransactionList from '@/components/common/PointTransactionList.vue';
import pointWalletApi from '@/api/pointWalletApi';
import {
  getApiErrorMessage,
  transactionTypeOptions,
} from '@/util/pointWallet';

const selectedType = ref('ALL');
const transactions = ref([]);
const loading = ref(false);
const message = ref('');

const loadTransactions = async () => {
  loading.value = true;
  message.value = '';

  try {
    const data = await pointWalletApi.getTransactions(selectedType.value);

    transactions.value = (data ?? []).filter((transaction) =>
      ['EARN', 'USE'].includes(transaction.transactionType)
    );
  } catch (error) {
    message.value = getApiErrorMessage(
      error,
      '포인트 이용내역을 불러오지 못했습니다.'
    );
  } finally {
    loading.value = false;
  }
};

const selectType = async (type) => {
  selectedType.value = type;
  await loadTransactions();
};

onMounted(loadTransactions);
</script>

<style scoped>
.transaction-page {
  color: var(--color-text-main);
  background: var(--color-bg-screen);
}

.result-count {
  color: var(--color-text-muted);
}
</style>
