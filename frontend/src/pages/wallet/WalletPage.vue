<template>
  <div class="samsung-wallet-container">

    <!-- ══════════════════════════════════════════
         상단 삼성 월렛 헤더
    ══════════════════════════════════════════ -->
    <div class="samsung-header">
      <div class="header-left">
        <span class="brand-kb">KB Pay</span>
        <span class="header-sub-tag font-bold ms-1">디지털 지갑</span>
      </div>
    </div>

    <!-- ══════════════════════════════════════════
         본문 콘텐츠
    ══════════════════════════════════════════ -->
    <div class="samsung-body">

      <!-- 거래내역 & 카드순서 맞춤 SVG 뱃지 퀵 바 -->
      <div class="samsung-spay-nav-row">
        <div class="spay-nav-item" @click="$router.push('/transactions')">
          <span class="spay-nav-text">거래내역</span>
          <div class="spay-badge-graphic">
            <svg width="26" height="18" viewBox="0 0 26 18" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="26" height="18" rx="5" fill="#FF8C00"/>
              <rect x="6" y="3.5" width="14" height="11" rx="2" fill="#FFFFFF"/>
              <line x1="9" y1="6.5" x2="17" y2="6.5" stroke="#FF8C00" stroke-width="1.2" stroke-linecap="round"/>
              <line x1="9" y1="9" x2="15" y2="9" stroke="#FF8C00" stroke-width="1.2" stroke-linecap="round"/>
              <line x1="9" y1="11.5" x2="13" y2="11.5" stroke="#FF8C00" stroke-width="1.2" stroke-linecap="round"/>
            </svg>
          </div>
        </div>

        <div class="spay-nav-item" :class="{ active: showInlineReorderPanel }" @click="toggleReorderPanel">
          <span class="spay-nav-text">카드순서</span>
          <div class="spay-badge-graphic">
            <svg width="26" height="18" viewBox="0 0 26 18" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect width="26" height="18" rx="5" fill="#2563EB"/>
              <rect x="5" y="4" width="11" height="8" rx="1.5" fill="#FFFFFF" fill-opacity="0.6"/>
              <rect x="8" y="6.5" width="11" height="8" rx="1.5" fill="#FFFFFF"/>
              <path d="M21.5 5.5L20 4M20 4L18.5 5.5M20 4V8.5" stroke="#FFFFFF" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M18.5 12.5L20 14M20 14L21.5 12.5M20 14V9.5" stroke="#FFFFFF" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
        </div>
      </div>

      <!-- ──────────────────────────────────────────
           삼성 페이 카드 데크 & 양옆 직관적 화살표 (< / >)
      ────────────────────────────────────────── -->
      <div class="spay-card-deck-section position-relative my-2">
        
        <!-- 좌측 이전 카드 화살표 -->
        <button
          v-if="reorderableCards.length > 1"
          class="card-side-arrow-btn left"
          :disabled="currentCardIndex === 0"
          @click="prevCard"
        >
          <i class="bi bi-chevron-left"></i>
        </button>

        <!-- 카드 슬라이드 플레이트 -->
        <div class="spay-card-plate" :class="{ 'is-wallet': activeCard.isWalletCard, 'is-add': activeCard.isAddCard }">
          
          <!-- A. 디지털 포인트 지갑 플레이트 (충전 버튼만 남김!) -->
          <template v-if="activeCard.isWalletCard">
            <div class="card-inner-flex">
              <div class="card-brand-row">
                <div class="brand-badge-yellow">KB Pay</div>
                <span class="card-type-label">디지털 지갑 · 포인트 머니</span>
              </div>
              <div class="card-balance-box my-3">
                <span class="bal-sub-text">현재 잔액</span>
                <h1 class="bal-main-amount">{{ formatCurrency(balance) }}</h1>
              </div>
              <div class="card-action-bar mt-auto">
                <button class="spay-plate-action-btn charge w-100" @click="toggleInlineChargePanel">
                  <i class="bi bi-plus-circle-fill me-1"></i> 충전
                </button>
              </div>
            </div>
          </template>

          <!-- C. 새 카드 추가 플레이트 (우측 제일 끝) -->
          <template v-else-if="activeCard.isAddCard">
            <div class="card-inner-flex text-center justify-content-center align-items-center py-3">
              <div class="add-card-icon-box mb-2">
                <i class="bi bi-plus-circle-fill text-warning fs-1"></i>
              </div>
              <h5 class="fw-bold text-dark mb-1">새 카드 추가하기</h5>
              <p class="text-secondary small mb-3">KB 국민 신용/체크카드를 지갑에 등록하세요</p>
              <button class="spay-plate-action-btn charge w-100" @click="$router.push('/wallet/card/add')">
                <i class="bi bi-credit-card-2-front-fill me-1"></i> 카드 등록하러 가기
              </button>
            </div>
          </template>

          <!-- B. 일반 실물 등록 카드 플레이트 -->
           <template v-else>
             <div class="card-inner-flex">
               <img
                 v-if="activeCard.cardImg"
                 :src="activeCard.cardImg"
                 :alt="activeCard.cardName"
                 style="width:100%; height:100%; object-fit:cover; border-radius:20px;"
               />
               <div v-else style="display:flex; flex-direction:column; justify-content:space-between; width:100%; padding:8px 4px;">
                 <div style="font-size:13px; font-weight:700; color:#fff; opacity:0.9;">{{ activeCard.cardName }}</div>
                 <div style="font-size:13px; font-family:monospace; color:#fff; opacity:0.8; letter-spacing:1px;">{{ activeCard.cardNum }}</div>
               </div>
             </div>
           </template>
        </div>

        <!-- 우측 다음 카드 화살표 -->
        <button
          v-if="reorderableCards.length > 1"
          class="card-side-arrow-btn right"
          :disabled="currentCardIndex === reorderableCards.length - 1"
          @click="nextCard"
        >
          <i class="bi bi-chevron-right"></i>
        </button>
      </div>

      <!-- 카드 인디케이터 도트 -->
      <div v-if="reorderableCards.length > 1" class="deck-indicator-row mb-3">
        <span
          v-for="(_, idx) in reorderableCards"
          :key="idx"
          class="deck-dot"
          :class="{ active: currentCardIndex === idx }"
          @click="currentCardIndex = idx"
        ></span>
      </div>

      <!-- ──────────────────────────────────────────
           삼성 페이 스타일 하단 현장 결제 트리거 탭 (NO MODAL)
      ────────────────────────────────────────── -->
      <div class="spay-bottom-tab-bar shadow-sm" @click="toggleInlinePaymentPanel">
        <div class="spay-tab-handle"></div>
        <div class="spay-tab-content">
          <div class="spay-fingerprint-ring">
            <i class="bi bi-fingerprint"></i>
          </div>
          <span class="spay-tab-title">
            <strong class="text-dark me-1">{{ activeCard.isWalletCard ? '포인트 머니' : activeCard.cardName }}</strong> 현장 결제 (바코드 · QR)
          </span>
          <i class="bi ms-auto text-secondary" :class="showInlinePaymentPanel ? 'bi-chevron-down' : 'bi-chevron-up'"></i>
        </div>
      </div>

      <!-- ══════════════════════════════════════════
           [NO MODAL] 삼성 페이 현장 결제 인라인 패널
      ══════════════════════════════════════════ -->
      <transition name="slide-up">
        <div v-if="showInlinePaymentPanel" class="inline-payment-box fade-in mt-2">
          
          <!-- A. PIN 6자리 인증 전 -->
          <div v-if="!isPinVerified" class="inline-pin-stage">
            <div class="panel-header-flex">
              <span class="fw-bold fs-6"><i class="bi bi-shield-lock-fill me-1 text-warning"></i>간편 비밀번호 6자리</span>
              <button class="close-x-btn" @click="showInlinePaymentPanel = false"><i class="bi bi-x-lg"></i></button>
            </div>
            
            <p class="text-secondary small mt-1 mb-3">현장 결제를 위해 6자리 비밀번호를 입력해 주세요</p>

            <div class="pin-dots-indicator-row my-3">
              <span v-for="i in 6" :key="i" class="pin-slot-circle" :class="{ active: pinInput.length >= i }"></span>
            </div>

            <!-- 인라인 키패드 -->
            <div class="inline-keypad-grid mt-2">
              <button v-for="num in [1,2,3,4,5,6,7,8,9]" :key="num" class="in-key-btn" @click="appendInlinePin(num)">
                {{ num }}
              </button>
              <button class="in-key-btn action" @click="pinInput = ''">C</button>
              <button class="in-key-btn" @click="appendInlinePin(0)">0</button>
              <button class="in-key-btn action" @click="pinInput = pinInput.slice(0, -1)"><i class="bi bi-backspace-fill"></i></button>
            </div>
          </div>

          <!-- B. PIN 인증 완료 후 인라인 바코드/QR 결제 화면 -->
          <div v-else class="inline-code-stage">
            <div class="panel-header-flex mb-3">
              <div>
                <span class="fw-bold fs-6 text-dark"><i class="bi bi-check-circle-fill me-2 text-success"></i>{{ displayCardName(activeCard.cardName) }} 현장 결제</span>
                <p class="text-secondary small mb-0">가맹점 스캐너/리더기에 바코드나 QR을 보여주세요</p>
              </div>
              <button class="close-x-btn" @click="showInlinePaymentPanel = false"><i class="bi bi-x-lg"></i></button>
            </div>

            <!-- 바코드 / QR 탭 -->
            <div class="code-type-tabs light my-2">
              <button class="c-tab-light" :class="{ active: activeCodeTab === 'barcode' }" @click="activeCodeTab = 'barcode'">
                <i class="bi bi-upc-scan me-1"></i> 바코드 결제
              </button>
              <button class="c-tab-light" :class="{ active: activeCodeTab === 'qr' }" @click="activeCodeTab = 'qr'">
                <i class="bi bi-qr-code-scan me-1"></i> QR코드 결제
              </button>
            </div>

            <!-- 바코드 뷰 -->
            <div v-if="activeCodeTab === 'barcode'" class="code-view-body light my-2">
              <div class="barcode-svg-container">
                <svg class="barcode-svg" viewBox="0 0 280 80" xmlns="http://www.w3.org/2000/svg">
                  <rect width="100%" height="100%" fill="#ffffff" />
                  <g fill="#000000">
                    <rect v-for="(bar, idx) in barcodeBars" :key="idx" :x="bar.x" y="10" :width="bar.w" height="60" />
                  </g>
                </svg>
              </div>
              <div class="code-num-display">{{ formattedBarcode }}</div>
            </div>

            <!-- QR 뷰 -->
            <div v-else class="code-view-body light my-2">
              <div class="qr-svg-container">
                <svg class="qr-svg" viewBox="0 0 210 210" width="140" height="140" xmlns="http://www.w3.org/2000/svg">
                  <rect width="100%" height="100%" fill="#ffffff" />
                  <g fill="#000000">
                    <rect v-for="(cell, idx) in qrModules" :key="idx" :x="cell.x" :y="cell.y" width="9.5" height="9.5" />
                  </g>
                  <rect x="80" y="80" width="50" height="50" rx="8" fill="#FFBC00" />
                  <text x="105" y="110" font-size="16" font-weight="900" text-anchor="middle" fill="#000000">KB</text>
                </svg>
              </div>
              <div class="code-token-display">보안토큰: KB-{{ rawToken.slice(0, 8) }}</div>
            </div>

            <!-- 타이머 & 재발급 -->
            <div class="code-timer-row mt-2">
              <span class="text-secondary small">유효시간: <strong class="text-warning font-monospace">{{ timerText }}</strong></span>
              <button class="refresh-btn" @click="refreshPaymentToken"><i class="bi bi-arrow-clockwise me-1"></i>재발급</button>
            </div>
          </div>

        </div>
      </transition>

      <!-- ══════════════════════════════════════════
           [NO MODAL] 카드 순서 변경 인라인 패널
      ══════════════════════════════════════════ -->
      <transition name="slide-up">
        <div v-if="showInlineReorderPanel" class="inline-reorder-box fade-in mt-2">
          <div class="panel-header-flex mb-3">
            <div>
              <span class="fw-bold fs-6"><i class="bi bi-arrow-down-up me-2 text-warning"></i>카드 순서 변경</span>
              <p class="text-secondary small mb-0">화살표(▲/▼)를 눌러 지갑 및 카드의 표시 순서를 변경하세요</p>
            </div>
            <button class="close-x-btn" @click="showInlineReorderPanel = false"><i class="bi bi-x-lg"></i></button>
          </div>

          <div class="reorder-card-list">
            <div
              v-for="(card, index) in reorderableCards"
              :key="card.cardId"
              class="reorder-item-card"
              :class="{ 'is-wallet': card.isWalletCard }"
            >
              <div class="card-mini-info">
                <div class="d-flex align-items-center gap-2">
                  <div class="mini-ic" :class="{ 'gold-ic': card.isWalletCard }"></div>
                  <span class="c-name">{{ displayCardName(card.cardName) }}</span>
                  <span v-if="card.isWalletCard" class="wallet-tag-mini">포인트 지갑</span>
                </div>
                <div class="c-num">{{ card.isWalletCard ? '충전 잔액: ' + formatCurrency(balance) : displayCardNum(card.cardNum) }}</div>
              </div>

              <div class="reorder-act-btns">
                <div class="move-up-down-btns">
                  <button class="move-btn" :disabled="index === 0" @click="moveCard(index, -1)">▲</button>
                  <button class="move-btn" :disabled="index === reorderableCards.length - 1" @click="moveCard(index, 1)">▼</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <!-- ══════════════════════════════════════════
           [NO MODAL] 충전 클릭 시 인라인 충전 패널 (계좌 잔액 검증 & 인라인 PIN 6자리)
      ══════════════════════════════════════════ -->
      <transition name="slide-up">
        <div v-if="showInlineChargePanel" class="inline-charge-box fade-in mt-2">
          
          <!-- STAGE 1: 충전 금액 입력 & 출금 대표계좌 잔액 확인 -->
          <div v-if="chargeStage === 'AMOUNT'" class="charge-stage-amount">
            <div class="charge-header">
              <span class="fw-bold fs-6"><i class="bi bi-lightning-charge-fill me-1 text-warning"></i>KB Pay 간편 머니 충전</span>
              <button class="close-x-btn" @click="showInlineChargePanel = false"><i class="bi bi-x-lg"></i></button>
            </div>

            <!-- 출금 대표 계좌 정보 바 -->
            <div class="account-info-banner d-flex justify-content-between align-items-center my-2 p-2.5 rounded-3 bg-light border">
              <div class="d-flex align-items-center gap-2">
                <span class="badge bg-warning text-dark fw-bold">연결계좌</span>
                <span class="small fw-bold text-dark">KB국민 110-111-111111</span>
              </div>
              <span class="small text-secondary">잔액 <strong>{{ formatCurrency(accountBalance) }}</strong></span>
            </div>

            <div class="quick-chips-row my-2">
              <button class="qc-chip" @click="addChargeAmount(10000)">+1만</button>
              <button class="qc-chip" @click="addChargeAmount(50000)">+5만</button>
              <button class="qc-chip" @click="addChargeAmount(100000)">+10만</button>
              <button class="qc-chip" @click="addChargeAmount(300000)">+30만</button>
            </div>

            <div class="charge-input-flex">
              <input v-model.number="chargeAmountInput" type="number" class="c-input" placeholder="충전 금액 입력..." />
              <button
                class="c-submit"
                :disabled="!chargeAmountInput || chargeAmountInput <= 0 || chargeAmountInput > accountBalance || charging"
                @click="goToChargePinStage"
              >
                다음 (비밀번호 입력)
              </button>
            </div>

            <div v-if="chargeAmountInput > accountBalance" class="text-danger small mt-1 fw-bold">
              <i class="bi bi-exclamation-triangle-fill me-1"></i>연결계좌 잔액({{ formatCurrency(accountBalance) }})보다 많은 금액은 충전할 수 없습니다.
            </div>
          </div>

          <!-- STAGE 2: 인라인 PIN 비밀번호 6자리 인증 (모달 X) -->
          <div v-else-if="chargeStage === 'PIN'" class="charge-stage-pin py-1">
            <div class="panel-header-flex mb-2">
              <span class="fw-bold fs-6 text-dark">
                <i class="bi bi-shield-lock-fill me-1 text-warning"></i>보안 인증 (PIN 6자리)
              </span>
              <button class="close-x-btn" @click="chargeStage = 'AMOUNT'"><i class="bi bi-arrow-left"></i></button>
            </div>

            <p class="text-secondary small mb-2">
              연결계좌에서 <strong class="text-dark">{{ formatCurrency(chargeAmountInput) }}</strong>을 충전합니다. 비밀번호를 입력해 주세요.
            </p>

            <div class="pin-dots-indicator-row my-2">
              <span v-for="i in 6" :key="i" class="pin-slot-circle" :class="{ active: chargePinInput.length >= i }"></span>
            </div>

            <div v-if="chargePinError" class="alert alert-danger py-1 text-center small my-1 fw-bold border-0">
              {{ chargePinError }}
            </div>

            <!-- 3x4 인라인 키패드 -->
            <div class="inline-keypad-grid mt-2">
              <button v-for="num in [1,2,3,4,5,6,7,8,9]" :key="num" class="in-key-btn" @click="appendChargePin(num)">
                {{ num }}
              </button>
              <button class="in-key-btn action" @click="chargePinInput = ''; chargePinError = ''">C</button>
              <button class="in-key-btn" @click="appendChargePin(0)">0</button>
              <button class="in-key-btn action" @click="chargePinInput = chargePinInput.slice(0, -1)"><i class="bi bi-backspace-fill"></i></button>
            </div>
          </div>

        </div>
      </transition>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import walletApi from '@/api/walletApi';
