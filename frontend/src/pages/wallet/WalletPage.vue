<template>
  <div class="kb-container py-3">
    <!-- 1. 사용자 선택 바 -->
    <div class="d-flex justify-content-between align-items-center mb-3 px-2">
      <div class="d-flex align-items-center gap-2">
        <div class="user-avatar bg-warning text-dark fw-bold rounded-circle d-flex align-items-center justify-content-center">
          <i class="bi bi-person-fill fs-5"></i>
        </div>
        <div>
          <span class="text-muted small d-block" style="font-size: 0.75rem;">KB Pay 회원</span>
          <strong class="text-dark fw-bold">사용자 #{{ currentUserId }}</strong>
        </div>
      </div>

      <!-- Quick User Switcher -->
      <div class="dropdown">
        <button class="btn btn-sm btn-outline-secondary rounded-pill px-3 dropdown-toggle text-dark border" type="button" data-bs-toggle="dropdown">
          계정 전환
        </button>
        <ul class="dropdown-menu dropdown-menu-end shadow border-0 rounded-3">
          <li><a class="dropdown-item py-2" href="#" @click.prevent="switchUser(1)">User #1 (김국민)</a></li>
          <li><a class="dropdown-item py-2" href="#" @click.prevent="switchUser(2)">User #2 (이KB)</a></li>
          <li><a class="dropdown-item py-2" href="#" @click.prevent="switchUser(3)">User #3 (박스타)</a></li>
        </ul>
      </div>
    </div>

    <!-- 2. KB Pay 지갑 카드 -->
    <div class="kb-card shadow-lg mb-4 text-dark position-relative overflow-hidden">
      <div class="card-bg-circle"></div>
      <div class="p-4 position-relative z-1">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <div class="d-flex align-items-center gap-2">
            <span class="badge bg-dark text-warning px-2.5 py-1 rounded-pill fw-bold" style="font-size: 0.7rem;">KB Pay</span>
            <span class="text-secondary small fw-medium">디지털 지갑</span>
          </div>
          <button class="btn btn-link text-secondary p-0" @click="fetchWallet" title="새로고침">
            <i class="bi bi-arrow-clockwise fs-5" :class="{ 'spin-icon': loading }"></i>
          </button>
        </div>

        <div class="my-3">
          <div class="text-secondary small mb-1">지갑 잔액</div>
          <div class="display-6 fw-extrabold text-dark tracking-tight">
            {{ formatCurrency(walletData?.balance) }}
          </div>
        </div>

        <!-- 퀵 버튼 그룹 (충전하기 / 송금하기) -->
        <div class="d-flex gap-2 mt-4 pt-2">
          <button class="btn btn-dark flex-fill py-2.5 rounded-3 fw-bold shadow-sm d-flex align-items-center justify-content-center gap-1.5" @click="openChargeModal">
            <i class="bi bi-plus-circle-fill text-warning"></i> 충전하기
          </button>
          <router-link :to="{ path: '/remittance', query: { walletId: walletData?.walletId || 1 } }" class="btn btn-warning flex-fill py-2.5 rounded-3 fw-bold shadow-sm d-flex align-items-center justify-content-center gap-1.5 text-dark">
            <i class="bi bi-send-fill"></i> 송금하기
          </router-link>
        </div>
      </div>
    </div>

    <!-- 3. 현장 결제 코드 퀵 메뉴 -->
    <div class="row g-3 mb-4">
      <div class="col-6">
        <div class="quick-action-card p-3 rounded-4 bg-white border shadow-sm cursor-pointer" @click="openPaymentModal('barcode')">
          <div class="icon-box bg-dark text-warning mb-2 rounded-3 d-flex align-items-center justify-content-center">
            <i class="bi bi-upc-scan fs-4"></i>
          </div>
          <h6 class="fw-bold mb-0 text-dark">바코드 결제</h6>
          <span class="text-muted small" style="font-size: 0.75rem;">1회용 3분 보안 바코드</span>
        </div>
      </div>
      <div class="col-6">
        <div class="quick-action-card p-3 rounded-4 bg-white border shadow-sm cursor-pointer" @click="openPaymentModal('qr')">
          <div class="icon-box bg-warning-subtle text-warning-emphasis mb-2 rounded-3 d-flex align-items-center justify-content-center">
            <i class="bi bi-qr-code-scan fs-4"></i>
          </div>
          <h6 class="fw-bold mb-0 text-dark">QR 코드 결제</h6>
          <span class="text-muted small" style="font-size: 0.75rem;">1회용 3분 보안 QR코드</span>
        </div>
      </div>
    </div>

    <!-- 4. 1회용 QR / 바코드 결제 모달 -->
    <PaymentCodeModal
      v-if="showPaymentModal"
      :userId="currentUserId"
      :initialTab="selectedPaymentMode"
      @close="showPaymentModal = false"
      @paymentSuccess="fetchWallet"
    />

    <!-- 5. 충전 모달 -->
    <div v-if="showChargeModal" class="kb-modal-backdrop" @click.self="showChargeModal = false">
      <div class="kb-modal-content bg-white rounded-4 shadow-lg p-4 animate__animated animate__fadeInUp">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-bold text-dark mb-0">지갑 머니 충전</h5>
          <button type="button" class="btn-close" @click="showChargeModal = false"></button>
        </div>

        <form @submit.prevent="executeCharge">
          <div class="mb-3">
            <label class="form-label text-secondary small fw-bold">출금 계좌 선택</label>
            <select v-model="chargeForm.bankCode" class="form-select form-select-lg fs-6 border-2">
              <option value="004">KB국민은행 (123-456-7890)</option>
              <option value="088">신한은행 (110-234-5678)</option>
              <option value="020">우리은행 (1002-345-6789)</option>
              <option value="011">NH농협은행 (302-1234-5678)</option>
              <option value="090">카카오뱅크 (3333-01-23456)</option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label text-secondary small fw-bold">충전 금액</label>
            <div class="input-group input-group-lg">
              <input
                type="number"
                v-model.number="chargeForm.amount"
                class="form-control fw-bold border-2"
                placeholder="충전할 금액 입력"
                min="1000"
                step="1000"
                required
              />
              <span class="input-group-text bg-light fw-bold">원</span>
            </div>
            <div class="d-flex gap-1.5 mt-2">
              <button type="button" class="btn btn-sm btn-outline-dark rounded-pill flex-fill" @click="addChargeAmount(10000)">+1만원</button>
              <button type="button" class="btn btn-sm btn-outline-dark rounded-pill flex-fill" @click="addChargeAmount(30000)">+3만원</button>
              <button type="button" class="btn btn-sm btn-outline-dark rounded-pill flex-fill" @click="addChargeAmount(50000)">+5만원</button>
              <button type="button" class="btn btn-sm btn-outline-dark rounded-pill flex-fill" @click="addChargeAmount(100000)">+10만원</button>
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label text-secondary small fw-bold">메모 (선택)</label>
            <input type="text" v-model="chargeForm.memo" class="form-control" placeholder="예: 용돈 충전" />
          </div>

          <button type="submit" class="btn btn-warning w-100 py-3 fw-bold rounded-3 fs-6 shadow-sm" :disabled="charging">
            <span v-if="charging" class="spinner-border spinner-border-sm me-2"></span>
            충전 완료하기
          </button>
        </form>
      </div>
    </div>

    <!-- 알림 메시지 (Toast) -->
    <div v-if="statusMessage" :class="['alert', isSuccess ? 'alert-success' : 'alert-danger', 'rounded-3 shadow-sm border-0 mb-3 d-flex align-items-center justify-content-between']">
      <span>{{ statusMessage }}</span>
      <button type="button" class="btn-close ms-2" @click="statusMessage = ''"></button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import walletApi from '@/api/walletApi';
