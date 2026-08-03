<template>
  <div class="kb-container py-3">
    <!-- 1. 상단 서비스 헤더 및 시작 화면 설정 버튼 -->
    <div class="d-flex justify-content-between align-items-center mb-3 px-2">
      <div>
        <span class="text-muted small d-block" style="font-size: 0.75rem;">KB Pay 결제 서비스</span>
        <strong class="text-dark fw-bold fs-5">
          {{ activePaymentTab === 'CARD' ? '무선 카드 결제' : '결제 서비스' }}
        </strong>
      </div>

      <!-- 시작 화면 전환 설정 버튼 -->
      <button 
        class="btn btn-sm btn-outline-warning text-dark border-warning-subtle bg-warning-subtle rounded-pill px-3 fw-bold d-flex align-items-center gap-1 shadow-sm"
        @click="toggleStartScreenMode"
      >
        <i class="bi bi-gear-wide-connected text-dark"></i>
        <span>시작 화면: {{ startScreenMode === 'WALLET' ? '카드결제로 변경' : '전자지갑으로 변경' }}</span>
      </button>
    </div>

    <!-- 2. 결제 방식 선택 세그먼트 버튼 (카드 결제 / 지갑 결제) -->
    <div class="bg-light p-1 rounded-4 d-flex mb-4 shadow-sm border">
      <button
        class="btn flex-fill py-2.5 rounded-3 fw-bold transition-all text-center border-0"
        :class="activePaymentTab === 'CARD' ? 'bg-white text-dark shadow-sm' : 'text-secondary'"
        @click="switchPaymentTab('CARD')"
      >
        카드 결제
      </button>
      <button
        class="btn flex-fill py-2.5 rounded-3 fw-bold transition-all text-center border-0"
        :class="activePaymentTab === 'WALLET' ? 'bg-white text-dark shadow-sm' : 'text-secondary'"
        @click="switchPaymentTab('WALLET')"
      >
        지갑 결제
      </button>
    </div>

    <!-- ==================== [MODE A] 무선 카드 결제 화면 ==================== -->
    <div v-if="activePaymentTab === 'CARD'" class="card-payment-section animate__animated animate__fadeIn">
      
      <!-- 상단 상태 배지 -->
      <div class="text-center mb-4">
        <span 
          v-if="userCards.length > 0 && isPinAuthenticated"
          class="badge bg-warning-subtle text-dark border border-warning px-3 py-2 rounded-pill fw-bold"
          style="font-size: 0.825rem;"
        >
          <i class="bi bi-broadcast me-1 text-warning-emphasis spin-subtle"></i>
          대표 카드 ({{ currentCardIndex + 1 }}/{{ userCards.length }}) 결제 신호 송신 중
        </span>
        <span 
          v-else-if="userCards.length > 0 && !isPinAuthenticated"
          class="badge bg-secondary-subtle text-dark border border-secondary px-3 py-2 rounded-pill fw-bold"
          style="font-size: 0.825rem;"
        >
          <i class="bi bi-lock-fill me-1 text-danger"></i>
          PIN 비밀번호 6자리 인증 필요 (결제 대기)
        </span>
        <span 
          v-else
          class="badge bg-danger-subtle text-danger border border-danger-subtle px-3 py-2 rounded-pill fw-bold"
          style="font-size: 0.825rem;"
        >
          <i class="bi bi-exclamation-triangle-fill me-1"></i>
          무선 카드 결제 대기 중 (카드 없음)
        </span>
      </div>

      <!-- CASE 1: 카드가 등록되어 있는 경우 (다중 카드 덱 UI) -->
      <div v-if="userCards.length > 0" class="card-deck-container mb-4">
        <div class="d-flex align-items-center justify-content-center gap-3 my-3">
          <!-- 이전 카드 버튼 -->
          <button 
            class="btn btn-light rounded-circle shadow-sm p-2 nav-arrow-btn"
            :disabled="currentCardIndex === 0"
            @click="prevCard"
          >
            <i class="bi bi-chevron-left fs-5 text-dark"></i>
          </button>

          <!-- 메인 카드 (깔끔한 네모 박스) -->
          <div class="card-deck-wrapper position-relative">
            <div 
              class="simple-card-box p-3.5 rounded-3 border border-2 shadow-sm text-dark position-relative d-flex flex-column justify-content-between"
              :class="currentCard.representYn === 'Y' ? 'bg-warning border-warning' : 'bg-light border-secondary-subtle'"
              style="width: 260px; height: 150px;"
            >
              <div class="d-flex justify-content-between align-items-center">
                <span class="fw-bold fs-6">{{ currentCard.cardName }}</span>
                <span 
                  v-if="currentCard.representYn === 'Y'" 
                  class="badge bg-dark text-warning px-2 py-1 rounded-2 fw-bold"
                  style="font-size: 0.75rem;"
                >
                  대표
                </span>
              </div>

              <div class="my-2">
                <div class="fs-5 fw-bold font-monospace text-center tracking-wide">
                  {{ currentCard.cardNum }}
                </div>
              </div>

              <div class="d-flex justify-content-between align-items-center text-secondary small">
                <span>{{ currentCard.holderName }}</span>
                <i class="bi bi-wifi fs-5" :class="{ 'text-danger': !isPinAuthenticated }"></i>
              </div>
            </div>
          </div>

          <!-- 다음 카드 버튼 -->
          <button 
            class="btn btn-light rounded-circle shadow-sm p-2 nav-arrow-btn"
            :disabled="currentCardIndex === userCards.length - 1"
            @click="nextCard"
          >
            <i class="bi bi-chevron-right fs-5 text-dark"></i>
          </button>
        </div>

        <!-- 카드 슬라이드 인디케이터 -->
        <div class="text-center mt-3">
          <div class="d-inline-flex gap-1.5 align-items-center mb-1">
            <span 
              v-for="(c, idx) in userCards" 
              :key="c.cardId" 
              class="indicator-dot rounded-circle transition-all"
              :class="idx === currentCardIndex ? 'bg-warning active-dot' : 'bg-secondary opacity-25'"
            ></span>
          </div>
          <p class="text-muted small mb-0">
            DB 등록 카드 {{ userCards.length }}장 중 {{ currentCardIndex + 1 }}번째 (좌우 스와이프)
          </p>
        </div>

        <!-- 하단 NFC 결제 단말기 안내 및 PIN 인증 박스 -->
        <div v-if="isPinAuthenticated" class="nfc-terminal-box p-3.5 rounded-4 bg-warning-subtle border border-warning text-center mt-4 shadow-sm animate__animated animate__fadeIn">
          <h6 class="fw-bold text-dark mb-1">
            <i class="bi bi-phone-vibrate text-warning me-1"></i>
            결제 단말기에 스마트폰 뒷면을 대어주세요
          </h6>
          <p class="text-secondary small mb-0">
            신호 송수신 대기 중... 남은 시간 
            <strong class="text-danger font-monospace fs-6">{{ formattedTimer }}</strong>
          </p>
        </div>

        <!-- PIN 인증 전 잠금 박스 -->
        <div v-else class="pin-lock-box p-3.5 rounded-4 bg-light border text-center mt-4 shadow-sm">
          <h6 class="fw-bold text-dark mb-2">
            <i class="bi bi-shield-lock-fill text-danger me-1"></i>
            결제 신호 송수신 차단됨
          </h6>
          <p class="text-muted small mb-3">
            보안을 위해 PIN 비밀번호 6자리를 인증해야 결제 신호가 활성화됩니다.
          </p>
          <button 
            class="btn btn-warning w-100 py-2.5 rounded-3 fw-bold text-dark shadow-sm"
            @click="openPinModal"
          >
            <i class="bi bi-key-fill me-1"></i>
            PIN 6자리 인증 후 결제 시작하기
          </button>
        </div>
      </div>

      <!-- CASE 2: 대표 카드가 없는 경우 -->
      <div v-else class="no-card-container text-center py-4">
        <!-- 원형 점선 가이드 -->
        <div class="dotted-circle-box mx-auto my-4 d-flex flex-column align-items-center justify-content-center border border-2 border-dashed rounded-circle bg-white text-muted shadow-sm">
          <i class="bi bi-plus-lg fs-3 mb-1"></i>
          <span class="small fw-bold">대표 카드 지정 필요</span>
        </div>

        <div class="alert alert-light border rounded-4 p-3 text-center mb-4 shadow-sm">
          <p class="text-dark fw-medium mb-1">결제를 하려면 먼저 실물 카드를 등록해 주세요</p>
          <span class="text-danger small fw-bold">대표 카드가 지정되지 않았습니다.</span>
        </div>

        <button 
          class="btn btn-dark w-100 py-3 rounded-4 fw-bold fs-6 shadow-sm d-flex align-items-center justify-content-center gap-2"
          @click="addCard"
        >
          <i class="bi bi-plus-circle-fill text-warning"></i>
          결제 카드 등록하기
        </button>
      </div>

    </div>

    <!-- ==================== [MODE B] 전자지갑 결제 화면 ==================== -->
    <div v-else class="wallet-payment-section animate__animated animate__fadeIn">
      <!-- 사용자 선택 바 -->
      <div class="d-flex justify-content-between align-items-center mb-3 px-2">
        <div class="d-flex align-items-center gap-2">
          <div class="user-avatar bg-warning text-dark fw-bold rounded-circle d-flex align-items-center justify-content-center">
            <i class="bi bi-person-fill fs-5"></i>
          </div>
          <div>
            <span class="text-muted small d-block" style="font-size: 0.75rem;">KB Pay 회원</span>
            <strong class="text-dark fw-bold">사용자 #{{ currentUserId }}</strong>
          </div>
        </div>

        <!-- 계정 전환 드롭다운 -->
        <div class="dropdown">
          <button class="btn btn-sm btn-outline-secondary rounded-pill px-3 dropdown-toggle text-dark border" type="button" data-bs-toggle="dropdown">
            계정 전환
          </button>
          <ul class="dropdown-menu dropdown-menu-end shadow border-0 rounded-3">
            <li><a class="dropdown-item py-2" href="#" @click.prevent="switchUser(1)">User #1 (김국민)</a></li>
            <li><a class="dropdown-item py-2" href="#" @click.prevent="switchUser(2)">User #2 (이KB)</a></li>
            <li><a class="dropdown-item py-2" href="#" @click.prevent="switchUser(3)">User #3 (박스타)</a></li>
          </ul>
        </div>
      </div>

      <!-- KB Pay 지갑 카드 -->
      <div class="kb-card shadow-lg mb-4 text-dark position-relative overflow-hidden">
        <div class="card-bg-circle"></div>
        <div class="p-4 position-relative z-1">
          <div class="d-flex justify-content-between align-items-center mb-2">
            <div class="d-flex align-items-center gap-2">
              <span class="badge bg-dark text-warning px-2.5 py-1 rounded-pill fw-bold" style="font-size: 0.7rem;">KB Pay</span>
              <span class="text-secondary small fw-medium">디지털 지갑</span>
            </div>
            <button class="btn btn-link text-secondary p-0" @click="fetchWallet" title="새로고침">
              <i class="bi bi-arrow-clockwise fs-5" :class="{ 'spin-icon': loading }"></i>
            </button>
          </div>

          <div class="my-3">
            <div class="text-secondary small mb-1">지갑 잔액</div>
            <div class="display-6 fw-extrabold text-dark tracking-tight">
              {{ formatCurrency(walletData?.balance) }}
            </div>
          </div>

          <!-- 퀵 버튼 그룹 (충전하기 / 송금하기) -->
          <div class="d-flex gap-2 mt-4 pt-2">
            <button class="btn btn-dark flex-fill py-2.5 rounded-3 fw-bold shadow-sm d-flex align-items-center justify-content-center gap-1.5" @click="openChargeModal">
              <i class="bi bi-plus-circle-fill text-warning"></i> 충전하기
            </button>
            <router-link :to="{ path: '/remittance', query: { walletId: walletData?.walletId || 1 } }" class="btn btn-warning flex-fill py-2.5 rounded-3 fw-bold shadow-sm d-flex align-items-center justify-content-center gap-1.5 text-dark">
              <i class="bi bi-send-fill"></i> 송금하기
            </router-link>
          </div>
        </div>
      </div>

      <!-- 현장 결제 코드 퀵 메뉴 -->
      <div class="row g-3 mb-4">
        <div class="col-6">
          <div class="quick-action-card p-3 rounded-4 bg-white border shadow-sm cursor-pointer" @click="openPaymentModal('barcode')">
            <div class="icon-box bg-dark text-warning mb-2 rounded-3 d-flex align-items-center justify-content-center">
              <i class="bi bi-upc-scan fs-4"></i>
            </div>
            <h6 class="fw-bold mb-0 text-dark">바코드 결제</h6>
            <span class="text-muted small" style="font-size: 0.75rem;">1회용 3분 보안 바코드</span>
          </div>
        </div>
        <div class="col-6">
          <div class="quick-action-card p-3 rounded-4 bg-white border shadow-sm cursor-pointer" @click="openPaymentModal('qr')">
            <div class="icon-box bg-warning-subtle text-warning-emphasis mb-2 rounded-3 d-flex align-items-center justify-content-center">
              <i class="bi bi-qr-code-scan fs-4"></i>
            </div>
            <h6 class="fw-bold mb-0 text-dark">QR 코드 결제</h6>
            <span class="text-muted small" style="font-size: 0.75rem;">1회용 3분 보안 QR코드</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 1회용 QR / 바코드 결제 모달 -->
    <PaymentCodeModal
      v-if="showPaymentModal"
      :userId="currentUserId"
      :initialTab="selectedPaymentMode"
      @close="showPaymentModal = false"
      @paymentSuccess="fetchWallet"
    />

    <!-- 충전 모달 -->
    <div v-if="showChargeModal" class="kb-modal-backdrop" @click.self="showChargeModal = false">
      <div class="kb-modal-content bg-white rounded-4 shadow-lg p-4 animate__animated animate__fadeInUp">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-bold text-dark mb-0">지갑 머니 충전</h5>
          <button type="button" class="btn-close" @click="showChargeModal = false"></button>
        </div>

        <form @submit.prevent="executeCharge">
          <div class="mb-3">
            <label class="form-label text-secondary small fw-bold">출금 계좌 선택</label>
            <select v-model="chargeForm.bankCode" class="form-select form-select-lg fs-6 border-2">
              <option value="004">KB국민은행 (123-456-7890)</option>
              <option value="088">신한은행 (110-234-5678)</option>
              <option value="020">우리은행 (1002-345-6789)</option>
              <option value="011">NH농협은행 (302-1234-5678)</option>
              <option value="090">카카오뱅크 (3333-01-23456)</option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label text-secondary small fw-bold">충전 금액</label>
            <div class="input-group input-group-lg">
              <input
                type="number"
                v-model.number="chargeForm.amount"
                class="form-control fw-bold border-2"
                placeholder="충전할 금액 입력"
                min="1000"
                step="1000"
                required
              />
              <span class="input-group-text bg-light fw-bold">원</span>
            </div>
            <div class="d-flex gap-1.5 mt-2">
              <button type="button" class="btn btn-sm btn-outline-dark rounded-pill flex-fill" @click="addChargeAmount(10000)">+1만원</button>
              <button type="button" class="btn btn-sm btn-outline-dark rounded-pill flex-fill" @click="addChargeAmount(30000)">+3만원</button>
              <button type="button" class="btn btn-sm btn-outline-dark rounded-pill flex-fill" @click="addChargeAmount(50000)">+5만원</button>
              <button type="button" class="btn btn-sm btn-outline-dark rounded-pill flex-fill" @click="addChargeAmount(100000)">+10만원</button>
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label text-secondary small fw-bold">메모 (선택)</label>
            <input type="text" v-model="chargeForm.memo" class="form-control" placeholder="예: 용돈 충전" />
          </div>

          <button type="submit" class="btn btn-warning w-100 py-3 fw-bold rounded-3 fs-6 shadow-sm" :disabled="charging">
            <span v-if="charging" class="spinner-border spinner-border-sm me-2"></span>
            충전 완료하기
          </button>
        </form>
      </div>
    </div>

    <!-- 공통 PIN 6자리 보안 인증 모달 (QR / 바코드와 100% 동일) -->
    <PinAuthModal
      :show="showPinModal"
      :userId="currentUserId"
      @close="showPinModal = false"
      @success="handlePinSuccess"
    />

    <!-- 알림 메시지 (Toast) -->
    <div v-if="statusMessage" :class="['alert', isSuccess ? 'alert-success' : 'alert-danger', 'rounded-3 shadow-sm border-0 mb-3 d-flex align-items-center justify-content-between']">
      <span>{{ statusMessage }}</span>
      <button type="button" class="btn-close ms-2" @click="statusMessage = ''"></button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import walletApi from '@/api/walletApi';
