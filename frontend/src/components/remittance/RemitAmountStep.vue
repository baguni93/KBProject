<template>
  <div class="remit-amount-step-container">
    <template v-if="remitType !== 'DUTCH'">
      <!-- 1. 수취 대상 확인 카드 (토스 style: 출금계좌 미니 캡슐 + 받으실 분 대형 강조) -->
      <div class="receiver-summary-box-v2">
        <div class="my-acc-capsule-badge">
          <span class="capsule-bank-name text-13-bold">{{ myAccountName || '페이머니' }}</span>
          <span class="capsule-dot">•</span>
          <span class="capsule-balance text-13">잔액 {{ formatCurrency(myBalance || 0) }}원</span>
        </div>

        <div class="summary-target-info-line">
          <img
            :src="`/api/banks/logo/${getBankLogoFileName(remitType === 'FRIEND' ? 'kb' : accountForm.bankCode)}`"
            class="bank-logo-img-medium"
            @error="(e) => (e.target.src = '/api/banks/logo/kb.png')"
          />
          <div class="receiver-info-col">
            <h3 class="receiver-name text-22-bold">
              <template v-if="remitType === 'FRIEND' || remitType === 'DUTCH_PAY'">
                {{ selectedFriendObj?.name || selectedFriendObj?.nickname || accountForm.receiverName || "친구" }}님에게
              </template>
              <template v-else>
                {{ accountForm.receiverName || "수취인" }}님에게
              </template>
            </h3>
            <p class="sub-handle text-13">
              <template v-if="remitType === 'FRIEND' || remitType === 'DUTCH_PAY'">
                @{{ selectedFriendObj?.username || selectedFriendObj?.nickname || "friend" }}
              </template>
              <template v-else>
                {{ getBankName(accountForm.bankCode) }} {{ accountForm.accountNumber }}
              </template>
            </p>
          </div>
        </div>
      </div>

      <!-- 2. 얼마나 보낼까요? 금액 입력 (토스 style 32px 초대형 타이포 + Soft Pill 퀵 버튼) -->
      <div class="amount-section-wrap">
        <label class="field-label text-20-bold" style="color: #0f172a; margin-bottom: 8px;">
          얼마나 보낼까요?
        </label>

        <div class="amount-large-display">
          <input
            :value="remitAmountDisplay"
            @input="$emit('onAmountInput', $event)"
            type="text"
            inputmode="numeric"
            class="amount-direct-input text-32-bold"
            placeholder="0"
          />
          <span class="krw-unit text-32-bold">원</span>
        </div>

        <div class="quick-amount-pill-row">
          <button
            type="button"
            class="btn-pill-quick text-13-bold"
            @click="$emit('addAmount', 10000)"
          >
            +1만
          </button>
          <button
            type="button"
            class="btn-pill-quick text-13-bold"
            @click="$emit('addAmount', 50000)"
          >
            +5만
          </button>
          <button
            type="button"
            class="btn-pill-quick text-13-bold"
            @click="$emit('addAmount', 100000)"
          >
            +10만
          </button>
          <button
            type="button"
            class="btn-pill-quick primary-pill text-13-bold"
            @click="$emit('setAllBalance')"
          >
            전액
          </button>
        </div>

        <!-- 잔액 초과 경고 메시지 -->
        <div v-if="isExceedBalance" class="balance-warning-msg text-13-bold">
          <i class="fa-solid fa-circle-exclamation"></i>
          출금 가능 잔액을 초과했습니다. (출금 가능: {{ formatCurrency(totalAvailableBalance || myBalance || 0) }}원)
        </div>

        <!-- 자동 충전 안내 메시지 -->
        <div v-else-if="isAutoChargeNeeded && autoChargeAmount > 0" class="auto-charge-info-msg text-13">
          <span class="auto-charge-text">지갑 잔액 부족으로 대표계좌에서 <strong class="text-13-bold">{{ formatCurrency(autoChargeAmount) }}원</strong>이 자동 충전됩니다.</span>
        </div>
      </div>

      <!-- 3. 하단 네이티브 금융 가상 키패드 (계좌/친구 전용) -->
      <div class="numeric-keypad-container">
        <div class="numeric-keypad-grid">
          <button
            v-for="num in ['1', '2', '3', '4', '5', '6', '7', '8', '9', '00', '0']"
            :key="num"
            type="button"
            class="keypad-btn text-22-bold"
            @click="pressKeypad(num)"
          >
            {{ num }}
          </button>
          <button
            type="button"
            class="keypad-btn text-20-bold keypad-back-btn"
            @click="backspaceKeypad"
          >
            ⌫
          </button>
        </div>
      </div>
    </template>

    <template v-else>
      <!-- 카카오페이 1:1 정산금액 입력 화면 (이상한 비어있는 카테고리 태그 철거, 닉네임 동적 노출) -->
      <!-- 1. 금액 입력 카드 박스 -->
      <div class="dutch-amount-card-box">
        <label class="dutch-card-label text-15-bold">얼마를 정산할까요?</label>
        <div class="dutch-amount-input-line">
          <input
            :value="remitAmountDisplay"
            @input="$emit('onAmountInput', $event)"
            type="text"
            inputmode="numeric"
            class="dutch-direct-input text-28-bold"
            placeholder="0"
          />
          <span class="krw-unit text-28-bold">원</span>
        </div>
      </div>

      <!-- 2. 결제 내역 불러오기 링크 -->
      <div class="tx-link-row">
        <button
          type="button"
          class="tx-link-btn text-13-bold"
          @click="$emit('openTxSelect')"
        >
          결제 내역 불러오기 <span class="tx-link-arr">&gt;</span>
        </button>
      </div>

      <!-- 3. 참여 친구 목록 (참여 인원 N명 | ✏️ 친구편집) -->
      <div class="dutch-participants-section">
        <div class="dutch-part-header">
          <span class="text-14-bold part-count-title">
            참여 인원 {{ selectedDutchFriends.length + 1 }}명 (나 포함)
          </span>
          <button type="button" class="btn-edit-friends text-13-bold" @click="$emit('editFriends')">
            <i class="fa-solid fa-pen"></i> 친구편집
          </button>
        </div>

        <div class="dutch-part-list">
          <!-- 나 (로그인 유저 닉네임 동적 노출) -->
          <div class="dutch-part-row">
            <div class="part-avatar-wrap">
              <img
                :src="myProfileImageUrl"
                class="part-avatar-img"
                @error="(e) => { e.target.onerror = null; e.target.style.display = 'none'; }"
              />
            </div>
            <div class="part-text-area">
              <span class="part-name text-15-bold">
                {{ myProfileName }} <span style="font-size: 12px; color: #ff9f00; font-weight: 700;">(나)</span>
              </span>
            </div>
          </div>

          <!-- 선택된 친구들 -->
          <div
            v-for="fId in selectedDutchFriends"
            :key="fId"
            class="dutch-part-row"
          >
            <div class="part-avatar-wrap">
              <img
                :src="getProfileImageUrl(getFriendObj(fId))"
                class="part-avatar-img"
                @error="(e) => { e.target.onerror = null; e.target.src = '/images/default_avatar.png'; }"
              />
            </div>
            <div class="part-text-area">
              <span class="part-name text-15-bold">{{ getFriendName(fId) }}</span>
            </div>
            <button
              type="button"
              class="btn-del-part"
              @click="$emit('removeFriend', fId)"
              title="참여자 제외"
            >
              <i class="fa-solid fa-circle-xmark"></i>
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from "vue";
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
  selectedDutchFriends: {
    type: Array,
    default: () => [],
  },
  customDutchAmounts: {
    type: Object,
    default: () => ({}),
  },
  myAccountName: {
    type: String,
    default: "페이머니",
  },
  myBalance: {
    type: Number,
    default: 0,
  },
  totalAvailableBalance: {
    type: Number,
    default: 0,
  },
  isExceedBalance: {
    type: Boolean,
    default: false,
  },
  isAutoChargeNeeded: {
    type: Boolean,
    default: false,
  },
  autoChargeAmount: {
    type: Number,
    default: 0,
  },
  myProfileImageUrl: {
    type: String,
    default: "/api/feeds/profile/profile_1.png",
  },
  myProfileName: {
    type: String,
    default: "내 프로필",
  },
  getBankLogoFileName: {
    type: Function,
    default: () => "kb.png",
  },
  getBankName: {
    type: Function,
    default: () => "KB국민",
  },
  getFriendName: {
    type: Function,
    default: (fId) => "친구",
  },
  getFriendObj: {
    type: Function,
    default: () => null,
  },
  getProfileImageUrl: {
    type: Function,
    default: (friend) => friend?.avatarUrl || "/api/feeds/profile/default_profile.png",
  },
  formatCurrency: {
    type: Function,
    default: (val) => new Intl.NumberFormat("ko-KR").format(val || 0),
  },
});

