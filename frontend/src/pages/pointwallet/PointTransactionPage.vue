<template>
  <div class="container py-4">
    <div class="mb-4">
      <div class="small text-muted mb-1">화면 ID: point-004</div>
      <div class="d-flex justify-content-between align-items-center">
        <h2 class="mb-0">포인트 이용내역</h2>
        <router-link class="btn btn-outline-secondary" to="/point-wallet">돌아가기</router-link>
      </div>
    </div>

    <div v-if="message" class="alert alert-danger">{{ message }}</div>

    <section class="card">
      <div class="card-header">
        <div class="d-flex flex-wrap gap-2">
          <button
            v-for="option in transactionTypeOptions"
            :key="option.value"
            type="button"
            :class="['btn', selectedType === option.value ? 'btn-warning' : 'btn-outline-secondary']"
            :disabled="loading"
            @click="selectType(option.value)"
          >
            {{ option.label }}
          </button>
        </div>
      </div>

      <div class="card-body p-0">
        <div v-if="loading" class="p-4 text-center">조회 중입니다.</div>

        <div v-else-if="transactions.length" class="list-group list-group-flush">
          <div
            v-for="transaction in transactions"
            :key="transaction.pointTransactionId"
            class="list-group-item d-flex justify-content-between align-items-center gap-3"
          >
            <div>
              <div class="fw-semibold">{{ getReasonTypeLabel(transaction.reasonType) }}</div>
              <div class="small text-muted">
                {{ getTransactionTypeLabel(transaction.transactionType) }} · {{ transaction.createdAt }}
              </div>
              <div class="small text-muted">거래번호 {{ transaction.pointTransactionId }}</div>
            </div>
            <div class="fw-bold text-nowrap">
              {{ getPointSign(transaction.transactionType) }}{{ formatNumber(transaction.pointAmount) }}P
            </div>
          </div>
        </div>

        <div v-else class="p-4 text-muted">해당 조건의 포인트 이용내역이 없습니다.</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
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

const loadTransactions = async () => {
  loading.value = true;
  message.value = '';

  try {
    transactions.value = await pointWalletApi.getTransactions(selectedType.value);
  } catch (error) {
    message.value = getApiErrorMessage(error, '포인트 이용내역을 불러오지 못했습니다.');
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