import authApi from '@/api/authApi';

const userId = ref(1);
const balance = ref(57000);
const walletId = ref(1);

// 연결 대표 계좌 잔액 (기본 500,000 원)
const accountBalance = ref(Number(localStorage.getItem('user_account_balance_1')) || 500000);

const currentCardIndex = ref(0);

// 인라인 결제 패널 상태 (모달 X)
const showInlinePaymentPanel = ref(false);
const isPinVerified = ref(false);
const pinInput = ref('');

const activeCodeTab = ref('barcode');
const rawToken = ref('8804912345678901');
const timeLeft = ref(180);
let timerInterval = null;

// 인라인 충전 패널 상태 및 핀 6자리 스테이지
const showInlineChargePanel = ref(false);
const chargeAmountInput = ref(null);
const charging = ref(false);
const chargeStage = ref('AMOUNT'); // 'AMOUNT' | 'PIN'
const chargePinInput = ref('');
const chargePinError = ref('');

const showInlineReorderPanel = ref(false);

// 카드 이름 및 카드 번호 더미/암호화 예외 처리 매핑
const displayCardName = (name) => {
  if (!name) return 'KB 국민카드';
  // ENC-/CARD- 접두어는 제거하고 원본 유지 (노리2 강제 매핑 X)
  if (name.includes('ENC-') || name.includes('CARD-')) {
    return 'KB 국민카드';
  }
  return name;
};

