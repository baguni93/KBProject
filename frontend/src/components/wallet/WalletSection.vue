<template>
  <div class="wallet-section-container">
    <!-- 섹션 헤더 -->
    <div class="section-header">
      <div class="d-flex align-items-center gap-2">
        <div class="section-icon-badge">
          <i class="bi bi-receipt-cutoff"></i>
        </div>
        <h6 class="section-title">최근 거래 내역</h6>
      </div>
      <router-link to="/transactions" class="more-link">
        전체보기 <i class="bi bi-chevron-right"></i>
      </router-link>
    </div>

    <!-- 필터 탭 -->
    <div class="filter-tab-row">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        type="button"
        class="filter-tab-btn"
        :class="{ active: selectedType === tab.value }"
        @click="changeTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 로딩 중 -->
    <div v-if="loading" class="text-center py-4">
      <div class="spinner-border spinner-border-sm text-warning" role="status"></div>
      <div class="small text-muted mt-2">내역을 불러오는 중...</div>
    </div>

    <!-- 내역 없음 -->
    <div v-else-if="transactions.length === 0" class="empty-tx-box">
      <i class="bi bi-receipt empty-icon"></i>
      <p class="empty-text">최근 거래 내역이 없습니다</p>
    </div>

    <!-- 거래 내역 목록 -->
    <div v-else class="tx-list">
      <div
        v-for="item in transactions.slice(0, 3)"
        :key="item.transactionId"
        class="tx-item"
        @click="openReceipt(item.transactionId)"
      >
        <div class="tx-left">
          <div :class="['tx-icon-circle', getTypeIconClass(item.transactionType)]">
            <i :class="getTypeIcon(item.transactionType)"></i>
          </div>
          <div class="tx-info">
            <strong class="tx-title">{{ getItemTitle(item) }}</strong>
            <span class="tx-date">{{ formatDate(item.createdAt) }}</span>
            <span v-if="item.memo" class="tx-memo">"{{ item.memo }}"</span>
          </div>
        </div>

        <div class="tx-right">
          <span :class="['tx-amount', getAmountClass(item.transactionType)]">
            {{ getAmountPrefix(item.transactionType) }}{{ formatCurrency(item.amount) }}
          </span>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import transactionApi from '@/api/transactionApi';

const router = useRouter();
const authStore = useAuthStore();

const props = defineProps({
  userId: {
    type: Number,
    default: null,
  },
});

const selectedType = ref('');
const transactions = ref([]);
const loading = ref(false);

const emit = defineEmits(['open-receipt']);

const tabs = [
  { label: '전체', value: '' },
  { label: '충전', value: 'CHARGE' },
  { label: '송금', value: 'TRANSFER' },
  { label: '결제', value: 'PAYMENT' },
];

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '₩0';
  return '₩' + Number(val).toLocaleString('ko-KR');
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
};

const getItemTitle = (item) => {
  const tType = (item.transactionType || item.type || '').toUpperCase();
  if (tType === 'CHARGE') return item.merchantName || item.merchant_name || item.title || '지갑 충전';
  if (tType === 'TRANSFER' || tType === 'REMIT') {
    return item.receiverName ? `송금 (${item.receiverName})` : (item.title || '송금 완료');
  }
  return (
    item.merchantName ||
    item.merchant_name ||
    item.receiverName ||
    item.storeName ||
    item.placeName ||
    item.targetName ||
    item.merchant ||
    item.title ||
    item.name ||
    item.memo ||
    item.description ||
    '현장 결제'
  );
};

const getTypeIcon = (type) => {
  const tStr = (type || '').toUpperCase();
  if (tStr.includes('CHARGE')) return 'bi bi-plus-lg';
  if (tStr.includes('TRANSFER') || tStr.includes('REMIT')) return 'bi bi-send-fill';
  if (tStr.includes('PAY')) return 'bi bi-bag-check-fill';
  return 'bi bi-arrow-left-right';
};

const getTypeIconClass = (type) => {
  const tStr = (type || '').toUpperCase();
  if (tStr.includes('CHARGE')) return 'yellow';
  if (tStr.includes('TRANSFER') || tStr.includes('REMIT')) return 'blue';
  if (tStr.includes('PAY')) return 'dark';
  return 'gray';
};

const getAmountClass = (type) => {
  const tStr = (type || '').toUpperCase();
  if (tStr.includes('CHARGE')) return 'plus';
  return 'minus';
};