import PaymentCodeModal from '@/components/wallet/PaymentCodeModal.vue';

const currentUserId = ref(1);
const walletData = ref(null);
const loading = ref(false);
const charging = ref(false);
const showChargeModal = ref(false);
const showPaymentModal = ref(false);
const selectedPaymentMode = ref('barcode'); // 'barcode' | 'qr'
const statusMessage = ref('');
const isSuccess = ref(true);

const chargeForm = reactive({
  bankCode: '004',
  accountNumber: '123-456-7890',
  amount: 50000,
  memo: '',
});

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '0 원';
  return Number(val).toLocaleString('ko-KR') + ' 원';
};

const switchUser = (userId) => {
  currentUserId.value = userId;
  fetchWallet();
};

const fetchWallet = async () => {
  loading.value = true;
  statusMessage.value = '';
  try {
    const data = await walletApi.getWalletByUserId(currentUserId.value);
    walletData.value = data;
  } catch (err) {
    console.error('Wallet fetch error:', err);
  } finally {
    loading.value = false;
  }
};

const openPaymentModal = (mode = 'barcode') => {
  selectedPaymentMode.value = mode;
  showPaymentModal.value = true;
};

const openChargeModal = () => {
  chargeForm.amount = 50000;
  chargeForm.memo = '';
  showChargeModal.value = true;
};

const addChargeAmount = (val) => {
  chargeForm.amount = (chargeForm.amount || 0) + val;
};

const executeCharge = async () => {
  charging.value = true;
  statusMessage.value = '';

  try {
    const payload = {
      userId: currentUserId.value,
      walletId: walletData.value?.walletId,
      amount: chargeForm.amount,
      bankCode: chargeForm.bankCode,
      accountNumber: chargeForm.accountNumber,
      memo: chargeForm.memo || 'KB Pay 연동계좌 충전',
    };

    await walletApi.chargeWallet(payload);
    isSuccess.value = true;
    statusMessage.value = `${Number(chargeForm.amount).toLocaleString('ko-KR')}원이 지갑에 충전되었습니다.`;
    showChargeModal.value = false;
    await fetchWallet();
  } catch (err) {
    console.error('Charge error:', err);
    isSuccess.value = false;
    statusMessage.value = err.response?.data?.message || '충전 실패: 입력 정보를 확인해주세요.';
  } finally {
    charging.value = false;
  }
};

onMounted(() => {
  fetchWallet();
});
</script>

<style scoped>
.kb-container {
  max-width: 500px;
  margin: 0 auto;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
}
.user-avatar {
  width: 40px;
  height: 40px;
}
.kb-card {
  background: linear-gradient(135deg, #ffbc00 0%, #ffaa00 100%);
  border-radius: 24px;
  box-shadow: 0 12px 30px rgba(255, 188, 0, 0.3) !important;
}
.card-bg-circle {
  position: absolute;
  right: -20px;
  bottom: -30px;
  width: 180px;
  height: 180px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  pointer-events: none;
}
.quick-action-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.quick-action-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08) !important;
}
.icon-box {
  width: 44px;
  height: 44px;
}
.kb-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  padding: 16px;
}
.kb-modal-content {
  width: 100%;
  max-width: 440px;
}
.spin-icon {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.cursor-pointer {
  cursor: pointer;
}
</style>
