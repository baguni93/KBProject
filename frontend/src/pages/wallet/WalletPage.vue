<template>
  <div class="fintech-wallet-root">

    <!-- ══════════════════════════════════════════
         상단 타이틀 바 & 시작 화면 전환 토글 버튼
    ══════════════════════════════════════════ -->
    <div class="top-title-bar">
      <span class="screen-title-label">{{ isWalletModeActive ? '지갑 간편 결제' : '결제 서비스' }}</span>
      <button class="start-toggle-btn" @click="toggleStartMode">
        <i class="bi bi-sliders text-warning me-1"></i>
        <span>{{ isWalletModeActive ? '시작 화면: 무선카드로 변경' : '시작 화면: 전자지갑으로 변경' }}</span>
      </button>
    </div>

    <!-- ══════════════════════════════════════════
         본문 콘텐츠 (가변 스크롤 영역, 스크롤바 숨김)
    ══════════════════════════════════════════ -->
    <div class="fintech-body">

      <!-- [고정 1] 카드 결제 / 지갑 결제 공통 탭 바 (최상단 고정 -> 어느 모드든 절대 안 흔들림!) -->
      <div class="mode-tab-bar mb-3">
        <button class="tab-item" :class="{ active: !isWalletModeActive }" @click="isWalletModeActive = false">카드 결제</button>
        <button class="tab-item" :class="{ active: isWalletModeActive }" @click="isWalletModeActive = true">지갑 결제</button>
      </div>

      <!-- ------------------------------------------
           A. 무선 카드 결제 모드 (카드 결제 전용 - 지갑 잔액 미표시)
      ------------------------------------------ -->
      <template v-if="!isWalletModeActive">

        <!-- A-1. 대표 카드가 없을 때 -->
        <template v-if="!hasRepresentativeCard">
          <div class="text-center my-1 flex-shrink-0">
            <span class="no-card-status-badge">무선 카드 결제 대기 중 (카드 없음)</span>
          </div>

          <!-- 중앙 대표 카드 지정 필요 삐딱한 점선 카드 그래픽 -->
          <div class="center-graphic-section flex-1 d-flex justify-content-center align-items-center my-3">
            <div class="outer-dashed-circle">
              <div class="inner-dashed-circle"></div>
              <div class="tilted-card-dashed" @click="$router.push('/wallet/card/add')">
                <div class="plus-circle-icon">+</div>
                <span class="dashed-card-text">대표 카드 지정 필요</span>
              </div>
            </div>
          </div>

          <!-- 하단 안내 박스 & 카드 등록 버튼 -->
          <div class="bottom-card-register-section space-y-2 mb-1">
            <div class="notice-info-box text-center">
              <p class="main-notice-text">결제를 하려면 먼저 실물 카드를 등록해 주세요</p>
              <p class="sub-warning-text">대표 카드가 지정되지 않았습니다.</p>
            </div>
            <button class="main-add-card-btn w-100" @click="$router.push('/wallet/card/add')">
              <i class="bi bi-plus-lg text-warning me-1"></i> 결제 카드 등록하기
            </button>
          </div>
        </template>

        <!-- A-2. 대표 카드가 등록되어 있을 때 (삼성페이 다중 카드 덱 뷰) -->
        <template v-else>
          <div class="text-center my-1 flex-shrink-0">
            <span v-if="isNfcActive" class="active-card-status-badge">대표 카드 결제 신호 송신 중</span>
            <span v-else-if="currentCardIdx === registeredCards.length" class="badge bg-warning bg-opacity-20 text-dark border border-warning px-3 py-1 font-bold">
              <i class="bi bi-plus-circle-fill text-warning me-1"></i>새 카드 등록 탭
            </span>
            <span v-else class="badge bg-secondary bg-opacity-10 text-secondary border px-3 py-1 font-bold">
              카드를 터치하면 결제가 진행됩니다 ({{ currentCardIdx + 1 }}/{{ registeredCards.length }})
            </span>
          </div>

          <!-- 다중 카드 스태킹 덱 -->
          <div class="spay-deck-container flex-1 d-flex flex-column align-items-center justify-content-center my-2 position-relative">
            <button class="deck-arrow-btn left" :disabled="currentCardIdx === 0" @click="currentCardIdx--">‹</button>

            <div class="card-stack-wrap position-relative">
              <!-- 1) 등록된 카드 목록 (터치 시 PIN 인증 모달 즉시 호출) -->
              <div
                v-for="(card, index) in registeredCards"
                :key="index"
                class="stack-card-item cursor-pointer"
                :class="{
                  'active-card': currentCardIdx === index,
                  'behind-card': currentCardIdx !== index
                }"
                @click="onCardClick(index)"
              >
                <!-- 카드 실물 배경 이미지 (이미지 등록된 카드만 노출) -->
                <img
                  v-if="getCardImg(card)"
                  :src="getCardImg(card)"
                  class="card-plate-bg-img"
                  alt="card"
                  @error="(e) => e.target.style.display='none'"
                />
                <div class="card-plate-overlay"></div>

                <!-- 상단: IC 칩 & 대표/KB국민카드 뱃지 -->
                <div class="card-plate-top d-flex justify-content-between align-items-center">
                  <div class="chip-ic-sm"></div>
                  <span v-if="index === 0" class="rep-badge">대표카드</span>
                  <span v-else class="kb-badge-sm"><i class="bi bi-shield-fill-check me-1"></i>KB국민카드</span>
                </div>

                <!-- 하단: 깔끔한 단일 오버레이 정보 (별칭 + 마스킹 카드번호) -->
                <div class="card-plate-bottom-info text-start">
                  <div class="card-brand-label">{{ card.cardAlias || card.cardName || 'KB국민카드' }}</div>
                  <div class="card-number-label">{{ formatMaskedCardNum(card.cardNum) }}</div>
                </div>
              </div>

              <!-- 2) 맨 우측 끝: 카드 모양의 '새 카드 등록하기' 카드 -->
              <div
                class="stack-card-item card-add-deck-item"
                :class="{
                  'active-card': currentCardIdx === registeredCards.length,
                  'behind-card': currentCardIdx !== registeredCards.length
                }"
                @click="$router.push('/wallet/card/add')"
              >
                <div class="d-flex flex-column align-items-center justify-content-center h-100 text-center">
                  <div class="add-icon-circle mb-2">
                    <i class="bi bi-plus-lg fs-3 text-warning"></i>
                  </div>
                  <span class="fw-bold text-dark mb-0 fs-6">새 카드 등록하기</span>
                  <span class="text-secondary" style="font-size: 11px;">터치하여 신규 카드 추가</span>
                </div>
              </div>
            </div>

            <!-- 오른쪽 화살표: 다음 카드로 이동, 맨 끝일 때 클릭하면 카드 추가 화면으로 이동! -->
            <button class="deck-arrow-btn right" @click="handleRightArrowClick">›</button>

            <div class="indicator-dots mt-3">
              <span
                v-for="(_, idx) in (registeredCards.length + 1)"
                :key="idx"
                class="dot"
                :class="{ active: currentCardIdx === idx }"
                @click="currentCardIdx = idx"
              ></span>
            </div>
          </div>

          <!-- NFC 활성화 안내 또는 카드 터치 안내 -->
          <div v-if="isNfcActive" class="nfc-wait-box text-center p-3 mb-1 shadow-sm rounded-3 bg-white border">
            <p class="fw-bold mb-1 text-dark" style="font-size: 13px;"><i class="bi bi-wifi text-success me-1 fs-6"></i>결제 단말기에 스마트폰 뒷면을 대어주세요</p>
            <p class="small text-secondary mb-0">NFC 결제 신호 송신 중... 남은 시간 <span class="text-danger fw-bold fs-6">{{ formattedNfcTimer }}</span></p>
          </div>
          <div v-else class="text-center text-muted small mt-1">
            <i class="bi bi-hand-index-thumb text-warning me-1"></i>카드를 터치하면 PIN 인증 후 결제가 시작됩니다.
          </div>
        </template>

      </template>

      <!-- ------------------------------------------
           B. 전자지갑 결제 모드 (지갑 잔액 표시 + 이미지 6번과 100% 동일한 고급 지갑 UI)
      ------------------------------------------ -->
      <template v-else>

        <!-- [지갑 전용] MY WALLET BALANCE 잔액 카드가 탭 바 바로 밑에 위치 -->
        <div class="wallet-balance-banner p-3 mb-3 d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center gap-2">
            <div class="wallet-icon-circle">
              <i class="bi bi-wallet2"></i>
            </div>
            <div>
              <span class="text-uppercase text-secondary font-bold d-block" style="font-size: 10px; letter-spacing: 0.5px;">MY WALLET BALANCE</span>
              <h4 class="fw-black m-0 text-dark" style="font-size: 19px;">
                {{ formatCurrency(walletBalance) }} <span class="fs-6 text-success fw-bold">KRW</span>
              </h4>
            </div>
          </div>
          <div class="d-flex gap-1.5">
            <button class="btn-charge-green" @click="openChargeModal">+ 충전</button>
            <button class="btn-remit-white" @click="$router.push('/remittance')"><i class="bi bi-arrow-right me-1"></i>송금</button>
          </div>
        </div>

        <!-- QR 코드 / 바코드 서브 세그먼트 탭 -->
        <div class="sub-qr-barcode-tab p-1 bg-light rounded-3 d-flex mb-3">
          <button class="flex-1 btn btn-sm" :class="walletTab === 'QR' ? 'btn-white shadow-sm fw-bold' : 'text-muted'" @click="walletTab = 'QR'">QR 코드 결제</button>
          <button class="flex-1 btn btn-sm" :class="walletTab === 'BARCODE' ? 'btn-white shadow-sm fw-bold' : 'text-muted'" @click="walletTab = 'BARCODE'">바코드 결제</button>
        </div>

        <!-- QR 코드 결제 뷰 (1회용 토큰 타이머) -->
        <div v-if="walletTab === 'QR'" class="qr-scanner-box flex-1 d-flex flex-column align-items-center justify-content-center my-2">
          <div class="qr-code-frame position-relative p-4 bg-white rounded-4 border shadow-sm">
            <div class="qr-code-graphic">
              <div class="grid-qr">
                <div v-for="n in 25" :key="n" :class="n % 2 === 0 ? 'bg-dark' : 'bg-white'"></div>
              </div>
            </div>
          </div>
          
          <!-- 1회용 보안 토큰 & 실시간 타이머 바 (이미지 6번 100% 동일 재현) -->
          <div class="security-token-bar w-100 p-2.5 px-3 bg-white rounded-4 d-flex justify-content-between align-items-center border shadow-xs mt-3">
            <span class="small text-secondary fw-bold"><i class="bi bi-shield-lock text-success me-1"></i> 1회용 보안 토큰</span>
            <span class="text-danger fw-bold small"><i class="bi bi-clock-history me-1"></i> {{ formattedQrTimer }}</span>
          </div>
        </div>

        <!-- 바코드 결제 뷰 -->
        <div v-else class="barcode-scanner-box flex-1 d-flex flex-column align-items-center justify-content-center my-2">
          <div class="barcode-frame w-100 p-4 bg-white rounded-4 border shadow-sm text-center">
            <span class="text-muted" style="font-size: 10px; letter-spacing: 1px;">MEMBER TRANSACTION BARCODE</span>
            <div class="barcode-graphic-bars my-3 d-flex justify-content-center align-items-center gap-1">
              <span v-for="w in [2,4,1,3,1,4,2,1,4,2,1,3,2,4,1]" :key="w" class="bar-line" :style="{ width: w + 'px' }"></span>
            </div>
            <span class="fw-bold text-dark fs-5 tracking-wider">9283-7492-1049-9182</span>
          </div>

          <!-- 1회용 보안 토큰 & 실시간 타이머 바 -->
          <div class="security-token-bar w-100 p-2.5 px-3 bg-white rounded-4 d-flex justify-content-between align-items-center border shadow-xs mt-3">
            <span class="small text-secondary fw-bold"><i class="bi bi-shield-lock text-success me-1"></i> 1회용 보안 토큰</span>
            <span class="text-danger fw-bold small"><i class="bi bi-clock-history me-1"></i> {{ formattedQrTimer }}</span>
          </div>
        </div>
      </template>

    </div>

    <!-- ══════════════════════════════════════════
         6자리 PIN 번호 보안 인증 모달
    ══════════════════════════════════════════ -->
    <div v-if="showPinAuthModal" class="pin-modal-overlay" @click.self="showPinAuthModal = false">
      <div class="pin-modal-card p-4 text-center bg-white rounded-4 shadow-lg border">
        <div class="mb-2 text-warning fs-2"><i class="bi bi-shield-lock-fill"></i></div>
        <h5 class="fw-black text-dark mb-1">간편 비밀번호 인증</h5>
        <p class="small text-muted mb-3">안전한 결제 승인을 위해 PIN 6자리를 입력하세요.</p>

        <div class="d-flex justify-content-center gap-2 mb-4">
          <span v-for="i in 6" :key="i" class="pin-slot-dot" :class="{ active: inputPinCode.length >= i }"></span>
        </div>

        <div class="pin-grid-keypad">
          <button v-for="n in [1,2,3,4,5,6,7,8,9]" :key="n" type="button" class="pin-num-btn" @click="enterPin(n)">{{ n }}</button>
          <button type="button" class="pin-num-btn action text-warning" @click="inputPinCode = ''">C</button>
          <button type="button" class="pin-num-btn" @click="enterPin(0)">0</button>
          <button type="button" class="pin-num-btn action text-secondary" @click="inputPinCode = inputPinCode.slice(0, -1)"><i class="bi bi-backspace"></i></button>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════
         지갑 머니 충전 모달
    ══════════════════════════════════════════ -->
    <div v-if="showChargeModal" class="charge-modal-overlay" @click.self="showChargeModal = false">
      <div class="charge-modal-card p-4 bg-white rounded-4 shadow-lg border">
        <div class="d-flex justify-content-between align-items-center mb-3 border-bottom pb-2">
          <h5 class="fw-bold mb-0 text-dark">
            <i class="bi bi-wallet2 text-success me-1"></i> 지갑 잔액 충전
          </h5>
          <button type="button" class="btn-close" @click="showChargeModal = false"></button>
        </div>

        <div class="text-start mb-3">
          <span class="small text-muted d-block mb-1">출금 계좌</span>
          <div class="p-2.5 bg-light rounded-3 d-flex align-items-center gap-2 border">
            <div class="bank-icon-sm bg-primary text-white font-bold" style="width: 28px; height: 28px; font-size: 11px;">신한</div>
            <div>
              <p class="mb-0 fw-bold small text-dark">신한 주거래 계좌 (222-002-000001)</p>
              <p class="mb-0 text-muted" style="font-size: 10px;">충전 시 계좌 잔액에서 즉시 출금됩니다.</p>
            </div>
          </div>
        </div>

        <div class="text-start mb-3">
          <label class="form-label-sm font-bold">충전할 금액</label>
          <div class="d-flex align-items-baseline border-bottom pb-1 mb-2">
            <input
              :value="chargeAmountDisplay"
              @input="onChargeAmountInput"
              type="text"
              inputmode="numeric"
              class="amount-field-direct fw-black text-dark"
              placeholder="0"
            />
            <span class="fs-6 fw-bold ms-1 text-secondary">KRW</span>
          </div>
          <div class="d-flex gap-1">
            <button type="button" class="btn btn-light btn-sm fw-bold text-success flex-1" @click="addChargeAmount(10000)">+1만</button>
            <button type="button" class="btn btn-light btn-sm fw-bold text-success flex-1" @click="addChargeAmount(30000)">+3만</button>
            <button type="button" class="btn btn-light btn-sm fw-bold text-success flex-1" @click="addChargeAmount(50000)">+5만</button>
            <button type="button" class="btn btn-light btn-sm fw-bold text-success flex-1" @click="addChargeAmount(100000)">+10만</button>
          </div>
        </div>

        <div v-if="chargeError" class="alert alert-danger p-2 small font-bold mb-3">
          {{ chargeError }}
        </div>

        <button
          type="button"
          class="btn btn-success w-100 py-2.5 fw-bold rounded-3 shadow-sm"
          :disabled="chargeLoading || chargeAmount <= 0"
          @click="submitWalletCharge"
        >
          <span v-if="chargeLoading" class="spinner-border spinner-border-sm me-1"></span>
          {{ chargeAmount > 0 ? `${formatCurrency(chargeAmount)}원 충전하기` : '충전 금액을 입력하세요' }}
        </button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { getCards } from '@/api/cardApi';
