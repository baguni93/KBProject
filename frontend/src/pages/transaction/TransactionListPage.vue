<template>
  <div class="kb-container py-3">
    <!-- 헤더 -->
    <div class="d-flex align-items-center mb-3 px-1">
      <router-link to="/wallet" class="btn btn-link text-dark p-0 me-2">
        <i class="bi bi-chevron-left fs-4"></i>
      </router-link>
      <h5 class="fw-bold mb-0 text-dark">거래 내역</h5>
    </div>

    <!-- 필터 탭 (전체 / 충전 / 송금 / 결제) -->
    <div class="btn-group w-100 mb-3 shadow-sm rounded-3 overflow-hidden bg-white p-1" role="group">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        type="button"
        class="btn btn-sm py-2 fw-bold border-0 rounded-3"
        :class="selectedType === tab.value ? 'btn-dark' : 'btn-light text-secondary'"
        @click="changeTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 로딩 스피너 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-warning" role="status"></div>
      <div class="small text-secondary mt-2">거래 내역을 불러오는 중입니다...</div>
    </div>

    <!-- 거래 내역 데이터 없는 경우 -->
    <div v-else-if="transactions.length === 0" class="card border-0 shadow-sm rounded-4 p-5 text-center bg-white my-3">
      <div class="text-secondary mb-2"><i class="bi bi-receipt fs-1"></i></div>
      <h6 class="fw-bold text-dark mb-1">거래 내역이 없습니다.</h6>
      <p class="text-muted small mb-0">KB Pay로 첫 충전이나 송금을 진행해 보세요.</p>
    </div>

    <!-- 거래 내역 리스트 -->
    <div v-else class="list-group shadow-sm border-0 rounded-4 overflow-hidden mb-4">
      <div
        v-for="item in transactions"
        :key="item.transactionId"
        class="list-group-item list-group-item-action p-3 border-0 border-bottom d-flex align-items-center justify-content-between kb-item"
        @click="openReceipt(item.transactionId)"
      >
        <div class="d-flex align-items-center gap-3">
          <div :class="['icon-circle rounded-circle d-flex align-items-center justify-content-center fw-bold', getTypeIconClass(item.transactionType)]">
            <i :class="getTypeIcon(item.transactionType)"></i>
          </div>
          <div>
            <div class="fw-bold text-dark mb-0.5">
              {{ getItemTitle(item) }}
            </div>
            <div class="text-secondary small">
              {{ formatDate(item.createdAt) }} · <span v-if="item.memo" class="text-primary fw-bold">"{{ item.memo }}"</span>
            </div>
          </div>
        </div>

        <div class="text-end">
          <div :class="['fw-extrabold fs-6', getAmountClass(item.transactionType)]">
            {{ getAmountPrefix(item.transactionType) }}{{ formatCurrency(item.amount) }}
          </div>
          <span class="badge bg-light text-secondary border small">{{ item.transactionStatus }}</span>
        </div>
      </div>
    </div>

    <!-- 상세 영수증 팝업 모달 -->
    <ReceiptDetailModal
      :show="showReceiptModal"
      :transactionId="selectedTransactionId"
      @close="showReceiptModal = false"
      @updated="fetchTransactions"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import transactionApi from '@/api/transactionApi';
import ReceiptDetailModal from '@/components/transaction/ReceiptDetailModal.vue';

const userId = ref(1);
const selectedType = ref('');
const transactions = ref([]);
const loading = ref(false);

const showReceiptModal = ref(false);
const selectedTransactionId = ref(null);

const tabs = [
  { label: '전체', value: '' },
  { label: '충전', value: 'CHARGE' },
  { label: '송금', value: 'TRANSFER' },
  { label: '결제', value: 'PAYMENT' },
];

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '0 원';
  return Number(val).toLocaleString('ko-KR') + ' 원';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleString('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const getItemTitle = (item) => {
  if (item.transactionType === 'CHARGE') return 'KB Pay 머니 충전';
  if (item.transactionType === 'PAYMENT') return '가맹점 현장 결제';
  if (item.transactionType === 'TRANSFER') {
    return item.receiverName ? `송금 (${item.receiverName})` : '송금 완료';
  }
  return item.transactionType;
};

const getTypeIcon = (type) => {
  switch (type) {
    case 'CHARGE': return 'bi bi-plus-circle-fill';
    case 'TRANSFER': return 'bi bi-send-fill';
    case 'PAYMENT': return 'bi bi-bag-check-fill';
    default: return 'bi bi-arrow-left-right';
  }
};

const getTypeIconClass = (type) => {
  switch (type) {
    case 'CHARGE': return 'bg-warning-subtle text-warning';
    case 'TRANSFER': return 'bg-primary-subtle text-primary';
    case 'PAYMENT': return 'bg-dark text-white';
    default: return 'bg-light text-secondary';
  }
};

const getAmountClass = (type) => {
  if (type === 'CHARGE') return 'text-success';
  return 'text-dark';
};

const getAmountPrefix = (type) => {
  if (type === 'CHARGE') return '+';
  return '-';
};

const changeTab = (val) => {
  selectedType.value = val;
  fetchTransactions();
};

const fetchTransactions = async () => {
  loading.value = true;
  try {
    const list = await transactionApi.getTransactions(userId.value, selectedType.value);
    transactions.value = list || [];
  } catch (err) {
    console.error('Fetch transactions error:', err);
  } finally {
    loading.value = false;
  }
};

const openReceipt = (transactionId) => {
  selectedTransactionId.value = transactionId;
  showReceiptModal.value = true;
};

onMounted(() => {
  fetchTransactions();
});
</script>

<style scoped>
.kb-container {
  max-width: 500px;
  margin: 0 auto;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
}
.icon-circle {
  width: 42px;
  height: 42px;
  flex-shrink: 0;
}
.kb-item {
  cursor: pointer;
  transition: background-color 0.15s ease;
}
.kb-item:hover {
  background-color: #f8fafc;
}
.fw-extrabold {
  font-weight: 800;
}
</style>
