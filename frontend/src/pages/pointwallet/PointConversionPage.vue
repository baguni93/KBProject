<template>
  <main class="conversion-page">
    <PageHeader title="포인트 전환" :showBack="true" :customBack="true" @back="goToPointWallet" />

    <div class="conversion-content">
      <section class="balance-card kb-card">
        <div class="balance-info">
          <div class="balance-label text-13-bold">내 포인트</div>

          <div class="balance-value text-28-bold">
            {{ formatNumber(pointWallet?.pointBalance) }}<span class="text-18-bold">P</span>
          </div>

          <div class="balance-sub text-13">현금처럼 전환해서 사용할 수 있어요</div>
        </div>

        <div class="point-symbol">P</div>
      </section>

      <section class="kb-section conversion-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">전환할 포인트</h2>

          <button type="button" class="policy-button" aria-label="포인트 전환 정책 보기" @click="showPolicy = true">
            <i class="fa-solid fa-circle-info"></i>
          </button>
        </div>

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

          <div class="minimum-guide text-13">최소 {{ formatNumber(MINIMUM_POINT) }}P부터 전환할 수 있어요.</div>

          <div class="quick-amounts">
            <button
                v-for="amount in quickAmounts"
                :key="amount"
                type="button"
                class="content-btn secondary"
                :disabled="loading || !canAddQuickAmount(amount)"
                @click="addPointAmount(amount)"
            >
              +{{ formatNumber(amount) }}
            </button>

            <button type="button" class="content-btn secondary" :disabled="loading || maximumPointAmount === 0" @click="setMaximumAmount">전액</button>
          </div>

          <div class="expected-box">
            <div>
              <span class="text-13">전환 후 포인트</span>
              <strong class="text-13-bold">{{ formatNumber(expectedPointBalance) }}P</strong>
            </div>

            <div>
              <span class="text-13">전자지갑 예상 잔액</span>
              <strong class="text-13-bold">{{ formatNumber(expectedWalletBalance) }}원</strong>
            </div>
          </div>

          <button type="submit" class="content-btn primary conversion-submit" :disabled="loading">
            {{ loading ? '전환 중...' : '지갑에 전환' }}
          </button>
        </form>
      </section>
    </div>

    <transition name="policy-fade">
      <div v-if="showPolicy" class="policy-overlay" @click.self="showPolicy = false">
        <section class="policy-sheet" role="dialog" aria-modal="true" aria-labelledby="policy-title">
          <div class="policy-handle"></div>

          <div class="policy-header">
            <h2 id="policy-title" class="text-20-bold">포인트 전환 안내</h2>
          </div>

          <div class="policy-list">
            <div class="policy-row">
              <span class="text-15">전환 비율</span>
              <strong class="text-15-bold">1P = 1원</strong>
            </div>

            <div class="policy-row">
              <span class="text-15">최소 전환 포인트</span>
              <strong class="text-15-bold">{{ formatNumber(MINIMUM_POINT) }}P</strong>
            </div>

            <div class="policy-row">
              <span class="text-15">전환 가능 포인트</span>
              <strong class="text-15-bold">보유 포인트 이내</strong>
            </div>
          </div>

          <p class="policy-note text-13">입력한 포인트만큼 전자지갑 잔액에 원 단위로 반영돼요.</p>

          <button type="button" class="bottom-btn policy-confirm" @click="showPolicy = false">확인</button>
        </section>
      </div>
    </transition>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import pointWalletApi from '@/api/pointWalletApi';
import walletApi from '@/api/walletApi';
import { formatNumber, getApiErrorMessage } from '@/util/pointWallet';

const TEMPORARY_USER_ID = 1;
const MINIMUM_POINT = 100;
const router = useRouter();
const pointWallet = ref(null);
const wallet = ref(null);
const pointAmount = ref(MINIMUM_POINT);
const conversionResult = ref(null);
const loading = ref(false);
const message = ref('');
const messageType = ref('success');
const showPolicy = ref(false);
const quickAmounts = [100, 500];

const maximumPointAmount = computed(() => Math.max(Number(pointWallet.value?.pointBalance ?? 0), 0));
const formattedPointAmount = computed(() => pointAmount.value === '' ? '' : formatNumber(pointAmount.value));
const expectedPointBalance = computed(() => Math.max(Number(pointWallet.value?.pointBalance ?? 0) - Number(pointAmount.value ?? 0), 0));
const expectedWalletBalance = computed(() => Number(wallet.value?.balance ?? 0) + Math.max(Number(pointAmount.value ?? 0), 0));

const goToPointWallet = () => router.push('/point-wallet');
const loadBalances = async () => {
  const [pointWalletData, walletData] = await Promise.all([pointWalletApi.getWallet(), walletApi.getWalletByUserId(TEMPORARY_USER_ID)]);
  pointWallet.value = pointWalletData;
  wallet.value = walletData;
};

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

const canAddQuickAmount = (amount) => {
  const currentAmount = Number(pointAmount.value) || 0;
  return currentAmount + amount <= maximumPointAmount.value;
};

