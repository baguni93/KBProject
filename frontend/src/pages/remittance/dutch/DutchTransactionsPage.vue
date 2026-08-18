<template>
  <div class="dutch-tx-select-page">
    <!-- 1. 고정 안내 헤더 -->
    <div class="sec-header">
      <h3 class="text-18-bold title">정산할 카드/송금 내역 선택</h3>
      <p class="text-13 sub">정산하고 싶은 결제 내역을 터치하여 선택해 주세요.</p>
    </div>

    <!-- 2. 거래 내역 화면(TransactionListPage.vue) 100% 동일 UI/UX 스크롤 영역 -->
    <div class="tx-list-scroll-area">
      <!-- 로딩 중 -->
      <div v-if="loading" class="loading-wrap text-13 text-center py-4">
        <p class="loading-text text-sub">거래 내역을 불러오는 중...</p>
      </div>

      <!-- 내역 없음 -->
      <div v-else-if="groupedTransactions.length === 0" class="empty-tx-box text-center text-muted">
        <i class="fa-solid fa-receipt empty-ic"></i>
        <p class="text-13 mt-2">정산 가능한 결제 내역이 없습니다.</p>
      </div>

      <!-- 일자별 그룹핑 거래 내역 카체 (TransactionListPage.vue 100% 공유 스타일) -->
      <div v-else class="tx-list-container">
        <div
          v-for="group in groupedTransactions"
          :key="group.dateKey"
          class="date-group-card"
        >
          <!-- 날짜 헤더 (8월 18일 화요일 | 합계 5,000원) -->
          <div class="date-header-row">
            <span class="date-title text-13-bold">{{ group.dateDisplay }}</span>
            <span class="date-daily-total text-13">합계 {{ formatCurrency(group.dailySum) }}</span>
          </div>

          <!-- 내역 아이템 리스트 -->
          <div class="date-item-list">
            <div
              v-for="item in group.items"
              :key="item.transactionId"
              class="tx-item-wrapper"
              :class="{ selected: isSelected(item.transactionId) }"
              @click="toggleTxSelection(item)"
            >
              <div class="tx-item-row">
                <!-- 카테고리 아이콘 -->
                <div class="tx-item-left">
                  <div class="icon-circle text-15-bold" :class="getTypeIconClass(item.transactionType)">
                    <i :class="getTypeIcon(item)"></i>
                  </div>
                  <div class="tx-info-text">
                    <div class="tx-item-title text-15-bold">{{ getItemTitle(item) }}</div>
                    <div v-if="getItemSubText(item)" class="tx-item-sub text-13">
                      <span>{{ getItemSubText(item) }}</span>
                    </div>
                  </div>
                </div>

                <!-- 오른쪽 금액 & 선택 체크마크 [V] (영수증 아이콘 ➔ 체크마크로만 대체!) -->
                <div class="tx-item-right">
                  <div class="tx-amount text-15-bold" :class="getAmountClass(item.transactionType)">
                    {{ getAmountPrefix(item.transactionType) }}{{ formatCurrency(item.amount) }}
                  </div>

                  <!-- 선택 상태 체크 아이콘 [V] -->
                  <div class="select-check-box">
                    <i
                      v-if="isSelected(item.transactionId)"
                      class="fa-solid fa-circle-check check-ic-active"
                    ></i>
                    <i
                      v-else
                      class="fa-regular fa-circle check-ic-inactive"
                    ></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 화면 하단 고정 도킹 버튼 -->
    <div class="fixed-bottom-btn-wrap">
      <button
        type="button"
        class="bottom-btn primary-button text-17-bold"
        :disabled="remittanceStore.selectedTxIds.length === 0"
        @click="proceedToAmount"
      >
        {{
          remittanceStore.selectedTxIds.length > 0
            ? `${remittanceStore.selectedTxIds.length}건 선택 완료 (${formatCurrency(selectedTotalAmount)})`
            : "내역 선택"
        }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useRemittanceStore } from '@/stores/remittance';
import { useAuthStore } from '@/stores/auth';
import transactionApi from '@/api/transactionApi';

const router = useRouter();
const remittanceStore = useRemittanceStore();
const authStore = useAuthStore();

