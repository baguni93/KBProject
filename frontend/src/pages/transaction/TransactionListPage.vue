<template>
  <div class="transaction-root">
    <!-- 공통 페이지 헤더 -->
    <PageHeader title="거래 내역" :show-back="true" :show-refresh="false" />

    <div class="tx-body">
      <!-- ══════════════════════════════════════════
           [1] 월 선택 피커 & 소비 인사이트 요약 카드
      ══════════════════════════════════════════ -->
      <div class="month-selector-card">
        <div class="month-picker-row">
          <button class="month-nav-btn" @click="changeMonth(-1)">
            <i class="fa-solid fa-chevron-left"></i>
          </button>
          <div class="current-month-text text-20-bold">
            {{ selectedYear }}년 {{ selectedMonth }}월
          </div>
          <button
            class="month-nav-btn"
            :disabled="isCurrentMonth"
            @click="changeMonth(1)"
          >
            <i class="fa-solid fa-chevron-right"></i>
          </button>
        </div>

        <div class="insight-summary-grid">
          <div class="summary-box expense">
            <span class="summary-label text-13">총 지출</span>
            <span class="summary-amount text-18-bold text-expense"
              >{{ formatCurrency(summaryExpense) }}</span
            >
          </div>
          <div class="summary-divider"></div>
          <div class="summary-box income">
            <span class="summary-label text-13">총 수입 / 충전</span>
            <span class="summary-amount text-18-bold text-income"
              >{{ formatCurrency(summaryIncome) }}</span
            >
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
            class="period-chip text-13-bold"
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
            class="type-tab-btn text-13-bold"
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
      <div v-if="loading" class="loading-wrap text-13">
        <div class="spinner"></div>
        <p class="loading-text text-13">거래 내역을 불러오는 중...</p>
      </div>

      <!-- ══════════════════════════════════════════
           [4] 거래 내역 없음
      ══════════════════════════════════════════ -->
      <div
        v-else-if="filteredGroupedTransactions.length === 0"
        class="empty-wrap"
      >
        <EmptyList desc="해당 조건에 맞는 거래 내역이 없습니다." />
        <p class="empty-sub text-13">
          기간 및 거래 유형 필터를 변경하거나 새로운 결제/충전을 진행해 보세요.
        </p>
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
            <span class="date-title text-13-bold">{{ group.dateDisplay }}</span>
            <span class="date-daily-total text-13"
              >합계 {{ formatCurrency(group.dailySum) }}</span
            >
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
              <div class="tx-item-row" @click="openReceiptModal(item)">
                <div class="tx-item-left">
                  <div
                    class="icon-circle text-15-bold"
                    :class="getTypeIconClass(item)"
                  >
                    <i :class="getTypeIcon(item)"></i>
                  </div>
                  <div class="tx-info-text">
                    <div class="tx-item-title text-15-bold">
                      {{ getItemTitle(item) }}
                    </div>
                    <div v-if="getItemSubText(item)" class="tx-item-sub text-13">
                      <span>{{ getItemSubText(item) }}</span>
                    </div>
                  </div>
                </div>

                <div class="tx-item-right">
                  <div
                    class="tx-amount text-15-bold"
                    :class="getAmountClass(item)"
                  >
                    {{ getAmountPrefix(item)
                    }}{{ formatCurrency(item.amount) }}
                  </div>
                  <div class="expand-indicator text-13">
                    <i class="fa-solid fa-receipt"></i>
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
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import PageHeader from "@/components/common/PageHeader.vue";
import transactionApi from "@/api/transactionApi";
import EmptyList from "@/components/common/EmptyList.vue";
import ReceiptDetailModal from "@/components/transaction/ReceiptDetailModal.vue";

const router = useRouter();
const authStore = useAuthStore();

const userId = computed(() => authStore.userId || 1);
const loading = ref(false);
const rawTransactions = ref([]);

const showReceiptModal = ref(false);
const selectedTransactionId = ref(null);

// 날짜/필터 상태
const now = new Date();
const selectedYear = ref(now.getFullYear());
const selectedMonth = ref(now.getMonth() + 1);

const selectedPeriod = ref("ALL"); // ALL, CURRENT, 1M, 3M
const selectedType = ref(""); // '', CHARGE, TRANSFER, PAYMENT

const expandedTxId = ref(null);

const typeTabs = [
  { label: "전체", value: "" },
  { label: "충전", value: "CHARGE" },
  { label: "송금", value: "TRANSFER" },
  { label: "결제", value: "PAYMENT" },
];

const periodOptions = [
  { label: "전체", value: "ALL" },
  { label: "당월", value: "CURRENT" },
  { label: "1개월", value: "1M" },
  { label: "3개월", value: "3M" },
];