import PaymentCodeModal from '@/components/wallet/PaymentCodeModal.vue';
import PinAuthModal from '@/components/auth/PinAuthModal.vue';

// 1. 상태 설정
const currentUserId = ref(1);
const walletData = ref(null);
const loading = ref(false);
const charging = ref(false);
const showChargeModal = ref(false);
const showPaymentModal = ref(false);
const showPinModal = ref(false);
const isPinAuthenticated = ref(false);

const selectedPaymentMode = ref('barcode'); 
const statusMessage = ref('');
const isSuccess = ref(true);

// 시작 화면 설정 ('WALLET' | 'CARD')
const startScreenMode = ref(localStorage.getItem('kb_pay_start_mode') || 'WALLET');
// 현재 활성화된 결제 탭 ('CARD' | 'WALLET')
const activePaymentTab = ref(startScreenMode.value === 'CARD' ? 'CARD' : 'WALLET');

// DB 조회 카드 덱 상태
const currentCardIndex = ref(0);
const userCards = ref([]);

const currentCard = computed(() => userCards.value[currentCardIndex.value] || {});

// NFC 신호 송수신 카운트다운 타이머 (60초 / 1분 통일)
const timerSeconds = ref(60);
let timerInterval = null;

const formattedTimer = computed(() => {
  if (timerSeconds.value <= 0) return '00:00 (만료)';
  const m = Math.floor(timerSeconds.value / 60).toString().padStart(2, '0');
  const s = (timerSeconds.value % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
});

const startTimer = () => {
  stopTimer();
  timerSeconds.value = 60;
  timerInterval = setInterval(() => {
    if (timerSeconds.value > 0) {
      timerSeconds.value--;
    } else {
      timerSeconds.value = 60; // 타이머 초기화 리셋
    }
  }, 1000);
};

const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval);
    timerInterval = null;
  }
};

