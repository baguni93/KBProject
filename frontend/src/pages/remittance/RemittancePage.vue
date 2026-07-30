<template>
  <div class="kb-container py-3">
    <!-- 헤더 -->
    <div class="d-flex align-items-center mb-3 px-1">
      <router-link to="/wallet" class="btn btn-link text-dark p-0 me-2">
        <i class="bi bi-chevron-left fs-4"></i>
      </router-link>
      <h5 class="fw-bold mb-0 text-dark">송금하기</h5>
    </div>

    <!-- 송금 폼 카드 -->
    <div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
      <form @submit.prevent="confirmTransfer">
        <!-- 1. 보낼 출금 정보 (내 지갑) -->
        <div class="mb-4 p-3 bg-light rounded-3 d-flex justify-content-between align-items-center">
          <div>
            <span class="text-secondary small d-block">출금 지갑</span>
            <strong class="text-dark">KB Pay 지갑 (#{{ form.walletId }})</strong>
          </div>
          <div class="text-end">
            <span class="text-secondary small d-block">잔액</span>
            <span class="fw-bold text-dark">{{ formatCurrency(myBalance) }}</span>
          </div>
        </div>

        <!-- 2. 송금 유형 선택 (친구 송금 vs 계좌 송금) -->
        <div class="mb-4">
          <label class="form-label text-secondary small fw-bold">송금 방식</label>
          <div class="btn-group w-100" role="group">
            <input
              type="radio"
              class="btn-check"
              name="receiverType"
              id="typeFriend"
              value="WALLET"
              v-model="form.receiverType"
            />
            <label class="btn btn-outline-dark py-2.5 fw-bold" for="typeFriend">
              <i class="bi bi-person-heart me-1"></i> 친구 송금
            </label>

            <input
              type="radio"
              class="btn-check"
              name="receiverType"
              id="typeAccount"
              value="ACCOUNT"
              v-model="form.receiverType"
            />
            <label class="btn btn-outline-dark py-2.5 fw-bold" for="typeAccount">
              <i class="bi bi-bank me-1"></i> 계좌 송금
            </label>
          </div>
        </div>

        <!-- 3. 받으실 분 입력 (유형별 분기) -->
        <!-- 3-A. 친구 송금 -->
        <div v-if="form.receiverType === 'WALLET'" class="mb-4">
          <label class="form-label text-secondary small fw-bold">받는 친구 (회원 ID)</label>
          <input
            type="number"
            v-model.number="form.receiverId"
            class="form-control form-control-lg fs-6 border-2"
            placeholder="친구 회원 번호 입력 (예: 2)"
            required
          />
        </div>

        <!-- 3-B. 계좌 송금 & 최근 송금 계좌 -->
        <div v-else class="mb-4">
          <!-- 최근 송금 계좌 리스트 -->
          <div class="mb-3">
            <div class="mb-2">
              <span class="text-secondary small fw-bold">최근 송금 계좌</span>
            </div>
            <div class="d-flex flex-column gap-2">
              <div
                v-for="(acc, idx) in recentAccounts"
                :key="idx"
                class="p-2.5 rounded-3 border bg-light d-flex align-items-center justify-content-between cursor-pointer recent-acc-item"
                :class="{ 'border-warning bg-warning-subtle shadow-sm': form.accountNumber === acc.accountNumber }"
                @click="selectRecentAccount(acc)"
              >
                <div class="d-flex align-items-center gap-2.5">
                  <div class="bank-badge bg-warning text-dark fw-bold rounded-circle d-flex align-items-center justify-content-center" style="width: 36px; height: 36px; font-size: 0.75rem;">
                    {{ acc.bankName?.substring(0, 2) || '은행' }}
                  </div>
                  <div>
                    <div class="fw-bold text-dark small mb-0">{{ acc.ownerName }} ({{ acc.bankName }})</div>
                    <div class="text-secondary font-monospace" style="font-size: 0.8rem;">{{ acc.accountNumber }}</div>
                  </div>
                </div>
                <i class="bi bi-chevron-right text-muted small"></i>
              </div>
            </div>
          </div>

          <!-- 입금 은행 및 계좌번호 직접 입력 -->
          <label class="form-label text-secondary small fw-bold mt-2">입금 은행 및 계좌번호</label>
          <div class="row g-2 mb-2">
            <div class="col-5">
              <select v-model="form.bankCode" class="form-select form-select-lg fs-6 border-2">
                <option v-for="b in bankList" :key="b.bankCode" :value="b.bankCode">
                  {{ b.bankName }}
                </option>
              </select>
            </div>
            <div class="col-7">
              <input
                type="text"
                v-model="form.accountNumber"
                class="form-control form-control-lg fs-6 border-2"
                placeholder="계좌번호 (- 없이 입력)"
                required
              />
            </div>
          </div>
        </div>

        <!-- 4. 송금 금액 -->
        <div class="mb-4">
          <label class="form-label text-secondary small fw-bold">보낼 금액</label>
          <div class="input-group input-group-lg">
            <input
              type="number"
              v-model.number="form.amount"
              class="form-control fw-bold border-2"
              placeholder="0"
              min="1"
              required
            />
            <span class="input-group-text bg-light fw-bold">원</span>
          </div>
          <!-- 퀵 금액 버튼 -->
          <div class="d-flex gap-1.5 mt-2">
            <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill flex-fill" @click="addAmount(10000)">+1만</button>
            <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill flex-fill" @click="addAmount(30000)">+3만</button>
            <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill flex-fill" @click="addAmount(50000)">+5만</button>
            <button type="button" class="btn btn-sm btn-outline-secondary rounded-pill flex-fill" @click="addAmount(100000)">+10만</button>
          </div>
        </div>

        <!-- 5. 메모 및 피드 공유 설정 -->
        <div class="mb-3">
          <label class="form-label text-secondary small fw-bold">송금 메모 (피드 내용)</label>
          <input
            type="text"
            v-model="form.memo"
            class="form-control border-2"
            placeholder="예: 축의금, 점심값 송금 완료!"
          />
        </div>

        <!-- 6. 피드 공개 설정 (visibility) -->
        <div class="mb-4">
          <label class="form-label text-secondary small fw-bold">피드 공유 공개 범위 (visibility)</label>
          <select v-model="form.visibility" class="form-select border-2">
            <option value="PUBLIC">전체 공개 (PUBLIC)</option>
            <option value="FRIENDS">친구 공개 (FRIENDS)</option>
            <option value="PRIVATE">나만 보기 (PRIVATE)</option>
          </select>
        </div>

        <!-- 송금 실행 버튼 -->
        <button
          type="submit"
          class="btn btn-warning w-100 py-3 fw-bold rounded-3 fs-6 shadow-sm text-dark"
          :disabled="loading"
        >
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          송금하기
        </button>
      </form>
    </div>

    <!-- 성공 / 실패 통지 Toast -->
    <div v-if="statusMessage" :class="['alert', isSuccess ? 'alert-success' : 'alert-danger', 'rounded-3 shadow-sm border-0 mb-3 d-flex align-items-center justify-content-between']">
      <div>
        <h6 class="fw-bold mb-1">{{ isSuccess ? '송금이 완료되었습니다.' : '송금 실패' }}</h6>
        <div class="small">{{ statusMessage }}</div>
        <div v-if="lastTransactionId" class="small text-muted font-monospace mt-1">
          거래 번호: #{{ lastTransactionId }} | 피드 타입: {{ lastFeedType }} | 공개 범위: {{ lastVisibility }}
        </div>
      </div>
      <router-link v-if="isSuccess" to="/wallet" class="btn btn-sm btn-dark rounded-pill px-3">지갑 보기</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import remittanceApi from '@/api/remittanceApi';
import walletApi from '@/api/walletApi';

const route = useRoute();
const myBalance = ref(0);
const loading = ref(false);
const statusMessage = ref('');
const isSuccess = ref(false);
const lastTransactionId = ref(null);
const lastFeedType = ref('REMITTANCE');
const lastVisibility = ref('PUBLIC');

const defaultAccounts = [
  { bankCode: '088', bankName: '신한은행', accountNumber: '222-002-000001', ownerName: '이KB' },
  { bankCode: '004', bankName: 'KB국민은행', accountNumber: '110-111-111111', ownerName: '김국민' },
  { bankCode: '020', bankName: '우리은행', accountNumber: '1002-345-6789', ownerName: '박스타' },
];

const recentAccounts = ref([...defaultAccounts]);
const bankList = ref([
  { bankCode: '004', bankName: 'KB국민' },
  { bankCode: '088', bankName: '신한' },
  { bankCode: '020', bankName: '우리' },
  { bankCode: '011', bankName: 'NH농협' },
  { bankCode: '090', bankName: '카카오뱅크' },
]);

const form = reactive({
  walletId: 1,
  receiverType: 'WALLET',
  receiverId: 2,
  bankCode: '088',
  accountNumber: '222-002-000001',
  amount: 10000,
  memo: '',
  visibility: 'PUBLIC',
});

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '0 원';
  return Number(val).toLocaleString('ko-KR') + ' 원';
};

