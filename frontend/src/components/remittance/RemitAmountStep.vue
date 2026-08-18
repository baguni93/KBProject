<template>
  <div class="remit-amount-step-container">
    <template v-if="remitType !== 'DUTCH'">
      <!-- 수취 대상 확인 카드 (토스 UI 룩앤필) -->
      <div class="receiver-summary-box">
        <div class="summary-top-my-account">
          <p class="text-15-bold text-main">
            내 <span class="highlight-acc">{{ myAccountName || 'KB국민 주거래통장' }}</span>에서
          </p>
          <p class="text-13 text-sub" style="margin-top: 2px;">
            잔액 {{ formatCurrency(myBalance || 0) }}원
          </p>
        </div>

        <div class="summary-target-info-line">
          <img
            :src="`/api/banks/logo/${getBankLogoFileName(remitType === 'FRIEND' ? 'kb' : accountForm.bankCode)}`"
            class="bank-logo-img-medium"
            @error="(e) => (e.target.src = '/api/banks/logo/kb.png')"
          />
          <div class="receiver-info-col">
            <h3 class="receiver-name text-20-bold">
              <template v-if="remitType === 'FRIEND'">
                {{ selectedFriendObj?.name || "선택한 친구" }}님에게
              </template>
              <template v-else>
                {{ accountForm.receiverName || "수취인" }}님에게
              </template>
            </h3>
            <p class="sub-handle text-13">
              <template v-if="remitType === 'FRIEND'">
                @{{ selectedFriendObj?.username || selectedFriendObj?.nickname || "친구" }}
              </template>
              <template v-else>
                {{ getBankName(accountForm.bankCode) }} {{ accountForm.accountNumber }}
              </template>
            </p>
          </div>
        </div>
      </div>

      <!-- 얼마나 보낼까요? 금액 입력 -->
      <div class="form-field-group" style="margin-top: 28px;">
        <label class="field-label text-24-bold" style="color: #111111; margin-bottom: 16px;">
          얼마나 보낼까요?
        </label>
        <div class="amount-input-row">
          <input
            :value="remitAmountDisplay"
            @input="$emit('onAmountInput', $event)"
            type="text"
            inputmode="numeric"
            class="amount-direct-input text-28-bold"
            placeholder="0"
          />
          <span class="krw-unit text-28-bold">원</span>
        </div>
        <div class="quick-amount-row" style="margin-top: 14px;">
          <button
            class="content-btn secondary text-13-bold"
            @click="$emit('addAmount', 10000)"
          >
            +1만
          </button>
          <button
            class="content-btn secondary text-13-bold"
            @click="$emit('addAmount', 50000)"
          >
            +5만
          </button>
          <button
            class="content-btn secondary text-13-bold"
            @click="$emit('addAmount', 100000)"
          >
            +10만
          </button>
          <button
            class="content-btn primary text-13-bold"
            @click="$emit('setAllBalance')"
          >
            전액
          </button>
        </div>
      </div>
    </template>

    <template v-else>
      <!-- Dutch 2번 화면: 정산 금액 입력 및 정산 방식 선택 -->
      <div class="form-field-group">
        <label class="field-label text-20-bold" style="color: #111111; margin-bottom: 10px;">
          얼마를 정산할까요?
        </label>
        <div class="amount-input-row">
          <input
            :value="remitAmountDisplay"
            @input="$emit('onAmountInput', $event)"
            type="text"
            inputmode="numeric"
            class="amount-direct-input text-28-bold"
            placeholder="0"
          />
          <span class="krw-unit text-28-bold">원</span>
        </div>
        <button
          type="button"
          class="tx-link-btn text-13-bold"
          @click="$emit('openTxSelect')"
        >
          결제 내역 불러오기 <span class="tx-link-arr">&gt;</span>
        </button>
      </div>

      <!-- 정산 방식 선택 (1/N 균등 vs 차등 정산) -->
      <div class="form-field-group" style="margin-top: 20px">
        <label class="field-label text-15-bold" style="color: #111111; margin-bottom: 10px;">정산 방식 선택</label>
        <div class="split-tab-bar">
          <button
            type="button"
            class="split-tab text-14-bold"
            :class="{ active: localDutchSplitMode === 'EQUAL' }"
            @click="switchSplitMode('EQUAL')"
          >
            1/N 균등 정산
          </button>
          <button
            type="button"
            class="split-tab text-14-bold"
            :class="{ active: localDutchSplitMode === 'CUSTOM' }"
            @click="switchSplitMode('CUSTOM')"
          >
            차등 정산 (직접 입력)
          </button>
        </div>
      </div>

      <!-- 정산 배분 요약 카드 -->
      <div class="dutch-calc-box" style="margin-top: 16px">
        <div class="calc-header-row">
          <span class="calc-title text-14-bold">
            {{
              localDutchSplitMode === "EQUAL"
                ? `1/N 균등 정산 배분 (총 ${selectedDutchFriends.length + 1}명)`
                : "멤버별 정산 금액 직접 입력"
            }}
          </span>
        </div>

        <div v-if="localDutchSplitMode === 'EQUAL'" class="calc-list">
          <div class="calc-row text-15-bold">
            <span class="my-name">나</span>
            <span class="val-text"
              >{{
                formatCurrency(
                  Math.floor(
                    (remitAmount || 0) / (selectedDutchFriends.length + 1),
                  ),
                )
              }}
              원</span
            >
          </div>
          <div
            v-for="fId in selectedDutchFriends"
            :key="fId"
            class="calc-row text-15-bold"
          >
            <span class="friend-name">{{ getFriendName(fId) }}</span>
            <span class="val-text red-val"
              >{{
                formatCurrency(
                  Math.floor(
                    (remitAmount || 0) / (selectedDutchFriends.length + 1),
                  ),
                )
              }}
              원</span
            >
          </div>
        </div>

        <div v-else class="calc-list">
          <div class="calc-row text-15-bold">
            <span>나</span>
            <div class="custom-input-wrap">
              <input
                type="number"
                v-model.number="localCustomAmounts['my']"
                class="custom-calc-input text-15-bold"
                placeholder="0"
              />
              <span class="text-13-bold">원</span>
            </div>
          </div>
          <div
            v-for="fId in selectedDutchFriends"
            :key="fId"
            class="calc-row text-15-bold"
          >
            <span>{{ getFriendName(fId) }}</span>
            <div class="custom-input-wrap">
              <input
                type="number"
                v-model.number="localCustomAmounts[fId]"
                class="custom-calc-input text-15-bold"
                placeholder="0"
              />
              <span class="text-13-bold">원</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import SpendingCategorySelector from "@/components/common/SpendingCategorySelector.vue";
