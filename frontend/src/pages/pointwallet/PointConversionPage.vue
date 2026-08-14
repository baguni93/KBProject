<template>
  <div class="page-layout conversion-page">
    <PageHeader title="포인트 지갑 전환" />

    <main class="page-content">
      <section class="balance-card kb-card">
        <div>
          <div class="balance-label text-13-bold">내 포인트</div>
          <div class="balance-value text-28-bold">
            {{ formatNumber(pointWallet?.pointBalance) }}<span class="text-18-bold">P</span>
          </div>
          <div class="balance-sub text-13">현금처럼 전환해서 사용할 수 있어요</div>
          <div class="balance-guide text-13">최소 100P부터 전자지갑으로 전환할 수 있어요.</div>
        </div>
        <div class="point-symbol text-28-bold">P</div>
      </section>

      <section class="kb-section">
      <div class="kb-section-title-row"><h2 class="kb-section-title text-20-bold">전환할 포인트</h2><span class="rate-label text-13-bold">1P = 1원</span></div>
      <form class="conversion-card kb-card" @submit.prevent="submitConversion">
        <div class="amount-input-wrap">
          <input
            class="text-30-bold"
            :value="formattedPointAmount"
            type="text"
            inputmode="numeric"
            aria-label="전환할 포인트"
            @input="handlePointAmountInput"
          />
          <span class="text-18-bold">P</span>
        </div>
        <div class="quick-amounts">
          <button v-for="amount in quickAmounts" :key="amount" type="button" class="content-btn secondary" @click="addPointAmount(amount)">+{{ formatNumber(amount) }}</button>
          <button type="button" class="content-btn secondary" @click="setMaximumAmount">전액</button>
        </div>
        <div class="expected-box">
          <div><span class="text-13">전환 후 포인트</span><strong class="text-13-bold">{{ formatNumber(expectedPointBalance) }}P</strong></div>
          <div><span class="text-13">전자지갑 예상 잔액</span><strong class="text-13-bold">{{ formatNumber(expectedWalletBalance) }}원</strong></div>
        </div>
        <button type="submit" class="content-btn primary" :disabled="loading">{{ loading ? '전환 중...' : '지갑에 전환' }}</button>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup>
import PageHeader from '@/components/common/PageHeader.vue';
import { computed, onMounted, ref } from 'vue';
import pointWalletApi from '@/api/pointWalletApi';
import walletApi from '@/api/walletApi';
import { formatNumber, getApiErrorMessage } from '@/util/pointWallet';
const TEMPORARY_USER_ID = 1; const MINIMUM_POINT = 100;
const pointWallet = ref(null); const wallet = ref(null); const pointAmount = ref(MINIMUM_POINT); const conversionResult = ref(null); const loading = ref(false); const message = ref(''); const messageType = ref('success');
const quickAmounts = [100, 500, 1000];
const maximumPointAmount = computed(() => Math.max(Number(pointWallet.value?.pointBalance ?? 0), 0));
const formattedPointAmount = computed(() => pointAmount.value === '' ? '' : formatNumber(pointAmount.value));
const expectedPointBalance = computed(() => Math.max(Number(pointWallet.value?.pointBalance ?? 0) - Number(pointAmount.value ?? 0), 0));
const expectedWalletBalance = computed(() => Number(wallet.value?.balance ?? 0) + Math.max(Number(pointAmount.value ?? 0), 0));
const loadBalances = async () => { const [pointWalletData, walletData] = await Promise.all([pointWalletApi.getWallet(), walletApi.getWalletByUserId(TEMPORARY_USER_ID)]); pointWallet.value = pointWalletData; wallet.value = walletData; };
const clampPointAmount = (amount) => {
  if (!Number.isFinite(amount) || amount <= 0) return 0;
  return Math.min(Math.trunc(amount), maximumPointAmount.value);
};
const handlePointAmountInput = (event) => {
  const digitsOnly = event.target.value.replace(/[^0-9]/g, '');

  if (!digitsOnly) {
    pointAmount.value = '';
    event.target.value = '';
    return;
  }

  pointAmount.value = clampPointAmount(Number(digitsOnly));
  event.target.value = formatNumber(pointAmount.value);
};
const addPointAmount = (amount) => {
  const currentAmount = Number(pointAmount.value) || 0;
  pointAmount.value = clampPointAmount(currentAmount + amount);
};
const setMaximumAmount = () => { pointAmount.value = maximumPointAmount.value; };
const validatePointAmount = () => { const amount = Number(pointAmount.value); if (!Number.isInteger(amount)) return '전환 포인트는 정수로 입력해야 합니다.'; if (amount < MINIMUM_POINT) return `최소 ${formatNumber(MINIMUM_POINT)}P부터 전환할 수 있습니다.`; return ''; };
const submitConversion = async () => { message.value = ''; const validationMessage = validatePointAmount(); if (validationMessage) { messageType.value = 'error'; message.value = validationMessage; return; } loading.value = true; try { const result = await pointWalletApi.convertPoints(pointAmount.value); conversionResult.value = result; messageType.value = 'success'; message.value = `${formatNumber(result.convertedPoint)}P가 전자지갑으로 전환되었습니다.`; await loadBalances(); pointAmount.value = MINIMUM_POINT; } catch (error) { messageType.value = 'error'; message.value = getApiErrorMessage(error, '포인트 전환에 실패했습니다.'); } finally { loading.value = false; } };
const initialize = async () => { loading.value = true; try { await loadBalances(); } catch (error) { messageType.value = 'error'; message.value = getApiErrorMessage(error, '지갑 잔액을 불러오지 못했습니다.'); } finally { loading.value = false; } };
onMounted(initialize);
</script>