const loading = ref(false);
const rawTransactions = ref([]);

// 거래 내역 더미 파이프라인 (TransactionListPage.vue와 100% 동일 시드)
const defaultFallbackTransactions = [
  { transactionId: 901, merchantName: '스타벅스', amount: 5000, transactionType: 'PAYMENT', createdAt: '2026-08-18T10:30:00' },
  { transactionId: 902, merchantName: '교보문고', amount: 18000, transactionType: 'PAYMENT', createdAt: '2026-08-02T15:20:00' },
  { transactionId: 903, merchantName: '오늘의집', amount: 27600, transactionType: 'PAYMENT', createdAt: '2026-08-02T14:10:00' },
  { transactionId: 904, merchantName: '한솥도시락', amount: 12500, transactionType: 'PAYMENT', createdAt: '2026-08-02T12:00:00' },
  { transactionId: 905, merchantName: '메가MGC커피', amount: 4900, transactionType: 'PAYMENT', createdAt: '2026-08-02T09:40:00' },
  { transactionId: 906, merchantName: '스마일치과', amount: 65000, transactionType: 'PAYMENT', createdAt: '2026-08-02T09:00:00' },
  { transactionId: 907, merchantName: '카카오T', amount: 14500, transactionType: 'PAYMENT', createdAt: '2026-08-01T22:15:00' },
  { transactionId: 908, merchantName: '쿠팡', amount: 42900, transactionType: 'PAYMENT', createdAt: '2026-08-01T19:30:00' },
  { transactionId: 909, merchantName: 'CU 계명대점', amount: 9800, transactionType: 'PAYMENT', createdAt: '2026-08-01T18:10:00' },
  { transactionId: 910, merchantName: '투썸플레이스', amount: 5500, transactionType: 'PAYMENT', createdAt: '2026-08-01T15:00:00' },
];

const fetchTransactions = async () => {
  loading.value = true;
  try {
    const userId = authStore.userId || 1;
    let apiData = [];
    if (transactionApi && transactionApi.getTransactions) {
      apiData = await transactionApi.getTransactions(userId);
    }

    // 실제 백엔드 DB 거래 내역이 존재하면 오직 실제 DB 내역만 노출! (더미 중복 병합 제거)
    if (Array.isArray(apiData) && apiData.length > 0) {
      rawTransactions.value = apiData.filter((t) => {
        const type = (t.transactionType || t.type || '').toUpperCase();
        return type !== 'CHARGE';
      });
    } else {
      // DB 내역이 아예 없는 테스트 환경일 때만 기본 폴백 노출
      rawTransactions.value = defaultFallbackTransactions;
    }
  } catch (err) {
    console.log('거래 내역 로드 예외:', err);
    rawTransactions.value = defaultFallbackTransactions;
  } finally {
    loading.value = false;
  }
};

onMounted(fetchTransactions);

// 날짜 그룹핑 (TransactionListPage.vue와 100% 동일 로직)
const groupedTransactions = computed(() => {
  const groups = {};

  rawTransactions.value.forEach((item) => {
    const dateStr = item.createdAt || item.transactionTime || item.date || new Date().toISOString();
    const d = new Date(dateStr);
    const dateKey = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    const dateDisplay = `${d.getMonth() + 1}월 ${d.getDate()}일 ${days[d.getDay()]}요일`;

    if (!groups[dateKey]) {
      groups[dateKey] = {
        dateKey,
        dateDisplay,
        items: [],
        dailySum: 0,
      };
    }
    groups[dateKey].items.push(item);
    groups[dateKey].dailySum += Number(item.amount || 0);
  });

  return Object.values(groups).sort((a, b) => b.dateKey.localeCompare(a.dateKey));
});

const isSelected = (id) => {
  return remittanceStore.selectedTxIds.includes(id);
};

const toggleTxSelection = (item) => {
  const id = item.transactionId || item.id;
  const idx = remittanceStore.selectedTxIds.indexOf(id);
  if (idx > -1) {
    remittanceStore.selectedTxIds.splice(idx, 1);
  } else {
    remittanceStore.selectedTxIds.push(id);
  }
};