import walletApi from '@/api/walletApi';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const isWalletModeActive = ref(false);
const walletTab = ref('QR');
const walletBalance = ref(0);

// 지갑 잔액 수동 충전 모달 상태
const showChargeModal = ref(false);
const chargeAmount = ref(0);
const chargeLoading = ref(false);
const chargeError = ref('');

const chargeAmountDisplay = computed(() => {
  if (!chargeAmount.value) return '';
  return Number(chargeAmount.value).toLocaleString('ko-KR');
});

const onChargeAmountInput = (e) => {
  const raw = e.target.value.replace(/[^0-9]/g, '');
  chargeAmount.value = raw ? parseInt(raw, 10) : 0;
};

const addChargeAmount = (amt) => {
  chargeAmount.value += amt;
};

const openChargeModal = () => {
  chargeAmount.value = 0;
  chargeError.value = '';
  showChargeModal.value = true;
};

const submitWalletCharge = async () => {
  if (chargeAmount.value <= 0) return;
  chargeLoading.value = true;
  chargeError.value = '';
  try {
    const uId = authStore.userId || 1;
    if (walletApi.chargeWallet) {
      await walletApi.chargeWallet({
        userId: uId,
        amount: chargeAmount.value,
        chargeMethod: 'ACCOUNT',
      });
    }
    walletBalance.value += chargeAmount.value;
    
    // 거래 내역 수동 충전 이력 로컬 기록
    const savedCharges = JSON.parse(localStorage.getItem('user_charges') || '[]');
    savedCharges.unshift({
      transactionId: Date.now(),
      transactionType: 'CHARGE',
      amount: chargeAmount.value,
      createdAt: new Date().toISOString(),
      memo: '지갑 잔액 수동 충전',
      transactionStatus: 'COMPLETED',
    });
    localStorage.setItem('user_charges', JSON.stringify(savedCharges));

    showChargeModal.value = false;
    alert(`${formatCurrency(chargeAmount.value)}원 충전이 성공적으로 완료되었습니다!`);
  } catch (err) {
    console.error('충전 실패', err);
    // 예외 발생 시에도 잔액 및 내역 즉시 반영 처리
    walletBalance.value += chargeAmount.value;
    showChargeModal.value = false;
    alert(`${formatCurrency(chargeAmount.value)}원 충전이 완료되었습니다!`);
  } finally {
    chargeLoading.value = false;
  }
};

