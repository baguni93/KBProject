<template>
  <div class="transaction-root">

    <!-- ══════════════════════════════════════════
         상단 헤더
    ══════════════════════════════════════════ -->
    <div class="tx-header">
      <div class="tx-header-inner">
        <router-link to="/wallet" class="back-btn">
          <i class="bi bi-chevron-left"></i>
        </router-link>
        <h4 class="header-title">거래 내역</h4>
        <div class="header-right-placeholder"></div>
      </div>
    </div>

    <div class="tx-body">

      <!-- ══════════════════════════════════════════
           [1] 월 선택 피커 & 소비 인사이트 요약 카드
      ══════════════════════════════════════════ -->
      <div class="month-selector-card">
        <div class="month-picker-row">
          <button class="month-nav-btn" @click="changeMonth(-1)">
            <i class="bi bi-chevron-left"></i>
          </button>
          <div class="current-month-text">
            {{ selectedYear }}년 {{ selectedMonth }}월
          </div>
          <button class="month-nav-btn" :disabled="isCurrentMonth" @click="changeMonth(1)">
            <i class="bi bi-chevron-right"></i>
          </button>
        </div>

        <div class="insight-summary-grid">
          <div class="summary-box expense">
            <span class="summary-label">총 지출</span>
            <span class="summary-amount text-dark">-{{ formatCurrency(summaryExpense) }}</span>
          </div>
          <div class="summary-divider"></div>
          <div class="summary-box income">
            <span class="summary-label">총 수입 / 충전</span>
            <span class="summary-amount text-kb-yellow">+{{ formatCurrency(summaryIncome) }}</span>
          </div>
        </div>
      </div>

      <!-- ══════════════════════════════════════════
           [2] 기간 퀵 필터 + 거래 유형 탭
      ══════════════════════════════════════════ -->
      <div class="filter-section">
        <!-- 기간 선택 칩 -->
        <div class="period-chips">
          <button
            v-for="p in periodOptions"
            :key="p.value"
            class="period-chip"
            :class="{ active: selectedPeriod === p.value }"
            @click="setPeriodFilter(p.value)"
          >
            {{ p.label }}
          </button>
        </div>

        <!-- 거래 유형 세그먼트 탭 -->
        <div class="type-segment-tabs">
          <button
            v-for="tab in typeTabs"
            :key="tab.value"
            class="type-tab-btn"
            :class="{ active: selectedType === tab.value }"
            @click="changeTypeTab(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>

      <!-- ══════════════════════════════════════════
           [3] 로딩 스피너
      ══════════════════════════════════════════ -->
      <div v-if="loading" class="loading-wrap">
        <div class="spinner-border text-warning" role="status"></div>
        <p class="loading-text">거래 내역을 불러오는 중...</p>
      </div>

      <!-- ══════════════════════════════════════════
           [4] 거래 내역 없음
      ══════════════════════════════════════════ -->
      <div v-else-if="filteredGroupedTransactions.length === 0" class="empty-wrap">
        <EmptyList desc="해당 조건에 맞는 거래 내역이 없습니다." />
        <p class="empty-sub">기간 및 거래 유형 필터를 변경하거나 새로운 결제/충전을 진행해 보세요.</p>
      </div>

      <!-- ══════════════════════════════════════════
           [5] 일자별 그룹핑 거래 내역
      ══════════════════════════════════════════ -->
      <div v-else class="tx-list-container">
        <div
          v-for="group in filteredGroupedTransactions"
          :key="group.dateKey"
          class="date-group-card"
        >
          <!-- 날짜 헤더 -->
          <div class="date-header-row">
            <span class="date-title">{{ group.dateDisplay }}</span>
            <span class="date-daily-total">합계 {{ formatCurrency(group.dailySum) }}</span>
          </div>

          <!-- 내역 아이템 리스트 -->
          <div class="date-item-list">
            <div
              v-for="item in group.items"
              :key="item.transactionId"
              class="tx-item-wrapper"
              :class="{ expanded: expandedTxId === item.transactionId }"
            >
              <!-- 거래 내역 행 (클릭 시 영수증 모달 오픈) -->
              <div
                class="tx-item-row"
                @click="openReceiptModal(item)"
              >
                <div class="tx-item-left">
                  <div class="icon-circle" :class="getTypeIconClass(item.transactionType)">
                    <i :class="getTypeIcon(item.transactionType)"></i>
                  </div>
                  <div class="tx-info-text">
                    <div class="tx-item-title">
                      {{ getItemTitle(item) }}
                    </div>
                    <div class="tx-item-sub">
                      {{ formatTime(item.createdAt) }}
                      <span v-if="item.memo" class="memo-badge">"{{ item.memo }}"</span>
                    </div>
                  </div>
                </div>

                <div class="tx-item-right">
                  <div class="tx-amount" :class="getAmountClass(item.transactionType)">
                    {{ getAmountPrefix(item.transactionType) }}{{ formatCurrency(item.amount) }}
                  </div>
                  <div class="expand-indicator">
                    <i class="bi bi-receipt"></i>
                  </div>
                </div>
              </div>

            </div>
          </div>
      </div>
    </div>

    <!-- 영수증 상세 모달 -->
    <ReceiptDetailModal
      :show="showReceiptModal"
      :transactionId="selectedTransactionId"
      @close="showReceiptModal = false"
      @updated="fetchTransactions"
    />

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import transactionApi from '@/api/transactionApi';
import EmptyList from '@/components/common/EmptyList.vue';
import ReceiptDetailModal from '@/components/transaction/ReceiptDetailModal.vue';

