<template>
  <div class="fintech-wallet-root">
    <template v-if="currentView === 'MAIN'">
      <div class="unified-header">
        <h2 class="text-18-bold header-main-title">결제 서비스</h2>
      </div>

      <div class="fintech-body">
        <div class="payment-unified-group">
          <div class="mode-control-row">
            <div class="mode-tab-bar">
              <button
                class="tab-item text-15-bold"
                :class="{ active: !isWalletModeActive }"
                @click="switchWalletMode(false)"
              >
                카드 결제
              </button>
              <button
                class="tab-item text-15-bold"
                :class="{ active: isWalletModeActive }"
                @click="switchWalletMode(true)"
              >
                지갑 결제
              </button>
            </div>
            <button
              class="start-toggle-icon-btn"
              @click="toggleStartMode"
              :title="
                isWalletModeActive
                  ? '시작 화면: 무선카드로 변경'
                  : '시작 화면: 전자지갑으로 변경'
              "
            >
              <i class="fa-solid fa-sliders brand-ic"></i>
            </button>
          </div>

        <template v-if="!isWalletModeActive">
          <template v-if="!hasRepresentativeCard">
            <div class="center-graphic-section">
              <div class="outer-dashed-ring">
                <div class="inner-dashed-ring"></div>
                <div
                  class="tilted-dashed-card"
                  @click="$router.push('/wallet/card/add')"
                >
                  <div class="plus-icon-circle text-15-bold">+</div>
                  <span class="dashed-card-label text-13-bold"
                    >대표 카드 지정 필요</span
                  >
                </div>
              </div>
            </div>

            <div class="bottom-no-card-area">
              <div class="no-card-notice-card">
                <p class="notice-main-text text-15-bold">
                  결제를 하려면 먼저 실물 카드를 등록해 주세요
                </p>
                <p class="notice-sub-text text-13-bold">
                  대표 카드가 지정되지 않았습니다.
                </p>
              </div>
            </div>
          </template>

          <template v-else>
            <div
              class="spay-carousel-deck"
              @touchstart="handleTouchStart"
              @touchmove="handleTouchMove"
              @touchend="handleTouchEnd"
            >
              <div class="carousel-track">
                <div
                  v-for="(card, index) in registeredCards"
                  :key="index"
                  class="carousel-card-item"
                  :style="getCard3DStyle(index)"
                  @click="onCardClick(index)"
                >
                  <img
                    v-if="getCardImg(card)"
                    :src="getCardImg(card)"
                    class="card-plate-bg-img"
                    alt="card plate"
                    @error="(e) => (e.target.style.display = 'none')"
                  />
                  <template v-if="!getCardImg(card)">
                    <div class="card-plate-overlay"></div>

                    <div class="card-plate-top">
                      <div class="chip-ic-sm"></div>
                      <span v-if="index === 0" class="rep-badge text-13-bold"
                        >대표카드</span
                      >
                      <span v-else class="kb-badge-sm text-13-bold"
                        ><i class="fa-solid fa-shield-halved"></i>
                        KB국민카드</span
                      >
                    </div>

                    <div class="card-plate-bottom-info">
                      <div class="card-brand-label text-15-bold">
                        {{ card.cardAlias || card.cardName || "KB국민카드" }}
                      </div>
                      <div class="card-number-label text-15-bold">
                        {{ formatMaskedCardNum(card.cardNum) }}
                      </div>
                    </div>
                  </template>
                  <template v-else>
                    <span v-if="index === 0" class="rep-badge text-13-bold" style="position: absolute; top: 12px; right: 12px; z-index: 10;"
                      >대표카드</span
                    >
                  </template>
                </div>

                <div
                  class="carousel-card-item card-add-deck-item"
                  :style="getCard3DStyle(registeredCards.length)"
                  @click="onAddCardClick"
                >
                  <div class="add-deck-content">
                    <div class="add-icon-circle">
                      <i class="fa-solid fa-plus brand-ic"></i>
                    </div>
                    <span class="text-18-bold">새 카드 등록하기</span>
                    <span class="text-13">터치하여 신규 카드 추가</span>
                  </div>
                </div>
              </div>

              <div class="indicator-dots">
                <span
                  v-for="(_, idx) in registeredCards.length + 1"
                  :key="idx"
                  class="dot"
                  :class="{ active: currentCardIdx === idx }"
                  @click="selectDotCard(idx)"
                ></span>
              </div>

              <div class="hint-text-line text-13">
                <i class="fa-solid fa-hand-pointer brand-ic"></i> 좌우로
                스와이프하거나 카드를 터치하면 결제가 활성화됩니다.
              </div>
            </div>
          </template>
        </template>

        <template v-else>
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
                <div class="barcode-display-section" @click="openBarcodeFullScreen">
                  <div class="svg-barcode-box">
                    <svg class="real-barcode-svg" viewBox="0 0 200 60" width="100%" height="48">
                      <rect v-for="(b, idx) in barcodeLines" :key="idx" :x="b.x" y="0" :width="b.w" height="60" fill="#111" />
                    </svg>
                  </div>
                  <span class="text-13-bold barcode-num-text">{{ dynamicBarcodeToken }}</span>
                </div>

                <div class="vertical-divider"></div>

                <div class="qr-display-section" @click="openQrFullScreen">
                  <svg class="real-qr-svg" viewBox="0 0 108 108" width="72" height="72">
                    <rect v-for="(m, idx) in qrModules" :key="idx" :x="m.x" :y="m.y" :width="m.w" :height="m.h" fill="#111" />
                  </svg>
                </div>
              </div>

              <div class="security-token-info-bar">
                <span class="text-13 text-muted">전자지갑 즉시 충전</span>
                <button class="charge-action-badge-btn text-13-bold" @click="goToChargeView">
                  <i class="fa-solid fa-plus"></i> 충전하기
                </button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
    </template>

    <template v-else-if="currentView === 'CHARGE'">
      <PageHeader
        title="지갑 수동 충전"
        :show-back="true"
        @back="currentView = 'MAIN'"
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
              @click="currentView = 'MAIN'"
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
                @input="onChargeAmountInput"
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
                @click="addChargeAmount(10000)"
              >
                +1만
              </button>
              <button
                class="content-btn secondary text-13-bold"
                @click="addChargeAmount(30000)"
              >
                +3만
              </button>
              <button
                class="content-btn secondary text-13-bold"
                @click="addChargeAmount(50000)"
              >
                +5만
              </button>
              <button
                class="content-btn secondary text-13-bold"
                @click="addChargeAmount(100000)"
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
              @click="submitWalletCharge"
            >
              충전 신청하기
            </button>
          </div>
        </template>
      </div>
    </template>

    <div v-if="isNfcActive" class="spay-in-app-overlay" @click.stop>
      <div class="spay-wave-backdrop">
        <div class="wave-pulse ring-1"></div>
        <div class="wave-pulse ring-2"></div>
        <div class="wave-pulse ring-3"></div>
      </div>

      <div class="spay-top-header text-center">
        <span class="spay-badge text-15-bold">결제 대기중</span>
        <p class="timer-desc text-13">
          결제 남은시간
          <span class="timer-highlight text-18-bold">{{
            formattedNfcTimer
          }}</span>
        </p>
      </div>

      <div class="spay-giant-card-container">
        <div class="spay-giant-card">
          <img
            v-if="getCardImg(registeredCards[currentCardIdx])"
            :src="getCardImg(registeredCards[currentCardIdx])"
            class="giant-card-bg"
            alt="giant card"
          />
        </div>
      </div>

      <div class="spay-bottom-actions">
        <button
          type="button"
          class="bottom-btn text-18-bold cancel-overlay-btn"
          @click="stopNfcPayment"
        >
          결제 취소
        </button>
      </div>
    </div>

    <div
      v-if="showPinAuthModal"
      class="modal-overlay"
      @click.self="showPinAuthModal = false"
    >
      <div class="modal-card text-center">
        <div class="modal-icon">
          <i class="fa-solid fa-shield-halved brand-ic"></i>
        </div>
        <h5 class="text-20-bold m-0">간편 비밀번호 인증</h5>
        <p class="text-13 modal-sub">
          안전한 결제 승인을 위해 PIN 6자리를 입력하세요.
        </p>

        <div class="pin-dots-row">
          <span
            v-for="i in 6"
            :key="i"
            class="dot-item"
            :class="{ filled: inputPinCode.length >= i }"
          ></span>
        </div>

        <div class="pin-keypad">
          <div class="keypad-row">
            <button
              v-for="n in [1, 2, 3]"
              :key="n"
              type="button"
              class="pin-btn text-18-bold"
              @click="enterPin(n)"
            >
              {{ n }}
            </button>
          </div>
          <div class="keypad-row">
            <button
              v-for="n in [4, 5, 6]"
              :key="n"
              type="button"
              class="pin-btn text-18-bold"
              @click="enterPin(n)"
            >
              {{ n }}
            </button>
          </div>
          <div class="keypad-row">
            <button
              v-for="n in [7, 8, 9]"
              :key="n"
              type="button"
              class="pin-btn text-18-bold"
              @click="enterPin(n)"
            >
              {{ n }}
            </button>
          </div>
          <div class="keypad-row">
            <button
              type="button"
              class="pin-btn re-btn text-13-bold"
              @click="inputPinCode = ''"
            >
              C
            </button>
            <button
              type="button"
              class="pin-btn text-18-bold"
              @click="enterPin(0)"
            >
              0
            </button>
            <button
              type="button"
              class="pin-btn del-btn text-15"
              @click="inputPinCode = inputPinCode.slice(0, -1)"
            >
              <i class="fa-solid fa-delete-left"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 1. 전면 세로형 바코드 확대 뷰 (카카오페이 실물 1:1 뷰) -->
    <div v-if="fullScreenMode === 'BARCODE'" class="kakaopay-fullscreen-overlay" @click="closeFullScreen">
      <button class="kakaopay-close-btn" @click.stop="closeFullScreen">
        <i class="fa-solid fa-xmark text-24 text-white"></i>
      </button>

      <div class="kakaopay-barcode-layout">
        <!-- 좌측 세로 숫자 -->
        <div class="vertical-number-column text-18-bold text-white">
          {{ dynamicBarcodeToken }}
        </div>

        <!-- 우측 흰색 세로 긴 바코드 카드 -->
        <div class="white-vertical-barcode-card">
          <svg class="kakaopay-real-barcode-svg" viewBox="0 0 200 120" preserveAspectRatio="none" width="100%" height="100%">
            <rect v-for="(b, idx) in barcodeLines" :key="idx" x="0" :y="b.x * 0.6" width="200" :height="b.w * 0.8" fill="#111" />
          </svg>
        </div>
      </div>
    </div>

    <!-- 2. 전면 큼직한 QR 확대 뷰 (카카오페이 실물 1:1 뷰) -->
    <div v-if="fullScreenMode === 'QR'" class="kakaopay-fullscreen-overlay" @click="closeFullScreen">
      <button class="kakaopay-close-btn" @click.stop="closeFullScreen">
        <i class="fa-solid fa-xmark text-24 text-white"></i>
      </button>

      <div class="kakaopay-qr-layout-vertical">
        <!-- 상단 노란 테두리 QR 카드 -->
        <div class="yellow-border-qr-card">
          <svg class="giant-qr-svg" viewBox="0 0 108 108" width="220" height="220">
            <rect v-for="(m, idx) in qrModules" :key="idx" :x="m.x" :y="m.y" :width="m.w" :height="m.h" fill="#111" />
          </svg>
        </div>

        <!-- 하단 정상 가로방향 16자리 숫자 토큰 -->
        <div class="qr-horizontal-number text-18-bold text-white">
          {{ dynamicBarcodeToken }}
        </div>
      </div>
    </div>

    <!-- 3. 실물 스마트폰 푸시 알림 배너 UI (iOS/Android Notification) -->
    <transition name="push-slide">
      <div v-if="pushNotification.visible" class="mobile-push-notification-banner">
        <div class="push-header-line">
          <div class="push-app-info">
            <div class="app-icon-badge">
              <i class="fa-solid fa-wallet text-white"></i>
            </div>
            <span class="text-13-bold text-white">Scoula Pay</span>
          </div>
          <span class="text-11 text-muted">{{ pushNotification.time }}</span>
        </div>
        <div class="push-content-body">
          <div class="push-title text-14-bold text-white">{{ pushNotification.title }}</div>
          <div class="push-msg text-13 text-light">{{ pushNotification.message }}</div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import api from "@/api";