const openPinModal = () => {
  showPinModal.value = true;
};

const handlePinSuccess = () => {
  isPinAuthenticated.value = true;
  showPinModal.value = false;
  statusMessage.value = 'PIN 보안 인증 성공! 무선 카드 결제 신호 송신이 시작되었습니다.';
  isSuccess.value = true;
  startTimer();
};

// 시작 화면 전환 버튼 클릭
const toggleStartScreenMode = () => {
  if (startScreenMode.value === 'WALLET') {
    startScreenMode.value = 'CARD';
    activePaymentTab.value = 'CARD';
  } else {
    startScreenMode.value = 'WALLET';
    activePaymentTab.value = 'WALLET';
  }
  localStorage.setItem('kb_pay_start_mode', startScreenMode.value);
  statusMessage.value = `시작 화면이 '${startScreenMode.value === 'CARD' ? '카드 결제' : '전자 지갑'}'로 설정되었습니다.`;
  isSuccess.value = true;
};

// 결제 탭 변경
const switchPaymentTab = (tab) => {
  activePaymentTab.value = tab;
  if (tab === 'CARD') {
    startTimer();
  } else {
    stopTimer();
  }
};

const prevCard = () => {
  if (currentCardIndex.value > 0) {
    currentCardIndex.value--;
  }
};

