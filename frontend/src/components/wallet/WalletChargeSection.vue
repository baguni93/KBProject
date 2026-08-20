<template>
  <div class="charge-section-container">
    <div v-if="!chargeSuccess" class="charge-header-wrap">
      <PageHeader
        title="지갑 수동 충전"
        :show-back="true"
        :custom-back="true"
        @back="$emit('backToMain')"
      />
    </div>

    <!-- 1. 충전 완료 화면 (송금 완료 화면 RemitResultStep.vue과 100% 동일한 규격 및 애니메이션 적용) -->
    <div v-if="chargeSuccess" class="complete-page">
      <!-- 배경 장식 -->
      <div class="background-decoration decoration-left"></div>
      <div class="background-decoration decoration-right"></div>

      <main class="page-content complete-container">
        <section class="complete-content">
          <!-- 완료 애니메이션 -->
          <div class="success-visual">
            <span class="particle particle-1"></span>
            <span class="particle particle-2"></span>
            <span class="particle particle-3"></span>
            <span class="particle particle-4"></span>
            <span class="particle particle-5"></span>
            <span class="particle particle-6"></span>

            <!-- 별 장식 -->
            <span class="spark spark-1">
              <i class="fa-solid fa-star"></i>
            </span>
            <span class="spark spark-2">
              <i class="fa-solid fa-star"></i>
            </span>

            <div class="success-glow"></div>
            <div class="success-circle">
              <i class="fa-solid fa-check"></i>
            </div>
          </div>

          <!-- 완료 메시지 -->
          <div class="complete-message">
            <h1 class="text-30-bold">충전이 완료되었어요!</h1>
            <p class="complete-description text-20-bold" style="margin-top: 8px; color: #111111;">
              +{{ formatCurrency(lastChargedAmount) }}원
            </p>
          </div>

          <!-- 상세 내역 요약 카드 -->
          <div class="complete-detail-card">
            <div class="detail-row text-14">
              <span class="lbl text-14">출금 계좌</span>
              <span class="val text-15-bold">
                {{ primaryAccount?.bankName || "KB국민" }} {{ primaryAccount?.accountNumber || "110-111-111111" }}
              </span>
            </div>
          </div>
        </section>
      </main>

      <!-- 하단 2개 버튼 (좌측: 결제하기(흰색) / 우측: 확인(노란색)) -->
      <div class="bottom-btn-area double complete-button-area">
        <button
          class="bottom-btn secondary-button complete-button text-16-bold"
          type="button"
          @click="$emit('backToMain')"
        >
          결제하기
        </button>

        <button
          class="bottom-btn primary-button complete-button text-16-bold"
          type="button"
          @click="$emit('backToMain')"
        >
          확인
        </button>
      </div>
    </div>

    <!-- 2. 충전 입력 화면 -->
    <div v-else class="card-body-scroll">
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
            1만원
          </button>
          <button
            class="content-btn secondary text-13-bold"
            @click="$emit('addAmount', 30000)"
          >
            3만원
          </button>
          <button
            class="content-btn secondary text-13-bold"
            @click="$emit('addAmount', 50000)"
          >
            5만원
          </button>
          <button
            class="content-btn secondary text-13-bold"
            @click="$emit('addAmount', 100000)"
          >
            10만원
          </button>
        </div>
      </div>

      <div v-if="chargeError" class="error-msg-box text-13 text-danger">
        {{ chargeError }}
      </div>

      <!-- 하단 고정 충전하기 버튼 -->
      <div class="bottom-btn-area single">
        <button
          type="button"
          class="bottom-btn text-17-bold"
          :disabled="!chargeAmount || chargeLoading"
          @click="$emit('submit')"
        >
          {{ chargeLoading ? "충전 중..." : "충전하기" }}
        </button>
      </div>
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
@import "@/components/common/common/common.css";

.charge-section-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background-color: #ffffff;
  position: relative;
  box-sizing: border-box;
}

.charge-header-wrap {
  width: 100%;
  padding: 0 16px;
  background-color: #ffffff;
  box-sizing: border-box;
  border-bottom: 1px solid #f0f0f0;
}

.card-body-scroll {
  flex: 1;
  padding: 24px 20px 110px;
  overflow-y: auto;
}