const getAmountPrefix = (type) => {
  const tStr = (type || '').toUpperCase();
  if (tStr.includes('CHARGE')) return '+';
  return '-';
};

const changeTab = (val) => {
  selectedType.value = val;
  fetchTransactions();
};

const fetchTransactions = async () => {
  loading.value = true;
  try {
    const targetUserId = props.userId || authStore.userId;
    if (!targetUserId) return;

    const list = await transactionApi.getTransactions(targetUserId);
    if (list && Array.isArray(list)) {
      let filtered = list;
      if (selectedType.value) {
        filtered = list.filter(t => {
          const typeStr = (t.transactionType || t.type || '').toUpperCase();
          if (selectedType.value === 'CHARGE') return typeStr.includes('CHARGE');
          if (selectedType.value === 'TRANSFER') return typeStr.includes('TRANSFER') || typeStr.includes('REMIT');
          if (selectedType.value === 'PAYMENT') return typeStr.includes('PAY');
          return true;
        });
      }

      // 최신순 정렬 (날짜 기준 내림차순)
      filtered.sort((a, b) => {
        const dateA = new Date(a.createdAt || a.transactionDate || a.date || 0);
        const dateB = new Date(b.createdAt || b.transactionDate || b.date || 0);
        return dateB - dateA;
      });

      transactions.value = filtered;
    }
  } catch (err) {
    console.log('WalletSection 거래내역 로드 예외', err);
  } finally {
    loading.value = false;
  }
};

const addTransaction = (newTx) => {
  transactions.value.unshift(newTx);
};

defineExpose({
  fetchTransactions,
  addTransaction
});

const openReceipt = (transactionId) => {
  emit('open-receipt', transactionId);
};

onMounted(() => {
  fetchTransactions();
});
</script>

<style scoped>
.wallet-section-container {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-top: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.section-icon-badge {
  width: 32px;
  height: 32px;
  background: #FFF8E1;
  color: #FFBC00;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

.section-title {
  font-size: 0.95rem;
  font-weight: 800;
  color: #1A1A2E;
  margin: 0;
}

.more-link {
  font-size: 0.78rem;
  font-weight: 700;
  color: #94A3B8;
  text-decoration: none;
  transition: color 0.15s ease;
}
.more-link:hover {
  color: #FFBC00;
}

/* 필터 탭 */
.filter-tab-row {
  display: flex;
  gap: 6px;
  margin-bottom: 14px;
  background: #F8FAFC;
  padding: 4px;
  border-radius: 12px;
}

.filter-tab-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 7px 0;
  font-size: 0.78rem;
  font-weight: 700;
  color: #94A3B8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-tab-btn.active {
  background: #fff;
  color: #1A1A2E;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 빈 목록 */
.empty-tx-box {
  padding: 32px 0;
  text-align: center;
}
.empty-icon {
  font-size: 2rem;
  color: #CBD5E1;
  margin-bottom: 8px;
  display: block;
}
.empty-text {
  font-size: 0.82rem;
  color: #94A3B8;
  margin: 0;
}

/* 리스트 */
.tx-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tx-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  background: #FAFAFA;
  border-radius: 14px;
  border: 1px solid #F1F5F9;
  cursor: pointer;
  transition: all 0.15s ease;
}
.tx-item:hover {
  background: #FFFBEB;
  border-color: #FFBC00;
}

.tx-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tx-icon-circle {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  flex-shrink: 0;
}
.tx-icon-circle.yellow { background: #FFF8E1; color: #FFBC00; }
.tx-icon-circle.blue { background: #EFF6FF; color: #3B82F6; }
.tx-icon-circle.dark { background: #1A1A2E; color: #fff; }
.tx-icon-circle.gray { background: #F1F5F9; color: #64748B; }

.tx-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.tx-title {
  font-size: 0.85rem;
  font-weight: 800;
  color: #1A1A2E;
}
.tx-date {
  font-size: 0.72rem;
  color: #94A3B8;
}
.tx-memo {
  font-size: 0.72rem;
  color: #FF9900;
  font-weight: 700;
}

.tx-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.tx-amount {
  font-size: 0.92rem;
  font-weight: 900;
}
.tx-amount.plus { color: #10B981; }
.tx-amount.minus { color: #1A1A2E; }
</style>
