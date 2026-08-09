<template>
  <div class="kb-mobile-page transaction-page">
    <PageHeader
      title="포인트 이용내역"
      :show-refresh="true"
      @refresh="loadTransactions"
    />

    <div
      v-if="message"
      class="kb-toast kb-toast--error"
    >
      {{ message }}
    </div>

    <div class="transaction-tabs">
      <CommonTabBar
          :tabs="transactionTypeOptions"
          :model-value="selectedType"
          @update:model-value="selectType"
      />
    </div>

    <section class="kb-section transaction-section">
      <div class="kb-section-title-row">
        <h2 class="kb-section-title text-20-bold">
          포인트 이용내역
        </h2>

        <span class="result-count text-13">
          {{ transactions.length }}건
        </span>
      </div>

      <div class="list-card kb-card">
        <div
          v-if="loading"
          class="kb-loading"
        >
          <div
            class="spinner-border kb-spinner"
            role="status"
          ></div>

          <div class="text-13">
            이용내역을 불러오는 중이에요.
          </div>
        </div>

        <div v-else-if="transactions.length">
          <div
            v-for="transaction in transactions"
            :key="transaction.pointTransactionId"
            class="point-row"
          >
            <div
              :class="[
                'point-icon',
                getIconClass(transaction.transactionType)
              ]"
            >
              <i :class="getReasonIcon(transaction.reasonType)"></i>
            </div>

            <div class="point-info">
              <strong class="text-15-bold">
                {{ getReasonTypeLabel(transaction.reasonType) }}
              </strong>

              <span class="text-13">
                {{ formatDateTime(transaction.createdAt) }}
              </span>
            </div>

            <div class="point-right">
              <strong
                class="text-15-bold"
                :class="
                  transaction.transactionType === 'EARN'
                    ? 'kb-amount-positive'
                    : 'kb-amount-negative'
                "
              >
                {{ getPointSign(transaction.transactionType) }}{{ formatNumber(transaction.pointAmount) }}P
              </strong>

              <span class="text-13">
                {{ getTransactionTypeLabel(transaction.transactionType) }}
              </span>
            </div>
          </div>
        </div>

        <div
          v-else
          class="kb-empty-state"
        >
          <div class="kb-empty-state__icon">
            <i class="fa-solid fa-receipt"></i>
          </div>

          <strong class="text-15-bold">
            해당 조건의 이용내역이 없어요.
          </strong>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import CommonTabBar from '@/components/common/CommonTabBar.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import pointWalletApi from '@/api/pointWalletApi';
import {
  formatNumber,
  getApiErrorMessage,
  getPointSign,
  getReasonTypeLabel,
  getTransactionTypeLabel,
  transactionTypeOptions,
} from '@/util/pointWallet';

const selectedType = ref('ALL');
const transactions = ref([]);
const loading = ref(false);
const message = ref('');

const getReasonIcon = (reason) => ({
  ATTENDANCE: 'fa-solid fa-calendar-check',
  RANDOM_BOX: 'fa-solid fa-gift',
  CONVERSION: 'fa-solid fa-arrow-right-arrow-left',
  EVENT: 'fa-solid fa-star',
}[reason] ?? 'fa-solid fa-coins');

const getIconClass = (type) => (
  type === 'EARN' ? 'earn' : 'use'
);

const formatDateTime = (value) => (
  value ? String(value).replace('T', ' ').slice(0, 16) : '-'
);

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

.transaction-section {
  //padding-top: 14px;
}

.result-count {
  color: var(--color-text-muted);
}

.list-card {
  overflow: hidden;
}

.point-row {
  padding: 13px 15px;
  display: flex;
  align-items: center;
  gap: 11px;
  border-bottom: 1px solid var(--color-divider);
}

.point-row:last-child {
  border-bottom: 0;
}

.point-icon {
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 14px;
}

.point-icon.earn {
  background: var(--kb-yellow-soft);
  color: #e09a00;
}

.point-icon.use {
  background: #f1f2f4;
  color: #555;
}

.point-info {
  min-width: 0;
  flex: 1;
}

.point-info strong,
.point-info span,
.point-right strong,
.point-right span {
  display: block;
}

.point-info span {
  margin-top: 3px;
  color: var(--color-text-muted);
}

.point-right {
  text-align: right;
}

.point-right strong {
  white-space: nowrap;
}

.point-right span {
  margin-top: 3px;
  color: var(--color-text-muted);
}

.transaction-tabs {
  margin-top: 14px;
}
</style>

