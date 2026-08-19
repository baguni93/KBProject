<template>
  <div class="account-remit-container">
    <!-- 1. 계좌번호 입력 바 -->
    <div class="form-field-group">
      <div class="toss-search-bar">
        <i class="fa-solid fa-magnifying-glass search-icon"></i>
        <input
          :value="accountNumber"
          @input="handleAccountNumberInput($event.target.value)"
          type="text"
          class="toss-search-input text-15-bold"
          placeholder="'-' 없이 계좌번호 입력"
        />
      </div>
    </div>

    <!-- 2. 은행 선택 (Toss style 테두리 없는 깔끔한 동동이 로고 그리드) -->
    <div class="form-field-group">
      <span class="field-sec-title text-15-bold">은행 선택</span>
      <div class="bank-chip-grid">
        <button
          v-for="b in bankOptions"
          :key="b.code"
          class="bank-chip-card"
          :class="{ active: bankCode === b.code }"
          @click="$emit('update:bankCode', b.code)"
        >
          <div class="bank-logo-circle">
            <img
              :src="`/api/banks/logo/${b.fileName}`"
              class="bank-logo-img"
              :alt="b.name"
            />
          </div>
          <span class="bank-chip-name text-13-bold">{{ b.name }}</span>
        </button>
      </div>
    </div>

    <!-- 3. 최근 보낸 계좌 (카카오페이/토스 1:1 보더리스 클린 라인) -->
    <div class="form-field-group">
      <span class="field-sec-title text-15-bold">최근 보낸 계좌</span>
      <div
        v-if="uniqueRecentAccounts.length === 0"
        class="empty-recent-msg text-13"
      >
        최근 보낸 계좌가 없습니다.
      </div>
      <div v-else class="toss-recent-list">
        <div
          v-for="recent in uniqueRecentAccounts"
          :key="recent.accountNumber + '_' + (recent.bankCode || recent.bankName)"
          class="toss-recent-row"
          @click="$emit('selectRecent', recent)"
        >
          <div class="toss-recent-avatar-wrap">
            <img
              :src="`/api/banks/logo/${getBankLogoFileName(recent.bankName || recent.bankCode)}`"
              class="toss-bank-avatar-img"
              :alt="getBankName(recent.bankName || recent.bankCode)"
              @error="(e) => (e.target.src = '/api/banks/logo/kb.png')"
            />
          </div>
          <div class="toss-recent-text-area">
            <span class="toss-recent-name text-15-bold">
              {{ recent.ownerName || recent.receiverName || recent.name || "수취인" }}
            </span>
            <span class="toss-recent-account text-13">
              {{ getBankName(recent.bankName || recent.bankCode) }} {{ recent.accountNumber }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  accountNumber: {
    type: String,
    default: "",
  },
  bankCode: {
    type: String,
    default: "",
  },
  bankOptions: {
    type: Array,
    default: () => [],
  },
  recentAccounts: {
    type: Array,
    default: () => [],
  },
  getBankLogoFileName: {
    type: Function,
    default: () => "kb.png",
  },
  getBankName: {
    type: Function,
    default: () => "KB국민",
  },
  formatCurrency: {
    type: Function,
    default: (val) => new Intl.NumberFormat("ko-KR").format(val || 0),
  },
});

const emit = defineEmits([
  "update:accountNumber",
  "update:bankCode",
  "selectRecent",
]);