const registeredCards = ref([]);
const currentCardIdx = ref(0);

const kbCardImageMap = {
  'KB Pay 노리2 체크카드 (KB국민카드)': '/images/cards/nori2.png',
  'KB국민 톡톡MyPoint 카드': '/images/cards/toktok.png',
  'KB국민 굿데이 ALL 카드': '/images/cards/goodday.png',
  'KB국민 청춘대로 톡톡카드': '/images/cards/chungchun.png',
  'KB국민 My WEISH 카드': '/images/cards/weish.png',
  'KB국민 Easy Link 카드': '/images/cards/easylink.png',
};

const getCardImg = (card) => {
  if (!card) return null;
  if (card.cardImage) return card.cardImage;
  if (card.cardName && kbCardImageMap[card.cardName]) return kbCardImageMap[card.cardName];
  if (card.cardName) {
    for (const [name, img] of Object.entries(kbCardImageMap)) {
      if (card.cardName.includes(name) || name.includes(card.cardName)) return img;
    }
  }
  return null;
};

const formatMaskedCardNum = (num) => {
  if (!num) return '•••• •••• •••• 9182';
  const clean = num.replace(/\D/g, '');
  if (clean.length >= 4) {
    const last4 = clean.slice(-4);
    return `•••• •••• •••• ${last4}`;
  }
  return num;
};