<style scoped>
.conversion-page {
  color: var(--color-text-main);
  background: var(--color-bg-screen);
}

/* =========================
   보유 포인트 카드
========================= */

.balance-card {
  min-height: 126px;
  padding: 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #fff4c6 0%, #fffaf0 100%);
}

.balance-label {
  color: var(--color-text-sub);
}

.balance-value {
  margin-top: 2px;
  line-height: 1.2;
  letter-spacing: -1px;
}

.balance-value span {
  margin-left: 3px;
}

.balance-sub {
  margin-top: 6px;
  color: var(--color-text-muted);
}

.balance-guide {
  margin-top: 3px;
  color: var(--color-text-muted);
}

.point-symbol {
  width: 54px;
  height: 54px;
  flex: 0 0 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-text-white);
  box-shadow: inset 0 -3px 0 rgba(0, 0, 0, 0.08);
}

/* =========================
   포인트 전환
========================= */

.rate-label {
  color: var(--color-primary-border);
}

.conversion-card {
  padding: 18px;
  border: 1px solid var(--color-divider);
  box-shadow: none;
}

.amount-input-wrap {
  display: flex;
  align-items: center;
  padding: 6px 0 10px;
  border-bottom: 2px solid var(--color-text-main);
}

.amount-input-wrap input {
  min-width: 0;
  flex: 1;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--color-text-main);
  text-align: right;
}

.amount-input-wrap span {
  margin-left: 8px;
}

.quick-amounts {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 7px;
  margin: 13px 0 18px;
}

.expected-box {
  margin-bottom: 16px;
  padding: 13px;
  border-radius: 12px;
  background: var(--color-bg-screen);
}

.expected-box div {
  display: flex;
  justify-content: space-between;
}

.expected-box div + div {
  margin-top: 9px;
}

.expected-box span {
  color: var(--color-text-sub);
}

/* =========================
   최근 전환 결과
========================= */

.result-card {
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 11px;
  border-color: var(--color-divider);
}

.result-check {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #e9f8f0;
  color: var(--color-success);
}

.result-card > div:nth-child(2) {
  flex: 1;
}

.result-card strong,
.result-card span {
  display: block;
}

.result-card span {
  margin-top: 2px;
  color: var(--color-text-muted);
}
</style>