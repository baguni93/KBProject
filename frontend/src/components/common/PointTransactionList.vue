
<!--이 페이지는 Point 거래 내역을 재활용하기 위해 사용하였습니다. -->
<template>
  <div class="point-transaction-list kb-card">
    <div v-if="loading" class="kb-loading">
      <div class="spinner-border kb-spinner" role="status"></div>
      <div class="text-13">{{ loadingText }}</div>
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
            transaction.transactionType === 'EARN' ? 'earn' : 'use',
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

    <div v-else class="kb-empty-state">
      <div class="kb-empty-state__icon">
        <i class="fa-solid fa-receipt"></i>
      </div>

      <strong class="text-15-bold">
        {{ emptyText }}
      </strong>
    </div>
  </div>
</template>

<script setup>
import {
  formatNumber,
  getPointSign,
  getReasonTypeLabel,
  getTransactionTypeLabel,
} from '@/util/pointWallet';

defineProps({
  transactions: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  loadingText: {
    type: String,
    default: '이용내역을 불러오는 중이에요.',
  },
  emptyText: {
    type: String,
    default: '해당 조건의 이용내역이 없어요.',
  },
});

const getReasonIcon = (reason) => ({
  ATTENDANCE: 'fa-solid fa-calendar-check',
  RANDOM_BOX: 'fa-solid fa-gift',
  CONVERSION: 'fa-solid fa-arrow-right-arrow-left',
  EVENT: 'fa-solid fa-star',
}[reason] ?? 'fa-solid fa-coins');

const formatDateTime = (value) => (
  value ? String(value).replace('T', ' ').slice(0, 16) : '-'
);
</script>

<style scoped>
.point-transaction-list {
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
  background: color-mix(in srgb, var(--color-primary) 18%, var(--color-bg-page));
  color: var(--color-primary-border);
}

.point-icon.use {
  background: var(--color-bg-disabled);
  color: var(--color-text-sub);
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
</style>