const nextCard = () => {
  if (currentCardIndex.value < userCards.value.length - 1) {
    currentCardIndex.value++;
  }
};

const addCard = () => {
  userCards.value.push({
    cardId: Date.now(),
    cardName: 'KB국민 신규 등록 카드',
    holderName: '김국민',
    cardNum: '•••• ' + Math.floor(1000 + Math.random() * 9000),
    representYn: userCards.value.length === 0 ? 'Y' : 'N',
    bgColor: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
  });
  currentCardIndex.value = userCards.value.length - 1;
};

const chargeForm = reactive({
  bankCode: '004',
  accountNumber: '123-456-7890',
  amount: 50000,
  memo: '',
});

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '0 원';
  return Number(val).toLocaleString('ko-KR') + ' 원';
};

const formatCardNumDisplay = (raw) => {
  if (!raw) return '•••• 9182';
  const clean = String(raw).replace(/\D/g, '');
  if (clean.length >= 4) {
    return '•••• ' + clean.slice(-4);
  }
  return raw;
};

const fetchUserCards = async () => {
  try {
    const dbCards = await walletApi.getUserCards(currentUserId.value);
    if (dbCards && dbCards.length > 0) {
      userCards.value = dbCards.map(c => ({
        cardId: c.cardId,
        cardName: c.cardName || 'KB국민 Custom Card',
        holderName: c.holderName || '김국민',
        cardNum: formatCardNumDisplay(c.cardNum),
        representYn: c.representYn || 'N',
      }));
    } else {
      userCards.value = [];
    }
  } catch (err) {
    console.error('DB 카드 목록 조회 실패:', err);
    userCards.value = [];
  }
};

