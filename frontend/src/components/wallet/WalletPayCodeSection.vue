<template>
  <div class="wallet-pay-group-box">
    <!-- KakaoPay 스타일 메인 결제 카드 (바코드 + QR + 페이머니 잔액 + 연결 계좌) -->
    <div class="active-barcode-qr-card kakaopay-style-card">
      <!-- 1. 바코드 & QR 코드 영역 -->
      <div class="barcode-qr-dual-row">
        <div class="barcode-display-section" @click="$emit('openBarcode')">
          <div class="svg-barcode-box">
            <svg class="real-barcode-svg" viewBox="0 0 200 60" width="100%" height="48">
              <rect v-for="(b, idx) in barcodeLines" :key="idx" :x="b.x" y="0" :width="b.w" height="60" fill="#111" />
            </svg>
          </div>
          <span class="text-13-bold barcode-num-text">{{ dynamicBarcodeToken }}</span>
        </div>

        <div class="vertical-divider"></div>

        <div class="qr-display-section" @click="$emit('openQr')">
          <svg class="real-qr-svg" viewBox="0 0 108 108" width="72" height="72">
            <rect v-for="(m, idx) in qrModules" :key="idx" :x="m.x" :y="m.y" :width="m.w" :height="m.h" fill="#111" />
          </svg>
        </div>
      </div>

      <div class="kakaopay-card-divider"></div>

      <!-- 2. KakaoPay 스타일 페이머니 잔액 & 연결 충전계좌 -->
      <div class="kakaopay-account-info-section">
        <!-- 행 1: 페이머니 잔액 -->
        <div class="pay-money-row" @click="$router.push('/transactions')" style="cursor: pointer;">
          <span class="lbl-text text-14">페이머니</span>
          <div class="val-text text-16-bold balance-highlight">
            {{ formatCurrency(walletBalance) }}원
            <i class="fa-solid fa-chevron-right text-11 icon-sub"></i>
          </div>
        </div>

        <!-- 행 2: 연결 충전계좌 (클릭 시 /setting/accounts 로 이동) -->
        <div class="charge-account-row" @click="$router.push('/setting/accounts')" style="cursor: pointer;">
          <span class="lbl-text text-13">충전계좌</span>
          <div class="val-text text-13-bold acc-val-text">
            <span>{{ primaryAccount?.bankName || "KB국민" }} {{ maskAccount(primaryAccount?.accountNumber) }}</span>
            <i class="fa-solid fa-chevron-right text-11 icon-sub"></i>
          </div>
        </div>

        <!-- 행 3: 하단 전폭 충전 버튼 (지갑 충전 라벨 제거) -->
        <div class="card-charge-btn-wrap">
          <button
            type="button"
            class="card-charge-btn text-14-bold"
            @click="$emit('goToCharge')"
          >
            충전
          </button>
        </div>
      </div>
    </div>

    <!-- 1. 전면 세로형 바코드 확대 뷰 (Teleport to .app) -->
    <Teleport to=".app">
      <div v-if="fullScreenMode === 'BARCODE'" class="kakaopay-fullscreen-overlay" @click="$emit('closeFullScreen')">
        <button class="kakaopay-close-btn" @click.stop="$emit('closeFullScreen')">
          <i class="fa-solid fa-xmark text-24 text-white"></i>
        </button>

        <div class="kakaopay-barcode-layout" @click.stop="$emit('approveBarcode')">
          <div class="vertical-number-column text-18-bold text-white">
            {{ dynamicBarcodeToken }}
          </div>

          <div class="white-vertical-barcode-card">
            <svg class="kakaopay-real-barcode-svg" viewBox="0 0 200 120" preserveAspectRatio="none" width="100%" height="100%">
              <rect v-for="(b, idx) in barcodeLines" :key="idx" x="0" :y="b.x * 0.6" width="200" :height="b.w * 0.8" fill="#111" />
            </svg>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 2. 전면 큼직한 QR 확대 뷰 (Teleport to .app) -->
    <Teleport to=".app">
      <div v-if="fullScreenMode === 'QR'" class="kakaopay-fullscreen-overlay" @click="$emit('closeFullScreen')">
        <button class="kakaopay-close-btn" @click.stop="$emit('closeFullScreen')">
          <i class="fa-solid fa-xmark text-24 text-white"></i>
        </button>

        <div class="kakaopay-qr-layout-vertical" @click.stop="$emit('approveQr')">
          <div class="yellow-border-qr-card">
            <svg class="giant-qr-svg" viewBox="0 0 108 108" width="220" height="220">
              <rect v-for="(m, idx) in qrModules" :key="idx" :x="m.x" :y="m.y" :width="m.w" :height="m.h" fill="#111" />
            </svg>
          </div>

          <div class="qr-horizontal-number text-18-bold text-white">
            {{ dynamicBarcodeToken }}
          </div>
        </div>
      </div>
    </Teleport>
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
    default: "9283-7492-1049-9182",
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

