<template>
  <div class="fintech-wallet-root">
    <!-- 1. 메인 결제 화면 -->
    <template v-if="currentView === 'MAIN'">
      <PageHeader title="결제 서비스" :show-back="false" />

      <div class="fintech-body">
        <div class="payment-unified-group">
          <!-- 상단 탭바 (Option 1: 슬라이딩 필 인디케이터 스위치) -->
          <div class="mode-control-row">
            <div class="mode-tab-bar" style="width: 100%;">
              <div class="tab-pill-slider" :class="{ right: isWalletModeActive }"></div>

              <button
                class="tab-item text-15-bold"
                :class="{ active: !isWalletModeActive }"
                @click="switchWalletMode(false)"
              >
                <i class="fa-solid fa-credit-card icon-left"></i> 카드 결제
              </button>
              <button
                class="tab-item text-15-bold"
                :class="{ active: isWalletModeActive }"
                @click="switchWalletMode(true)"
              >
                <i class="fa-solid fa-qrcode icon-left"></i> 지갑 결제
              </button>
            </div>
          </div>

          <!-- A. 카드 결제 모드 ↔ B. 지갑 결제 모드 부드러운 전환 트랜지션 (WalletPage 전용 스코프) -->
          <transition name="mode-slide-fade" mode="out-in">
            <WalletCardSliderSection
              v-if="!isWalletModeActive"
              key="card-mode"
              :has-representative-card="hasRepresentativeCard"
              :registered-cards="registeredCards"
              :current-card-idx="currentCardIdx"
              :get-card3-d-style="getCard3DStyle"
              :get-card-img="getCardImg"
              :format-masked-card-num="formatMaskedCardNum"
              @touch-start="handleTouchStart"
              @touch-move="handleTouchMove"
              @touch-end="handleTouchEnd"
              @card-click="onCardClick"
              @add-card-click="onAddCardClick"
              @select-dot="selectDotCard"
            />

            <WalletPayCodeSection
              v-else
              key="wallet-mode"
              :wallet-balance="walletBalance"
              :primary-account="primaryAccount"
              :barcode-lines="barcodeLines"
              :qr-modules="qrModules"
              :dynamic-barcode-token="dynamicBarcodeToken"
              :full-screen-mode="fullScreenMode"
              :format-currency="formatCurrency"
              @open-barcode="openBarcodeFullScreen"
              @open-qr="openQrFullScreen"
              @close-full-screen="closeFullScreen"
              @approve-barcode="approveBarcodePayment"
              @approve-qr="approveQrPayment"
              @go-to-charge="goToChargeView"
            />
          </transition>
        </div>
      </div>
    </template>

    <!-- 2. 지갑 충전 화면 (PIN 인증 시 풀페이지 적용) -->
    <template v-else-if="currentView === 'CHARGE'">
      <!-- 충전 전용 풀페이지 PIN 인증 단독 화면 (모달 팝업 아님! 100% PinLoginPage 규격) -->
      <div v-if="isChargePinPage" class="charge-pin-full-page">
        <!-- 헤더 -->
        <div class="pin-page-header">
          <button type="button" class="back-btn" @click="cancelChargePin">
            <i class="fa-solid fa-chevron-left"></i>
          </button>
          <h3 class="text-18-bold header-title">간편비밀번호 인증</h3>
          <div class="header-right-empty"></div>
        </div>

        <!-- 본문 -->
        <div class="pin-page-body text-center">
          <!-- 비주얼 (PinLoginPage 100% 동일) -->
          <div class="login-visual mt-3">
            <div class="visual-glow"></div>
            <div class="visual-icon">
              <i class="fa-solid fa-lock"></i>
            </div>
            <span class="visual-dot dot-one"></span>
            <span class="visual-dot dot-two"></span>
            <span class="visual-dot dot-three"></span>
          </div>

          <!-- 타이틀 -->
          <div class="login-header mt-3">
            <h2 class="text-24-bold m-0">간편비밀번호 입력</h2>
            <p class="text-14 text-sub mt-2 mb-0">
              충전 금액 <strong class="text-main text-16-bold">{{ formatCurrency(chargeAmount) }}원</strong> 승인을 위해<br/>PIN 6자리를 입력해주세요.
            </p>
          </div>

          <!-- 6자리 PIN Box (보안 카드 박스 100% 삭제) -->
          <div class="pin-boxes mt-4">
            <div
              v-for="index in 6"
              :key="index"
              class="pin-box"
              :class="{
                filled: inputPinCode.length >= index,
                active: inputPinCode.length === index - 1
              }"
            >
              <span v-if="inputPinCode.length >= index" class="pin-dot"></span>
            </div>
          </div>
        </div>

        <!-- 하단 키패드 -->
        <div class="pin-keypad-bottom">
          <div class="keypad-row">
            <button v-for="n in [1, 2, 3]" :key="n" type="button" class="pin-num-btn text-20-bold" @click="enterPin(n)">{{ n }}</button>
          </div>
          <div class="keypad-row">
            <button v-for="n in [4, 5, 6]" :key="n" type="button" class="pin-num-btn text-20-bold" @click="enterPin(n)">{{ n }}</button>
          </div>
          <div class="keypad-row">
            <button v-for="n in [7, 8, 9]" :key="n" type="button" class="pin-num-btn text-20-bold" @click="enterPin(n)">{{ n }}</button>
          </div>
          <div class="keypad-row">
            <button type="button" class="pin-num-btn action-text-btn text-14-bold" @click="inputPinCode = ''">C</button>
            <button type="button" class="pin-num-btn text-20-bold" @click="enterPin(0)">0</button>
            <button type="button" class="pin-num-btn del-icon-btn text-16" @click="inputPinCode = inputPinCode.slice(0, -1)"><i class="fa-solid fa-delete-left"></i></button>
          </div>
        </div>
      </div>

      <!-- 충전 입력 양식 / 성공 화면 -->
      <WalletChargeSection
        v-else
        :charge-success="chargeSuccess"
        :last-charged-amount="lastChargedAmount"
        :primary-account="primaryAccount"
        :account-balance="accountBalance"
        :charge-amount="chargeAmount"
        :charge-amount-display="chargeAmountDisplay"
        :charge-error="chargeError"
        :charge-loading="chargeLoading"
        :format-currency="formatCurrency"
        :get-bank-logo-file-name="getBankLogoFileName"
        @back-to-main="currentView = 'MAIN'"
        @on-amount-input="onChargeAmountInput"
        @add-amount="addChargeAmount"
        @submit="submitWalletCharge"
      />
    </template>

    <!-- 3. NFC 결제 애니메이션 오버레이 (Teleport to body 적용) -->
    <WalletNfcPaymentOverlay
      :is-nfc-active="isNfcActive"
      :formatted-timer="formattedNfcTimer"
      :card-img="getCardImg(registeredCards[currentCardIdx] || registeredCards[0])"
      :card="registeredCards[currentCardIdx] || registeredCards[0] || {}"
      @cancel="stopNfcPayment"
    />

    <!-- 4. PIN 간편 비밀번호 인증 모달 (Teleport to body 적용) -->
    <WalletPinAuthModal
      :show="showPinAuthModal"
      :input-pin-code="inputPinCode"
      :error-message="pinErrorMessage"
      :pin-locked="pinLocked"
      @close="closePinModal"
      @enter-pin="enterPin"
      @clear-pin="clearPinCode"
      @delete-pin="deletePinCode"
      @forgot-pin="goPinReset"
    />

    <!-- 5. 모바일 푸시 알림 배너 (Teleport to body 적용) -->
    <WalletPushNotificationBanner :notification="pushNotification" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import api from "@/api";
