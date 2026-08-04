<template>
  <div class="kb-mobile-page conversion-page">
    <header class="kb-app-header">
      <router-link class="kb-icon-button" to="/point-wallet" aria-label="뒤로가기"><i class="fa-solid fa-chevron-left"></i></router-link>
      <h1 class="kb-app-header__title">포인트 지갑 전환</h1>
      <span></span>
    </header>

    <div v-if="message" :class="['kb-toast', messageType === 'success' ? 'kb-toast--success' : 'kb-toast--error']">{{ message }}</div>

    <section class="balance-card kb-card">
      <div>
        <div class="balance-label">내 포인트</div>
        <div class="balance-value">{{ formatNumber(pointWallet?.pointBalance) }}<span>P</span></div>
        <div class="balance-sub">현금처럼 전환해서 사용할 수 있어요</div>
        <div class="balance-guide">최소 100P부터 전자지갑으로 전환할 수 있어요.</div>
      </div>
      <div class="point-symbol">P</div>
    </section>

    <section class="kb-section">
      <div class="kb-section-title-row"><h2 class="kb-section-title">전환할 포인트</h2><span class="rate-label">1P = 1원</span></div>
      <form class="conversion-card kb-card" @submit.prevent="submitConversion">
        <div class="amount-input-wrap">
          <input v-model.number="pointAmount" type="number" min="100" step="1" required aria-label="전환할 포인트" />
          <span>P</span>
        </div>
        <div class="quick-amounts">
          <button v-for="amount in quickAmounts" :key="amount" type="button" @click="addPointAmount(amount)">+{{ formatNumber(amount) }}</button>
          <button type="button" @click="setMaximumAmount">전액</button>
        </div>
        <div class="expected-box">
          <div><span>전환 후 포인트</span><strong>{{ formatNumber(expectedPointBalance) }}P</strong></div>
          <div><span>전자지갑 예상 잔액</span><strong>{{ formatNumber(expectedWalletBalance) }}원</strong></div>
        </div>
        <button type="submit" class="kb-primary-button w-100" :disabled="loading">{{ loading ? '전환 중...' : '지갑에 전환' }}</button>
      </form>
    </section>

    <section v-if="conversionResult" class="kb-section">
      <div class="kb-section-title-row"><h2 class="kb-section-title">최근 전환 결과</h2></div>
      <div class="result-card kb-card">
        <div class="result-check"><i class="fa-solid fa-check"></i></div>
        <div><strong>{{ formatNumber(conversionResult.convertedPoint) }}P 전환 완료</strong><span>{{ conversionResult.convertedAt }}</span></div>
        <strong>{{ formatNumber(conversionResult.walletBalance) }}원</strong>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import pointWalletApi from '@/api/pointWalletApi';
