<template>
  <div class="wallet-pay-group-box">
    <div class="kakao-pay-header-row">
      <div class="pay-title-area">
        <span class="lightning-icon">⚡</span>
        <span class="text-15-bold">바로 결제</span>
      </div>
      <div
        class="balance-link-area"
        @click="$router.push('/transactions')"
      >
        <span class="text-13">전자지갑 잔액</span>
        <span class="text-15-bold balance-highlight"
          >{{ formatCurrency(walletBalance) }}원</span
        >
        <i class="fa-solid fa-chevron-right text-11"></i>
      </div>
    </div>

    <!-- 결제 코드 (좌측 바코드 / 우측 QR 각각 개별 터치 진입) -->
    <div class="active-barcode-qr-card">
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

      <div class="security-token-info-bar">
        <span class="text-13 text-muted">전자지갑 즉시 충전</span>
        <button class="charge-action-badge-btn text-13-bold" @click="$emit('goToCharge')">
          <i class="fa-solid fa-plus"></i> 충전하기
        </button>
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
</script>

<style scoped>
.wallet-pay-group-box {
  width: 100%;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background-color: var(--color-bg-page, #ffffff);
  box-sizing: border-box;
}

.kakao-pay-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
}

.pay-title-area {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 800;
  color: var(--color-text-main, #111111);
}

.lightning-icon {
  font-size: 16px;
}

.balance-link-area {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-sub, #777777);
  cursor: pointer;
}

.balance-highlight {
  color: var(--color-text-main, #111111);
}

.active-barcode-qr-card {
  background-color: var(--color-bg-page, #ffffff);
  border-radius: 14px;
  padding: 16px;
  border: 1px solid var(--color-border-card, #e8e8e8);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 14px;
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

.security-token-info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px dashed var(--color-border-main, #dddddd);
}

.charge-action-badge-btn {
  border: none;
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  padding: 4px 12px;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
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