import { getCards, requestCardPayment, cancelCardPayment, getCardTransactionStatus, approveCardPayment } from "@/api/cardApi";
import walletApi from "@/api/walletApi";
import { useAuthStore } from "@/stores/auth";
import { useSignupStore } from "@/stores/signup";

import PageHeader from "@/components/common/PageHeader.vue";
import WalletCardSliderSection from "@/components/wallet/WalletCardSliderSection.vue";
import WalletPayCodeSection from "@/components/wallet/WalletPayCodeSection.vue";
import WalletChargeSection from "@/components/wallet/WalletChargeSection.vue";
import WalletNfcPaymentOverlay from "@/components/wallet/WalletNfcPaymentOverlay.vue";
import WalletPinAuthModal from "@/components/wallet/WalletPinAuthModal.vue";
import WalletPushNotificationBanner from "@/components/wallet/WalletPushNotificationBanner.vue";

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const signupStore = useSignupStore();

const pinErrorMessage = ref("");
const pinLocked = ref(false);

const currentView = ref("MAIN");

watch(currentView, (newVal) => {
  if (newVal === "CHARGE") {
    route.meta.hideBottomNav = true;
  } else {
    route.meta.hideBottomNav = false;
  }
}, { immediate: true });

onUnmounted(() => {
  route.meta.hideBottomNav = false;
});

const isWalletModeActive = ref(false);
const walletBalance = ref(0);

// 모바일 푸시 알림 상태
const pushNotification = ref({
  visible: false,
  title: "",
  message: "",
  icon: "fa-solid fa-wallet",
  time: "방금 전",
});

const triggerMobilePush = (title, message, iconClass = "fa-solid fa-wallet") => {
  pushNotification.value = {
    visible: true,
    title,
    message,
    icon: iconClass,
    time: "방금 전",
  };
  setTimeout(() => {
    pushNotification.value.visible = false;
  }, 3800);
};

// 전면 결제 스캔 뷰 상태 ('BARCODE' | 'QR' | null)
const fullScreenMode = ref(null);

const openBarcodeFullScreen = () => { fullScreenMode.value = "BARCODE"; };
const openQrFullScreen = () => { fullScreenMode.value = "QR"; };
const closeFullScreen = () => { fullScreenMode.value = null; };

const cardPaymentApi = { approveWalletTransaction: approveCardPayment };