import { getCategoryIcon } from "@/util/analysis";

const props = defineProps({
  accountForm: {
    type: Object,
    default: () => ({}),
  },
  selectedFriendObj: {
    type: Object,
    default: null,
  },
  remitAmount: {
    type: Number,
    default: 0,
  },
  remitAmountDisplay: {
    type: String,
    default: "",
  },
  categoryList: {
    type: Array,
    default: () => [],
  },
  selectedCategoryId: {
    type: [Number, String],
    default: 1,
  },
  dutchSplitMode: {
    type: String,
    default: "EQUAL",
  },
  remitType: {
    type: String,
    default: "ACCOUNT",
  },
  myBalance: {
    type: Number,
    default: 0,
  },
  myAccountName: {
    type: String,
    default: "KB국민 주거래통장",
  },
  selectedDutchFriends: {
    type: Array,
    default: () => [],
  },
  customDutchAmounts: {
    type: Object,
    default: () => ({}),
  },
  getBankLogoFileName: {
    type: Function,
    required: true,
  },
  getBankName: {
    type: Function,
    required: true,
  },
  getFriendName: {
    type: Function,
    required: true,
  },
  formatCurrency: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits([
  "onAmountInput",
  "addAmount",
  "setAllBalance",
  "update:selectedCategoryId",
  "openTxSelect",
  "update:dutchSplitMode",
]);

const isCategoryExpanded = ref(false);
const localDutchSplitMode = ref(props.dutchSplitMode || "EQUAL");
const localCustomAmounts = ref(props.customDutchAmounts || {});

const defaultSlim = [
  { id: 1, name: "식비" },
  { id: 2, name: "카페" },
  { id: 3, name: "생활" },
  { id: 4, name: "교통" },
];

const slimCategories = computed(() => {
  const selId = props.selectedCategoryId;
  if (selId && !defaultSlim.some(d => d.id === selId)) {
    const found = props.categoryList.find(c => (c.spendingCategoryId || c.id) === selId);
    if (found) {
      return [
        ...defaultSlim.slice(0, 3),
        { id: selId, name: found.categoryName || found.name || "선택됨" }
      ];
    }
  }
  return defaultSlim;
});

const selectSlimCategory = (id) => {
  emit("update:selectedCategoryId", id);
};

// 더보기 영역에서 카테고리 선택 시 자동으로 1줄 슬림뷰로 접힘!
const onSelectExpandedCategory = (id) => {
  emit("update:selectedCategoryId", id);
  isCategoryExpanded.value = false;
};

const switchSplitMode = (mode) => {
  localDutchSplitMode.value = mode;
  emit("update:dutchSplitMode", mode);
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.remit-amount-step-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  box-sizing: border-box;
}

.receiver-summary-box {
  background-color: var(--color-bg-screen, #f8f9fa);
  border-radius: 16px;
  padding: 18px 20px;
  margin-bottom: 28px;
}

.summary-top-my-account {
  margin-bottom: 14px;
}

.summary-target-info-line {
  display: flex;
  align-items: center;
  gap: 14px;
}

.bank-logo-img-medium {
  width: 44px;
  height: 44px;
  object-fit: contain;
  border-radius: 50%;
  border: 1px solid #edf2f7;
}

.receiver-info-col {
  flex: 1;
}

.receiver-name {
  margin: 0;
  color: #111111;
}

.sub-handle {
  margin: 2px 0 0;
  color: #718096;
}

.form-field-group {
  margin-bottom: 16px;
}

.category-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.category-toggle-sub-btn {
  background: none;
  border: none;
  color: #ffbc2e;
  cursor: pointer;
  padding: 0;
}

/* 1줄 슬림 카테고리 칩 */
.dutch-cat-chip-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.dutch-cat-chip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 38px;
  border-radius: 10px;
  background-color: #f7fafc;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 0 4px;
}

.dutch-cat-chip.active {
  background-color: #fffdf8;
  border-color: #ffbc2e;
  box-shadow: 0 2px 6px rgba(255, 188, 46, 0.2);
}

.cat-fa-ic {
  font-size: 14px;
  color: #718096;
}

.dutch-cat-chip.active .cat-fa-ic {
  color: #ffbc2e;
}

.cat-name {
  color: #2d3748;
}

.dutch-cat-chip.active .cat-name {
  color: #111111;
}

.amount-input-row {
  display: flex;
  align-items: center;
  border-bottom: 2px solid #ffbc2e;
  padding-bottom: 8px;
}

.amount-direct-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  color: #111111;
}

.krw-unit {
  color: #111111;
  font-weight: 700;
  margin-left: 6px;
  flex-shrink: 0;
}

.quick-amount-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.content-btn {
  height: 42px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.content-btn.primary {
  background-color: #ffbc2e;
  border-color: #ffbc2e;
  color: #111111;
}

.tx-link-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  background: none;
  border: none;
  color: #718096;
  cursor: pointer;
  padding: 4px 0;
  transition: color 0.2s ease;
}

.tx-link-btn:hover {
  color: #111111;
}

/* 정산 방식 선택 탭 */
.split-tab-bar {
  display: grid;
  grid-template-columns: 1fr 1fr;
  background-color: #edf2f7;
  border-radius: 12px;
  padding: 3px;
}

.split-tab {
  height: 40px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #718096;
  cursor: pointer;
  transition: all 0.2s ease;
}

.split-tab.active {
  background-color: #ffffff;
  color: #111111;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

/* 정산 배분 요약 카드 */
.dutch-calc-box {
  background-color: #f8f9fa;
  border-radius: 16px;
  padding: 16px;
  box-sizing: border-box;
}

.calc-header-row {
  margin-bottom: 12px;
}

.calc-title {
  color: #4a5568;
}

.calc-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.calc-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.my-name, .friend-name {
  color: #111111;
}

.val-text {
  color: #111111;
}

.val-text.red-val {
  color: #e53e3e;
}

.custom-input-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
}

.custom-calc-input {
  width: 100px;
  height: 36px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 0 10px;
  text-align: right;
  outline: none;
  background-color: #ffffff;
  color: #111111;
}

.custom-calc-input:focus {
  border-color: #ffbc2e;
}
</style>
