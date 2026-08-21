<template>
  <div class="wallet-pay-group-box">
    <!-- KakaoPay 스타일 메인 결제 카드 (바코드 + QR + 페이머니 잔액 + 연결 계좌) -->
    <div class="active-barcode-qr-card kakaopay-style-card">
      <!-- 1. 바코드 & QR 코드 영역 (전폭 카드 규격에 맞춰 큼직하게) -->
      <div class="barcode-qr-dual-row">
        <div class="barcode-display-section">
          <div class="svg-barcode-box">
            <svg class="real-barcode-svg" viewBox="0 0 200 60" width="100%" height="72">
              <rect v-for="(b, idx) in barcodeLines" :key="idx" :x="b.x" y="0" :width="b.w" height="60" fill="#111" />
            </svg>
          </div>
          <span class="text-15-bold barcode-num-text">{{ dynamicBarcodeToken }}</span>
        </div>

        <div class="vertical-divider"></div>

        <div class="qr-display-section">
          <svg class="real-qr-svg" viewBox="0 0 108 108" width="96" height="96">
            <rect v-for="(m, idx) in qrModules" :key="idx" :x="m.x" :y="m.y" :width="m.w" :height="m.h" fill="#111" />
          </svg>
        </div>
      </div>

      <div class="kakaopay-card-divider"></div>

      <!-- 2. KakaoPay 스타일 페이머니 잔액 & 연결 충전계좌 -->
      <div class="kakaopay-account-info-section">
        <!-- 행 1: 페이머니 잔액 & 깔끔한 인라인 [충전] 심플 버튼 -->
        <div class="pay-money-row">
          <span class="lbl-text text-15">페이머니</span>
          <div class="pay-money-right-group">
            <span class="val-text text-18-bold balance-highlight">
              {{ formatCurrency(walletBalance) }}원
            </span>
            <button
              type="button"
              class="btn-charge-simple text-14-bold"
              @click.stop="$emit('goToCharge')"
            >
              충전
            </button>
          </div>
        </div>

        <!-- 행 2: 연결 충전계좌 (클릭 시 /setting/accounts 로 이동) -->
        <div class="charge-account-row" @click="$router.push('/setting/accounts')" style="cursor: pointer;">
          <span class="lbl-text text-14">충전계좌</span>
          <div class="val-text text-14-bold acc-val-text">
            <span>{{ primaryAccount?.bankName || "KB국민" }} {{ maskAccount(primaryAccount?.accountNumber) }}</span>
            <span class="tx-arr text-13">&gt;</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  walletBalance: {
    type: Number,
    default: 0,
  },
  primaryAccount: {
    type: Object,
    default: () => ({}),
  },
  barcodeLines: {
    type: Array,
    default: () => [],
  },
  qrModules: {
    type: Array,
    default: () => [],
  },
  dynamicBarcodeToken: {
    type: String,
    default: "0000-0000-0000-0000",
  },
  fullScreenMode: {
    type: String,
    default: null,
  },
  formatCurrency: {
    type: Function,
    required: true,
  },
});

defineEmits([
  "openBarcode",
  "openQr",
  "closeFullScreen",
  "approveBarcode",
  "approveQr",
  "goToCharge",
]);

const maskAccount = (num) => {
  if (!num) return "(1111)";
  const str = String(num).replace(/-/g, "");
  if (str.length > 8) {
    return `${str.slice(0, 3)}-***-${str.slice(-4)}`;
  }
  return `(${str.slice(-4)})`;
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.wallet-pay-group-box {
  width: 100%;
  min-height: 310px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.kakaopay-style-card {
  width: 100%;
  min-height: 310px;
  background-color: #ffffff;
  border-radius: 22px;
  border: 1px solid #e2e8f0;
  padding: 26px 22px;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.barcode-qr-dual-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.barcode-display-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.svg-barcode-box {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.barcode-num-text {
  color: var(--color-text-main, #111111);
  letter-spacing: 1.5px;
  margin-top: 6px;
}

.vertical-divider {
  width: 1px;
  height: 76px;
  background-color: var(--color-border-main, #dddddd);
}

.qr-display-section {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 14px;
  background-color: var(--color-bg-screen, #f8fafc);
}

.kakaopay-card-divider {
  height: 1px;
  background-color: #edf2f7;
  margin: 22px 0 20px;
}

.kakaopay-account-info-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pay-money-row,
.charge-account-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pay-money-row .lbl-text {
  color: #4a5568;
  font-weight: 600;
}

.pay-money-right-group {
  display: flex;
  align-items: center;
}

.balance-highlight {
  color: #0f172a;
}

/* 깔끔하고 심플한 인라인 [충전] 버튼 (+ 기호 없이 깔끔하게!) */
.btn-charge-simple {
  background-color: var(--color-primary, #ffbc2e);
  border: none;
  border-radius: 8px;
  padding: 5px 13px;
  color: #111111;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.18s cubic-bezier(0.4, 0, 0.2, 1);
  margin-left: 10px;
  box-shadow: 0 2px 6px rgba(255, 188, 46, 0.25);
}

.btn-charge-simple:active {
  transform: scale(0.96);
  opacity: 0.9;
}

.charge-account-row .lbl-text {
  color: #718096;
}

.charge-account-row .acc-val-text {
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 4px;
}

.icon-sub,
.tx-arr {
  color: #a0aec0;
  font-family: monospace, sans-serif;
  margin-left: 2px;
}

/* 안3 적용: 바코드 카드 아래 외부 공간 최근 지갑 내역 퀵 뷰 */
.recent-wallet-activity-box {
  width: 100%;
  padding: 16px 18px;
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  box-sizing: border-box;
}

.activity-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 12px;
}

.link-text {
  cursor: pointer;
  color: var(--color-text-sub, #64748b);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-item-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.activity-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 6px;
}

.activity-badge.charge {
  background-color: #eff6ff;
  color: #2563eb;
}

.activity-badge.pay {
  background-color: #fef2f2;
  color: #dc2626;
}

.amount-plus {
  color: #2563eb;
}

.amount-minus {
  color: #0f172a;
}

/* 풀스크린 바코드 & QR 오버레이 */
.kakaopay-fullscreen-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 99999;
  background: rgba(17, 17, 17, 0.92);
  backdrop-filter: blur(6px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
}

@media (max-width: 430px) {
  .kakaopay-fullscreen-overlay {
    border-radius: 0;
  }
}

.kakaopay-close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: none;
  border: none;
  cursor: pointer;
}

.kakaopay-barcode-layout {
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
}

.vertical-number-column {
  writing-mode: vertical-rl;
  letter-spacing: 3px;
}

.white-vertical-barcode-card {
  width: 200px;
  height: 380px;
  background-color: #ffffff;
  border-radius: 18px;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  box-sizing: border-box;
}

.kakaopay-real-barcode-svg {
  transform: rotate(90deg);
}

.kakaopay-qr-layout-vertical {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  cursor: pointer;
}

.yellow-border-qr-card {
  background-color: #ffffff;
  padding: 20px;
  border-radius: 20px;
  border: 3px solid #ffbc2e;
  box-shadow: 0 10px 30px rgba(255, 188, 46, 0.3);
}

.qr-horizontal-number {
  letter-spacing: 2px;
}
</style>