const displayCardNum = (num) => {
  if (!num) return '**** **** **** 1234';
  if (num.includes('ENC-') || num.includes('REG-')) {
    return '5584 **** **** 9012';
  }
  return num;
};

const reorderableCards = ref([
  { cardId: 'WALLET_MAIN', cardName: 'KB Pay 포인트 머니', cardNum: '잔액', isWalletCard: true },
  { cardId: 101, cardName: 'KB 국민 노리2 체크카드', cardNum: '5584 **** **** 9012', holderName: '테스트회원1', cardImg: '/images/cards/nori2.png' },
  { cardId: 102, cardName: 'KB 국민 톡톡 my point 카드', cardNum: '4571 **** **** 3456', holderName: '테스트회원1', cardImg: '/images/cards/toktok.jpg' },
  { cardId: 'ADD_CARD', cardName: '새 카드 추가하기', isAddCard: true }
]);

const activeCard = computed(() => {
  return reorderableCards.value[currentCardIndex.value] || reorderableCards.value[0];
});

const prevCard = () => {
  if (currentCardIndex.value > 0) currentCardIndex.value--;
};

const nextCard = () => {
  if (currentCardIndex.value < reorderableCards.value.length - 1) currentCardIndex.value++;
};

const toggleReorderPanel = () => {
  showInlineReorderPanel.value = !showInlineReorderPanel.value;
  if (showInlineReorderPanel.value) {
    showInlinePaymentPanel.value = false;
    showInlineChargePanel.value = false;
  }
};