// 1. 바코드 결제 승인
const approveBarcodePayment = async () => {
  try {
    const uId = Number(authStore.userId);
    if (!uId) {
      modalStore.showAlert('로그인이 필요합니다.', '결제 안내');
      return;
    }
    const payAmount = 15000;
    const res = await cardPaymentApi.approveWalletTransaction({
      userId: uId,
      amount: payAmount,
      merchantName: "CU 편의점 (바코드 결제)",
    });

    if (res && res.status === "FAILED") {
      closeFullScreen();
      triggerMobilePush(
        "결제 실패",
        res.message || "전자지갑 잔액이 부족합니다.",
        "fa-solid fa-triangle-exclamation"
      );
      return;
    }

    if (res && res.updatedWalletBalance !== undefined && res.updatedWalletBalance !== null) {
      walletBalance.value = res.updatedWalletBalance;
    } else {
      walletBalance.value = Math.max(0, walletBalance.value - payAmount);
    }

    closeFullScreen();
    triggerMobilePush(
      "바코드 결제 승인 완료",
      `${formatCurrency(payAmount)}원이 바코드 결제로 성공적으로 처리되었습니다. (잔액: ${formatCurrency(walletBalance.value)}원)`,
      "fa-solid fa-barcode"
    );
  } catch (err) {
    console.error("바코드 결제 오류:", err);
    closeFullScreen();
    triggerMobilePush(
      "결제 처리 실패",
      err.response?.data?.message || err.message || "바코드 결제 처리 중 오류가 발생했습니다.",
      "fa-solid fa-triangle-exclamation"
    );
  }
};

// 2. QR 결제 승인
const approveQrPayment = async () => {
  try {
    const uId = Number(authStore.userId);
    if (!uId) {
      modalStore.showAlert('로그인이 필요합니다.', '결제 안내');
      return;
    }
    const payAmount = 25000;
    const res = await cardPaymentApi.approveWalletTransaction({
      userId: uId,
      amount: payAmount,
      merchantName: "스타벅스 (QR 결제)",
    });

    if (res && res.status === "FAILED") {
      closeFullScreen();
      triggerMobilePush(
        "결제 실패",
        res.message || "전자지갑 잔액이 부족합니다.",
        "fa-solid fa-triangle-exclamation"
      );
      return;
    }

    if (res && res.updatedWalletBalance !== undefined && res.updatedWalletBalance !== null) {
      walletBalance.value = res.updatedWalletBalance;
    } else {
      walletBalance.value = Math.max(0, walletBalance.value - payAmount);
    }

    closeFullScreen();
    triggerMobilePush(
      "QR 결제 승인 완료",
      `${formatCurrency(payAmount)}원이 QR 결제로 성공적으로 처리되었습니다. (잔액: ${formatCurrency(walletBalance.value)}원)`,
      "fa-solid fa-qrcode"
    );
  } catch (err) {
    console.error("QR 결제 오류:", err);
    closeFullScreen();
    triggerMobilePush(
      "결제 처리 실패",
      err.response?.data?.message || err.message || "QR 결제 처리 중 오류가 발생했습니다.",
      "fa-solid fa-triangle-exclamation"
    );
  }
};

const isNfcActive = ref(false);
const pinTarget = ref("CARD");
const nfcTimerSeconds = ref(50);
let nfcTimerInterval = null;

const isPaymentInProgress = computed(() => isNfcActive.value);

const checkPaymentInProgressAndWarn = () => {
  if (isPaymentInProgress.value) {
    triggerMobilePush(
      "결제 진행 중",
      "결제가 진행 중입니다. 결제를 취소하거나 완료한 후 전환해주세요.",
      "fa-solid fa-triangle-exclamation"
    );
    return true;
  }
  return false;
};

const walletPayPendingTxId = ref(null);

const startWalletPaySession = async () => {
  stopWalletPaySession();
  try {
    const res = await requestCardPayment(1);
    if (res && res.cardTransactionId) {
      walletPayPendingTxId.value = res.cardTransactionId;
    }
  } catch (e) {
    console.error("지갑 QR/바코드 결제 대기 세션 생성 실패:", e);
  }
};

const stopWalletPaySession = async () => {
  try {
    const uId = authStore.userId || 1;
    walletApi.expirePaymentToken(uId).catch(() => {});
  } catch (e) {}

  if (walletPayPendingTxId.value) {
    const txId = walletPayPendingTxId.value;
    walletPayPendingTxId.value = null;
    try {
      await cancelCardPayment(txId);
    } catch (e) {
      console.warn("지갑 결제 대기 세션 취소 실패:", e);
    }
  }
};

const toggleStartMode = () => {
  if (checkPaymentInProgressAndWarn()) return;
  isWalletModeActive.value = !isWalletModeActive.value;
  const newMode = isWalletModeActive.value ? "WALLET" : "CARD";
  localStorage.setItem("user_default_pay_mode", newMode);
  if (isWalletModeActive.value) {
    startWalletPaySession();
  } else {
    stopWalletPaySession();
  }
};

const switchWalletMode = (mode) => {
  if (checkPaymentInProgressAndWarn()) return;
  isWalletModeActive.value = mode;
  localStorage.setItem("user_default_pay_mode", mode ? "WALLET" : "CARD");
  if (mode) {
    startWalletPaySession();
  } else {
    stopWalletPaySession();
  }
};

const registeredCards = ref([]);
const currentCardIdx = ref(0);