import walletApi from '@/api/walletApi';
import { formatNumber, getApiErrorMessage } from '@/util/pointWallet';
const TEMPORARY_USER_ID = 1; const MINIMUM_POINT = 100;
const pointWallet = ref(null); const wallet = ref(null); const pointAmount = ref(MINIMUM_POINT); const conversionResult = ref(null); const loading = ref(false); const message = ref(''); const messageType = ref('success');
const quickAmounts = [100, 500, 1000];
const expectedPointBalance = computed(() => Math.max(Number(pointWallet.value?.pointBalance ?? 0) - Number(pointAmount.value ?? 0), 0));
const expectedWalletBalance = computed(() => Number(wallet.value?.balance ?? 0) + Math.max(Number(pointAmount.value ?? 0), 0));
const loadBalances = async () => { const [pointWalletData, walletData] = await Promise.all([pointWalletApi.getWallet(), walletApi.getWalletByUserId(TEMPORARY_USER_ID)]); pointWallet.value = pointWalletData; wallet.value = walletData; };
const addPointAmount = (amount) => {
  const currentAmount = Number(pointAmount.value);
  pointAmount.value = (Number.isFinite(currentAmount) ? currentAmount : 0) + amount;
};
const setMaximumAmount = () => { pointAmount.value = Number(pointWallet.value?.pointBalance ?? 0); };
const validatePointAmount = () => { const amount = Number(pointAmount.value); const balance = Number(pointWallet.value?.pointBalance ?? 0); if (!Number.isInteger(amount)) return '전환 포인트는 정수로 입력해야 합니다.'; if (amount < MINIMUM_POINT) return `최소 ${MINIMUM_POINT}P부터 전환할 수 있습니다.`; if (amount > balance) return '보유 포인트보다 많은 금액은 전환할 수 없습니다.'; return ''; };
const submitConversion = async () => { message.value = ''; const validationMessage = validatePointAmount(); if (validationMessage) { messageType.value = 'error'; message.value = validationMessage; return; } loading.value = true; try { const result = await pointWalletApi.convertPoints(pointAmount.value); conversionResult.value = result; messageType.value = 'success'; message.value = `${formatNumber(result.convertedPoint)}P가 전자지갑으로 전환되었습니다.`; await loadBalances(); pointAmount.value = MINIMUM_POINT; } catch (error) { messageType.value = 'error'; message.value = getApiErrorMessage(error, '포인트 전환에 실패했습니다.'); } finally { loading.value = false; } };
const initialize = async () => { loading.value = true; try { await loadBalances(); } catch (error) { messageType.value = 'error'; message.value = getApiErrorMessage(error, '지갑 잔액을 불러오지 못했습니다.'); } finally { loading.value = false; } };
onMounted(initialize);
</script>

<style scoped>
.balance-card {
  min-height: 126px;
  padding: 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #fff4c6 0%, #fffaf0 100%);
}
.balance-label {
  color: #6c654f;
  font-size: 13px;
  font-weight: 600;
}
.balance-value {
  margin-top: 2px;
  font-size: 32px;
  line-height: 1.2;
  font-weight: 900;
  letter-spacing: -1px;
}
.balance-value span {
  margin-left: 3px;
  font-size: 17px;
  font-weight: 800;
}
.balance-sub {
  margin-top: 6px;
  color: #827b68;
  font-size: 11px;
}
.balance-guide {
  margin-top: 3px;
  color: #9a927f;
  font-size: 10px;
}
.point-symbol {
  width: 54px;
  height: 54px;
  flex: 0 0 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--kb-yellow);
  color: #fff;
  font-size: 28px;
  font-weight: 900;
  box-shadow: inset 0 -3px 0 rgba(0, 0, 0, 0.08);
}
.rate-label{color:#9d7600;font-size:11px;font-weight:700;}.conversion-card{padding:18px;box-shadow:none;border:1px solid #eee;}.amount-input-wrap{display:flex;align-items:center;border-bottom:2px solid #222;padding:6px 0 10px;}.amount-input-wrap input{min-width:0;flex:1;border:0;outline:0;background:transparent;text-align:right;font-size:30px;font-weight:900;}.amount-input-wrap span{margin-left:8px;font-size:18px;font-weight:800;}.quick-amounts{display:grid;grid-template-columns:repeat(4,1fr);gap:7px;margin:13px 0 18px;}.quick-amounts button{height:34px;border:1px solid #e3e3e3;border-radius:9px;background:#fafafa;color:#666;font-size:11px;font-weight:700;}.expected-box{margin-bottom:16px;padding:13px;border-radius:12px;background:#f7f7f7;}.expected-box div{display:flex;justify-content:space-between;font-size:11px;}.expected-box div+div{margin-top:9px;}.expected-box span{color:#777;}.expected-box strong{font-size:12px;}.result-card{padding:14px;display:flex;align-items:center;gap:11px;}.result-check{width:38px;height:38px;display:flex;align-items:center;justify-content:center;border-radius:50%;background:#e9f8f0;color:#1f9d62;}.result-card>div:nth-child(2){flex:1;}.result-card strong,.result-card span{display:block;}.result-card strong{font-size:12px;}.result-card span{margin-top:2px;color:#888;font-size:9px;}
</style>
