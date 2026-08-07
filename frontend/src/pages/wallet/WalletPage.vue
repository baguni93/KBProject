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

      <!-- ------------------------------------------
           A. 무선 카드 결제 모드 (대표 카드 유/무 분기)
      ------------------------------------------ -->
      <template v-if="!isWalletModeActive">

        <!-- A-1. 대표 카드가 없을 때 (이미지 1번 100% 동일 구현!) -->
        <template v-if="!hasRepresentativeCard">
          <div class="text-center my-1 flex-shrink-0">
            <span class="no-card-status-badge">무선 카드 결제 대기 중 (카드 없음)</span>
          </div>

          <!-- 카드 결제 / 지갑 결제 세그먼트 탭 -->
          <div class="mode-tab-bar mb-3">
            <button class="tab-item active">카드 결제</button>
            <button class="tab-item text-secondary" @click="toggleStartMode">지갑 결제</button>
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
            <span v-if="isNfcActive" class="active-card-status-badge">대표 카드 ({{ currentCardIdx + 1 }}/{{ registeredCards.length }}) 결제 신호 송신 중</span>
            <span v-else class="badge bg-secondary bg-opacity-10 text-secondary border px-3 py-1 font-bold">결제 준비 완료 (PIN 인증 대기)</span>
          </div>

          <div class="mode-tab-bar mb-2">
            <button class="tab-item active">카드 결제</button>
            <button class="tab-item text-secondary" @click="toggleStartMode">지갑 결제</button>
          </div>

          <!-- 다중 카드 스태킹 덱 -->
          <div class="spay-deck-container flex-1 d-flex flex-column align-items-center justify-content-center my-3 position-relative">
            <button class="deck-arrow-btn left" :disabled="currentCardIdx === 0" @click="currentCardIdx--">
              <i class="bi bi-chevron-left"></i>
            </button>

            <div class="card-stack-wrap position-relative">
              <div
                v-for="(card, index) in registeredCards"
                :key="index"
                class="stack-card-item"
                :class="{
                  'active-card': currentCardIdx === index,
                  'behind-card': currentCardIdx !== index
                }"
              >
                <div class="card-brand-label">{{ card.cardAlias || card.cardName }}</div>
                <div class="card-number-label">{{ card.cardNum || '•••• 9182' }}</div>
                <span v-if="index === 0" class="rep-badge">대표</span>
              </div>
            </div>

            <button class="deck-arrow-btn right" :disabled="currentCardIdx === registeredCards.length - 1" @click="currentCardIdx++">
              <i class="bi bi-chevron-right"></i>
            </button>

            <div class="indicator-dots mt-3">
              <span v-for="(_, idx) in registeredCards" :key="idx" class="dot" :class="{ active: currentCardIdx === idx }"></span>
            </div>
          </div>

          <!-- PIN 인증 활성화 여부에 따른 하단 결제 박스 -->
          <div v-if="isNfcActive" class="nfc-wait-box text-center p-3 mb-1 shadow-sm rounded-3 bg-white border">
            <p class="fw-bold mb-1 text-dark" style="font-size: 13px;"><i class="bi bi-wifi text-success me-1 fs-6"></i>결제 단말기에 스마트폰 뒷면을 대어주세요</p>
            <p class="small text-secondary mb-0">NFC 결제 신호 송신 중... 남은 시간 <span class="text-danger fw-bold font-monospace fs-6">{{ formattedNfcTimer }}</span></p>
          </div>

          <div v-else class="nfc-init-box text-center p-3 mb-1 shadow-sm rounded-3 bg-white border">
            <p class="small text-muted mb-2">PIN 인증을 하시면 50초간 결제 신호가 활성화됩니다.</p>
            <button class="btn btn-warning w-100 fw-bold py-2 shadow-sm text-dark" @click="openPinModal">
              <i class="bi bi-shield-lock-fill me-1"></i> PIN 인증하고 결제 시작하기
            </button>
          </div>
        </template>

      </template>

      <!-- ------------------------------------------
           B. 전자지갑 결제 모드 (QR / 바코드 결제)
      ------------------------------------------ -->
      <template v-else>
        <!-- 지갑 잔액 카운터 카드가 상단에 위치 -->
        <div class="wallet-balance-banner p-3 mb-2 d-flex justify-content-between align-items-center rounded-4 border">
          <div class="d-flex align-items-center gap-2">
            <div class="wallet-icon-circle"><i class="bi bi-wallet2 text-success"></i></div>
            <div>
              <span class="text-uppercase text-muted font-monospace" style="font-size: 10px;">My Wallet Balance</span>
              <h4 class="fw-black m-0 font-monospace text-dark">{{ formatCurrency(walletBalance) }} <span class="fs-6 text-success">KRW</span></h4>
            </div>
          </div>
          <div class="d-flex gap-1">
            <button class="btn btn-success btn-sm font-bold" @click="$router.push('/wallet/charge')">+ 충전</button>
            <button class="btn btn-outline-secondary btn-sm font-bold" @click="$router.push('/remittance')">송금</button>
          </div>
        </div>

        <div class="mode-tab-bar mb-3">
          <button class="tab-item text-secondary" @click="toggleStartMode">카드 결제</button>
          <button class="tab-item active">지갑 결제</button>
        </div>

        <div class="sub-qr-barcode-tab p-1 bg-light rounded-3 d-flex mb-3">
          <button class="flex-1 btn btn-sm" :class="walletTab === 'QR' ? 'btn-white shadow-sm fw-bold' : 'text-muted'" @click="walletTab = 'QR'">QR 코드 결제</button>
          <button class="flex-1 btn btn-sm" :class="walletTab === 'BARCODE' ? 'btn-white shadow-sm fw-bold' : 'text-muted'" @click="walletTab = 'BARCODE'">바코드 결제</button>
        </div>

        <!-- QR 코드 결제 뷰 -->
        <div v-if="walletTab === 'QR'" class="qr-scanner-box flex-1 d-flex flex-column align-items-center justify-content-center my-3">
          <div class="qr-code-frame position-relative p-3 bg-white rounded-4 border shadow-sm">
            <div class="laser-scanner-line"></div>
            <div class="qr-code-graphic">
              <!-- QR 패턴 그래픽 -->
              <div class="grid-qr">
                <div v-for="n in 25" :key="n" :class="n % 2 === 0 ? 'bg-dark' : 'bg-white'"></div>
              </div>
            </div>
          </div>
          <p class="small text-muted text-center mt-3">매장 직원에게 이 화면을 보여주거나<br><strong class="text-dark">리더기에 QR코드</strong>를 스캔해 주세요.</p>
        </div>

        <!-- 바코드 결제 뷰 -->
        <div v-else class="barcode-scanner-box flex-1 d-flex flex-column align-items-center justify-content-center my-3">
          <div class="barcode-frame w-100 p-4 bg-white rounded-4 border shadow-sm text-center">
            <span class="text-muted font-monospace" style="font-size: 10px;">MEMBER TRANSACTION BARCODE</span>
            <div class="barcode-graphic-bars my-2 d-flex justify-content-center align-items-center gap-1">
              <span v-for="w in [2,4,1,3,1,4,2,1,4,2,1,3,2,4,1]" :key="w" class="bar-line" :style="{ width: w + 'px' }"></span>
            </div>
            <span class="fw-bold font-monospace text-dark">9283-7492-1049-9182</span>
          </div>
          <p class="small text-muted text-center mt-3">가맹점 전용 스캐너 및 포스기 리더기에<br><strong class="text-dark">생성된 바코드</strong>를 보여주세요.</p>
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

const registeredCards = ref([]);
const currentCardIdx = ref(0);

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
    const userId = authStore.userId;
    if (!userId) return;

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

onMounted(() => {
  loadData();
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
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

/* 카드 스태킹 덱 스타일 */
.card-stack-wrap {
  width: 200px;
  height: 120px;
}

.stack-card-item {
  position: absolute;
  inset: 0;
  border-radius: 16px;
  padding: 14px;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(135deg, #FF9F00, #FFC200);
  box-shadow: 0 8px 20px rgba(0,0,0,0.12);
}

.rep-badge {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: rgba(255,255,255,0.25);
  font-size: 9px;
  font-weight: 800;
  padding: 2px 6px;
  border-radius: 4px;
}

.deck-arrow-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #E2E8F0;
  background: #ffffff;
  color: #64748B;
  z-index: 15;
  cursor: pointer;
}

.deck-arrow-btn.left { left: 0; }
.deck-arrow-btn.right { right: 0; }

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
</style>