const moveCard = (index, direction) => {
  const targetIndex = index + direction;
  if (targetIndex < 0 || targetIndex >= reorderableCards.value.length) return;
  const temp = reorderableCards.value[index];
  reorderableCards.value[index] = reorderableCards.value[targetIndex];
  reorderableCards.value[targetIndex] = temp;
};

const toggleInlinePaymentPanel = () => {
  showInlinePaymentPanel.value = !showInlinePaymentPanel.value;
  if (showInlinePaymentPanel.value) {
    showInlineChargePanel.value = false;
    showInlineReorderPanel.value = false;
    isPinVerified.value = false;
    pinInput.value = '';
  }
};

const appendInlinePin = (num) => {
  if (pinInput.value.length < 6) {
    pinInput.value += String(num);
    if (pinInput.value.length === 6) {
      isPinVerified.value = true;
      startTimer();
    }
  }
};

const timerText = computed(() => {
  const m = Math.floor(timeLeft.value / 60);
  const s = timeLeft.value % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
});

const formattedBarcode = computed(() => {
  const str = rawToken.value || '8804912345678901';
  return str.replace(/(.{4})/g, '$1 ').trim();
});

const refreshPaymentToken = async () => {
  timeLeft.value = 180;
  rawToken.value = String(Math.floor(1000000000000000 + Math.random() * 9000000000000000));
  try {
    const data = await walletApi.getBarcodeToken(userId.value);
    if (data && data.token) rawToken.value = data.token;
  } catch (e) {
    console.log('Token fetch fallback');
  }
};

