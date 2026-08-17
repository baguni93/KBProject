<template>
  <div class="account-remit-container">
    <!-- 1. 계좌번호 입력 바 (카메라 아이콘 제거) -->
    <div class="form-field-group">
      <div class="toss-search-bar">
        <i class="fa-solid fa-magnifying-glass search-icon"></i>
        <input
          :value="accountNumber"
          @input="$emit('update:accountNumber', $event.target.value)"
          type="text"
          class="toss-search-input text-15-bold"
          placeholder="'-' 없이 계좌번호 입력"
        />
      </div>
    </div>

    <!-- 2. 은행 선택 (공용 폰트 규격 text-15-bold 통일) -->
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
          <img
            :src="`/api/banks/logo/${b.fileName}`"
            class="bank-logo-img"
            :alt="b.name"
          />
          <span class="bank-chip-name text-13-bold">{{ b.name }}</span>
        </button>
      </div>
    </div>

    <!-- 3. 최근 보낸 계좌 (공용 폰트 규격 text-15-bold 통일, 파란 별 제거) -->
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
    required: true,
  },
  getBankName: {
    type: Function,
    required: true,
  },
  formatCurrency: {
    type: Function,
    required: true,
  },
});

const uniqueRecentAccounts = computed(() => {
  const seen = new Set();
  return (props.recentAccounts || []).filter((item) => {
    const acc = item.accountNumber || "";
    const bank = item.bankCode || item.bankName || "";
    const key = `${bank}_${acc}`;
    if (!acc || seen.has(key)) return false;
    seen.add(key);
    return true;
  });
});

defineEmits([
  "update:accountNumber",
  "update:bankCode",
  "selectRecent",
]);
</script>

<style scoped>
@import "@/components/common/common/common.css";

.account-remit-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
  box-sizing: border-box;
}

.form-field-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 팀 공통 타이포그래피 표준 적용 섹션 타이틀 */
.field-sec-title {
  color: #111111;
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 2px;
}

/* 입력 바 */
.toss-search-bar {
  display: flex;
  align-items: center;
  background-color: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 0 16px;
  height: 50px;
  transition: all 0.2s ease;
}

.toss-search-bar:focus-within {
  background-color: #ffffff;
  border-color: #ffbc2e;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.15);
}

.search-icon {
  color: #a0aec0;
  font-size: 15px;
  margin-right: 10px;
}

.toss-search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 15px;
  color: #111111;
  line-height: 1.4;
}

.toss-search-input::placeholder {
  color: #a0aec0;
}

/* 은행 칩 그리드 */
.bank-chip-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.bank-chip-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  aspect-ratio: 1 / 1;
  padding: 6px 2px;
  border: 1px solid #edf2f7;
  border-radius: 14px;
  background-color: #ffffff;
  cursor: pointer;
  gap: 5px;
  box-sizing: border-box;
  transition: all 0.15s ease;
}

.bank-chip-card:hover {
  background-color: #f7fafc;
}

.bank-chip-card.active {
  border-color: #ffbc2e;
  background-color: #fffdf8;
  box-shadow: 0 2px 8px rgba(255, 188, 46, 0.2);
}

.bank-logo-img {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: contain;
}

.bank-chip-name {
  font-size: 12px;
  color: #2d3748;
  line-height: 1;
  text-align: center;
}

/* 최근 보낸 계좌 리스트 (공용 폰트 일치) */
.empty-recent-msg {
  color: #a0aec0;
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
  padding: 12px 10px;
  border-radius: 14px;
  background-color: transparent;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.toss-recent-row:hover,
.toss-recent-row:active {
  background-color: #f7fafc;
}

.toss-recent-avatar-wrap {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
  border: 1px solid #edf2f7;
}

.toss-bank-avatar-img {
  width: 28px;
  height: 28px;
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
  color: #111111;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.2;
}

.toss-recent-account {
  color: #718096;
  font-size: 13px;
  line-height: 1.2;
}
</style>
