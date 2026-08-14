<template>
  <div>
    <template v-if="remitType !== 'DUTCH'">
      <!-- 수취 대상 확인 카드 -->
      <div class="receiver-summary-box">
        <div class="summary-main-line">
          <img
            :src="`/api/banks/logo/${getBankLogoFileName(remitType === 'FRIEND' ? 'kb' : accountForm.bankCode)}`"
            class="bank-logo-img-medium"
          />
          <div class="receiver-info-col">
            <h4 class="receiver-name text-15-bold">
              <template v-if="remitType === 'FRIEND'">
                {{ selectedFriendObj?.name || "선택한 친구" }}
                <span class="sub-handle text-13"
                  >(@{{ selectedFriendObj?.username || "" }})</span
                >
              </template>
              <template v-else>
                {{ accountForm.receiverName || "수취인" }}
                <span class="sub-handle text-13"
                  >({{ getBankName(accountForm.bankCode) }}
                  {{ accountForm.accountNumber }})</span
                >
              </template>
            </h4>
          </div>
          <span class="summary-type-tag text-13-bold">{{
            remitType === "FRIEND" ? "친구 송금" : "계좌 송금"
          }}</span>
        </div>
      </div>

      <!-- 송금할 금액 입력 및 빠른 금액 버튼 -->
      <div class="form-field-group">
        <label class="field-label text-13-bold">송금할 금액 입력</label>
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
        <div class="quick-amount-row">
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
      <!-- Dutch 2번 화면: 카테고리 & 정산 금액 선택 및 정산 방식 선택 -->
      <div class="form-field-group">
        <div class="category-title-flex">
          <label class="field-label text-13-bold" style="margin-bottom: 0"
            ><i class="fa-solid fa-shapes brand-ic"></i> 소비 카테고리
            선택</label
          >
          <button
            type="button"
            class="category-toggle-sub-btn text-13-bold"
            @click="$emit('toggleCategoryExpanded')"
          >
            <span>{{
              isCategoryExpanded
                ? "접기 ▲"
                : "더보기 (" + (categoryList.length - 4) + "개) ▼"
            }}</span>
          </button>
        </div>
        <SpendingCategorySelector
          :model-value="selectedCategoryId"
          @update:model-value="$emit('update:selectedCategoryId', $event)"
          :categories="displayedCategoryList"
          compact
        />
      </div>

      <div class="form-field-group" style="margin-top: 16px">
        <label class="field-label text-13-bold">얼마를 정산할까요?</label>
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
          내 결제 내역에서 선택 <i class="fa-solid fa-chevron-right"></i>
        </button>
      </div>

      <!-- 정산 방식 선택 (1/N 균등 vs 차등 정산) -->
      <div class="form-field-group" style="margin-top: 20px">
        <label class="field-label text-13-bold">정산 방식 선택</label>
        <div class="split-tab-bar">
          <button
            type="button"
            class="split-tab text-13-bold"
            :class="{ active: dutchSplitMode === 'EQUAL' }"
            @click="$emit('update:dutchSplitMode', 'EQUAL')"
          >
            1/N 균등 정산
          </button>
          <button
            type="button"
            class="split-tab text-13-bold"
            :class="{ active: dutchSplitMode === 'CUSTOM' }"
            @click="$emit('update:dutchSplitMode', 'CUSTOM')"
          >
            차등 정산 (직접 입력)
          </button>
        </div>
      </div>

      <!-- 정산 배분 요약 박스 -->
      <div class="dutch-calc-box" style="margin-top: 16px">
        <span class="calc-title text-13-bold">
          {{
            dutchSplitMode === "EQUAL"
              ? `1/N 균등 정산 배분 (총 ${selectedDutchFriends.length + 1}명)`
              : "멤버별 정산 금액 직접 입력"
          }}
        </span>

        <div v-if="dutchSplitMode === 'EQUAL'" class="calc-list">
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
            <input
              type="number"
              v-model.number="customDutchAmounts['my']"
              class="custom-calc-input text-13-bold"
              placeholder="금액 입력"
            />
          </div>
          <div
            v-for="fId in selectedDutchFriends"
            :key="fId"
            class="calc-row text-15-bold"
          >
            <span>{{ getFriendName(fId) }}</span>
            <input
              type="number"
              v-model.number="customDutchAmounts[fId]"
              class="custom-calc-input text-13-bold"
              placeholder="금액 입력"
            />
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import SpendingCategorySelector from "@/components/common/SpendingCategorySelector.vue";

defineProps({
  remitType: {
    type: String,
    required: true,
  },
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
  isCategoryExpanded: {
    type: Boolean,
    default: false,
  },
  categoryList: {
    type: Array,
    default: () => [],
  },
  displayedCategoryList: {
    type: Array,
    default: () => [],
  },
  selectedCategoryId: {
    type: [Number, String],
    default: null,
  },
  dutchSplitMode: {
    type: String,
    default: "EQUAL",
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

defineEmits([
  "onAmountInput",
  "addAmount",
  "setAllBalance",
  "toggleCategoryExpanded",
  "update:selectedCategoryId",
  "openTxSelect",
  "update:dutchSplitMode",
]);
</script>

<style scoped>
.receiver-summary-box {
  background-color: var(--color-bg-screen, #f8f9fa);
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.summary-main-line {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bank-logo-img-medium {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.receiver-info-col {
  flex: 1;
}

.receiver-name {
  margin: 0;
}

.sub-handle {
  color: #777777;
  font-weight: normal;
}

.summary-type-tag {
  color: #ffbc00;
}

.form-field-group {
  margin-bottom: 20px;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  color: var(--color-text-main, #111111);
}

.amount-input-row {
  display: flex;
  align-items: center;
  border-bottom: 2px solid #ffbc00;
  padding-bottom: 8px;
  margin-bottom: 16px;
}

.amount-direct-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 28px;
  font-weight: 700;
}

.krw-unit {
  margin-left: 8px;
}

.quick-amount-row {
  display: flex;
  gap: 8px;
}

.content-btn {
  flex: 1;
  padding: 10px;
  border: 1px solid var(--color-border-main, #ededed);
  border-radius: 10px;
  background-color: #ffffff;
  cursor: pointer;
}

.content-btn.primary {
  background-color: #ffbc00;
  border-color: #ffbc00;
  color: #111111;
}

.category-title-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.category-toggle-sub-btn {
  border: none;
  background: none;
  color: #ffbc00;
  cursor: pointer;
}

.tx-link-btn {
  border: none;
  background: none;
  color: #666666;
  padding: 0;
  cursor: pointer;
  margin-top: 8px;
}

.split-tab-bar {
  display: flex;
  background-color: var(--color-bg-screen, #f5f6f8);
  padding: 4px;
  border-radius: 10px;
  gap: 4px;
}

.split-tab {
  flex: 1;
  border: none;
  background: transparent;
  padding: 10px;
  border-radius: 8px;
  color: #777777;
  cursor: pointer;
}

.split-tab.active {
  background-color: #ffffff;
  color: #111111;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.dutch-calc-box {
  background-color: var(--color-bg-screen, #f8f9fa);
  border-radius: 16px;
  padding: 16px 20px;
}

.calc-title {
  display: block;
  margin-bottom: 12px;
  color: #333333;
}

.calc-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.calc-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.val-text {
  color: #111111;
}

.val-text.red-val {
  color: #d32f2f;
}

.custom-calc-input {
  width: 120px;
  height: 36px;
  border: 1px solid var(--color-border-main, #ededed);
  border-radius: 8px;
  padding: 0 10px;
  text-align: right;
}
</style>
