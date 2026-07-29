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

        <!-- 3-B. 계좌 송금 -->
        <div v-else class="mb-4">
          <label class="form-label text-secondary small fw-bold">입금 은행 및 계좌번호</label>
          <div class="row g-2 mb-2">
            <div class="col-5">
              <select v-model="form.bankCode" class="form-select form-select-lg fs-6 border-2">
                <option value="004">KB국민</option>
                <option value="088">신한</option>
                <option value="020">우리</option>
                <option value="011">NH농협</option>
                <option value="090">카카오뱅크</option>
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

        <!-- 5. 받는 분 표기 / 메모 -->
        <div class="mb-4">
          <label class="form-label text-secondary small fw-bold">받는 분 내통장 표기 (메모)</label>
          <input
            type="text"
            v-model="form.memo"
            class="form-control border-2"
            placeholder="예: 축의금, 점심값 (선택)"
          />
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
      </div>
      <router-link v-if="isSuccess" to="/wallet" class="btn btn-sm btn-dark rounded-pill px-3">지갑 보기</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import remittanceApi from '@/api/remittanceApi';
import walletApi from '@/api/walletApi';

const route = useRoute();
const myBalance = ref(0);
const loading = ref(false);
const statusMessage = ref('');
const isSuccess = ref(false);

const form = reactive({
  walletId: 1,
  receiverType: 'WALLET',
  receiverId: 2,
  bankCode: '004',
  accountNumber: '',
  amount: 10000,
  memo: '',
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

const confirmTransfer = async () => {
  if (form.amount > myBalance.value) {
    if (!confirm('지갑 잔액이 부족합니다. 계속 진행하시겠습니까?')) {
      return;
    }
  }

  loading.value = true;
  statusMessage.value = '';
  isSuccess.value = false;

  const payload = {
    walletId: form.walletId,
    receiverType: form.receiverType,
    amount: form.amount,
    memo: form.memo,
  };

  if (form.receiverType === 'WALLET') {
    payload.receiverId = form.receiverId;
  } else {
    payload.bankCode = form.bankCode;
    payload.accountNumber = form.accountNumber;
  }

  try {
    await remittanceApi.sendMoney(payload);
    isSuccess.value = true;
    const targetName = form.receiverType === 'WALLET' ? `친구 #${form.receiverId}` : `계좌(${form.accountNumber})`;
    statusMessage.value = `${targetName}님에게 ${Number(form.amount).toLocaleString('ko-KR')}원을 송금했습니다.`;
    await fetchMyBalance();
  } catch (err) {
    console.error('Remittance Error:', err);
    isSuccess.value = false;
    statusMessage.value = err.response?.data?.message || '송금 실패: 출금 잔액 및 입력 정보를 확인해 주세요.';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  if (route.query.walletId) {
    form.walletId = Number(route.query.walletId);
  }
  fetchMyBalance();
});
</script>

<style scoped>
.kb-container {
  max-width: 500px;
  margin: 0 auto;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
}
</style>