const getCard3DStyle = (index) => {
  const diff = index - currentCardIdx.value;
  if (diff === 0) {
    return {
      transform: "translateX(0) scale(1) rotate(0deg)",
      zIndex: 10,
      opacity: 1,
      filter: "none",
      pointerEvents: "auto",
    };
  } else if (diff === -1) {
    return {
      transform: "translateX(-36%) scale(0.85) rotate(-3.5deg)",
      zIndex: 5,
      opacity: 0.72,
      filter: "brightness(0.92)",
      pointerEvents: "auto",
    };
  } else if (diff === 1) {
    return {
      transform: "translateX(36%) scale(0.85) rotate(3.5deg)",
      zIndex: 5,
      opacity: 0.72,
      filter: "brightness(0.92)",
      pointerEvents: "auto",
    };
  } else if (diff < -1) {
    return {
      transform: "translateX(-85%) scale(0.7)",
      zIndex: 1,
      opacity: 0,
      pointerEvents: "none",
    };
  } else {
    return {
      transform: "translateX(85%) scale(0.7)",
      zIndex: 1,
      opacity: 0,
      pointerEvents: "none",
    };
  }
};

const touchStartX = ref(0);
const touchEndX = ref(0);

const handleTouchStart = (e) => {
  if (checkPaymentInProgressAndWarn()) return;
  touchStartX.value = e.touches[0].clientX;
};

const handleTouchMove = (e) => {
  touchEndX.value = e.touches[0].clientX;
};

const handleTouchEnd = () => {
  if (!touchStartX.value || !touchEndX.value) return;
  const distance = touchStartX.value - touchEndX.value;
  if (Math.abs(distance) > 40) {
    if (distance > 0 && currentCardIdx.value < registeredCards.value.length) {
      currentCardIdx.value++;
    } else if (distance < 0 && currentCardIdx.value > 0) {
      currentCardIdx.value--;
    }
  }
  touchStartX.value = 0;
  touchEndX.value = 0;
};

const selectDotCard = (idx) => {
  if (checkPaymentInProgressAndWarn()) return;
  currentCardIdx.value = idx;
};

const onCardClick = (index) => {
  if (checkPaymentInProgressAndWarn()) return;
  if (!registeredCards.value || registeredCards.value.length === 0) {
    router.push("/wallet/card/add");
    return;
  }
  if (currentCardIdx.value === index) {
    pinTarget.value = "CARD";
    openPinModal();
  } else {
    currentCardIdx.value = index;
  }
};

const onAddCardClick = () => {
  if (checkPaymentInProgressAndWarn()) return;
  if (currentCardIdx.value === registeredCards.value.length) {
    router.push("/wallet/card/add");
  } else {
    currentCardIdx.value = registeredCards.value.length;
  }
};

const formattedNfcTimer = computed(() => {
  const m = Math.floor(nfcTimerSeconds.value / 60);
  const s = nfcTimerSeconds.value % 60;
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
});

const currentPendingTxId = ref(null);

const startNfcTimer = async () => {
  stopNfcPayment();

  if (!registeredCards.value || registeredCards.value.length === 0) {
    triggerMobilePush(
      "결제 불가",
      "등록된 카드가 없습니다. 카드를 먼저 등록해주세요.",
      "fa-solid fa-credit-card"
    );
    router.push("/wallet/card/add");
    return;
  }

  const selectedCard = registeredCards.value[currentCardIdx.value] || registeredCards.value[0];
  if (!selectedCard) {
    triggerMobilePush(
      "결제 불가",
      "결제할 카드가 지정되지 않았습니다.",
      "fa-solid fa-credit-card"
    );
    return;
  }

  const linkedCardId = selectedCard.cardId || selectedCard.linkedCardId;
  if (!linkedCardId) {
    triggerMobilePush(
      "결제 불가",
      "카드 고유번호(ID)를 찾을 수 없습니다.",
      "fa-solid fa-credit-card"
    );
    return;
  }

  nfcTimerSeconds.value = 50;
  isNfcActive.value = true;

  try {
    const res = await requestCardPayment(linkedCardId);
    if (res && res.cardTransactionId) {
      currentPendingTxId.value = res.cardTransactionId;
    }
  } catch (e) {
    console.error("결제 대기 DB 등록 예외:", e);
  }

  nfcTimerInterval = setInterval(async () => {
    if (nfcTimerSeconds.value > 0) {
      nfcTimerSeconds.value--;
      if (currentPendingTxId.value && nfcTimerSeconds.value % 2 === 0) {
        try {
          const statusRes = await getCardTransactionStatus(currentPendingTxId.value);
          if (statusRes && statusRes.status === "SUCCESS") {
            const merchant = statusRes.merchantName || "가맹점";
            const amt = statusRes.amount ? statusRes.amount.toLocaleString() : "10,000";
            triggerMobilePush(
              "카드 결제 승인 완료",
              `${merchant}에서 ${amt}원 결제가 성공적으로 완료되었습니다.`,
              "fa-solid fa-credit-card"
            );
            currentPendingTxId.value = null;
            stopNfcPayment();
            return;
          }
        } catch (pollErr) {
          console.warn("결제 상태 폴링 경고:", pollErr);
        }
      }
    } else {
      stopNfcPayment();
    }
  }, 1000);
};