const addAmount = (val) => {
  form.amount = (form.amount || 0) + val;
};

const fetchMyBalance = async () => {
  try {
    const data = await walletApi.getWalletByUserId(form.walletId);
    if (data) {
      myBalance.value = data.balance;
    }
  } catch (e) {
    console.error('Balance fetch error:', e);
  }
};

const fetchRecentBankInfo = async () => {
  try {
    const data = await remittanceApi.getBankRemittanceInfo(form.walletId);
    if (data) {
      if (data.banks && data.banks.length > 0) {
        bankList.value = data.banks;
      }
      if (data.recentAccounts && data.recentAccounts.length > 0) {
        recentAccounts.value = data.recentAccounts.slice(0, 3);
      } else {
        recentAccounts.value = [...defaultAccounts];
      }
    }
  } catch (e) {
    console.error('Fetch recent bank info error:', e);
    recentAccounts.value = [...defaultAccounts];
  }
};

const selectRecentAccount = (acc) => {
  form.bankCode = acc.bankCode;
  form.accountNumber = acc.accountNumber;
};

const confirmTransfer = async () => {
  if (form.amount > myBalance.value) {
    if (!confirm('지갑 잔액이 부족합니다. 계속 진행하시겠습니까?')) {
      return;
    }
  }

  loading.value = true;
  statusMessage.value = '';
  isSuccess.value = false;
  lastTransactionId.value = null;

  const payload = {
    walletId: form.walletId,
    receiverType: form.receiverType,
    amount: form.amount,
    memo: form.memo,
    feedType: 'REMITTANCE',
    content: form.memo || '송금 완료!',
    visibility: form.visibility,
  };

  if (form.receiverType === 'WALLET') {
    payload.receiverId = form.receiverId;
  } else {
    payload.bankCode = form.bankCode;
    payload.accountNumber = form.accountNumber;
  }

  try {
    const res = await remittanceApi.sendMoney(payload);
    isSuccess.value = true;
    if (res) {
      lastTransactionId.value = res.transactionId;
      lastFeedType.value = res.feedType || 'REMITTANCE';
      lastVisibility.value = res.visibility || form.visibility;
    }
    const targetName = form.receiverType === 'WALLET' ? `친구 #${form.receiverId}` : `계좌(${form.accountNumber})`;
    statusMessage.value = `${targetName}님에게 ${Number(form.amount).toLocaleString('ko-KR')}원을 송금했습니다.`;
    await fetchMyBalance();
    await fetchRecentBankInfo();
  } catch (err) {
    console.error('Remittance Error:', err);
    isSuccess.value = false;
    statusMessage.value = err.response?.data?.message || '송금 실패: 출금 잔액 및 입력 정보를 확인해 주세요.';
  } finally {
    loading.value = false;
  }
};

watch(() => form.receiverType, (newVal) => {
  if (newVal === 'ACCOUNT') {
    fetchRecentBankInfo();
  }
});

onMounted(() => {
  if (route.query.walletId) {
    form.walletId = Number(route.query.walletId);
  }
  fetchMyBalance();
  fetchRecentBankInfo();
});
</script>

<style scoped>
.kb-container {
  max-width: 500px;
  margin: 0 auto;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
}
.recent-acc-item {
  transition: all 0.15s ease;
}
.recent-acc-item:hover {
  background-color: #f1f5f9 !important;
  transform: translateY(-1px);
}
.cursor-pointer {
  cursor: pointer;
}
</style>