const emit = defineEmits([
  "onAmountInput",
  "addAmount",
  "setAllBalance",
  "update:selectedCategoryId",
  "openTxSelect",
  "update:dutchSplitMode",
  "editFriends",
  "removeFriend",
]);

const pressKeypad = (val) => {
  let currentStr = props.remitAmountDisplay ? String(props.remitAmountDisplay).replace(/[^0-9]/g, "") : "";
  if (currentStr === "0") currentStr = "";
  const newStr = currentStr + val;
  if (newStr.length <= 10) {
    emit("onAmountInput", { target: { value: newStr } });
  }
};

const backspaceKeypad = () => {
  let currentStr = props.remitAmountDisplay ? String(props.remitAmountDisplay).replace(/[^0-9]/g, "") : "";
  if (currentStr.length <= 1) {
    emit("onAmountInput", { target: { value: "0" } });
  } else {
    const newStr = currentStr.slice(0, -1);
    emit("onAmountInput", { target: { value: newStr } });
  }
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

/* 수취인 확인 카드 (계좌/친구) */
.receiver-summary-box-v2 {
  background-color: #f8fafc;
  border-radius: 20px;
  padding: 16px 18px 20px;
  margin-bottom: 24px;
  border: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.my-acc-capsule-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 4px 12px;
  align-self: flex-start;
  color: #475569;
}

.capsule-bank-name {
  color: #0f172a;
}

.capsule-dot {
  color: #cbd5e1;
}

.capsule-balance {
  color: #64748b;
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
  color: #0f172a;
}

.sub-handle {
  margin: 2px 0 0;
  color: #64748b;
}

.amount-section-wrap {
  display: flex;
  flex-direction: column;
  margin-bottom: 24px;
}

.amount-large-display {
  display: flex;
  align-items: center;
  border-bottom: 2px solid #ffbc2e;
  padding-bottom: 8px;
  margin-bottom: 14px;
  width: 100%;
  box-sizing: border-box;
}

.amount-direct-input {
  flex: 1;
  min-width: 0;
  width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: #0f172a;
  font-size: 30px;
  font-weight: 800;
  padding: 0;
  margin: 0;
}

.krw-unit {
  flex-shrink: 0;
  color: #0f172a;
  font-size: 26px;
  font-weight: 800;
  margin-left: 6px;
}

.quick-amount-pill-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.btn-pill-quick {
  height: 42px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.18s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02);
}