const stopNfcPayment = async () => {
  isNfcActive.value = false;
  if (nfcTimerInterval) clearInterval(nfcTimerInterval);
  if (currentPendingTxId.value) {
    const txIdToCancel = currentPendingTxId.value;
    currentPendingTxId.value = null;
    try {
      await cancelCardPayment(txIdToCancel);
    } catch (e) {
      console.error("결제 취소 DB 업데이트 실패:", e);
    }
  }
};

const qrPayloadUrl = ref(
  "https://kbpay.scoula.org/pay?userId=1&token=KB_PAY_SECURE_INITIAL",
);

const qrModules = computed(() => {
  const size = 25;
  const grid = Array.from({ length: size }, () => Array(size).fill(false));
  const addFinderPattern = (startR, startC) => {
    for (let r = 0; r < 7; r++) {
      for (let c = 0; c < 7; c++) {
        if (
          r === 0 ||
          r === 6 ||
          c === 0 ||
          c === 6 ||
          (r >= 2 && r <= 4 && c >= 2 && c <= 4)
        ) {
          grid[startR + r][startC + c] = true;
        }
      }
    }
  };

  addFinderPattern(0, 0);
  addFinderPattern(0, 18);
  addFinderPattern(18, 0);

  for (let r = 14; r <= 18; r++) {
    for (let c = 14; c <= 18; c++) {
      if (
        r === 14 ||
        r === 18 ||
        c === 14 ||
        c === 18 ||
        (r === 16 && c === 16)
      ) {
        grid[r][c] = true;
      }
    }
  }

  for (let i = 8; i < 18; i++) {
    if (i % 2 === 0) {
      grid[6][i] = true;
      grid[i][6] = true;
    }
  }

  const str = qrPayloadUrl.value;
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }

  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      const isTopLeft = r < 9 && c < 9;
      const isTopRight = r < 9 && c >= 16;
      const isBottomLeft = r >= 16 && c < 9;
      const isAlignment = r >= 14 && r <= 18 && c >= 14 && c <= 18;
      const isTiming = r === 6 || c === 6;

      if (
        !isTopLeft &&
        !isTopRight &&
        !isBottomLeft &&
        !isAlignment &&
        !isTiming
      ) {
        const val = Math.abs(Math.sin(r * 25 + c + hash) * 10000);
        grid[r][c] = Math.floor(val) % 2 === 0;
      }
    }
  }

  const modules = [];
  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      if (grid[r][c]) {
        modules.push({ x: c * 4 + 4, y: r * 4 + 4, w: 4, h: 4 });
      }
    }
  }
  return modules;
});

const dynamicBarcodeToken = ref("9283-7492-1049-9182");

const barcodeLines = computed(() => {
  const pattern = [
    3, 2, 1, 4, 2, 5, 1, 2, 4, 1, 3, 2, 1, 4, 3, 1, 2, 5, 2, 1, 4, 2, 3, 1, 4,
    2, 1, 3, 2, 4, 1, 3, 2, 4, 1, 3,
  ];
  let currentX = 5;
  return pattern.map((w) => {
    const x = currentX;
    currentX += w + 3;
    return { x, w };
  });
});

const showPinAuthModal = ref(false);
const isChargePinPage = ref(false);
const inputPinCode = ref("");

const cancelChargePin = () => {
  isChargePinPage.value = false;
  inputPinCode.value = "";
  pinErrorMessage.value = "";
};

const openPinModal = () => {
  inputPinCode.value = "";
  pinErrorMessage.value = "";
  pinLocked.value = false;
  showPinAuthModal.value = true;
};

const closePinModal = () => {
  showPinAuthModal.value = false;
  inputPinCode.value = "";
  pinErrorMessage.value = "";
};

const clearPinCode = () => {
  inputPinCode.value = "";
  pinErrorMessage.value = "";
};

const deletePinCode = () => {
  inputPinCode.value = inputPinCode.value.slice(0, -1);
  pinErrorMessage.value = "";
};

const goPinReset = () => {
  showPinAuthModal.value = false;
  inputPinCode.value = "";
  pinErrorMessage.value = "";
  signupStore.setVerificationPurpose('PIN_RESET');
  router.push('/signup/check');
};

const enterPin = async (num) => {
  if (pinLocked.value) return;

  pinErrorMessage.value = "";
  if (inputPinCode.value.length < 6) {
    inputPinCode.value += String(num);
    if (inputPinCode.value.length === 6) {
      const enteredPin = inputPinCode.value;
      const uId = authStore.userId || 1;

      try {
        const verifyResult = await walletApi.verifyPin(uId, enteredPin);
        if (!verifyResult || !verifyResult.verified) {
          pinErrorMessage.value = verifyResult?.message || "간편비밀번호가 일치하지 않습니다.";
          inputPinCode.value = "";
          if (verifyResult?.pinLocked || pinErrorMessage.value.includes("초과") || pinErrorMessage.value.includes("잠겼습니다")) {
            pinLocked.value = true;
          }
          return;
        }
      } catch (err) {
        console.error("PIN 인증 실패:", err);
        const errData = err.response?.data;
        pinErrorMessage.value = (typeof errData === 'string' ? errData : errData?.message) || err.message || "간편비밀번호가 일치하지 않습니다.";
        inputPinCode.value = "";
        if (pinErrorMessage.value.includes("초과") || pinErrorMessage.value.includes("잠겼습니다")) {
          pinLocked.value = true;
        }
        return;
      }

      pinErrorMessage.value = "";
      showPinAuthModal.value = false;
      isChargePinPage.value = false;
      inputPinCode.value = "";

      if (pinTarget.value === "CARD") {
        startNfcTimer();
      } else if (pinTarget.value === "CHARGE") {
        await executeWalletCharge();
      }
    }
  }
};