const switchUser = (userId) => {
  currentUserId.value = userId;
  isPinAuthenticated.value = false;
  stopTimer();
  currentCardIndex.value = 0;
  fetchWallet();
  fetchUserCards();
};

const fetchWallet = async () => {
  loading.value = true;
  statusMessage.value = '';
  try {
    const data = await walletApi.getWalletByUserId(currentUserId.value);
    walletData.value = data;
  } catch (err) {
    console.error('Wallet fetch error:', err);
  } finally {
    loading.value = false;
  }
};

const openPaymentModal = (mode = 'barcode') => {
  selectedPaymentMode.value = mode;
  showPaymentModal.value = true;
};

const openChargeModal = () => {
  chargeForm.amount = 50000;
  chargeForm.memo = '';
  showChargeModal.value = true;
};

const addChargeAmount = (val) => {
  chargeForm.amount = (chargeForm.amount || 0) + val;
};

const executeCharge = async () => {
  charging.value = true;
  statusMessage.value = '';

  try {
    const payload = {
      userId: currentUserId.value,
      walletId: walletData.value?.walletId,
      amount: chargeForm.amount,
      bankCode: chargeForm.bankCode,
      accountNumber: chargeForm.accountNumber,
      memo: chargeForm.memo || 'KB Pay 연동계좌 충전',
    };

    await walletApi.chargeWallet(payload);
    isSuccess.value = true;
    statusMessage.value = `${Number(chargeForm.amount).toLocaleString('ko-KR')}원이 지갑에 충전되었습니다.`;
    showChargeModal.value = false;
    await fetchWallet();
  } catch (err) {
    console.error('Charge error:', err);
    isSuccess.value = false;
    statusMessage.value = err.response?.data?.message || '충전 실패: 입력 정보를 확인해주세요.';
  } finally {
    charging.value = false;
  }
};