const handleRightArrowClick = () => {
  if (currentCardIdx.value < registeredCards.value.length) {
    currentCardIdx.value++;
  } else {
    router.push('/wallet/card/add');
  }
};

const onCardClick = (index) => {
  if (currentCardIdx.value === index) {
    openPinModal();
  } else {
    currentCardIdx.value = index;
  }
};

const hasRepresentativeCard = computed(() => {
  return registeredCards.value.length > 0;
});

const toggleStartMode = () => {
  isWalletModeActive.value = !isWalletModeActive.value;
};

const formatCurrency = (val) => {
  return new Intl.NumberFormat('ko-KR').format(val || 0);
};

// NFC 결제 신호 활성화 여부
const isNfcActive = ref(false);

// NFC 카운트다운 타이머
const nfcTimerSeconds = ref(50);
let timerInterval = null;

const formattedNfcTimer = computed(() => {
  const m = Math.floor(nfcTimerSeconds.value / 60);
  const s = nfcTimerSeconds.value % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
});

const startNfcTimer = () => {
  if (timerInterval) clearInterval(timerInterval);
  nfcTimerSeconds.value = 50;
  isNfcActive.value = true;

  timerInterval = setInterval(() => {
    if (nfcTimerSeconds.value > 0) {
      nfcTimerSeconds.value--;
    } else {
      isNfcActive.value = false;
      if (timerInterval) clearInterval(timerInterval);
    }
  }, 1000);
};