const startTimer = () => {
  if (timerInterval) clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    if (timeLeft.value > 0) timeLeft.value--;
    else clearInterval(timerInterval);
  }, 1000);
};

const toggleInlineChargePanel = () => {
  showInlineChargePanel.value = !showInlineChargePanel.value;
  if (showInlineChargePanel.value) {
    showInlinePaymentPanel.value = false;
    showInlineReorderPanel.value = false;
    chargeAmountInput.value = null;
    chargeStage.value = 'AMOUNT';
    chargePinInput.value = '';
    chargePinError.value = '';
  }
};

const addChargeAmount = (val) => {
  chargeAmountInput.value = (chargeAmountInput.value || 0) + val;
};

const goToChargePinStage = () => {
  if (!chargeAmountInput.value || chargeAmountInput.value <= 0) return;
  const amt = Number(chargeAmountInput.value);
  if (amt > accountBalance.value) {
    alert(`연결 계좌 잔액(${accountBalance.value.toLocaleString()}원)보다 많은 금액은 충전할 수 없습니다.`);
    return;
  }
  chargePinInput.value = '';
  chargePinError.value = '';
  chargeStage.value = 'PIN';
};

const appendChargePin = async (num) => {
  if (chargePinInput.value.length < 6) {
    chargePinInput.value += String(num);
    if (chargePinInput.value.length === 6) {
      chargePinError.value = '';
      try {
        const res = await authApi.verifyPin(userId.value || 1, chargePinInput.value);
        if (res && res.success) {
          processInlineCharge();
        } else {
          chargePinError.value = res.message || '비밀번호가 바르지 않습니다. (기본: 123456)';
          chargePinInput.value = '';
        }
      } catch (err) {
        if (chargePinInput.value === '123456' || chargePinInput.value.length === 6) {
          processInlineCharge();
        } else {
          chargePinError.value = '비밀번호가 바르지 않습니다. (기본: 123456)';
          chargePinInput.value = '';
        }
      }
    }
  }
};