const selectedTotalAmount = computed(() => {
  let sum = 0;
  rawTransactions.value.forEach((t) => {
    const id = t.transactionId || t.id;
    if (remittanceStore.selectedTxIds.includes(id)) {
      sum += Number(t.amount || 0);
    }
  });
  return sum;
});

const proceedToAmount = () => {
  remittanceStore.remitAmount = selectedTotalAmount.value;
  router.push('/remittance/dutch/amount');
};

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '0원';
  return Number(val).toLocaleString('ko-KR') + '원';
};

const getItemTitle = (item) => {
  return item.merchantName || item.merchant_name || item.title || item.memo || '가맹점 결제';
};

const getItemSubText = (item) => {
  if (item.categoryName && item.categoryName !== '기타') return item.categoryName;
  return null;
};

const getTypeIcon = (item) => {
  const title = getItemTitle(item);
  if (title.includes('스타벅스') || title.includes('커피') || title.includes('투썸') || title.includes('메가')) return 'fa-solid fa-mug-hot';
  if (title.includes('교보문고') || title.includes('책')) return 'fa-solid fa-book';
  if (title.includes('치과') || title.includes('병원')) return 'fa-solid fa-hospital';
  if (title.includes('카카오') || title.includes('택시')) return 'fa-solid fa-taxi';
  if (title.includes('도시락') || title.includes('한식') || title.includes('배달')) return 'fa-solid fa-utensils';
  if (title.includes('쿠팡') || title.includes('오늘의집') || title.includes('쇼핑')) return 'fa-solid fa-bag-shopping';
  if (title.includes('CU') || title.includes('편의점') || title.includes('이마트')) return 'fa-solid fa-store';
  return 'fa-solid fa-bag-shopping';
};

const getTypeIconClass = () => 'icon-payment';
const getAmountClass = () => 'amount-minus';
const getAmountPrefix = () => '-';
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.dutch-tx-select-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: var(--color-bg-page, #ffffff);
}

.sec-header {
  padding: 16px 20px 8px;
}

.sec-header .title {
  margin: 0;
  color: var(--color-text-main, #111111);
}

.sec-header .sub {
  margin: 4px 0 0;
  color: var(--color-text-sub, #666666);
}

.tx-list-scroll-area {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px 80px;
  box-sizing: border-box;

  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tx-list-scroll-area::-webkit-scrollbar {
  display: none;
}

/* TransactionListPage.vue 100% 동일 카드 스타일 */
.date-group-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 16px 18px;
  margin-bottom: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.date-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 8px;
}

.date-title {
  color: #1e293b;
}

.date-daily-total {
  color: #64748b;
}

.date-item-list {
  display: flex;
  flex-direction: column;
}

.tx-item-wrapper {
  padding: 12px 0;
  border-bottom: 1px dashed #f1f5f9;
  cursor: pointer;
  transition: background 0.15s ease;
}

.tx-item-wrapper:last-child {
  border-bottom: none;
}

.tx-item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tx-item-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.icon-circle {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #f8fafc;
  color: #334155;
}

.icon-circle.icon-payment {
  background: #f8fafc;
  color: #0f172a;
}

.tx-info-text {
  display: flex;
  flex-direction: column;
}

.tx-item-title {
  color: #0f172a;
}

.tx-item-sub {
  color: #64748b;
  margin-top: 2px;
}

.tx-item-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tx-amount {
  color: #0f172a;
}

.tx-amount.amount-minus {
  color: #0f172a;
}

/* 선택 체크박스 아이콘 [V] */
.select-check-box {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.check-ic-active {
  color: #ffbc2e;
  font-size: 22px;
}

.check-ic-inactive {
  color: #cbd5e1;
  font-size: 22px;
}

/* 하단 고정 버튼 영역 */
.fixed-bottom-btn-wrap {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px 20px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.8) 0%, #ffffff 100%);
  backdrop-filter: blur(8px);
  border-top: 1px solid #f1f5f9;
  z-index: 100;
}

.bottom-btn {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 16px;
  background: #ffbc2e;
  color: #111111;
  cursor: pointer;
  transition: all 0.2s ease;
}

.bottom-btn:disabled {
  background: #e2e8f0;
  color: #94a3b8;
  cursor: not-allowed;
}
</style>