const chargeAmount = ref(0);
const chargeLoading = ref(false);
const chargeError = ref("");
const chargeSuccess = ref(false);
const lastChargedAmount = ref(0);
const accountBalance = ref(0);
const primaryAccount = ref({
  accountNumber: "",
  bankName: "KB국민",
  bankCode: "004",
});

const chargeAmountDisplay = computed(() => {
  if (!chargeAmount.value) return "";
  return Number(chargeAmount.value).toLocaleString("ko-KR");
});

const onChargeAmountInput = (e) => {
  const raw = e.target.value.replace(/[^0-9]/g, "");
  chargeAmount.value = raw ? parseInt(raw, 10) : 0;
};

const addChargeAmount = (amt) => {
  chargeAmount.value += amt;
};

const goToChargeView = () => {
  if (checkPaymentInProgressAndWarn()) return;
  chargeAmount.value = 0;
  chargeError.value = "";
  chargeSuccess.value = false;
  currentView.value = "CHARGE";
};

const getBankLogoFileName = (bank) => {
  if (!bank) return "kb.png";
  const str = String(bank);
  if (str.includes("신한")) return "shinhan.png";
  if (str.includes("KB") || str.includes("국민")) return "kb.png";
  if (str.includes("우리")) return "woori.png";
  if (str.includes("하나")) return "hana.png";
  if (str.includes("기업") || str.includes("IBK")) return "ibk.png";
  if (str.includes("농협") || str.includes("NH")) return "nh.png";
  if (str.includes("카카오")) return "kakaobank.png";
  if (str.includes("케이뱅크")) return "kbank.png";
  if (str.includes("토스")) return "tossbank.png";
  if (str.includes("제일") || str.includes("SC")) return "sc.png";
  return "kb.png";
};

const submitWalletCharge = () => {
  if (chargeAmount.value <= 0) return;
  chargeError.value = "";
  const amtToCharge = Number(chargeAmount.value);

  if (amtToCharge > 2000000) {
    chargeError.value = "1회 최대 충전 가능 금액은 2,000,000원입니다.";
    return;
  }

  if (amtToCharge > accountBalance.value) {
    chargeError.value = `출금 계좌 잔액이 부족합니다. (출금 가능 계좌 잔액: ${formatCurrency(accountBalance.value)} 원)`;
    return;
  }

  pinTarget.value = "CHARGE";
  inputPinCode.value = "";
  isChargePinPage.value = true;
};

const executeWalletCharge = async () => {
  const amtToCharge = Number(chargeAmount.value);
  chargeLoading.value = true;

  try {
    const uId = Number(authStore.userId);
    if (!uId) {
      modalStore.showAlert('로그인이 필요합니다.', '충전 안내');
      return;
    }
    const { data: chargeResult } = await api.post("/api/wallets/charges", {
      userId: uId,
      walletId: uId,
      amount: amtToCharge,
      bankCode: primaryAccount.value?.bankCode || "004",
      accountNumber: primaryAccount.value?.accountNumber || "",
      memo: "전자지갑 계좌 충전",
    });

    accountBalance.value = Math.max(0, accountBalance.value - amtToCharge);

    if (chargeResult && chargeResult.updatedBalance !== undefined) {
      walletBalance.value = chargeResult.updatedBalance;
    } else {
      const wInfo = await walletApi.getWalletByUserId(uId);
      if (wInfo && wInfo.balance !== undefined) {
        walletBalance.value = wInfo.balance;
      } else {
        walletBalance.value += amtToCharge;
      }
    }

    lastChargedAmount.value = amtToCharge;
    chargeSuccess.value = true;
  } catch (err) {
    console.error("지갑 충전 오류:", err);
    chargeError.value = "지갑 충전 처리 중 오류가 발생했습니다.";
  } finally {
    chargeLoading.value = false;
  }
};

const kbCardImageMap = {
  "KB Pay 노리2 체크카드 (KB국민카드)": "/images/cards/nori2.png",
  "KB국민 톡톡MyPoint 카드": "/images/cards/toktok.png",
  "KB국민 굿데이 ALL 카드": "/images/cards/goodday.png",
  "KB국민 청춘대로 톡톡카드": "/images/cards/chungchun.png",
  "KB국민 My WEISH 카드": "/images/cards/weish.png",
  "KB국민 Easy Link 카드": "/images/cards/easylink.png",
};