onMounted(() => {
  fetchWallet();
  fetchUserCards();
});

onUnmounted(() => {
  stopTimer();
});
</script>

<style scoped>
.kb-container {
  max-width: 500px;
  margin: 0 auto;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
}
.user-avatar {
  width: 40px;
  height: 40px;
}
.kb-card {
  background: linear-gradient(135deg, #ffbc00 0%, #ffaa00 100%);
  border-radius: 24px;
  box-shadow: 0 12px 30px rgba(255, 188, 0, 0.3) !important;
}
.card-bg-circle {
  position: absolute;
  right: -20px;
  bottom: -30px;
  width: 180px;
  height: 180px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  pointer-events: none;
}
.quick-action-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.quick-action-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08) !important;
}
.icon-box {
  width: 44px;
  height: 44px;
}

/* 카드 덱 UI 스타일 */
.card-deck-wrapper {
  width: 260px;
  height: 150px;
}
.simple-card-box {
  border-radius: 14px !important;
}
.nav-arrow-btn {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #eee;
}
.indicator-dot {
  width: 8px;
  height: 8px;
}
.indicator-dot.active-dot {
  width: 18px;
  border-radius: 4px !important;
}

/* 점선 원 박스 (대표 카드 없음) */
.dotted-circle-box {
  width: 170px;
  height: 170px;
}

.spin-subtle {
  animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0% { opacity: 0.4; }
  50% { opacity: 1; }
  100% { opacity: 0.4; }
}

.kb-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  padding: 16px;
}
.kb-modal-content {
  width: 100%;
  max-width: 440px;
}
.spin-icon {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.cursor-pointer {
  cursor: pointer;
}
</style>