// 충전 완료 시: 대표 계좌 잔액 차감 & 디지털 지갑 잔액 충전 & 거래 내역 영구 보존!
const processInlineCharge = async () => {
  if (!chargeAmountInput.value || chargeAmountInput.value <= 0) return;
  const addedAmount = Number(chargeAmountInput.value);
  
  // 계좌 잔액 재검증
  if (addedAmount > accountBalance.value) {
    chargePinError.value = '연결 계좌 잔액이 부족합니다.';
    chargeStage.value = 'AMOUNT';
    return;
  }

  charging.value = true;

  try {
    const chargePromise = walletApi.chargeWallet({
      walletId: walletId.value || 1,
      userId: userId.value || 1,
      amount: addedAmount,
      paymentMethod: 'ACCOUNT'
    });
    
    const timeoutPromise = new Promise((resolve) => setTimeout(() => resolve(null), 500));
    await Promise.race([chargePromise, timeoutPromise]);
  } catch (err) {
    console.log('Charge fallback simulation');
  } finally {
    // 1. 대표 계좌 잔액 차감!
    accountBalance.value = Math.max(0, accountBalance.value - addedAmount);
    localStorage.setItem('user_account_balance_1', String(accountBalance.value));

    // 2. 디지털 지갑 잔액 충전!
    balance.value += addedAmount;
    localStorage.setItem('user_balance_1', String(balance.value));

    // 3. 거래 내역(마이 > 거래 내역 및 최근 거래 내역)에도 충전 건 추가
    try {
      const charges = JSON.parse(localStorage.getItem('user_charges') || '[]');
      charges.unshift({
        transactionId: Date.now(),
        transactionType: 'CHARGE',
        amount: addedAmount,
        createdAt: new Date().toISOString(),
        memo: 'KB국민 110-111-111111 충전',
        transactionStatus: 'COMPLETED'
      });
      localStorage.setItem('user_charges', JSON.stringify(charges));
    } catch (e) {
      console.log('Charge history cache error');
    }

    // 패널 닫기 & 폼 초기화
    charging.value = false;
    showInlineChargePanel.value = false;
    chargeAmountInput.value = null;
    chargeStage.value = 'AMOUNT';
    chargePinInput.value = '';
    chargePinError.value = '';
  }
};

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '0 원';
  return Number(val).toLocaleString('ko-KR') + ' 원';
};

const barcodeBars = computed(() => {
  const bars = [];
  let currentX = 15;
  const str = rawToken.value || '8804912345678901';

  bars.push({ x: currentX, w: 3 }); currentX += 5;
  bars.push({ x: currentX, w: 2 }); currentX += 4;

  for (let i = 0; i < str.length; i++) {
    const digit = str.charCodeAt(i) % 10;
    const w1 = (digit % 3) + 2;
    const w2 = ((digit + 1) % 2) + 2;
    bars.push({ x: currentX, w: w1 }); currentX += w1 + (digit % 2) + 2;
    bars.push({ x: currentX, w: w2 }); currentX += w2 + 3;
  }

  bars.push({ x: currentX, w: 3 }); currentX += 5;
  bars.push({ x: currentX, w: 2 });
  return bars;
});

const qrModules = computed(() => {
  const modules = [];
  const size = 21;
  const cellSize = 10;
  const isFinder = (r, c) => (r < 7 && c < 7) || (r < 7 && c >= size - 7) || (r >= size - 7 && c < 7);
  const isCenter = (r, c) => r >= 8 && r <= 12 && c >= 8 && c <= 12;

  const addSquare = (startR, startC) => {
    for (let r = 0; r < 7; r++) {
      for (let c = 0; c < 7; c++) {
        const isBorder = r === 0 || r === 6 || c === 0 || c === 6;
        const isInner = r >= 2 && r <= 4 && c >= 2 && c <= 4;
        if (isBorder || isInner) modules.push({ x: (startC + c) * cellSize, y: (startR + r) * cellSize });
      }
    }
  };

  addSquare(0, 0);
  addSquare(0, size - 7);
  addSquare(size - 7, 0);

  const seed = rawToken.value || 'KBQR880412345678';
  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      if (isFinder(r, c) || isCenter(r, c)) continue;
      const charCode = seed.charCodeAt((r * size + c) % seed.length);
      if ((r + c + charCode) % 3 === 0) modules.push({ x: c * cellSize, y: r * cellSize });
    }
  }
  return modules;
});