const getCardImg = (card) => {
  if (!card) return null;
  if (card.cardImageUrl) return card.cardImageUrl;
  if (card.cardImage) return card.cardImage;
  if (card.imageUrl) return card.imageUrl;
  if (card.cardName && kbCardImageMap[card.cardName])
    return kbCardImageMap[card.cardName];
  if (card.cardName) {
    for (const [name, img] of Object.entries(kbCardImageMap)) {
      if (card.cardName.includes(name) || name.includes(card.cardName))
        return img;
    }
  }
  return null;
};

const formatMaskedCardNum = (num) => {
  if (!num) return "•••• •••• •••• 9182";
  const clean = num.replace(/\D/g, "");
  if (clean.length >= 4) {
    const last4 = clean.slice(-4);
    return `•••• •••• •••• ${last4}`;
  }
  return num;
};

const hasRepresentativeCard = computed(() => registeredCards.value.length > 0);

const formatCurrency = (val) => new Intl.NumberFormat("ko-KR").format(val || 0);

const loadData = async () => {
  try {
    const userId = Number(authStore.userId);
    if (!userId) return;
    const cardsData = await getCards(userId);
    if (cardsData && Array.isArray(cardsData)) {
      registeredCards.value = cardsData;
    }

    const savedPayMode = localStorage.getItem("user_default_pay_mode");
    if (savedPayMode === "WALLET") {
      isWalletModeActive.value = true;
    } else if (savedPayMode === "CARD") {
      isWalletModeActive.value = false;
    }

    try {
      const wInfo = await walletApi.getWalletByUserId(userId);
      if (wInfo) {
        walletBalance.value = wInfo.balance ?? wInfo.amount ?? wInfo.pointMoney ?? 0;
      }
    } catch (wErr) {
      console.log("지갑 조회 예외:", wErr);
    }

    try {
      const { data: accList } = await api.get('/api/users/accounts');
      if (accList && Array.isArray(accList) && accList.length > 0) {
        const primaryAcc =
          accList.find(
            (a) =>
              a.isPrimary === "Y" ||
              a.isPrimary === true ||
              a.primaryYn === "Y",
          ) || accList[0];
        if (primaryAcc) {
          primaryAccount.value = {
            accountNumber:
              primaryAcc.accountNumber || primaryAcc.accountNo || "",
            bankName: primaryAcc.bankName || "KB국민",
            bankCode: primaryAcc.bankCode || "004",
          };
          if (primaryAcc.balance !== undefined) {
            accountBalance.value = primaryAcc.balance;
          }
        }
      }
    } catch (accErr) {
      console.log("계좌 조회 예외:", accErr);
    }
  } catch (err) {
    console.log("데이터 로드 예외", err);
  }
};

const handleRealtimeNotification = async (event) => {
  const notif = event?.detail || {};
  const notifType = (notif.notificationType || notif.type || "").toUpperCase();

  // 사용자의 명확한 지침: 결제(CARD_PAYMENT, PAYMENT)를 제외하면 푸시 알림을 띄우지 않음
  if (notifType.includes("CARD_PAYMENT") || notifType.includes("PAYMENT")) {
    const pushTitle = "카드 결제 승인 완료";
    const pushIcon = "fa-solid fa-credit-card";
    let pushMsg = notif.message || "";

    if (!pushMsg && notif.targetId) {
      try {
        const { data: txInfo } = await api.get(`/api/cards/payments/transactions/${notif.targetId}`);
        if (txInfo && txInfo.merchantName) {
          pushMsg = `${txInfo.merchantName}에서 ${(txInfo.amount || 0).toLocaleString()}원 결제가 성공적으로 완료되었습니다.`;
        }
      } catch (txErr) {
        console.log("실시간 거래 정보 조회 예외:", txErr);
      }
    }
    if (!pushMsg) pushMsg = "결제가 성공적으로 승인되었습니다.";

    triggerMobilePush(
      pushTitle,
      pushMsg,
      pushIcon
    );
  }

  // 송금/정산/충전/결제 후 실시간 지갑 잔액 갱신
  try {
    const uId = Number(authStore.userId);
    if (uId) {
      const wInfo = await walletApi.getWalletByUserId(uId);
      if (wInfo) {
        walletBalance.value = wInfo.balance ?? wInfo.amount ?? wInfo.pointMoney ?? 0;
      }
    }
  } catch (err) {
    console.error("실시간 지갑 잔액 재조회 실패:", err);
  }
};

const handleLongPressPaymentEvent = () => {
  if (!registeredCards.value || registeredCards.value.length === 0) {
    triggerMobilePush(
      "결제 불가",
      "등록된 카드가 없습니다. 카드를 먼저 등록해주세요.",
      "fa-solid fa-credit-card"
    );
    router.push("/wallet/card/add");
    return;
  }
  startNfcTimer();
};

const checkViewModeFromQuery = () => {
  if (route.query?.view === "CHARGE" || route.query?.mode === "charge") {
    currentView.value = "CHARGE";
  } else {
    currentView.value = "MAIN";
  }
};

watch(
  () => route.query?.view,
  (newView) => {
    if (newView === "CHARGE") {
      currentView.value = "CHARGE";
    } else {
      currentView.value = "MAIN";
    }
  }
);

