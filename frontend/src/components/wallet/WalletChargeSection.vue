<template>
  <div>
    <PageHeader
      title="지갑 수동 충전"
      :show-back="true"
      @back="$emit('backToMain')"
    />

    <div class="card-body-scroll">
      <div v-if="chargeSuccess" class="complete-step-wrap">
        <div class="success-icon-circle">
          <i class="fa-solid fa-check"></i>
        </div>
        <h4 class="text-20-bold m-0">충전이 완료되었습니다!</h4>
        <p class="text-28-bold success-amt">
          +{{ formatCurrency(lastChargedAmount) }} 원
        </p>
        <p class="text-13 sub-txt">전자지갑 잔액에 즉시 반영되었습니다.</p>
        <div class="next-btn-wrap" style="width: 100%; margin-top: 24px">
          <button
            class="bottom-btn text-18-bold"
            @click="$emit('backToMain')"
          >
            지갑 홈으로 돌아가기 <i class="fa-solid fa-arrow-right"></i>
          </button>
        </div>
      </div>

      <template v-else>
        <div class="receiver-summary-box">
          <div class="summary-top-line">
            <span class="summary-label text-13-bold">출금 계좌 정보</span>
            <span class="summary-type-tag text-13-bold"
              >{{ primaryAccount.bankName || "KB" }} 주거래</span
            >
          </div>
          <div class="summary-main-line">
            <div class="bank-circle-icon bg-kb text-13-bold">
              <img
                v-if="primaryAccount.bankCode"
                :src="`/api/banks/logo/${getBankLogoFileName(primaryAccount.bankName)}`"
                class="bank-logo-img-small"
                alt="bank"
              />
              <span v-else>KB</span>
            </div>
            <div class="receiver-info-col">
              <h4 class="receiver-name text-15-bold">
                {{ primaryAccount.bankName || "KB국민" }} 주거래 계좌
              </h4>
              <p class="receiver-desc text-13 account-desc-row">
                <span class="acc-num">{{
                  primaryAccount.accountNumber || "110-111-111111"
                }}</span>
                <span class="right-align-balance"
                  >출금 가능 잔액:
                  <strong
                    >{{ formatCurrency(accountBalance) }} 원</strong
                  ></span
                >
              </p>
            </div>
          </div>
        </div>

        <div class="form-field-group">
          <label class="field-label text-15-bold">충전할 금액 입력</label>
          <div class="amount-input-row">
            <input
              :value="chargeAmountDisplay"
              @input="$emit('onAmountInput', $event)"
              type="text"
              inputmode="numeric"
              class="amount-direct-input text-26-bold"
              placeholder="0"
            />
            <span class="krw-unit text-26-bold">원</span>
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
              @click="$emit('addAmount', 30000)"
            >
              +3만
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
          </div>
        </div>

        <div v-if="chargeError" class="error-msg-box text-13-bold">
          {{ chargeError }}
        </div>

        <div class="next-btn-wrap">
          <button
            class="btn-primary text-18-bold"
            :disabled="chargeLoading || chargeAmount <= 0"
            @click="$emit('submit')"
          >
            충전 신청하기
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import PageHeader from "@/components/common/PageHeader.vue";

defineProps({
  chargeSuccess: {
    type: Boolean,
    default: false,
  },
  lastChargedAmount: {
    type: Number,
    default: 0,
  },
  primaryAccount: {
    type: Object,
    default: () => ({ accountNumber: "", bankName: "KB국민", bankCode: "004" }),
  },
  accountBalance: {
    type: Number,
    default: 0,
  },
  chargeAmount: {
    type: Number,
    default: 0,
  },
  chargeAmountDisplay: {
    type: String,
    default: "",
  },
  chargeError: {
    type: String,
    default: "",
  },
  chargeLoading: {
    type: Boolean,
    default: false,
  },
  formatCurrency: {
    type: Function,
    required: true,
  },
  getBankLogoFileName: {
    type: Function,
    required: true,
  },
});

defineEmits([
  "backToMain",
  "onAmountInput",
  "addAmount",
  "submit",
]);
</script>

<style scoped>
.card-body-scroll {
  padding: 20px;
}

.complete-step-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 40px 0;
}

.success-icon-circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background-color: #2e7d32;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-bottom: 16px;
}

.success-amt {
  color: #ffbc00;
  margin: 12px 0 4px;
}

.sub-txt {
  color: #777777;
}

.receiver-summary-box {
  background-color: var(--color-bg-screen, #f8f9fa);
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 24px;
}

.summary-top-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.summary-label {
  color: #666666;
}

.summary-type-tag {
  color: #ffbc00;
}

.summary-main-line {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bank-circle-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background-color: #ffbc00;
}

.bank-logo-img-small {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.receiver-info-col {
  flex: 1;
}

.receiver-name {
  margin: 0 0 2px;
}

.account-desc-row {
  margin: 0;
  display: flex;
  justify-content: space-between;
  color: #777777;
}

.form-field-group {
  margin-bottom: 24px;
}

.field-label {
  display: block;
  margin-bottom: 10px;
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
  font-size: 26px;
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

.error-msg-box {
  color: #d32f2f;
  background-color: #ffebee;
  padding: 12px 16px;
  border-radius: 10px;
  margin-bottom: 20px;
}

.next-btn-wrap {
  width: 100%;
}

.btn-primary,
.bottom-btn {
  width: 100%;
  height: 52px;
  border: none;
  background-color: #ffbc00;
  color: #111111;
  border-radius: 14px;
  font-weight: 700;
  cursor: pointer;
}

.btn-primary:disabled {
  background-color: #e0e0e0;
  color: #a0a0a0;
  cursor: not-allowed;
}
</style>