.btn-pill-quick:active {
  transform: scale(0.96);
  background-color: #f1f5f9;
}

.btn-pill-quick.primary-pill {
  background-color: #fff8e5;
  border-color: #ffbc2e;
  color: #d97706;
}

/* =========================================
   카카오페이 1:1 정산금액 입력 화면
========================================= */
.dutch-amount-card-box {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dutch-card-label {
  color: #64748b;
}

.dutch-amount-input-line {
  display: flex;
  align-items: baseline;
}

.dutch-direct-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  color: #0f172a;
  font-size: 28px;
  font-weight: 800;
}

.tx-link-row {
  margin-top: 10px;
}

.tx-link-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  padding: 0;
}

.dutch-participants-section {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dutch-part-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.part-count-title {
  color: #0f172a;
}

.btn-edit-friends {
  background: none;
  border: none;
  color: #d97706;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.dutch-part-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dutch-part-row {
  display: flex;
  align-items: center;
  padding: 10px 4px;
  border-bottom: 1px solid #f1f5f9;
}

.part-avatar-wrap {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 14px;
  border: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.part-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.part-text-area {
  flex: 1;
}

.part-name {
  color: #0f172a;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-del-part {
  background: none;
  border: none;
  color: #cbd5e1;
  font-size: 20px;
  cursor: pointer;
  transition: color 0.15s ease;
}

.btn-del-part:hover {
  color: #ef4444;
}

/* 하단 키패드 (계좌/친구 전용) */
.numeric-keypad-container {
  margin-top: auto;
  padding-top: 12px;
  width: 100%;
}

.numeric-keypad-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  width: 100%;
}

.keypad-btn {
  height: 52px;
  border-radius: 16px;
  border: none;
  background-color: #f8fafc;
  color: #0f172a;
  font-size: 22px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s cubic-bezier(0.16, 1, 0.3, 1);
  user-select: none;
}

.keypad-btn:active {
  background-color: #e2e8f0;
  transform: scale(0.95);
}

.keypad-back-btn {
  color: #64748b;
}

.balance-warning-msg {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 14px;
  background-color: #fef2f2;
  color: #ef4444;
  border-radius: 12px;
  font-size: 13px;
  animation: fadeIn 0.2s ease-in-out;
}

.auto-charge-info-msg {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 14px;
  background-color: #fffbeb;
  color: #b45309;
  border-radius: 12px;
  font-size: 13px;
  animation: fadeIn 0.2s ease-in-out;
  text-align: left;
}

.auto-charge-bolt {
  flex-shrink: 0;
}

.auto-charge-text {
  flex: 1;
  word-break: keep-all;
  line-height: 1.4;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