onMounted(async () => {
  checkViewModeFromQuery();
  await loadData();
  if (isWalletModeActive.value && currentView.value === 'MAIN') {
    startWalletPaySession();
  }
  window.addEventListener('notification-received', handleRealtimeNotification);
  window.addEventListener('TRIGGER_LONG_PRESS_PAYMENT', handleLongPressPaymentEvent);
});

onUnmounted(() => {
  stopNfcPayment();
  stopWalletPaySession();
  window.removeEventListener('notification-received', handleRealtimeNotification);
  window.removeEventListener('TRIGGER_LONG_PRESS_PAYMENT', handleLongPressPaymentEvent);
});
</script>

<style scoped>
.mode-slide-fade-enter-active,
.mode-slide-fade-leave-active {
  transition: opacity 0.22s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.22s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden !important;
}

.mode-slide-fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.mode-slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
input,
button,
select,
textarea {
  font-family: inherit;
}

.fintech-wallet-root {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue",
    Arial, sans-serif;
  color: var(--color-text-main, #111111);
  box-sizing: border-box;
  background-color: var(--color-bg-page, #ffffff);
  position: relative;
  overflow: hidden;
}

.fintech-wallet-root * {
  box-sizing: border-box;
}

.unified-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 14px 16px;
  background-color: var(--color-bg-page, #ffffff);
  border-bottom: none;
  flex-shrink: 0;
}

.header-main-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text-main, #111111);
  text-align: center;
}

.fintech-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 20px 24px;
  overflow: hidden !important;
  gap: 24px;
  width: 100%;
  box-sizing: border-box;
}

.payment-unified-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 28px;
  width: 100%;
  margin: auto 0;
  overflow: hidden !important;
}

.mode-control-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  flex-shrink: 0;
}

.mode-tab-bar {
  position: relative;
  display: flex;
  flex: 1;
  background-color: #f1f5f9;
  padding: 4px;
  border-radius: 16px;
  height: 50px;
  box-sizing: border-box;
}

.tab-pill-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
  transition: transform 0.28s cubic-bezier(0.16, 1, 0.3, 1);
  z-index: 1;
}

.tab-pill-slider.right {
  transform: translateX(100%);
}

.mode-tab-bar .tab-item {
  position: relative;
  z-index: 2;
  flex: 1;
  border: none;
  background: transparent;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #64748b;
  cursor: pointer;
  transition: color 0.2s ease;
  white-space: nowrap;
}

.mode-tab-bar .tab-item.active {
  color: #0f172a;
  background-color: transparent;
  box-shadow: none;
}

.start-toggle-icon-btn {
  width: 44px;
  height: 44px;
  border: 1px solid var(--color-border-card, #e8e8e8);
  background-color: var(--color-bg-page, #ffffff);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

/* 충전 전용 풀페이지 PIN 인증 단독 화면 (PinLoginPage 100% 동일 규격 - 모달 팝업 아님) */
.charge-pin-full-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background-color: #ffffff;
  box-sizing: border-box;
  padding: 16px;
  position: relative;
  overflow-y: auto;
}

.pin-page-header {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.pin-page-header .back-btn {
  background: none;
  border: none;
  font-size: 18px;
  color: #111111;
  cursor: pointer;
  padding: 4px;
}

.header-right-empty {
  width: 24px;
}

.pin-page-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 12px;
}

.login-visual {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto;
}

.visual-glow {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(255, 188, 46, 0.15);
  animation: pulse-glow 2s ease-in-out infinite;
}

.visual-icon {
  position: absolute;
  inset: 10px;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
  background: linear-gradient(135deg, #ffca52 0%, #ffbc2e 65%, #f3a711 100%);
  box-shadow: 0 8px 18px rgba(255, 188, 46, 0.3);
  color: #ffffff;
  font-size: 24px;
}

.visual-dot {
  position: absolute;
  z-index: 3;
  border-radius: 50%;
}

.dot-one { top: 2px; right: 6px; width: 7px; height: 7px; background: #8f81f5; }
.dot-two { bottom: 4px; left: 2px; width: 6px; height: 6px; background: #6fd0bd; }
.dot-three { right: 2px; bottom: 12px; width: 5px; height: 5px; background: #ff9aa7; }

.pin-boxes {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
  width: 100%;
  max-width: 320px;
}

.pin-box {
  display: flex;
  height: 52px;
  align-items: center;
  justify-content: center;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  background: #fafafa;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.pin-box.active {
  border-color: #ffbc2e;
  background: #fffaf0;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.18);
}

.pin-box.filled {
  border-color: #ffbc2e;
  background: #fff8e5;
}

.pin-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #111111;
}

.pin-keypad-bottom {
  margin-top: auto;
  padding-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  max-width: 340px;
  margin-left: auto;
  margin-right: auto;
}

.keypad-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.pin-num-btn {
  height: 52px;
  border: none;
  background-color: #f8f9fa;
  border-radius: 14px;
  color: #111111;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.pin-num-btn:hover {
  background-color: #edf2f7;
}

.action-text-btn { color: #d97706; }
.del-icon-btn { color: #718096; }

@keyframes pulse-glow {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}
</style>
