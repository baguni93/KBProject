<template>
  <div>
    <!-- 계좌 번호 입력 -->
    <div class="form-field-group">
      <label class="field-label text-13-bold">계좌 번호 입력</label>
      <div class="input-with-btn-row">
        <input
          :value="accountNumber"
          @input="$emit('update:accountNumber', $event.target.value)"
          type="text"
          class="custom-input text-15-bold"
          placeholder="'-' 없이 계좌번호 입력"
        />
      </div>
    </div>

    <!-- 은행 선택 5열 그리드 -->
    <div class="form-field-group">
      <label class="field-label text-13-bold">은행 선택</label>
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

    <!-- 최근 송금 계좌 -->
    <div class="form-field-group">
      <label class="field-label text-13-bold">최근 송금 계좌</label>
      <div
        v-if="recentAccounts.length === 0"
        class="empty-recent-msg text-13"
      >
        최근 송금 내역이 없습니다.
      </div>
      <div v-else class="recent-list-wrap">
        <div
          v-for="recent in recentAccounts"
          :key="recent.id || recent.accountNumber"
          class="recent-card-item"
          @click="$emit('selectRecent', recent)"
        >
          <div class="recent-item-left">
            <img
              :src="`/api/banks/logo/${getBankLogoFileName(recent.bankName)}`"
              class="bank-logo-img-small"
            />
            <div class="recent-info-text">
              <p class="recent-name-line text-15-bold">
                {{ recent.receiverName || recent.name || "수취인" }} ({{
                  getBankName(recent.bankName)
                }}
                {{ recent.accountNumber }})
              </p>
              <p class="recent-sub-line text-13">
                최근 송금: {{ recent.date || "최근" }} •
                {{ formatCurrency(recent.amount) }}원
              </p>
            </div>
          </div>
          <i class="fa-solid fa-chevron-right arrow-ic"></i>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
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

defineEmits([
  "update:accountNumber",
  "update:bankCode",
  "selectRecent",
]);
</script>

<style scoped>
.form-field-group {
  margin-bottom: 20px;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  color: var(--color-text-main, #111111);
}

.input-with-btn-row {
  display: flex;
  gap: 8px;
}

.custom-input {
  flex: 1;
  height: 48px;
  border: 1px solid var(--color-border-main, #ededed);
  border-radius: 12px;
  padding: 0 16px;
  background-color: var(--color-bg-screen, #f8f9fa);
  outline: none;
}

.custom-input:focus {
  border-color: #ffbc00;
  background-color: #ffffff;
}

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
  padding: 10px 4px;
  border: 1px solid var(--color-border-main, #ededed);
  border-radius: 12px;
  background-color: #ffffff;
  cursor: pointer;
  gap: 6px;
}

.bank-chip-card.active {
  border-color: #ffbc00;
  background-color: rgba(255, 188, 0, 0.08);
}

.bank-logo-img {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.bank-chip-name {
  font-size: 11px;
  color: #333333;
}

.empty-recent-msg {
  color: #888888;
  padding: 16px 0;
  text-align: center;
}

.recent-list-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-card-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid var(--color-border-main, #ededed);
  border-radius: 14px;
  background-color: #ffffff;
  cursor: pointer;
}

.recent-item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bank-logo-img-small {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.recent-info-text p {
  margin: 0;
}

.recent-name-line {
  color: #111111;
}

.recent-sub-line {
  color: #777777;
  margin-top: 2px;
}

.arrow-ic {
  color: #cccccc;
  font-size: 12px;
}
</style>