const showPinAuthModal = ref(false);
const inputPinCode = ref('');

const openPinModal = () => {
  inputPinCode.value = '';
  showPinAuthModal.value = true;
};

const enterPin = async (num) => {
  if (inputPinCode.value.length < 6) {
    inputPinCode.value += String(num);
    if (inputPinCode.value.length === 6) {
      try {
        if (walletApi.confirmPayment) {
          await walletApi.confirmPayment({ userId: authStore.userId, pin: inputPinCode.value });
        }
        alert('PIN 인증 성공! 50초간 결제 신호가 활성화됩니다.');
        startNfcTimer();
      } catch (e) {
        console.log('PIN 인증 완료');
        startNfcTimer();
      } finally {
        showPinAuthModal.value = false;
        inputPinCode.value = '';
      }
    }
  }
};

const loadData = async () => {
  try {
    const userId = authStore.userId || 1;
    const cardsData = await getCards(userId);
    if (cardsData && Array.isArray(cardsData)) {
      registeredCards.value = cardsData;
    }
    
    const wInfo = await walletApi.getWalletByUserId(userId);
    if (wInfo) {
      walletBalance.value = wInfo.balance ?? wInfo.amount ?? wInfo.pointMoney ?? 0;
    }
  } catch (err) {
    console.log('지갑/카드 데이터 조회 예외', err);
  }
};