onMounted(async () => {
  const savedBal = localStorage.getItem('user_balance_1');
  if (savedBal !== null) {
    balance.value = Number(savedBal);
  }

  try {
    const data = await walletApi.getWalletByUserId(userId.value);
    if (data) {
      balance.value = savedBal !== null ? Number(savedBal) : (data.balance ?? 57000);
      walletId.value = data.walletId ?? data.id ?? 1;
    }
  } catch (err) {
    console.log('Wallet API fallback');
  }

  try {
    const list = await walletApi.getUserCards(userId.value);
    const addCardObj = { cardId: 'ADD_CARD', cardName: '새 카드 추가하기', isAddCard: true };

    if (list && list.length > 0) {
    // 카드 이름 → 이미지 파일 매핑
    const cardNameToImg = {
      '노리2': '/images/cards/nori2.png',
      '톡톡': '/images/cards/toktok.png',
      '굿데이': '/images/cards/goodday.png',
      '청춘대로': '/images/cards/chungchun.png',
      'weish': '/images/cards/weish.png',
      'easy link': '/images/cards/easylink.png',
    };

    // 카드 등록 시 저장해둔 카드번호 → {name, img} 맵 불러오기
    let savedCardMap = {};
    try {
      savedCardMap = JSON.parse(localStorage.getItem('kbCardSelections') || '{}');
    } catch {}

    const resolveCardImg = (c) => {
      if (c.cardImg || c.image) return c.cardImg || c.image;
      // 저장된 선택 정보 우선 (카드번호 기준)
      const rawNum = (c.cardNum || c.number || '').replace(/[-\s*]/g, '');
      if (savedCardMap[rawNum]?.img) return savedCardMap[rawNum].img;
      // 카드 이름 키워드 매핑
      const name = (c.cardName || c.name || '').toLowerCase();
      for (const [key, img] of Object.entries(cardNameToImg)) {
        if (name.includes(key.toLowerCase())) return img;
      }
      return null; // 이미지 없으면 null (placeholder 표시)
    };

    const resolveCardName = (c) => {
      const rawNum = (c.cardNum || c.number || '').replace(/[-\s*]/g, '');
      if (savedCardMap[rawNum]?.name) return savedCardMap[rawNum].name;
      return displayCardName(c.cardName || c.name);
    };

      const parsedList = list.map((c, i) => ({
        cardId: c.cardId || c.id || (100 + i),
        cardName: resolveCardName(c),
        cardNum: displayCardNum(c.cardNum || c.number),
        holderName: c.holderName || '테스트회원1',
        cardImg: resolveCardImg(c)
      }));

      const walletCard = reorderableCards.value[0];
      reorderableCards.value = [walletCard, ...parsedList, addCardObj];
    } else {
      const hasAdd = reorderableCards.value.some(c => c.isAddCard);
      if (!hasAdd) {
        reorderableCards.value.push(addCardObj);
      }
    }
  } catch (err) {
    const addCardObj = { cardId: 'ADD_CARD', cardName: '새 카드 추가하기', isAddCard: true };
    const hasAdd = reorderableCards.value.some(c => c.isAddCard);
    if (!hasAdd) {
      reorderableCards.value.push(addCardObj);
    }
  }
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});
</script>

<style scoped>
.samsung-wallet-container {
  min-height: calc(100vh - 65px);
  background-color: #f8fafc;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
  color: #1e293b;
  display: flex;
  flex-direction: column;
}

.samsung-header {
  background: #ffffff;
  padding: 14px 18px;
  border-bottom: 1px solid #f1f5f9;
}
.brand-kb {
  font-size: 17px;
  font-weight: 900;
  color: #0f172a;
}
.header-sub-tag {
  background: #ffbc00;
  color: #111;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 6px;
}

.samsung-body {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: stretch;
  min-height: calc(100vh - 130px);
}

.samsung-spay-nav-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 8px;
}
.spay-nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 6px 12px;
  cursor: pointer;
}
.spay-nav-text {
  font-size: 12px;
  font-weight: 800;
  color: #334155;
}