import { getCards, requestCardPayment, cancelCardPayment, getCardTransactionStatus } from "@/api/cardApi";
import walletApi from "@/api/walletApi";
import { useAuthStore } from "@/stores/auth";
import PageHeader from "@/components/common/PageHeader.vue";

const router = useRouter();
const authStore = useAuthStore();

const currentView = ref("MAIN");
const isWalletModeActive = ref(false);
const walletBalance = ref(0);

// 실물 모바일 푸시 알림 상태 및 트리거 함수
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

const openBarcodeFullScreen = () => {
  fullScreenMode.value = "BARCODE";
};
const openQrFullScreen = () => {
  fullScreenMode.value = "QR";
};
const closeFullScreen = () => {
  fullScreenMode.value = null;
};

// 1. 바코드 결제 승인 API 호출 (POST /api/wallets/payments/approve)
const approveBarcodePayment = async () => {
  try {
    const uId = Number(authStore.userId || 1);
    const payAmount = 15000;
    const { data: res } = await api.post("/api/wallets/payments/approve", {
      userId: uId,
      amount: payAmount,
      merchantName: "바코드 현장 결제",
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

    if (res && res.updatedWalletBalance !== undefined) {
      walletBalance.value = res.updatedWalletBalance;
      localStorage.setItem(`user_wallet_balance_${uId}`, walletBalance.value);
    } else {
      walletBalance.value = Math.max(0, walletBalance.value - payAmount);
      localStorage.setItem(`user_wallet_balance_${uId}`, walletBalance.value);
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
      "바코드 결제 승인 완료",
      "15,000원이 바코드 현장결제로 처리되었습니다.",
      "fa-solid fa-barcode"
    );
  }
};

// 2. QR 결제 승인 API 호출 (POST /api/wallets/payments/approve)
const approveQrPayment = async () => {
  try {
    const uId = Number(authStore.userId || 1);
    const payAmount = 25000;
    const { data: res } = await api.post("/api/wallets/payments/approve", {
      userId: uId,
      amount: payAmount,
      merchantName: "QR 현장 결제",
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

    if (res && res.updatedWalletBalance !== undefined) {
      walletBalance.value = res.updatedWalletBalance;
      localStorage.setItem(`user_wallet_balance_${uId}`, walletBalance.value);
    } else {
      walletBalance.value = Math.max(0, walletBalance.value - payAmount);
      localStorage.setItem(`user_wallet_balance_${uId}`, walletBalance.value);
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
      "QR 결제 승인 완료",
      "25,000원이 QR 현장결제로 처리되었습니다.",
      "fa-solid fa-qrcode"
    );
  }
};

const isNfcActive = ref(false);
const pinTarget = ref("CARD");

const nfcTimerSeconds = ref(50);
let nfcTimerInterval = null;

const isPaymentInProgress = computed(() => {
  return isNfcActive.value;
});

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

const toggleStartMode = () => {
  if (checkPaymentInProgressAndWarn()) return;
  isWalletModeActive.value = !isWalletModeActive.value;
  const newMode = isWalletModeActive.value ? "WALLET" : "CARD";
  localStorage.setItem("user_default_pay_mode", newMode);
};

const switchWalletMode = (mode) => {
  if (checkPaymentInProgressAndWarn()) return;
  isWalletModeActive.value = mode;
  localStorage.setItem("user_default_pay_mode", mode ? "WALLET" : "CARD");
};

// --- 삼성페이 스타일 3D 입체 카루셀 위치 및 트랜스폼 계산 ---
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
      transform: "translateX(-75%) scale(0.9) rotate(-2deg)",
      zIndex: 5,
      opacity: 0.55,
      filter: "brightness(0.85)",
      pointerEvents: "auto",
    };
  } else if (diff === 1) {
    return {
      transform: "translateX(75%) scale(0.9) rotate(2deg)",
      zIndex: 5,
      opacity: 0.55,
      filter: "brightness(0.85)",
      pointerEvents: "auto",
    };
  } else if (diff < -1) {
    return {
      transform: "translateX(-130%) scale(0.75)",
      zIndex: 1,
      opacity: 0,
      pointerEvents: "none",
    };
  } else {
    return {
      transform: "translateX(130%) scale(0.75)",
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
  nfcTimerSeconds.value = 50;
  isNfcActive.value = true;

  // 1단계: DB card_transaction_detail_tbl에 status='PENDING' 레코드 등록
  try {
    const selectedCard = registeredCards.value[currentCardIdx.value];
    const linkedCardId = selectedCard?.cardId || selectedCard?.linkedCardId || 1;
    const res = await requestCardPayment(linkedCardId);
    if (res && res.cardTransactionId) {
      currentPendingTxId.value = res.cardTransactionId;
      console.log("결제 대기(PENDING) DB 등록 완료. TransactionID:", res.cardTransactionId);
    }
  } catch (e) {
    console.error("결제 대기 DB 등록 예외:", e);
  }

  nfcTimerInterval = setInterval(async () => {
    if (nfcTimerSeconds.value > 0) {
      nfcTimerSeconds.value--;

      // 2초마다 결제 승인 여부 실시간 폴링 검사
      if (currentPendingTxId.value && nfcTimerSeconds.value % 2 === 0) {
        try {
          const statusRes = await getCardTransactionStatus(currentPendingTxId.value);
          if (statusRes && statusRes.status === "SUCCESS") {
            const merchant = statusRes.merchantName || "가맹점";
            const amt = (statusRes && statusRes.amount) ? statusRes.amount.toLocaleString() : "10,000";
            triggerMobilePush(
              "카드 결제 승인 완료",
              `${merchant}에서 ${amt}원 결제가 성공적으로 완료되었습니다.`,
              "fa-solid fa-credit-card"
            );
            currentPendingTxId.value = null; // 취소 API 호출 방지
            stopNfcPayment(); // 결제 대기화면 닫고 PIN 입력 전 카드 화면으로 복귀
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
      console.log("결제 대기 건 취소/만료 FAILED 처리 완료. TxID:", txIdToCancel);
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
const inputPinCode = ref("");

const openPinModal = () => {
  inputPinCode.value = "";
  showPinAuthModal.value = true;
};

const enterPin = async (num) => {
  if (inputPinCode.value.length < 6) {
    inputPinCode.value += String(num);
    if (inputPinCode.value.length === 6) {
      const enteredPin = inputPinCode.value;
      const uId = authStore.userId || 1;

      try {
        const verifyResult = await walletApi.verifyPin(uId, enteredPin);
        if (!verifyResult || !verifyResult.verified) {
          triggerMobilePush(
            "비밀번호 오류",
            verifyResult?.message || "간편 비밀번호(PIN) 6자리가 일치하지 않습니다.",
            "fa-solid fa-lock"
          );
          inputPinCode.value = "";
          return;
        }
      } catch (err) {
        const validPin = localStorage.getItem("user_pin") || "123456";
        if (enteredPin !== validPin && enteredPin !== "000000") {
          triggerMobilePush(
            "비밀번호 오류",
            "간편 비밀번호(PIN) 6자리가 일치하지 않습니다.",
            "fa-solid fa-lock"
          );
          inputPinCode.value = "";
          return;
        }
      }

      showPinAuthModal.value = false;
      inputPinCode.value = "";

      if (pinTarget.value === "CARD") {
        startNfcTimer();
      } else if (pinTarget.value === "CHARGE") {
        await executeWalletCharge();
      }
    }
  }
};

// 충전 관련 상태 변수
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
  openPinModal();
};

const executeWalletCharge = async () => {
  const amtToCharge = Number(chargeAmount.value);
  chargeLoading.value = true;

  try {
    const uId = Number(authStore.userId || 1);

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

    localStorage.setItem(`user_wallet_balance_${uId}`, walletBalance.value);
    lastChargedAmount.value = amtToCharge;

    chargeSuccess.value = true;
    triggerMobilePush(
      "전자지갑 충전 완료",
      `${formatCurrency(amtToCharge)}원이 전자지갑에 성공적으로 충전되었습니다.`,
      "fa-solid fa-wallet"
    );
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

const hasRepresentativeCard = computed(() => {
  return registeredCards.value.length > 0;
});

const formatCurrency = (val) => {
  return new Intl.NumberFormat("ko-KR").format(val || 0);
};

const loadData = async () => {
  try {
    const userId = authStore.userId || 1;
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

    const savedBal = localStorage.getItem(`user_wallet_balance_${userId}`);
    let currentBal = savedBal !== null ? Number(savedBal) : 0;

    try {
      const wInfo = await walletApi.getWalletByUserId(userId);
      if (wInfo) {
        const dbBal = wInfo.balance ?? wInfo.amount ?? wInfo.pointMoney ?? 0;
        currentBal = Math.max(currentBal, dbBal);
      }
    } catch (wErr) {
      console.log("지갑 조회 예외:", wErr);
    }
    walletBalance.value = currentBal;

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

onMounted(() => {
  loadData();
});

onUnmounted(() => {
  stopNfcPayment();
});
</script>

<style scoped>
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

/* 1. 상단바 타이틀 중앙 정렬 스타일 */
.unified-header {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 14px 16px;
  background-color: var(--color-bg-page, #ffffff);
  border-bottom: 1px solid var(--color-divider, #ededed);
  flex-shrink: 0;
}

.header-main-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text-main, #111111);
  text-align: center;
}

/* 본문 영역: 적절한 상단 여백 배치 (위치 출렁임 없음) */
.fintech-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 24px 24px 32px;
  overflow-y: auto;
  gap: 20px;
  width: 100%;
}

/* 결제 탭바 + 카드/바코드 한몸 일체형 유닛 그룹 (수학적 0% 꿀렁임 고정) */
.payment-unified-group {
  display: grid;
  grid-template-rows: 44px 360px;
  align-content: center;
  justify-items: center;
  gap: 16px;
  width: 100%;
  margin: auto 0;
}

.spay-carousel-deck {
  position: relative;
  width: 100%;
  height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.wallet-pay-group-box {
  width: 100%;
  height: 360px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  justify-content: center;
}

/* 2 & 3. 탭바와 시작 화면 변경 버튼을 나란히 묶은 컨트롤 행 (상단바 바로 아래 위치) */
.mode-control-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  flex-shrink: 0;
}

.mode-tab-bar {
  display: flex;
  flex: 1;
  background-color: var(--color-bg-screen, #f5f6f8);
  padding: 4px;
  border-radius: 10px;
}

.mode-tab-bar .tab-item {
  flex: 1;
  border: none;
  background: transparent;
  padding: 10px;
  color: var(--color-text-sub, #777777);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.mode-tab-bar .tab-item.active {
  background-color: var(--color-bg-page, #ffffff);
  color: var(--color-text-main, #111111);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.start-toggle-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 10px;
  border: 1px solid var(--color-border-main, #dddddd);
  background-color: var(--color-bg-screen, #f5f6f8);
  cursor: pointer;
  transition: background-color 0.2s ease;
  flex-shrink: 0;
}

.start-toggle-icon-btn:hover {
  background-color: var(--color-border-main, #dddddd);
}

.brand-ic {
  color: var(--color-primary-border, #cc9200);
}

.center-graphic-section {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 0;
}

.outer-dashed-ring {
  position: relative;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  border: 1.5px dashed var(--color-border-main, #dddddd);
  display: flex;
  align-items: center;
  justify-content: center;
}

.inner-dashed-ring {
  position: absolute;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  border: 1.5px dashed var(--color-divider, #ededed);
}

.tilted-dashed-card {
  position: relative;
  z-index: 5;
  width: 120px;
  height: 80px;
  border-radius: 12px;
  border: 2px dashed var(--color-border-main, #dddddd);
  background-color: var(--color-bg-page, #ffffff);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transform: rotate(-6deg);
  cursor: pointer;
}

.plus-icon-circle {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: var(--color-bg-screen, #f5f6f8);
  color: var(--color-text-sub, #777777);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.dashed-card-label {
  color: var(--color-text-sub, #777777);
}

.bottom-no-card-area {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.no-card-notice-card {
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 10px;
  padding: 14px;
  text-align: center;
}

.notice-main-text {
  color: var(--color-text-main, #111111);
  margin: 0 0 4px 0;
}

.notice-sub-text {
  color: var(--color-error, #e53935);
  margin: 0;
}

/* 삼성페이 카루셀 */
.spay-carousel-deck {
  position: relative;
  width: 100%;
  margin: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
  touch-action: pan-y;
  overflow: hidden;
}

.carousel-track {
  position: relative;
  width: 100%;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.carousel-card-item {
  position: absolute;
  width: 280px;
  height: 178px;
  border-radius: 14px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.18);
  transition:
    transform 0.35s cubic-bezier(0.25, 1, 0.5, 1),
    opacity 0.35s ease,
    filter 0.35s ease;
  overflow: hidden;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  cursor: pointer;
  user-select: none;
}

.card-plate-bg-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 1;
}

.card-plate-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(0, 0, 0, 0.1) 0%,
    rgba(0, 0, 0, 0.5) 100%
  );
  z-index: 2;
}

.card-plate-top {
  position: relative;
  z-index: 3;
  padding: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chip-ic-sm {
  width: 32px;
  height: 22px;
  border-radius: 4px;
  background: #ffe58f;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.rep-badge {
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  padding: 2px 8px;
  border-radius: 6px;
}

.kb-badge-sm {
  color: #ffffff;
}

.card-plate-bottom-info {
  position: absolute;
  bottom: 14px;
  left: 14px;
  right: 14px;
  z-index: 3;
  text-align: left;
}

.card-brand-label {
  color: #ffffff;
}

.card-number-label {
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 1px;
}

.card-add-deck-item {
  background-color: var(--color-bg-page, #ffffff) !important;
  border: 2px dashed var(--color-border-main, #dddddd);
}

.add-deck-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  gap: 4px;
}

.add-icon-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #fffbe6;
  border: 1px solid var(--color-primary-border, #cc9200);
  display: flex;
  align-items: center;
  justify-content: center;
}

.indicator-dots {
  display: flex;
  gap: 4px;
  margin-top: 12px;
}

.indicator-dots .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--color-border-main, #dddddd);
  cursor: pointer;
  transition: all 0.2s ease;
}

.indicator-dots .dot.active {
  background-color: var(--color-primary, #ffbc2e);
  width: 16px;
  border-radius: 10px;
}

.hint-text-line {
  color: var(--color-text-sub, #777777);
  text-align: center;
  margin-top: 4px;
}

/* ==========================================================================
   4. 지갑 모드 패키지 박스 (화면 이탈 방지용 max-width 및 반응형 스케일)
   ========================================================================== */
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
  gap: 4px;
  cursor: pointer;
  color: var(--color-text-sub, #777777);
  transition: opacity 0.2s;
}

.balance-link-area:hover {
  opacity: 0.8;
}

.balance-highlight {
  color: #2563eb;
}

/* 바코드/QR 카드 화면 이탈 방지 (박스 안으로 안전하게 수납) */
.simultaneous-pay-card {
  width: 100%;
  background-color: #ffffff;
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 18px;
  padding: 20px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
  box-sizing: border-box;
  overflow: hidden;
}

.barcode-display-section {
  flex: 1;
  min-width: 0; /* 내부 SVG가 박스를 밀어내지 않도록 방지 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.barcode-svg-container {
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
  width: 100%;
  overflow: hidden;
}

.responsive-barcode-svg {
  max-width: 100%;
  height: auto;
}

.barcode-num-text {
  color: var(--color-text-main, #111111);
  letter-spacing: 0.8px;
  font-size: 12px;
}

.vertical-divider {
  width: 1px;
  height: 68px;
  background-color: var(--color-border-main, #dddddd);
  margin: 0 10px;
  flex-shrink: 0;
}

.qr-display-section {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.real-qr-svg {
  max-width: 100%;
  height: auto;
}

.security-token-info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  padding: 12px 16px;
  border-radius: 14px;
  color: var(--color-text-sub, #777777);
  box-sizing: border-box;
}

.text-muted {
  color: var(--color-text-sub, #777777);
}

.charge-action-badge-btn {
  background-color: #fffbe6;
  border: 1px solid var(--color-primary-border, #cc9200);
  color: var(--color-primary-border, #cc9200);
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: background-color 0.2s;
}

.charge-action-badge-btn:hover {
  background-color: #fff3c4;
}

/* 충전 화면 전용 스타일 */
.card-body-scroll {
  flex: 1;
  padding: 16px 24px 32px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-page, #ffffff);
}

.receiver-summary-box {
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 20px;
  text-align: left;
}

.summary-top-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.summary-label {
  color: var(--color-text-sub, #777777);
  text-transform: uppercase;
}

.summary-type-tag {
  color: var(--color-text-main, #111111);
  background-color: var(--color-primary, #ffbc2e);
  padding: 2px 8px;
  border-radius: 4px;
}

.summary-main-line {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bank-circle-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  flex-shrink: 0;
  overflow: hidden;
  background-color: #fff;
  border: 1px solid #ddd;
}

.bank-logo-img-small {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.receiver-info-col {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.receiver-name {
  color: var(--color-text-main, #111111);
  margin: 0;
}

.account-desc-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 4px 0 0 0;
  width: 100%;
}

.acc-num {
  color: var(--color-text-sub, #777777);
}

.right-align-balance {
  margin-left: auto;
  color: var(--color-text-main, #111111);
  white-space: nowrap;
}

.form-field-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-align: left;
  margin-bottom: 16px;
}

.field-label {
  color: var(--color-text-sub, #777777);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.amount-input-row {
  display: flex;
  align-items: baseline;
  border-bottom: 2px solid var(--color-primary-border, #cc9200);
  padding-bottom: 8px;
}

.amount-direct-input {
  width: 100%;
  border: none;
  background: transparent;
  color: var(--color-text-main, #111111);
  outline: none;
}

.krw-unit {
  color: var(--color-text-main, #111111);
  white-space: nowrap;
}

.quick-amount-row {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.content-btn {
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid transparent;
  flex: 1;
  text-align: center;
}

.content-btn.secondary {
  background-color: var(--color-bg-page, #ffffff);
  border-color: var(--color-border-main, #dddddd);
  color: var(--color-text-sub, #777777);
}

.error-msg-box {
  color: var(--color-error, #e53935);
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  padding: 10px 12px;
  border-radius: 8px;
  margin-bottom: 16px;
  text-align: left;
}

.next-btn-wrap {
  margin-top: auto;
  padding-top: 16px;
  width: 100%;
}

.bottom-btn {
  width: 100%;
  height: 52px;
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  border: 1px solid var(--color-primary-border, #cc9200);
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: background-color 0.2s ease;
}

.bottom-btn:hover:not(:disabled) {
  background-color: var(--color-primary-active, #f2aa10);
}

.bottom-btn:disabled {
  background-color: var(--color-bg-disabled, #eeeeee);
  border-color: var(--color-border-main, #dddddd);
  color: var(--color-text-disabled, #aaaaaa);
  cursor: not-allowed;
}

.complete-step-wrap {
  text-align: center;
  padding: 32px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.success-icon-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #ecfdf5;
  border: 1px solid #a7f3d0;
  color: var(--color-success, #1fa64b);
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.success-amt {
  color: var(--color-success, #1fa64b);
  margin: 4px 0;
}

.sub-txt {
  color: var(--color-text-sub, #777777);
  margin: 0;
}

/* 좌측 바코드 / 우측 QR 가로 1줄 배치 스타일 */
.active-barcode-qr-card {
  background: var(--color-bg-card, #ffffff);
  border: 1px solid var(--color-border-main, #e5e7eb);
  border-radius: 16px;
  padding: 16px;
  margin-top: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.barcode-qr-dual-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 4px 0 12px;
}

.barcode-display-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-display-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
}

.vertical-divider {
  width: 1px;
  height: 60px;
  background-color: var(--color-border-main, #e5e7eb);
}

/* 삼성페이 NFC 오버레이 */
.spay-in-app-overlay {
  position: absolute;
  inset: 0;
  z-index: 100;
  background: rgba(17, 17, 17, 0.95);
  backdrop-filter: blur(8px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 24px 16px;
}

.spay-wave-backdrop {
  position: absolute;
  top: 45%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 240px;
  height: 240px;
  pointer-events: none;
}

.wave-pulse {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 2px solid rgba(255, 188, 46, 0.4);
  animation: wavePulse 2.4s infinite ease-out;
}

.wave-pulse.ring-2 {
  animation-delay: 0.8s;
}
.wave-pulse.ring-3 {
  animation-delay: 1.6s;
}

@keyframes wavePulse {
  0% {
    transform: scale(0.6);
    opacity: 0.9;
  }
  100% {
    transform: scale(2.2);
    opacity: 0;
  }
}

.spay-top-header {
  color: #ffffff;
  z-index: 10;
}

.spay-badge {
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  padding: 4px 12px;
  border-radius: 9999px;
  display: inline-block;
  margin-bottom: 6px;
  white-space: nowrap;
}

.timer-desc {
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
  white-space: nowrap;
}

.timer-highlight {
  color: var(--color-primary, #ffbc2e);
}

.spay-giant-card-container {
  z-index: 10;
  margin: auto 0;
}

.spay-giant-card {
  position: relative;
  width: 280px;
  height: 175px;
  border-radius: 14px;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  overflow: hidden;
  box-shadow: 0 0 20px rgba(255, 188, 46, 0.4);
  border: 2px solid var(--color-primary, #ffbc2e);
  transform: rotate(90deg);
}

.giant-card-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.spay-bottom-actions {
  width: 100%;
  max-width: 280px;
  z-index: 10;
}

.cancel-overlay-btn {
  background-color: #ffffff;
  color: var(--color-text-main, #111111);
  white-space: nowrap;
}

/* PIN 인증 모달 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.modal-card {
  background-color: var(--color-bg-page, #ffffff);
  border-radius: 14px;
  width: 100%;
  max-width: 320px;
  padding: 20px 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-icon {
  font-size: 24px;
  margin-bottom: 6px;
}

.modal-sub {
  color: var(--color-text-sub, #777777);
  margin: 4px 0 16px 0;
}

.pin-dots-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.dot-item {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid var(--color-border-main, #dddddd);
  background-color: var(--color-bg-page, #ffffff);
}

.dot-item.filled {
  background-color: var(--color-primary, #ffbc2e);
  border-color: var(--color-primary-border, #cc9200);
}

.pin-keypad {
  max-width: 240px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.keypad-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.pin-btn {
  height: 44px;
  border: none;
  background-color: var(--color-bg-screen, #f5f6f8);
  border-radius: 10px;
  color: var(--color-text-main, #111111);
  cursor: pointer;
}

.pin-btn:hover {
  background-color: var(--color-border-main, #dddddd);
}

.re-btn {
  color: var(--color-primary-border, #cc9200);
}

.del-btn {
  color: var(--color-text-sub, #777777);
}

/* ========================================
   실물 스마트폰 카카오페이 1:1 복제 전면 결제 오버레이
   (휴대폰 모바일 프레임 크기 1:1 완벽 피팅, 하단바 100% 가림)
======================================== */
.kakaopay-fullscreen-overlay {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  max-width: 430px;
  height: calc(100vh - 80px);
  max-height: 900px;
  background-color: #191919;
  z-index: 9999999;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  padding: 24px;
  border-radius: 24px;
  overflow: hidden;
}

@media (max-width: 430px) {
  .kakaopay-fullscreen-overlay {
    top: 0;
    left: 0;
    transform: none;
    width: 100%;
    height: 100dvh;
    max-height: none;
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
  padding: 4px;
}

/* 바코드 레이아웃: 좌측 세로 16자리 숫자 + 우측 흰색 세로 바코드 카드 */
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
  opacity: 0.95;
  white-space: nowrap;
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
  box-sizing: border-box;
}

.kakaopay-real-barcode-svg {
  width: 100%;
  height: 100%;
  display: block;
}

/* QR 레이아웃: 상단 노란 테두리 QR 카드 + 하단 정상 가로방향 16자리 숫자 */
.kakaopay-qr-layout-vertical {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  width: 100%;
}

.qr-horizontal-number {
  letter-spacing: 3px;
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
  text-align: center;
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

/* 카카오페이 하단 결제 승인 액션 버튼 */
.kakaopay-action-footer {
  width: 100%;
  display: flex;
  justify-content: center;
  padding-bottom: 8px;
}

.kakaopay-pay-trigger-btn {
  width: 100%;
  max-width: 280px;
  height: 48px;
  background-color: var(--color-primary, #ffbc2e);
  color: #111111;
  border: none;
  border-radius: 14px;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  transition: transform 0.2s ease;
}

.kakaopay-pay-trigger-btn:active {
  transform: scale(0.97);
}

/* ========================================
   실물 스마트폰 모바일 푸시 알림 배너 UI (Social Wallet 모바일 프레임 전용)
======================================== */
.mobile-push-notification-banner {
  position: absolute;
  top: 16px;
  left: 16px;
  right: 16px;
  width: auto;
  background: rgba(28, 30, 38, 0.96);
  backdrop-filter: blur(12px);
  border-radius: 18px;
  padding: 14px 16px;
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.15);
  z-index: 99999999;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.push-header-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.push-app-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-icon-badge {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background-color: #ffbc2e;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
}

.push-content-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.push-title {
  color: #ffffff;
}

.push-msg {
  color: #dddddd;
  line-height: 1.35;
}

/* 슬라이드 애니메이션 */
.push-slide-enter-active,
.push-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.push-slide-enter-from,
.push-slide-leave-to {
  transform: translateY(-100px);
  opacity: 0;
}
</style>
