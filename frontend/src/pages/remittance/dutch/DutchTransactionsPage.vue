<template>
  <div class="tx-page-flex-container">
    <!-- 1. 고정 헤더 -->
    <div class="sec-header">
      <h3 class="text-18-bold title">정산할 카드/송금 내역 선택</h3>
      <p class="text-13 sub">정산하고 싶은 결제 내역을 터치하여 선택해 주세요.</p>
    </div>

    <!-- 2. 스크롤 가능한 내역 리스트 전용 영역 -->
    <div class="tx-list-scroll-area">
      <div
        v-for="tx in formattedTxList"
        :key="tx.id"
        class="tx-item-card"
        :class="{ active: remittanceStore.selectedTxIds.includes(tx.id) }"
        @click="toggleTxSelection(tx.id)"
      >
        <div class="tx-merchant-icon-wrap">
          <i :class="tx.faIcon" class="merchant-fa-ic"></i>
        </div>

        <div class="tx-info-left">
          <span class="tx-merchant text-15-bold">{{ tx.merchantName }}</span>
          <span class="tx-date text-13">{{ tx.formattedDate }}</span>
        </div>

        <div class="tx-amount-right">
          <span class="tx-amt text-16-bold">{{ remittanceStore.formatCurrency(tx.amount) }}원</span>
          <i
            v-if="remittanceStore.selectedTxIds.includes(tx.id)"
            class="fa-solid fa-circle-check sel-ic"
          ></i>
          <i v-else class="fa-regular fa-circle unsel-ic"></i>
        </div>
      </div>

      <div
        v-if="formattedTxList.length === 0"
        class="empty-tx-box text-center text-muted"
      >
        <p class="text-13">최근 결제 내역이 없습니다.</p>
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
        {{ remittanceStore.selectedTxIds.length > 0 ? `${remittanceStore.selectedTxIds.length}건 선택 완료 (${remittanceStore.formatCurrency(selectedTotalAmount)}원)` : "내역 선택" }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useRemittanceStore } from "@/stores/remittance";
import { getCategoryIcon } from "@/util/analysis";

const router = useRouter();
const remittanceStore = useRemittanceStore();

const defaultDummyTxList = [
  { id: 101, merchantName: "배달의민족 (153구포국수)", amount: 18500, formattedDate: "최근", faIcon: "fa-solid fa-utensils" },
  { id: 102, merchantName: "동성로 한식당", amount: 24000, formattedDate: "최근", faIcon: "fa-solid fa-utensils" },
  { id: 103, merchantName: "스타벅스 대구점", amount: 6200, formattedDate: "최근", faIcon: "fa-solid fa-mug-hot" },
  { id: 104, merchantName: "투썸플레이스", amount: 5500, formattedDate: "최근", faIcon: "fa-solid fa-mug-hot" },
  { id: 105, merchantName: "CU 계명대점", amount: 9800, formattedDate: "최근", faIcon: "fa-solid fa-store" },
  { id: 106, merchantName: "쿠팡", amount: 42900, formattedDate: "최근", faIcon: "fa-solid fa-cart-shopping" },
  { id: 107, merchantName: "카카오T", amount: 14500, formattedDate: "최근", faIcon: "fa-solid fa-taxi" },
  { id: 108, merchantName: "스마일치과", amount: 65000, formattedDate: "최근", faIcon: "fa-solid fa-tooth" },
  { id: 109, merchantName: "메가MGC커피", amount: 4900, formattedDate: "최근", faIcon: "fa-solid fa-mug-hot" },
];

const formattedTxList = computed(() => {
  if (remittanceStore.userTxList && remittanceStore.userTxList.length > 0) {
    return remittanceStore.userTxList.map((tx, idx) => {
      const idVal = tx.id || tx.transactionId || tx.historyId || (idx + 101);
      return {
        id: idVal,
        merchantName: tx.merchantName || tx.title || "결제 건",
        amount: Number(tx.amount || 10000),
        formattedDate: tx.transactionTime || tx.createdAt || "최근",
        faIcon: getCategoryIcon(tx.merchantName || tx.title),
      };
    });
  }
  return defaultDummyTxList;
});

const selectedTotalAmount = computed(() => {
  return formattedTxList.value
    .filter((tx) => remittanceStore.selectedTxIds.includes(tx.id))
    .reduce((sum, tx) => sum + Number(tx.amount || 0), 0);
});

const toggleTxSelection = (id) => {
  const idx = remittanceStore.selectedTxIds.indexOf(id);
  if (idx > -1) {
    remittanceStore.selectedTxIds.splice(idx, 1);
  } else {
    remittanceStore.selectedTxIds.push(id);
  }
};

const proceedToAmount = () => {
  if (remittanceStore.selectedTxIds.length === 0) {
    alert("정산할 내역을 최소 1건 이상 선택해 주세요.");
    return;
  }
  remittanceStore.remitAmount = selectedTotalAmount.value;
  router.push('/remittance/dutch/amount');
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.tx-page-flex-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  max-height: 100%;
  overflow: hidden;
  box-sizing: border-box;
}

.sec-header {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.title {
  margin: 0;
  color: #111111;
}

.sub {
  margin: 0;
  color: #718096;
}

/* 내역 리스트 전용 독립 스크롤 영역 */
.tx-list-scroll-area {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-right: 2px;
  padding-bottom: 8px;
  box-sizing: border-box;
}

.tx-item-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 16px;
  background-color: #ffffff;
  border: 1px solid #edf2f7;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
  flex-shrink: 0;
}

.tx-item-card.active {
  border-color: #ffbc2e;
  background-color: #fffdf8;
  box-shadow: 0 4px 12px rgba(255, 188, 46, 0.12);
}

.tx-merchant-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background-color: #f7fafc;
  border: 1px solid #edf2f7;
}

.merchant-fa-ic {
  font-size: 18px;
  color: #4a5568;
}

.tx-item-card.active .merchant-fa-ic {
  color: #ffbc2e;
}

.tx-info-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.tx-merchant {
  color: #111111;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tx-date {
  color: #a0aec0;
}

.tx-amount-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.tx-amt {
  color: #111111;
}

.sel-ic {
  color: #ffbc2e;
  font-size: 20px;
}

.unsel-ic {
  color: #cbd5e0;
  font-size: 20px;
}

.empty-tx-box {
  padding: 40px 0;
  color: #a0aec0;
}

/* 화면 극하단 고정 도킹 영역 */
.fixed-bottom-btn-wrap {
  flex-shrink: 0;
  padding-top: 12px;
  padding-bottom: 4px;
  background-color: #ffffff;
  border-top: 1px solid #edf2f7;
}

.fixed-bottom-btn-wrap .bottom-btn {
  width: 100%;
  height: 52px;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 700;
  border: none;
  background-color: #ffbc2e;
  color: #111111;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fixed-bottom-btn-wrap .bottom-btn:disabled {
  background-color: #e2e8f0;
  color: #a0aec0;
  cursor: not-allowed;
}
</style>