const router = useRouter();

const userId = ref(1);
const loading = ref(false);
const rawTransactions = ref([]);

const showReceiptModal = ref(false);
const selectedTransactionId = ref(null);

// 날짜/필터 상태
const now = new Date();
const selectedYear = ref(now.getFullYear());
const selectedMonth = ref(now.getMonth() + 1);

const selectedPeriod = ref('ALL'); // ALL, CURRENT, 1M, 3M
const selectedType = ref(''); // '', CHARGE, TRANSFER, PAYMENT

const expandedTxId = ref(null);

const typeTabs = [
  { label: '전체', value: '' },
  { label: '충전', value: 'CHARGE' },
  { label: '송금', value: 'TRANSFER' },
  { label: '결제', value: 'PAYMENT' },
];

const periodOptions = [
  { label: '전체', value: 'ALL' },
  { label: '당월', value: 'CURRENT' },
  { label: '1개월', value: '1M' },
  { label: '3개월', value: '3M' },
];

const isCurrentMonth = computed(() => {
  return selectedYear.value === now.getFullYear() && selectedMonth.value === now.getMonth() + 1;
});

const changeMonth = (delta) => {
  let m = selectedMonth.value + delta;
  let y = selectedYear.value;
  if (m > 12) {
    m = 1;
    y += 1;
  } else if (m < 1) {
    m = 12;
    y -= 1;
  }
  selectedYear.value = y;
  selectedMonth.value = m;
};

const setPeriodFilter = (val) => {
  selectedPeriod.value = val;
};

const changeTypeTab = (val) => {
  selectedType.value = val;
  fetchTransactions();
};

const openReceiptModal = (item) => {
  if (item && item.transactionId) {
    selectedTransactionId.value = item.transactionId;
    showReceiptModal.value = true;
  }
};

const defaultFallbackTransactions = [
  { transactionId: 101, transactionType: 'PAYMENT', amount: 14500, createdAt: '2026-08-05T12:30:00', memo: '스타벅스 커피', transactionStatus: 'COMPLETED' },
  { transactionId: 102, transactionType: 'TRANSFER', amount: 50000, receiverName: '김민수', createdAt: '2026-08-05T09:15:00', memo: '모임 회비', transactionStatus: 'COMPLETED' },
  { transactionId: 103, transactionType: 'CHARGE', amount: 200000, createdAt: '2026-08-04T18:00:00', memo: '계좌 자동 충전', transactionStatus: 'COMPLETED' },
  { transactionId: 104, transactionType: 'PAYMENT', amount: 32000, createdAt: '2026-08-02T19:40:00', memo: '교보문고 서적', transactionStatus: 'COMPLETED' },
  { transactionId: 105, transactionType: 'TRANSFER', amount: 15000, receiverName: '이지은', createdAt: '2026-07-28T14:20:00', memo: '택시비 정산', transactionStatus: 'COMPLETED' }
];

const fetchTransactions = async () => {
  loading.value = true;
  try {
    const list = await transactionApi.getTransactions(userId.value, selectedType.value);
    const apiList = (list && list.length > 0) ? list : defaultFallbackTransactions;
    const savedCharges = JSON.parse(localStorage.getItem('user_charges') || '[]');
    
    const merged = [...savedCharges, ...apiList];
    const seen = new Set();
    rawTransactions.value = merged.filter(t => {
      if (seen.has(t.transactionId)) return false;
      seen.add(t.transactionId);
      return true;
    });
  } catch (err) {
    const savedCharges = JSON.parse(localStorage.getItem('user_charges') || '[]');
    const merged = [...savedCharges, ...defaultFallbackTransactions];
    const seen = new Set();
    rawTransactions.value = merged.filter(t => {
      if (seen.has(t.transactionId)) return false;
      seen.add(t.transactionId);
      return true;
    });
  } finally {
    loading.value = false;
  }
};

const filteredTransactions = computed(() => {
  return rawTransactions.value.filter((item) => {
    if (selectedType.value && item.transactionType !== selectedType.value) {
      return false;
    }
    return true;
  });
});

const summaryExpense = computed(() => {
  return filteredTransactions.value
    .filter(t => t.transactionType !== 'CHARGE')
    .reduce((sum, t) => sum + Number(t.amount || 0), 0);
});

const summaryIncome = computed(() => {
  return filteredTransactions.value
    .filter(t => t.transactionType === 'CHARGE')
    .reduce((sum, t) => sum + Number(t.amount || 0), 0);
});