const isCurrentMonth = computed(() => {
  return (
    selectedYear.value === now.getFullYear() &&
    selectedMonth.value === now.getMonth() + 1
  );
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
  selectedPeriod.value = "ALL"; // 월 변경 시 전체 모드(해당 월 기준)로 설정
};

const setPeriodFilter = (val) => {
  selectedPeriod.value = val;
  if (val === "CURRENT") {
    selectedYear.value = now.getFullYear();
    selectedMonth.value = now.getMonth() + 1;
  }
};

const changeTypeTab = (val) => {
  selectedType.value = val;
  fetchTransactions();
};

const openReceiptModal = (item) => {
  if (item && item.transactionId) {
    const tType = (item.transactionType || item.type || "PAYMENT").toUpperCase();
    const title = getItemTitle(item);
    router.push({
      path: `/transactions/receipt/${item.transactionId}`,
      query: {
        type: tType,
        title: title,
        amount: item.amount || 0,
        createdAt: item.createdAt || ""
      }
    });
  }
};

const fetchTransactions = async () => {
  loading.value = true;
  try {
    if (!userId.value) {
      rawTransactions.value = [];
      return;
    }
    const list = await transactionApi.getTransactions(
      userId.value,
      selectedType.value,
    );
    rawTransactions.value = Array.isArray(list) ? list : [];
  } catch (err) {
    console.error("거래 내역 조회 실패:", err);
    rawTransactions.value = [];
  } finally {
    loading.value = false;
  }
};

const filteredTransactions = computed(() => {
  const currentDate = new Date();

  return rawTransactions.value.filter((item) => {
    // 1. 거래 유형 필터
    if (selectedType.value && item.transactionType !== selectedType.value) {
      return false;
    }

    if (!item.createdAt) return true;
    const txDate = new Date(item.createdAt);

    // 2. 기간 칩(ALL, CURRENT, 1M, 3M) 및 월 피커 필터링
    if (selectedPeriod.value === "CURRENT") {
      // 당월 (이번달)
      return (
        txDate.getFullYear() === currentDate.getFullYear() &&
        txDate.getMonth() === currentDate.getMonth()
      );
    } else if (selectedPeriod.value === "1M") {
      // 최근 1개월 (30일 이내)
      const oneMonthAgo = new Date(currentDate);
      oneMonthAgo.setDate(oneMonthAgo.getDate() - 30);
      return txDate >= oneMonthAgo;
    } else if (selectedPeriod.value === "3M") {
      // 최근 3개월 (90일 이내)
      const threeMonthsAgo = new Date(currentDate);
      threeMonthsAgo.setDate(threeMonthsAgo.getDate() - 90);
      return txDate >= threeMonthsAgo;
    } else {
      // ALL (선택된 년/월 기준 필터링)
      return (
        txDate.getFullYear() === selectedYear.value &&
        txDate.getMonth() + 1 === selectedMonth.value
      );
    }
  });
});

const isIncome = (item) => {
  if (!item) return false;
  const type = (item.transactionType || item.type || "").toUpperCase();
  if (type === "CHARGE") return true;
  if (type === "TRANSFER" && Number(item.receiveId) === Number(userId.value)) {
    return true;
  }
  return false;
};

const summaryExpense = computed(() => {
  return filteredTransactions.value
    .filter((t) => !isIncome(t))
    .reduce((sum, t) => sum + Number(t.amount || 0), 0);
});

const summaryIncome = computed(() => {
  return filteredTransactions.value
    .filter((t) => isIncome(t))
    .reduce((sum, t) => sum + Number(t.amount || 0), 0);
});

const filteredGroupedTransactions = computed(() => {
  const groups = {};

  filteredTransactions.value.forEach((item) => {
    const d = new Date(item.createdAt);
    const dateKey = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    const days = ["일", "월", "화", "수", "목", "금", "토"];
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

  return Object.values(groups).sort((a, b) =>
    b.dateKey.localeCompare(a.dateKey),
  );
});

const formatCurrency = (val) => {
  if (val === undefined || val === null) return "0원";
  return Number(val).toLocaleString("ko-KR") + "원";
};

const formatTime = (dateStr) => {
  if (!dateStr) return "";
  const d = new Date(dateStr);
  return d.toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" });
};

const isSettlement = (item) => {
  if (!item) return false;
  const type = (item.transactionType || item.type || "").toUpperCase();
  return type === "SETTLEMENT" || (item.settlementId && Number(item.settlementId) > 0);
};

const getItemTitle = (item) => {
  const type = (item.transactionType || item.type || "").toUpperCase();
  if (type === "CHARGE") return item.merchantName || item.merchant_name || item.memo || "전자지갑 충전";
  if (type === "TRANSFER" || type === "REMIT" || type === "SETTLEMENT") {
    if (isSettlement(item)) {
      if (isIncome(item)) {
        return item.senderName ? `${item.senderName} (정산 받음)` : "더치페이 정산 받음";
      }
      const rec = item.receiverName || item.merchantName || "정산 요청자";
      return `${rec} (정산 보냄)`;
    }
    if (isIncome(item)) {
      return item.senderName ? `${item.senderName}에게 받음` : "송금 받음";
    }
    let name = item.merchantName || item.merchant_name || item.receiverName || item.ownerName || item.memo || item.title || "";
    if (name.startsWith("송금 (") && name.endsWith(")")) {
      name = name.substring(4, name.length - 1);
    }
    if (name === "송금 완료" || name === "송금" || !name || name === "수취인") {
      name = (item.receiverName && item.receiverName !== "수취인") ? `${item.receiverName}` : (item.memo && item.memo !== "송금 완료" ? item.memo : "송금");
    }
    return name;
  }
  return item.merchantName || item.merchant_name || item.title || item.memo || "가맹점 결제";
};

const getItemSubText = (item) => {
  if (isSettlement(item)) {
    return item.memo && item.memo !== "송금 완료" && item.memo !== "정산 완료" ? `더치페이 정산 · ${item.memo}` : "더치페이 정산";
  }
  if (
    item.memo &&
    item.memo.trim() &&
    item.memo !== "송금 완료" &&
    item.memo !== "결제 완료" &&
    item.memo !== "상세 내역 피드 남기기" &&
    item.memo !== "기본 피드"
  ) {
    return item.memo;
  }
  if (
    item.content &&
    item.content.trim() &&
    item.content !== "송금 완료" &&
    item.content !== "결제 완료" &&
    item.content !== "상세 내역 피드 남기기" &&
    item.content !== "기본 피드"
  ) {
    return item.content;
  }
  if (item.categoryName && item.categoryName !== "기타") {
    return item.categoryName;
  }
  if (item.spendingCategoryName && item.spendingCategoryName !== "기타") {
    return item.spendingCategoryName;
  }
  const type = (item.transactionType || item.type || "").toUpperCase();
  if (type === "TRANSFER" || type === "REMIT") {
    return item.targetType === "ACCOUNT" || item.sourceType === "ACCOUNT" ? "계좌 송금" : "친구 송금";
  }
  return null;
};

const getTypeIcon = (item) => {
  const type = typeof item === "object" ? item.transactionType : item;
  const title = typeof item === "object" ? getItemTitle(item) : "";

  if (
    title.includes("스타벅스") ||
    title.includes("커피") ||
    title.includes("투썸") ||
    title.includes("메가")
  )
    return "fa-solid fa-mug-hot";
  if (title.includes("교보문고") || title.includes("책"))
    return "fa-solid fa-book";
  if (title.includes("치과") || title.includes("병원"))
    return "fa-solid fa-hospital";
  if (title.includes("카카오") || title.includes("택시"))
    return "fa-solid fa-taxi";
  if (
    title.includes("도시락") ||
    title.includes("한식") ||
    title.includes("배달")
  )
    return "fa-solid fa-utensils";
  if (
    title.includes("쿠팡") ||
    title.includes("오늘의집") ||
    title.includes("쇼핑") ||
    title.includes("무신사")
  )
    return "fa-solid fa-bag-shopping";
  if (
    title.includes("CU") ||
    title.includes("편의점") ||
    title.includes("이마트") ||
    title.includes("올리브영")
  )
    return "fa-solid fa-store";

  if (isSettlement(item)) {
    return "fa-solid fa-users";
  }

  if (isIncome(item)) {
    return "fa-solid fa-plus";
  }

  switch (type) {
    case "CHARGE":
      return "fa-solid fa-plus";
    case "TRANSFER":
      return "fa-solid fa-paper-plane";
    case "PAYMENT":
      return "fa-solid fa-bag-shopping";
    default:
      return "fa-solid fa-arrow-left-right";
  }
};

const getTypeIconClass = (item) => {
  if (isSettlement(item)) return "icon-settlement";
  if (isIncome(item)) return "icon-charge";
  const type = typeof item === "object" ? item.transactionType : item;
  switch (type) {
    case "CHARGE":
      return "icon-charge";
    case "TRANSFER":
      return "icon-transfer";
    case "PAYMENT":
      return "icon-payment";
    default:
      return "icon-default";
  }
};

const getAmountClass = (item) => {
  if (isIncome(item)) return "amount-plus";
  return "amount-minus";
};

const getAmountPrefix = (item) => {
  if (isIncome(item)) return "+";
  return "-";
};

onMounted(() => {
  fetchTransactions();
});
</script>

<style scoped>
/* ==========================================================================
   디자인 시스템 명세서(common.css) 100% 반영 스타일링
   ========================================================================== */

.transaction-root {
  min-height: 100vh;
  background-color: var(--color-bg-page, #ffffff);
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue",
    Arial, sans-serif;
  color: var(--color-text-main, #111111);
  padding-bottom: 50px;
}

.transaction-root * {
  box-sizing: border-box;
}

/* ========================================
   거래 내역 화면 전용 헤더 여백 보정 (공용 PageHeader.vue 원본 100% 보존)
======================================== */
.transaction-root :deep(.page-header) {
  padding: 0 16px;
}

.transaction-root :deep(.header-left .header-icon-btn) {
  transform: none;
}

.tx-body {
  max-width: 500px;
  margin: 0 auto;
  padding: 16px;
}

/* 월 선택 및 인사이트 카드 */
.month-selector-card {
  background-color: var(--color-bg-page, #ffffff);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.month-picker-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 14px;
}

.month-nav-btn {
  background: transparent;
  border: none;
  font-size: 16px;
  color: var(--color-text-sub, #777777);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
}

.month-nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.current-month-text {
  color: var(--color-text-main, #111111);
  letter-spacing: -0.5px;
}

.insight-summary-grid {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  padding: 12px;
}

.summary-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-label {
  color: var(--color-text-sub, #777777);
  margin-bottom: 2px;
}

.summary-amount {
  color: var(--color-text-main, #111111);
}

.summary-divider {
  width: 1px;
  height: 28px;
  background-color: var(--color-divider, #ededed);
}

.text-expense {
  color: #ef4444;
}

.text-income {
  color: #10b981;
}

.text-kb-yellow {
  color: var(--color-primary-border, #cc9200);
}

.text-dark {
  color: var(--color-text-main, #111111);
}

/* 필터 섹션 */
.filter-section {
  margin-bottom: 16px;
}

.period-chips {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
  overflow-x: auto;
}

.period-chip {
  background-color: var(--color-bg-page, #ffffff);
  border: 1px solid var(--color-border-main, #dddddd);
  padding: 6px 14px;
  border-radius: 9999px;
  color: var(--color-text-sub, #777777);
  cursor: pointer;
  white-space: nowrap;
}

.period-chip.active {
  background-color: var(--color-text-main, #111111);
  color: #ffffff;
  border-color: var(--color-text-main, #111111);
}

.type-segment-tabs {
  display: flex;
  background-color: var(--color-bg-screen, #f5f6f8);
  border-radius: 10px;
  padding: 4px;
}

.type-tab-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px 0;
  color: var(--color-text-sub, #777777);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.type-tab-btn.active {
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 로딩 및 빈 상태 */
.loading-wrap,
.empty-wrap {
  background-color: var(--color-bg-page, #ffffff);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 14px;
  padding: 36px 16px;
  text-align: center;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--color-border-main, #dddddd);
  border-top-color: var(--color-primary, #ffbc2e);
  border-radius: 50%;
  margin: 0 auto 10px auto;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  color: var(--color-text-sub, #777777);
  margin: 0;
}

.empty-sub {
  color: var(--color-text-muted, #888888);
  margin-top: 6px;
}

/* 일자별 카드 */
.date-group-card {
  background-color: var(--color-bg-page, #ffffff);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 12px;
  text-align: left;
}

.date-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-divider, #ededed);
  margin-bottom: 8px;
}

.date-title {
  color: var(--color-text-main, #111111);
}

.date-daily-total {
  color: var(--color-text-sub, #777777);
}

.tx-item-wrapper {
  border-bottom: 1px solid var(--color-divider, #ededed);
}

.tx-item-wrapper:last-child {
  border-bottom: none;
}

.tx-item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 4px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.15s ease;
}

.tx-item-row:hover {
  background-color: var(--color-bg-screen, #f5f6f8);
}

.tx-item-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-charge {
  background-color: #fffbe6;
  color: var(--color-primary-border, #cc9200);
}

.icon-transfer {
  background-color: #eff6ff;
  color: #2563eb;
}

.icon-settlement {
  background-color: #f5f3ff;
  color: #7c3aed;
}

.icon-payment {
  background-color: var(--color-bg-screen, #f5f6f8);
  color: var(--color-text-main, #111111);
}

.icon-default {
  background-color: var(--color-bg-screen, #f5f6f8);
  color: var(--color-text-sub, #777777);
}

.tx-info-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.tx-item-title {
  color: var(--color-text-main, #111111);
}

.tx-item-sub {
  color: var(--color-text-sub, #777777);
}

.memo-badge {
  color: var(--color-primary-border, #cc9200);
  margin-left: 4px;
}

.tx-item-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tx-amount.amount-plus {
  color: #10b981;
}

.tx-amount.amount-minus {
  color: #111111;
}

.expand-indicator {
  color: var(--color-text-muted, #888888);
}
</style>