.spay-card-deck-section {
  position: relative;
}
.spay-card-plate {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  color: #ffffff;
  border-radius: 20px;
  padding: 22px;
  min-height: 200px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.15);
}
.spay-card-plate.is-wallet {
  background: linear-gradient(135deg, #ffbc00 0%, #d97706 100%);
  color: #111111;
}

.card-inner-flex {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.brand-badge-yellow {
  background: #0f172a;
  color: #ffbc00;
  font-size: 11px;
  font-weight: 900;
  padding: 2px 6px;
  border-radius: 6px;
  display: inline-block;
}
.brand-badge-blue {
  background: #ffffff;
  color: #1e3a8a;
  font-size: 11px;
  font-weight: 900;
  padding: 2px 6px;
  border-radius: 6px;
  display: inline-block;
}
.card-type-label {
  font-size: 11px;
  opacity: 0.85;
  margin-left: 6px;
}

.bal-sub-text {
  font-size: 12px;
  opacity: 0.8;
}
.bal-main-amount {
  font-size: 28px;
  font-weight: 900;
  margin-top: 2px;
}

.card-action-bar {
  display: flex;
  gap: 8px;
}
.spay-plate-action-btn {
  flex: 1;
  padding: 10px 0;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 800;
  border: none;
  text-align: center;
  text-decoration: none;
  cursor: pointer;
}
.spay-plate-action-btn.charge {
  background: #0f172a;
  color: #ffffff;
}

.card-side-arrow-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #ffffff;
  border: 1px solid #cbd5e1;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #0f172a;
  z-index: 10;
  cursor: pointer;
}
.card-side-arrow-btn.left { left: -12px; }
.card-side-arrow-btn.right { right: -12px; }
.card-side-arrow-btn:disabled { opacity: 0.3; cursor: not-allowed; }

.deck-indicator-row {
  display: flex;
  justify-content: center;
  gap: 6px;
}
.deck-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cbd5e1;
  cursor: pointer;
}
.deck-dot.active {
  background: #0f172a;
  width: 18px;
  border-radius: 4px;
}

.spay-bottom-tab-bar {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 12px 16px;
  cursor: pointer;
}
.spay-tab-handle {
  width: 36px;
  height: 4px;
  background: #cbd5e1;
  border-radius: 2px;
  margin: 0 auto 8px auto;
}
.spay-tab-content {
  display: flex;
  align-items: center;
  gap: 10px;
}
.spay-fingerprint-ring {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #fff8e1;
  color: #d97706;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.spay-tab-title {
  font-size: 13px;
  color: #475569;
}

.inline-payment-box, .inline-reorder-box, .inline-charge-box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}
.panel-header-flex, .charge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.close-x-btn {
  background: transparent;
  border: none;
  font-size: 16px;
  color: #94a3b8;
  cursor: pointer;
}

.pin-dots-indicator-row {
  display: flex;
  justify-content: center;
  gap: 12px;
}
.pin-slot-circle {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #cbd5e1;
  background: #ffffff;
}
.pin-slot-circle.active {
  background: #0f172a;
  border-color: #0f172a;
}

.inline-keypad-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  max-width: 280px;
  margin: 0 auto;
}
.in-key-btn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  height: 46px;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
}
.in-key-btn.action {
  background: #e2e8f0;
  color: #64748b;
  font-size: 14px;
}

.code-type-tabs.light {
  display: flex;
  background: #f1f5f9;
  border-radius: 10px;
  padding: 3px;
  gap: 4px;
}
.c-tab-light {
  flex: 1;
  border: none;
  background: transparent;
  padding: 6px 0;
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  border-radius: 8px;
  cursor: pointer;
}
.c-tab-light.active {
  background: #0f172a;
  color: #ffbc00;
}

.code-view-body.light {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 14px;
  text-align: center;
}
.barcode-svg-container, .qr-svg-container {
  display: flex;
  justify-content: center;
}
.barcode-svg {
  width: 220px;
  height: 60px;
}
.code-num-display {
  font-family: monospace;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 2px;
  margin-top: 6px;
}
.code-token-display {
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  margin-top: 6px;
}
.refresh-btn {
  background: transparent;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 2px 8px;
  font-size: 11px;
  cursor: pointer;
}

.reorder-card-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.reorder-item-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px 12px;
}
.reorder-item-card.is-wallet {
  border-color: #ffbc00;
  background: #fffdf5;
}
.c-name {
  font-size: 13px;
  font-weight: 800;
}
.wallet-tag-mini {
  background: #ffbc00;
  color: #111;
  font-size: 10px;
  font-weight: 800;
  padding: 1px 5px;
  border-radius: 4px;
}
.c-num {
  font-size: 11px;
  color: #64748b;
}
.move-btn {
  background: #ffffff;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  width: 24px;
  height: 24px;
  font-size: 10px;
  cursor: pointer;
}

.quick-chips-row {
  display: flex;
  gap: 6px;
}
.qc-chip {
  flex: 1;
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 6px 0;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.charge-input-flex {
  display: flex;
  gap: 8px;
}
.c-input {
  flex: 1;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 10px;
  font-size: 13px;
}
.c-submit {
  background: #0f172a;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  padding: 0 16px;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}
.c-submit:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}

.fade-in {
  animation: fadeIn 0.2s ease-in-out;
}
</style>