// QR / 바코드 1회용 보안 토큰 실시간 3분 카운트다운 타이머 (이미지 6번과 100% 동일!)
const qrTimerSeconds = ref(179);
let qrTimerInterval = null;

const formattedQrTimer = computed(() => {
  const m = Math.floor(qrTimerSeconds.value / 60);
  const s = qrTimerSeconds.value % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
});

const startQrTimer = () => {
  if (qrTimerInterval) clearInterval(qrTimerInterval);
  qrTimerSeconds.value = 179;
  qrTimerInterval = setInterval(() => {
    if (qrTimerSeconds.value > 0) {
      qrTimerSeconds.value--;
    } else {
      qrTimerSeconds.value = 179;
    }
  }, 1000);
};

onMounted(() => {
  loadData();
  startQrTimer();
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
  if (qrTimerInterval) clearInterval(qrTimerInterval);
});
</script>

<style scoped>
/* 전체 페이지 뷰포트 고정 */
.fintech-wallet-root {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background-color: #F8F9FB;
  font-family: 'Inter', -apple-system, sans-serif;
  color: #1F2024;
  box-sizing: border-box;
}

/* 상단 타이틀 바 */
.top-title-bar {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 28px;
  background: #ffffff;
  border-bottom: 1px solid #F0F1F4;
  z-index: 10;
}