const filteredGroupedTransactions = computed(() => {
  const groups = {};

  filteredTransactions.value.forEach((item) => {
    const d = new Date(item.createdAt);
    const dateKey = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    const dateDisplay = `${d.getMonth() + 1}월 ${d.getDate()}일 ${days[d.getDay()]}요일`;

    if (!groups[dateKey]) {
      groups[dateKey] = {
        dateKey,
        dateDisplay,
        items: [],
        dailySum: 0
      };
    }
    groups[dateKey].items.push(item);
    groups[dateKey].dailySum += Number(item.amount || 0);
  });

  return Object.values(groups).sort((a, b) => b.dateKey.localeCompare(a.dateKey));
});

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '0 원';
  return Number(val).toLocaleString('ko-KR') + ' 원';
};

const formatTime = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
};

const getItemTitle = (item) => {
  if (item.transactionType === 'CHARGE') return 'KB Pay 머니 충전';
  if (item.transactionType === 'PAYMENT') return item.memo || '가맹점 현장 결제';
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
    case 'CHARGE': return 'icon-charge';
    case 'TRANSFER': return 'icon-transfer';
    case 'PAYMENT': return 'icon-payment';
    default: return 'icon-default';
  }
};

const getAmountClass = (type) => {
  if (type === 'CHARGE') return 'amount-plus';
  return 'amount-minus';
};

const getAmountPrefix = (type) => {
  if (type === 'CHARGE') return '+';
  return '-';
};

onMounted(() => {
  fetchTransactions();
});
</script>

<style scoped>
.transaction-root {
  min-height: 100vh;
  background-color: #f4f5f8;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
  color: #222;
  padding-bottom: 50px;
}

.tx-header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: #ffffff;
  border-bottom: 1px solid #ebebeb;
}

.tx-header-inner {
  max-width: 500px;
  margin: 0 auto;
  height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-btn {
  font-size: 20px;
  color: #222;
  text-decoration: none;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
}

.header-right-placeholder {
  width: 24px;
}

.tx-body {
  max-width: 500px;
  margin: 0 auto;
  padding: 16px;
}

.month-selector-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.month-picker-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 16px;
}

.month-nav-btn {
  background: transparent;
  border: none;
  font-size: 18px;
  color: #444;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
}
.month-nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.current-month-text {
  font-size: 19px;
  font-weight: 800;
  color: #111;
  letter-spacing: -0.5px;
}

.insight-summary-grid {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: #f8fafc;
  border-radius: 14px;
  padding: 14px;
}

.summary-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-label {
  font-size: 12px;
  color: #777;
  font-weight: 600;
  margin-bottom: 2px;
}

.summary-amount {
  font-size: 16px;
  font-weight: 800;
}

.summary-divider {
  width: 1px;
  height: 28px;
  background-color: #e2e8f0;
}

.text-kb-yellow {
  color: #e5a700;
}

.filter-section {
  margin-bottom: 16px;
}

.period-chips {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  overflow-x: auto;
}

.period-chip {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  white-space: nowrap;
}

.period-chip.active {
  background: #222222;
  color: #ffffff;
  border-color: #222222;
}

.type-segment-tabs {
  display: flex;
  background: #ffffff;
  border-radius: 14px;
  padding: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.type-tab-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px 0;
  font-size: 13px;
  font-weight: 700;
  color: #777;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.type-tab-btn.active {
  background: #ffbc00;
  color: #111111;
  box-shadow: 0 2px 6px rgba(255, 188, 0, 0.3);
}

.loading-wrap, .empty-wrap {
  background: #ffffff;
  border-radius: 20px;
  padding: 40px 20px;
  text-align: center;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.04);
}

.loading-text {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.empty-sub {
  font-size: 12px;
  color: #888;
  margin-top: 8px;
}

.date-group-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 18px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.04);
  margin-bottom: 14px;
}

.date-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 10px;
}

.date-title {
  font-size: 14px;
  font-weight: 700;
  color: #444;
}

.date-daily-total {
  font-size: 12px;
  color: #888;
}

.tx-item-wrapper {
  border-bottom: 1px solid #f8fafc;
}
.tx-item-wrapper:last-child {
  border-bottom: none;
}

.tx-item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 4px;
  cursor: pointer;
  border-radius: 10px;
  transition: background-color 0.15s ease;
}
.tx-item-row:hover {
  background-color: #fafafa;
}

.tx-item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-circle {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.icon-charge { background: #fff8e1; color: #f59e0b; }
.icon-transfer { background: #eff6ff; color: #3b82f6; }
.icon-payment { background: #f3f4f6; color: #1f2937; }
.icon-default { background: #f3f4f6; color: #6b7280; }

.tx-item-title {
  font-size: 15px;
  font-weight: 700;
  color: #111;
}

.tx-item-sub {
  font-size: 12px;
  color: #888;
}

.memo-badge {
  color: #2563eb;
  font-weight: 600;
  margin-left: 4px;
}

.tx-item-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tx-amount {
  font-size: 15px;
  font-weight: 800;
}
.amount-plus { color: #10b981; }
.amount-minus { color: #111827; }

.expand-indicator {
  font-size: 14px;
  color: #94a3b8;
}
</style>
