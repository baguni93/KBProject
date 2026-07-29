<template>
  <div class="container py-4">
    <div class="mb-4">
      <div class="small text-muted mb-1">화면 ID: point-003</div>
      <div class="d-flex justify-content-between align-items-center">
        <h2 class="mb-0">포인트 전환</h2>
        <router-link class="btn btn-outline-secondary" to="/point-wallet">돌아가기</router-link>
      </div>
    </div>

    <div v-if="message" :class="['alert', messageType === 'success' ? 'alert-success' : 'alert-danger']">
      {{ message }}
    </div>

    <section class="card mb-4">
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-6">
            <div class="text-muted small">보유 포인트</div>
            <div class="fs-3 fw-bold">{{ formatNumber(pointWallet?.pointBalance) }} P</div>
          </div>
          <div class="col-md-6">
            <div class="text-muted small">전자지갑 잔액</div>
            <div class="fs-3 fw-bold">{{ formatNumber(wallet?.balance) }} 원</div>
          </div>
        </div>
      </div>
    </section>

    <section class="card mb-4">
      <div class="card-header fw-bold">전환할 포인트</div>
      <div class="card-body">
        <form @submit.prevent="submitConversion">
          <div class="input-group mb-3">
            <input
              v-model.number="pointAmount"
              type="number"
              class="form-control"
              min="100"
              step="1"
              required
            />
            <span class="input-group-text">P</span>
          </div>

          <div class="d-flex flex-wrap gap-2 mb-3">
            <button
              v-for="amount in quickAmounts"
              :key="amount"
              type="button"
              class="btn btn-outline-secondary"
              @click="pointAmount = amount"
            >
              {{ formatNumber(amount) }}P
            </button>
            <button type="button" class="btn btn-outline-dark" @click="setMaximumAmount">
              전액
            </button>
          </div>

          <p class="small text-muted">
            최소 100P부터 전환할 수 있으며, 전환된 금액은 전자지갑 잔액에 동일하게 반영됩니다.
          </p>

          <div class="border rounded p-3 mb-3">
            <div class="d-flex justify-content-between mb-2">
              <span>전환 후 포인트</span>
              <strong>{{ formatNumber(expectedPointBalance) }}P</strong>
            </div>
            <div class="d-flex justify-content-between">
              <span>전환 후 전자지갑 잔액</span>
              <strong>{{ formatNumber(expectedWalletBalance) }}원</strong>
            </div>
          </div>

          <button type="submit" class="btn btn-warning w-100" :disabled="loading">
            전자지갑으로 전환
          </button>
        </form>
      </div>
    </section>

    <section v-if="conversionResult" class="card">
      <div class="card-header fw-bold">최근 전환 결과</div>
      <div class="card-body">
        <div>전환 포인트: {{ formatNumber(conversionResult.convertedPoint) }}P</div>
        <div>남은 포인트: {{ formatNumber(conversionResult.pointBalance) }}P</div>
        <div>전자지갑 잔액: {{ formatNumber(conversionResult.walletBalance) }}원</div>
        <div>전환 일시: {{ conversionResult.convertedAt }}</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import pointWalletApi from '@/api/pointWalletApi';
import walletApi from '@/api/walletApi';
import { formatNumber, getApiErrorMessage } from '@/util/pointWallet';

const TEMPORARY_USER_ID = 1;
const MINIMUM_POINT = 100;

const pointWallet = ref(null);
const wallet = ref(null);
const pointAmount = ref(MINIMUM_POINT);
const conversionResult = ref(null);
const loading = ref(false);
const message = ref('');
const messageType = ref('success');
const quickAmounts = [100, 200, 500];

const expectedPointBalance = computed(() => {
  return Math.max(Number(pointWallet.value?.pointBalance ?? 0) - Number(pointAmount.value ?? 0), 0);
});

const expectedWalletBalance = computed(() => {
  return Number(wallet.value?.balance ?? 0) + Math.max(Number(pointAmount.value ?? 0), 0);
});

const loadBalances = async () => {
  const [pointWalletData, walletData] = await Promise.all([
    pointWalletApi.getWallet(),
    walletApi.getWalletByUserId(TEMPORARY_USER_ID),
  ]);

  pointWallet.value = pointWalletData;
  wallet.value = walletData;
};

const setMaximumAmount = () => {
  pointAmount.value = Number(pointWallet.value?.pointBalance ?? 0);
};

const validatePointAmount = () => {
  const amount = Number(pointAmount.value);
  const balance = Number(pointWallet.value?.pointBalance ?? 0);

  if (!Number.isInteger(amount)) {
    return '전환 포인트는 정수로 입력해야 합니다.';
  }
  if (amount < MINIMUM_POINT) {
    return `최소 ${MINIMUM_POINT}P부터 전환할 수 있습니다.`;
  }
  if (amount > balance) {
    return '보유 포인트보다 많은 금액은 전환할 수 없습니다.';
  }
  return '';
};

const submitConversion = async () => {
  message.value = '';
  const validationMessage = validatePointAmount();

  if (validationMessage) {
    messageType.value = 'error';
    message.value = validationMessage;
    return;
  }

  loading.value = true;

  try {
    const result = await pointWalletApi.convertPoints(pointAmount.value);
    conversionResult.value = result;
    messageType.value = 'success';
    message.value = `${formatNumber(result.convertedPoint)}P가 전자지갑으로 전환되었습니다.`;
    await loadBalances();
    pointAmount.value = MINIMUM_POINT;
  } catch (error) {
    messageType.value = 'error';
    message.value = getApiErrorMessage(error, '포인트 전환에 실패했습니다.');
  } finally {
    loading.value = false;
  }
};

const initialize = async () => {
  loading.value = true;
  try {
    await loadBalances();
  } catch (error) {
    messageType.value = 'error';
    message.value = getApiErrorMessage(error, '지갑 잔액을 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
};

onMounted(initialize);
</script>