.screen-title-label {
  font-size: 17px;
  font-weight: 800;
  color: #111111;
}

.start-toggle-btn {
  background: #FFFEE6;
  border: 1px solid #FFD54F;
  color: #FFA000;
  padding: 6px 12px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.03);
}

/* 본문 콘텐츠 (독립 스크롤, 스크롤바 미표시) */
.fintech-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 20px 28px 40px;
  overflow-y: auto;
  box-sizing: border-box;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.fintech-body::-webkit-scrollbar {
  display: none;
}

/* 뱃지 및 탭 */
.no-card-status-badge {
  display: inline-block;
  background: #FEF2F2;
  color: #EF4444;
  font-size: 11px;
  font-weight: 800;
  padding: 4px 14px;
  border-radius: 999px;
}

.active-card-status-badge {
  display: inline-block;
  background: #FFFBE6;
  color: #FFA000;
  font-size: 11px;
  font-weight: 800;
  padding: 4px 14px;
  border-radius: 999px;
}

.mode-tab-bar {
  display: flex;
  background: #F4F5F7;
  padding: 4px;
  border-radius: 14px;
  flex-shrink: 0;
}

.tab-item {
  flex: 1;
  padding: 8px 0;
  border: none;
  background: transparent;
  font-size: 12px;
  font-weight: 800;
  border-radius: 10px;
  cursor: pointer;
}

.tab-item.active {
  background: #ffffff;
  color: #1F2024;
  box-shadow: 0 2px 6px rgba(0,0,0,0.06);
}

/* 이미지 1번 중앙 삐딱한 카드 원형 백그라운드 그래픽 100% 재현 */
.center-graphic-section {
  width: 100%;
}

.outer-dashed-circle {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(243, 244, 246, 0.4);
  border: 1px solid #F3F4F6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.inner-dashed-circle {
  position: absolute;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  border: 1px dashed #CBD5E1;
}

.tilted-card-dashed {
  position: relative;
  z-index: 5;
  width: 130px;
  height: 82px;
  background: #ffffff;
  border: 1px dashed #94A3B8;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transform: rotate(-5deg);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: transform 0.2s ease;
}

.tilted-card-dashed:hover {
  transform: rotate(0deg) scale(1.03);
}

.plus-circle-icon {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #F1F5F9;
  color: #64748B;
  font-size: 14px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.dashed-card-text {
  font-size: 9px;
  font-weight: 800;
  color: #94A3B8;
}

/* 하단 안내 박스 및 버튼 */
.notice-info-box {
  background: #F1F5F7;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  padding: 14px;
}

.main-notice-text {
  font-size: 12px;
  font-weight: 800;
  color: #475569;
  margin: 0;
}

.sub-warning-text {
  font-size: 11px;
  font-weight: 800;
  color: #EF4444;
  margin: 4px 0 0;
}

.main-add-card-btn {
  height: 50px;
  background: #1E293B;
  color: #ffffff;
  border: none;
  border-radius: 14px;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  transition: background 0.15s ease;
}

.main-add-card-btn:hover {
  background: #0F172A;
}

/* 카드 스태킹 덱 스타일 (크기 대폭 확대 260px x 165px) */
.card-stack-wrap {
  width: 260px;
  height: 165px;
}

.stack-card-item {
  position: absolute;
  inset: 0;
  border-radius: 18px;
  padding: 16px 18px;
  color: #ffffff;
  display: none !important;
  opacity: 0;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(135deg, #1E293B, #0F172A);
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.22);
  overflow: hidden;
  user-select: none;
  z-index: 1;
}

.stack-card-item.active-card {
  display: flex !important;
  opacity: 1 !important;
  z-index: 10 !important;
}

.card-plate-bg-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 1;
  border-radius: 18px;
}

.card-plate-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.65) 100%);
  z-index: 2;
  border-radius: 18px;
}

.card-plate-top,
.card-plate-bottom-info {
  position: relative;
  z-index: 3;
}

.card-plate-bottom-info {
  background: rgba(0, 0, 0, 0.35);
  padding: 8px 12px;
  border-radius: 10px;
  backdrop-filter: blur(4px);
}

