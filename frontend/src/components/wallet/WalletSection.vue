<template>
  <div class="wallet-section-container">
    <!-- 섹션 헤더 -->
    <div class="section-header">
      <div class="section-title-wrap">
        <div class="section-icon-badge">
          <i class="fa-solid fa-receipt"></i>
        </div>
        <h6 class="section-title text-18-bold">최근 거래 내역</h6>
      </div>
      <router-link to="/transactions" class="more-link text-13">
        전체보기 <i class="fa-solid fa-chevron-right"></i>
      </router-link>
    </div>

    <!-- 필터 탭 -->
    <div class="filter-tab-row">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        type="button"
        class="filter-tab-btn text-13-bold"
        :class="{ active: selectedType === tab.value }"
        @click="changeTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 로딩 중 -->
    <div v-if="loading" class="loading-box text-13">
      <div class="spinner"></div>
      <div class="loading-text">내역을 불러오는 중...</div>
    </div>

    <!-- 내역 없음 -->
    <div v-else-if="transactions.length === 0" class="empty-tx-box">
      <i class="fa-solid fa-receipt empty-icon"></i>
      <p class="empty-text text-13">최근 거래 내역이 없습니다</p>
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
          <div
            :class="['tx-icon-circle', getTypeIconClass(item.transactionType)]"
          >
            <i :class="getTypeIcon(item.transactionType)"></i>
          </div>
          <div class="tx-info">
            <strong class="tx-title text-15-bold">{{
              getItemTitle(item)
            }}</strong>
            <span class="tx-date text-13">{{
              formatDate(item.createdAt)
            }}</span>
            <span v-if="item.memo" class="tx-memo text-13"
              >"{{ item.memo }}"</span
            >
          </div>
        </div>

        <div class="tx-right">
          <span
            :class="[
              'tx-amount',
              'text-15-bold',
              getAmountClass(item.transactionType),
            ]"
          >
            {{ getAmountPrefix(item.transactionType)
            }}{{ formatCurrency(item.amount) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import transactionApi from "@/api/transactionApi";

const router = useRouter();
const authStore = useAuthStore();

const props = defineProps({
  userId: {
    type: Number,
    default: null,
  },
});

const selectedType = ref("");
const transactions = ref([]);
const loading = ref(false);

const emit = defineEmits(["open-receipt"]);

const tabs = [
  { label: "전체", value: "" },
  { label: "충전", value: "CHARGE" },
  { label: "송금", value: "TRANSFER" },
  { label: "결제", value: "PAYMENT" },
];

const formatCurrency = (val) => {
  if (val === undefined || val === null) return "0원";
  return Number(val).toLocaleString("ko-KR") + "원";
};

const formatDate = (dateStr) => {
  if (!dateStr) return "";
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return dateStr;
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
};

const getItemTitle = (item) => {
  const tType = (item.transactionType || item.type || "").toUpperCase();
  if (tType === "CHARGE")
    return item.merchantName || item.merchant_name || item.title || "지갑 충전";
  if (tType === "TRANSFER" || tType === "REMIT") {
    return item.receiverName
      ? `송금 (${item.receiverName})`
      : item.title || "송금 완료";
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
    "현장 결제"
  );
};

const getTypeIcon = (type) => {
  const tStr = (type || "").toUpperCase();
  if (tStr.includes("CHARGE")) return "fa-solid fa-plus";
  if (tStr.includes("TRANSFER") || tStr.includes("REMIT"))
    return "fa-solid fa-paper-plane";
  if (tStr.includes("PAY")) return "fa-solid fa-bag-shopping";
  return "fa-solid fa-arrow-left-right";
};

const getTypeIconClass = (type) => {
  const tStr = (type || "").toUpperCase();
  if (tStr.includes("CHARGE")) return "yellow";
  if (tStr.includes("TRANSFER") || tStr.includes("REMIT")) return "blue";
  if (tStr.includes("PAY")) return "dark";
  return "gray";
};

const getAmountClass = (type) => {
  const tStr = (type || "").toUpperCase();
  if (tStr.includes("CHARGE")) return "plus";
  return "minus";
};

const getAmountPrefix = (type) => {
  const tStr = (type || "").toUpperCase();
  if (tStr.includes("CHARGE")) return "+";
  return "-";
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
        filtered = list.filter((t) => {
          const typeStr = (t.transactionType || t.type || "").toUpperCase();
          if (selectedType.value === "CHARGE")
            return typeStr.includes("CHARGE");
          if (selectedType.value === "TRANSFER")
            return typeStr.includes("TRANSFER") || typeStr.includes("REMIT");
          if (selectedType.value === "PAYMENT") return typeStr.includes("PAY");
          return true;
        });
      }

      filtered.sort((a, b) => {
        const dateA = new Date(a.createdAt || a.transactionDate || a.date || 0);
        const dateB = new Date(b.createdAt || b.transactionDate || b.date || 0);
        return dateB - dateA;
      });

      transactions.value = filtered;
    }
  } catch (err) {
    console.log("WalletSection 거래내역 로드 예외", err);
  } finally {
    loading.value = false;
  }
};