const maskAccount = (acc) => {
  if (!acc) return "1111";
  const str = String(acc);
  if (str.length >= 4) {
    return str.slice(-4);
  }
  return str;
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.wallet-pay-group-box {
  width: 100%;
  margin: 16px 0 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: var(--color-bg-page, #ffffff);
  box-sizing: border-box;
}

.active-barcode-qr-card {
  background-color: var(--color-bg-page, #ffffff);
  border-radius: 20px;
  padding: 22px 20px;
  border: 1px solid var(--color-border-card, #e8e8e8);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.barcode-qr-dual-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.barcode-display-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.svg-barcode-box {
  width: 100%;
  height: 48px;
}

.barcode-num-text {
  color: var(--color-text-main, #111111);
  letter-spacing: 1px;
}

.vertical-divider {
  width: 1px;
  height: 50px;
  background-color: var(--color-border-main, #dddddd);
}

.qr-display-section {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 4px;
  border-radius: 10px;
  background-color: var(--color-bg-screen, #f5f6f8);
}

.kakaopay-card-divider {
  height: 1px;
  background-color: #edf2f7;
  margin: 2px 0;
}

.kakaopay-account-info-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
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
  gap: 10px;
}

.kakaopay-charge-btn {
  background-color: #ffbc2e;
  border: none;
  border-radius: 8px;
  padding: 5px 12px;
  color: #111111;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.kakaopay-charge-btn:hover {
  background-color: #e5a900;
}

.card-charge-btn-wrap {
  width: 100%;
  margin-top: 4px;
}

.card-charge-btn {
  width: 100%;
  height: 44px;
  background-color: #ffbc2e;
  border: none;
  border-radius: 12px;
  color: #111111;
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;
}

.card-charge-btn:active {
  background-color: #e5a900;
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

.icon-sub {
  color: #a0aec0;
}

.kakaopay-fullscreen-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #191919;
  z-index: 99999;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  padding: 24px;
  overflow: hidden;
  border-radius: 24px;
}

@media (max-width: 430px) {
  .kakaopay-fullscreen-overlay {
    border-radius: 0;
  }
}

.kakaopay-close-btn {
  position: absolute;
  top: 24px;
  right: 24px;
  background: transparent;
  border: none;
  cursor: pointer;
  z-index: 10;
}

.kakaopay-barcode-layout {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 28px;
  width: 100%;
}

.vertical-number-column {
  writing-mode: vertical-rl;
  transform: rotate(180deg);
  letter-spacing: 5px;
  font-size: 17px;
  font-weight: 700;
}

.white-vertical-barcode-card {
  background: #ffffff;
  border-radius: 28px;
  padding: 24px 28px;
  height: 66%;
  width: 215px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.6);
}

.kakaopay-qr-layout-vertical {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  width: 100%;
}

.yellow-border-qr-card {
  background: #ffffff;
  border: 5px solid #ffbc2e;
  border-radius: 24px;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.qr-horizontal-number {
  letter-spacing: 3px;
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
}
</style>