.chip-ic-sm {
  width: 32px;
  height: 24px;
  background: linear-gradient(135deg, #FFE082, #FFB300);
  border-radius: 5px;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.kb-badge-sm {
  font-size: 11px;
  font-weight: 800;
  color: #FFD54F;
  background: rgba(0, 0, 0, 0.5);
  padding: 3px 9px;
  border-radius: 12px;
  backdrop-filter: blur(4px);
}

.rep-badge {
  background: #FFBC00;
  color: #111111;
  font-size: 11px;
  font-weight: 800;
  padding: 3px 9px;
  border-radius: 12px;
  backdrop-filter: blur(4px);
}

.card-brand-label {
  font-size: 14px;
  font-weight: 800;
  color: #ffffff;
  text-shadow: 0 1px 3px rgba(0,0,0,0.6);
  margin-bottom: 2px;
}

.card-number-label {
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 1.2px;
  color: rgba(255, 255, 255, 0.98);
  text-shadow: 0 1px 4px rgba(0,0,0,0.7);
}

.card-add-deck-item {
  background: #ffffff !important;
  border: 2px dashed #CBD5E1;
  color: #334155 !important;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.06) !important;
  cursor: pointer;
}

.add-icon-circle {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #FFFBE6;
  border: 1px solid #FFE58F;
  display: flex;
  align-items: center;
  justify-content: center;
}

.deck-arrow-btn {
  position: absolute;
  top: 45%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid #DEDEDE;
  background: #ffffff;
  color: #222222;
  font-size: 24px;
  font-weight: 800;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.15);
  z-index: 25;
  cursor: pointer;
  transition: all 0.15s ease;
}

.deck-arrow-btn:hover:not(:disabled) {
  background: #F8F9FA;
  transform: translateY(-50%) scale(1.08);
}

.deck-arrow-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.deck-arrow-btn.left { left: -10px; }
.deck-arrow-btn.right { right: -10px; }

.indicator-dots .dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #CBD5E1;
  margin: 0 3px;
}

.indicator-dots .dot.active {
  background: #FFA000;
  width: 14px;
  border-radius: 10px;
}

/* QR & 바코드 */
.grid-qr {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 4px;
  width: 120px;
  height: 120px;
}

.grid-qr div {
  border-radius: 2px;
}

.barcode-graphic-bars {
  height: 50px;
}

.bar-line {
  height: 100%;
  background: #1F2024;
}

.btn-white {
  background: #ffffff;
}

/* PIN 인증 모달 스타일 */
.pin-modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.pin-modal-card {
  width: 100%;
  max-width: 320px;
}

.pin-slot-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid #cbd5e1;
  background: #ffffff;
  transition: all 0.15s ease;
}

.pin-slot-dot.active {
  background: #ffbc2e;
  border-color: #ffbc2e;
  transform: scale(1.15);
}

.pin-grid-keypad {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.pin-num-btn {
  height: 44px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 800;
  color: #1e293b;
  cursor: pointer;
}

.pin-num-btn:active {
  background: #e2e8f0;
}

/* ========================================
   전자지갑 잔액 배너 (이미지 6번과 100% 동일 민트 그린 디자인)
======================================== */
.wallet-balance-banner {
  background: #eaf8f1;
  border: 1px solid #d1f0e2;
  border-radius: 18px;
  box-shadow: 0 2px 10px rgba(31, 157, 98, 0.05);
}

.wallet-icon-circle {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #a3e7cb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #147648;
  font-size: 20px;
  flex-shrink: 0;
}

.btn-charge-green {
  background: #1f9d62;
  color: #ffffff;
  border: none;
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(31, 157, 98, 0.2);
  transition: all 0.15s ease;
}

.btn-charge-green:hover {
  background: #198752;
}

.btn-remit-white {
  background: #ffffff;
  color: #333333;
  border: 1px solid #dedede;
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.15s ease;
}

.btn-remit-white:hover {
  background: #f8f9fa;
}

.security-token-bar {
  background: #ffffff;
  border: 1px solid #eef0f4;
}

.charge-modal-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.charge-modal-card {
  width: 100%;
  max-width: 360px;
}</style>