const addPointAmount = (amount) => {
  const currentAmount = Number(pointAmount.value) || 0;
  pointAmount.value = clampPointAmount(currentAmount + amount);
};

const setMaximumAmount = () => { pointAmount.value = maximumPointAmount.value; };

const validatePointAmount = () => {
  const amount = Number(pointAmount.value);
  if (!Number.isInteger(amount)) return '전환 포인트는 정수로 입력해야 합니다.';
  if (amount < MINIMUM_POINT) return `최소 ${formatNumber(MINIMUM_POINT)}P부터 전환할 수 있습니다.`;
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

<style scoped>
.conversion-page {
  position: relative;
  min-height: 100%;
  overflow: hidden;

  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.conversion-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

.conversion-content {
  padding: 12px 24px 40px;
}

.balance-card {
  min-height: 132px;
  padding: 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border: 1px solid rgba(255, 188, 46, 0.22);
  background: linear-gradient(135deg, #fff4c6 0%, #fffaf0 100%);
  box-shadow: 0 5px 18px rgba(30, 30, 30, 0.05);
}

.balance-info {
  min-width: 0;
}

.balance-label {
  color: var(--color-text-sub);
}

.balance-value {
  margin-top: 6px;
  color: var(--color-text-main);
  line-height: 1.15;
  letter-spacing: -1px;
}

.balance-value span {
  margin-left: 3px;
  font-size: 17px;
  font-weight: 600;
}

.balance-sub {
  margin-top: 8px;
  color: var(--color-text-sub);
  line-height: 1.45;
  word-break: keep-all;
}

.point-symbol {
  width: 60px;
  height: 60px;
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-text-white);
  font-size: 30px;
  font-weight: 600;
  box-shadow: inset 0 -3px 0 rgba(0, 0, 0, 0.08), 0 6px 14px rgba(242, 170, 16, 0.18);
  animation: point-float 3.2s ease-in-out infinite;
}

.conversion-section {
  margin-top: 22px;
}

.policy-button {
  width: 28px;
  height: 28px;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 18px;
  cursor: pointer;
}

.policy-button:active {
  color: var(--color-primary-border);
}

.conversion-card {
  padding: 18px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: 0 5px 18px rgba(30, 30, 30, 0.04);
}

.amount-input-wrap {
  display: flex;
  align-items: center;
  padding: 8px 0 10px;
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
  color: var(--color-text-main);
}

.minimum-guide {
  margin-top: 8px;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.quick-amounts {
  margin: 14px 0 18px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.quick-amounts .content-btn {
  height: 44px;
  font-size: 15px;
  font-weight: 600;
}

.quick-amounts .content-btn:disabled {
  border-color: var(--color-border-main);
  background: var(--color-bg-disabled);
  color: var(--color-text-disabled);
  cursor: not-allowed;
}

.expected-box {
  margin-bottom: 16px;
  padding: 13px 14px;
  border-radius: 12px;
  background: var(--color-bg-screen);
}

.expected-box div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.expected-box div + div {
  margin-top: 9px;
}

.expected-box span {
  color: var(--color-text-sub);
}

.expected-box strong {
  color: var(--color-text-main);
}

.policy-overlay {
  position: absolute;
  inset: 0;
  z-index: 1000;

  display: flex;
  align-items: flex-end;

  background: rgba(17, 17, 17, 0.48);
}

.policy-sheet {
  width: 100%;
  padding: 10px 24px 24px;

  border-radius: 24px 24px 0 0;
  background: var(--color-bg-page);
}

.policy-handle {
  width: 42px;
  height: 4px;
  margin: 0 auto 22px;
  border-radius: 999px;
  background: var(--color-divider);
}

.policy-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.policy-header h2 {
  margin: 0;
}

.policy-list {
  margin-top: 22px;
  display: flex;
  flex-direction: column;
}

.policy-row {
  min-height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-bottom: 1px solid var(--color-divider);
}

.policy-row span {
  color: var(--color-text-sub);
}

.policy-row strong {
  color: var(--color-text-main);
}

.policy-note {
  margin: 14px 0 0;
  color: var(--color-text-muted);
  line-height: 1.45;
  word-break: keep-all;
}

.policy-confirm {
  margin-top: 22px;
}

.policy-fade-enter-active,
.policy-fade-leave-active {
  transition: opacity 0.2s ease;
}

.policy-fade-enter-from,
.policy-fade-leave-to {
  opacity: 0;
}

.conversion-submit {
  height: 48px;
  font-size: 15px;
  font-weight: 600;
}

@keyframes point-float {
  0%, 18%, 100% { transform: translateY(0) scale(1); }
  7% { transform: translateY(-4px) scale(1.03); }
  12% { transform: translateY(1px) scale(0.99); }
}

@media (prefers-reduced-motion: reduce) {
  .point-symbol {
    animation: none;
  }
}

@media (max-width: 360px) {
  .conversion-content {
    padding-right: 18px;
    padding-left: 18px;
  }

  .balance-card {
    min-height: 122px;
    padding: 18px;
  }

  .point-symbol {
    width: 52px;
    height: 52px;
    flex-basis: 52px;
    font-size: 26px;
  }
}
</style>