/* =========================
   완료 페이지 (RemitResultStep 100% 동일 스타일)
========================= */

.complete-page {
  position: relative;
  overflow: hidden;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
  background-color: #ffffff;
  padding: 20px 24px 32px;
}

.complete-container {
  position: relative;
  z-index: 2;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  padding: 20px 0;
}

.complete-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  width: 100%;
  max-width: 360px;
  margin: 0 auto;
}

.background-decoration {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.decoration-left {
  top: -100px;
  left: -120px;
  width: 240px;
  height: 240px;
  background: rgba(255, 188, 46, 0.1);
  animation: background-float-left 6s ease-in-out infinite;
}

.decoration-right {
  top: 280px;
  right: -110px;
  width: 210px;
  height: 210px;
  background: rgba(176, 164, 255, 0.05);
  animation: background-float-right 7s ease-in-out infinite;
}

.success-visual {
  position: relative;
  width: 180px;
  height: 180px;
  margin: 0 auto 22px;
}

.success-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 126px;
  height: 126px;
  border-radius: 50%;
  background: rgba(255, 188, 46, 0.16);
  transform: translate(-50%, -50%);
  animation: glow 2.2s ease-in-out 0.8s infinite;
}

.success-circle {
  position: absolute;
  z-index: 2;
  top: 50%;
  left: 50%;
  display: flex;
  width: 102px;
  height: 102px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(145deg, #ffd15c, var(--color-primary, #ffbc2e));
  box-shadow:
    0 16px 34px rgba(255, 188, 46, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  color: #ffffff;
  font-size: 42px;
  transform: translate(-50%, -50%) scale(0);
  animation: success-pop 0.55s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.success-circle i {
  opacity: 0;
  transform: scale(0.5) rotate(-15deg);
  animation: check-appear 0.35s ease 0.42s forwards;
}

.particle {
  position: absolute;
  z-index: 1;
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary, #ffbc2e);
  opacity: 0;
}

.particle-1 {
  top: 28px;
  left: 28px;
  animation: particle-pop 0.55s ease 0.35s forwards;
}

.particle-2 {
  top: 14px;
  right: 28px;
  width: 6px;
  height: 6px;
  background: #9d90ff;
  animation: particle-pop 0.55s ease 0.5s forwards;
}

.particle-3 {
  top: 70px;
  right: 6px;
  width: 9px;
  height: 9px;
  background: #7bd6c7;
  animation: particle-pop 0.55s ease 0.4s forwards;
}

.particle-4 {
  right: 24px;
  bottom: 18px;
  width: 7px;
  height: 7px;
  background: #ff9eaa;
  animation: particle-pop 0.55s ease 0.6s forwards;
}

.particle-5 {
  bottom: 20px;
  left: 24px;
  width: 6px;
  height: 6px;
  background: #9d90ff;
  animation: particle-pop 0.55s ease 0.48s forwards;
}

.particle-6 {
  top: 78px;
  left: 4px;
  width: 8px;
  height: 8px;
  background: #7bd6c7;
  animation: particle-pop 0.55s ease 0.58s forwards;
}

.spark {
  position: absolute;
  z-index: 1;
  color: #ffd65c;
  opacity: 0;
}

.spark-1 {
  top: 14px;
  left: 60px;
  font-size: 12px;
  animation: spark-pop 0.55s ease 0.55s forwards;
}

.spark-2 {
  right: 38px;
  bottom: 14px;
  color: #a99df7;
  font-size: 10px;
  animation: spark-pop 0.55s ease 0.7s forwards;
}

.complete-message {
  opacity: 0;
  transform: translateY(16px);
  animation: content-up 0.5s ease 0.55s forwards;
}

.complete-message h1 {
  margin: 0;
  color: var(--color-text-main, #111111);
  line-height: 1.3;
  letter-spacing: -0.7px;
}

.complete-detail-card {
  width: 100%;
  margin-top: 20px;
  padding: 16px 18px;
  background-color: var(--color-bg-screen, #f8f9fa);
  border-radius: 16px;
  box-sizing: border-box;
  opacity: 0;
  transform: translateY(16px);
  animation: content-up 0.5s ease 0.65s forwards;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.lbl {
  color: #777777;
}

.val {
  color: #111111;
}

.complete-button-area {
  position: relative;
  z-index: 3;
  width: 100%;
}

.complete-button {
  opacity: 0;
  transform: translateY(10px);
  animation: button-show 0.45s ease forwards;
}

.complete-button:nth-child(1) {
  animation-delay: 0.82s;
}

.complete-button:nth-child(2) {
  animation-delay: 0.9s;
}

.complete-button-area .bottom-btn.secondary-button,
.secondary-button {
  border: 1px solid #e0e0e0 !important;
  background: #ffffff !important;
  color: #111111 !important;
}

.complete-button-area .bottom-btn.secondary-button:active,
.secondary-button:active {
  background: #f8f9fa !important;
}

.complete-button-area .bottom-btn.primary-button,
.primary-button {
  border: none !important;
  background: #ffbc2e !important;
  color: #111111 !important;
}

.complete-button-area .bottom-btn.primary-button:active,
.primary-button:active {
  background: #e5a900 !important;
}

@keyframes success-pop {
  0% { transform: translate(-50%, -50%) scale(0); }
  70% { transform: translate(-50%, -50%) scale(1.08); }
  100% { transform: translate(-50%, -50%) scale(1); }
}

@keyframes check-appear {
  from { opacity: 0; transform: scale(0.5) rotate(-15deg); }
  to { opacity: 1; transform: scale(1) rotate(0); }
}

@keyframes particle-pop {
  0% { opacity: 0; transform: scale(0); }
  60% { opacity: 1; transform: scale(1.4); }
  100% { opacity: 0.7; transform: scale(1); }
}

@keyframes spark-pop {
  0% { opacity: 0; transform: scale(0) rotate(-40deg); }
  60% { opacity: 1; transform: scale(1.4) rotate(12deg); }
  100% { opacity: 0.75; transform: scale(1) rotate(0); }
}

@keyframes glow {
  0%, 100% { opacity: 0.55; transform: translate(-50%, -50%) scale(0.95); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.1); }
}

@keyframes content-up {
  to { opacity: 1; transform: translateY(0); }
}

@keyframes button-show {
  to { opacity: 1; transform: translateY(0); }
}

@keyframes background-float-left {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(12px, 16px); }
}

@keyframes background-float-right {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-14px, -12px); }
}

/* =========================
   충전 입력 관련 스타일
========================= */

.receiver-summary-box {
  background-color: #f8f9fa;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 24px;
}

.summary-top-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.summary-label {
  color: #666666;
}

.summary-type-tag {
  color: #f59e0b;
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
  background-color: #ffffff;
  border: 1px solid #e5e7eb;
}

.bank-logo-img-small {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.receiver-info-col {
  flex: 1;
}

.receiver-name {
  margin: 0 0 4px;
  color: #111111;
}

.receiver-desc {
  margin: 0;
  color: #666666;
  display: flex;
  justify-content: space-between;
}

.right-align-balance strong {
  color: #111111;
}

.form-field-group {
  margin-bottom: 24px;
}

.field-label {
  display: block;
  margin-bottom: 12px;
  color: #111111;
}

.amount-input-row {
  display: flex;
  align-items: center;
  border-bottom: 2px solid #ffbc2e;
  padding-bottom: 8px;
  margin-bottom: 16px;
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
  color: #111111;
  font-size: 26px;
  font-weight: 800;
  padding: 0;
  margin: 0;
}

.krw-unit {
  flex-shrink: 0;
  color: #111111;
  font-size: 22px;
  font-weight: 800;
  margin-left: 6px;
}

.quick-amount-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.content-btn {
  height: 40px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background-color: #ffffff;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.content-btn:hover {
  background-color: #f3f4f6;
}

.error-msg-box {
  background-color: #ffebee;
  padding: 12px 16px;
  border-radius: 10px;
  margin-bottom: 20px;
}

.bottom-btn-area.single {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 24px 32px;
  background: #ffffff;
  z-index: 100;
  box-sizing: border-box;
}

.bottom-btn-area.single .bottom-btn {
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

.bottom-btn-area.single .bottom-btn:disabled {
  background-color: #e2e8f0;
  color: #a0aec0;
  cursor: not-allowed;
}
</style>