// 계좌번호 앞자리 패턴 기반 은행 자동 감지
const detectBankByAccountNumber = (accNo) => {
  if (!accNo) return null;
  const clean = accNo.replace(/[^0-9]/g, "");
  if (clean.length < 3) return null;

  if (clean.startsWith("3333")) return "090"; // 카카오뱅크
  if (clean.startsWith("010") || clean.startsWith("011") || clean.startsWith("016") || clean.startsWith("017") || clean.startsWith("018") || clean.startsWith("019")) return "003"; // IBK기업 평생계좌
  if (clean.startsWith("110") || clean.startsWith("100") || clean.startsWith("150")) return "088"; // 신한은행
  if (clean.startsWith("937") || clean.startsWith("04") || clean.startsWith("92") || clean.startsWith("94") || clean.startsWith("01")) return "004"; // KB국민은행
  if (clean.startsWith("1002") || clean.startsWith("020")) return "020"; // 우리은행
  if (clean.startsWith("081") || clean.startsWith("101") || clean.startsWith("102")) return "081"; // 하나은행
  if (clean.startsWith("301") || clean.startsWith("302") || clean.startsWith("312") || clean.startsWith("351")) return "011"; // NH농협
  if (clean.startsWith("089") || clean.startsWith("1000")) return "089"; // 케이뱅크
  if (clean.startsWith("092") || clean.startsWith("10000")) return "092"; // 토스뱅크
  if (clean.startsWith("023")) return "023"; // SC제일

  return null;
};

const handleAccountNumberInput = (val) => {
  emit("update:accountNumber", val);
  const detected = detectBankByAccountNumber(val);
  if (detected) {
    emit("update:bankCode", detected);
  }
};

// 중복된 최근 계좌 제거 (동일 계좌번호는 1개만 노출)
const uniqueRecentAccounts = computed(() => {
  if (!props.recentAccounts || props.recentAccounts.length === 0) return [];
  const map = new Map();
  props.recentAccounts.forEach(item => {
    const accNo = item.accountNumber || item.accountNo;
    if (accNo && !map.has(accNo)) {
      map.set(accNo, item);
    }
  });
  return Array.from(map.values());
});
</script>

<style scoped>
@import "@/components/common/common/common.css";

.account-remit-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
  box-sizing: border-box;
}

.form-field-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-sec-title {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 2px;
}

/* 검색바 */
.toss-search-bar {
  display: flex;
  align-items: center;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 0 16px;
  height: 48px;
  transition: all 0.2s ease;
}

.toss-search-bar:focus-within {
  background-color: #ffffff;
  border-color: #ffbc2e;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.15);
}

.search-icon {
  color: #94a3b8;
  font-size: 15px;
  margin-right: 10px;
}

.toss-search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  color: #0f172a;
  line-height: 1.4;
}

.toss-search-input::placeholder {
  color: #94a3b8;
}

/* 은행 선택 (토스/카카오 style 테두리 없는 동동이 로고 그리드) */
.bank-chip-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px 8px;
  padding: 8px 0 4px;
}

.bank-chip-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4px 0;
  border: none;
  background: transparent;
  cursor: pointer;
  gap: 6px;
  box-sizing: border-box;
  transition: transform 0.18s cubic-bezier(0.16, 1, 0.3, 1);
}

.bank-chip-card:active {
  transform: scale(0.92);
}

.bank-logo-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
}

.bank-chip-card.active .bank-logo-circle {
  background-color: #fff8e5;
  box-shadow: inset 0 0 0 2px #ffbc2e, 0 4px 12px rgba(255, 188, 46, 0.25);
}

.bank-logo-img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.bank-chip-name {
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  line-height: 1;
  text-align: center;
  transition: color 0.2s ease;
}

.bank-chip-card.active .bank-chip-name {
  color: #0f172a;
  font-weight: 800;
}

/* 최근 보낸 계좌 리스트 (버튼/구분선 없는 카카오/토스 1:1 라인) */
.empty-recent-msg {
  color: #94a3b8;
  padding: 20px 0;
  text-align: center;
}

.toss-recent-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.toss-recent-row {
  display: flex;
  align-items: center;
  padding: 10px 4px;
  border: none;
  border-radius: 14px;
  background-color: transparent;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.toss-recent-row:hover,
.toss-recent-row:active {
  background-color: #f8fafc;
}

.toss-recent-avatar-wrap {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
  border: 1px solid #f1f5f9;
}

.toss-bank-avatar-img {
  width: 26px;
  height: 26px;
  object-fit: contain;
}

.toss-recent-text-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.toss-recent-name {
  color: #0f172a;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.2;
}

.toss-recent-account {
  color: #64748b;
  font-size: 13px;
  line-height: 1.2;
}
</style>