const addTransaction = (newTx) => {
  transactions.value.unshift(newTx);
};

defineExpose({
  fetchTransactions,
  addTransaction,
});

const openReceipt = (transactionId) => {
  emit("open-receipt", transactionId);
};

onMounted(() => {
  fetchTransactions();
});
</script>

<style scoped>
/* ==========================================================================
   디자인 시스템 명세서(common.css) 100% 반영 스타일링
   ========================================================================== */

.wallet-section-container {
  background-color: var(--color-bg-page, #ffffff);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 14px;
  padding: 16px;
  margin-top: 16px;
  text-align: left;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-icon-badge {
  width: 28px;
  height: 28px;
  background-color: #fffbe6;
  color: var(--color-primary-border, #cc9200);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
}

.section-title {
  color: var(--color-text-main, #111111);
  margin: 0;
}

.more-link {
  color: var(--color-text-sub, #777777);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.15s ease;
}

.more-link:hover {
  color: var(--color-primary-border, #cc9200);
}

/* 필터 탭 */
.filter-tab-row {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
  background-color: var(--color-bg-screen, #f5f6f8);
  padding: 4px;
  border-radius: 10px;
}

.filter-tab-btn {
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

.filter-tab-btn.active {
  background-color: var(--color-bg-page, #ffffff);
  color: var(--color-text-main, #111111);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 로딩 */
.loading-box {
  padding: 24px 0;
  text-align: center;
  color: var(--color-text-sub, #777777);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border-main, #dddddd);
  border-top-color: var(--color-primary, #ffbc2e);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 빈 목록 */
.empty-tx-box {
  padding: 28px 0;
  text-align: center;
}

.empty-icon {
  font-size: 24px;
  color: var(--color-text-disabled, #aaaaaa);
  margin-bottom: 6px;
  display: block;
}

.empty-text {
  color: var(--color-text-sub, #777777);
  margin: 0;
}

/* 리스트 */
.tx-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tx-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background-color: var(--color-bg-screen, #f5f6f8);
  border-radius: 10px;
  border: 1px solid var(--color-border-main, #dddddd);
  cursor: pointer;
  transition: all 0.15s ease;
}

.tx-item:hover {
  background-color: #fffbe6;
  border-color: var(--color-primary-border, #cc9200);
}

.tx-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tx-icon-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  flex-shrink: 0;
}

.tx-icon-circle.yellow {
  background-color: #fffbe6;
  color: var(--color-primary-border, #cc9200);
}

.tx-icon-circle.blue {
  background-color: #eff6ff;
  color: #2563eb;
}

.tx-icon-circle.dark {
  background-color: var(--color-text-main, #111111);
  color: #ffffff;
}

.tx-icon-circle.gray {
  background-color: var(--color-border-main, #dddddd);
  color: var(--color-text-sub, #777777);
}

.tx-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.tx-title {
  color: var(--color-text-main, #111111);
}

.tx-date {
  color: var(--color-text-sub, #777777);
}

.tx-memo {
  color: var(--color-primary-border, #cc9200);
}

.tx-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.tx-amount.plus {
  color: var(--color-success, #1fa64b);
}

.tx-amount.minus {
  color: var(--color-text-main, #111111);
}
</style>
