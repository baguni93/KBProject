<template>
  <div class="wallet-root">

    <!-- ══════════════════════════════════════════
         상단 히어로 배너 (계정 요약 + 잔액)
    ══════════════════════════════════════════ -->
    <div class="hero-banner">
      <div class="hero-inner">
        <!-- 상단 타이틀 행 -->
        <div class="hero-top-row">
          <div>
            <p class="hero-sub">KB Pay · 디지털 지갑</p>
            <h2 class="hero-title">{{ activePaymentTab === 'CARD' ? '카드 결제' : '지갑 결제' }}</h2>
          </div>
          <button class="mode-toggle-btn" @click="toggleStartScreenMode">
            <i class="bi bi-pin-angle-fill"></i>
            <span>시작 화면: {{ startScreenMode === 'CARD' ? '카드결제' : '지갑결제' }}</span>
          </button>
        </div>

        <!-- 세그먼트 탭 -->
        <div class="segment-wrap">
          <button
            class="segment-btn"
            :class="{ active: activePaymentTab === 'CARD' }"
            @click="switchPaymentTab('CARD')"
          >
            <i class="bi bi-credit-card-2-front-fill"></i> 카드 결제
          </button>
          <button
            class="segment-btn"
            :class="{ active: activePaymentTab === 'WALLET' }"
            @click="switchPaymentTab('WALLET')"
          >
            <i class="bi bi-wallet2"></i> 지갑 결제
          </button>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════════
         본문 컨텐츠
    ══════════════════════════════════════════ -->
    <div class="page-body">

      <!-- ────────── [MODE A] 카드 결제 ────────── -->
      <div v-if="activePaymentTab === 'CARD'" class="card-section">

        <!-- 카드 없을 때 -->
        <div v-if="userCards.length === 0" class="empty-card-box">
          <div class="empty-icon-wrap">
            <i class="bi bi-credit-card-2-back-fill"></i>
          </div>
          <h5>등록된 카드가 없어요</h5>
          <p>신용/체크카드를 등록하고<br>간편하게 결제해 보세요</p>
          <button class="btn-primary-kb" @click="openAddCardModal">
            <i class="bi bi-plus-lg me-1"></i>카드 등록하기
          </button>
        </div>

        <!-- 카드 있을 때 -->
        <div v-else>
          <!-- 3D 카드 뷰어 -->
          <div class="card-stage">
            <!-- 왼쪽 이전 버튼 -->
            <button class="card-nav-btn" :disabled="currentCardIndex === 0" @click="prevCard">
              <i class="bi bi-chevron-left"></i>
            </button>

            <!-- 카드 플레이트 -->
            <div
              class="card-plate"
              :class="currentCard.representYn === 'Y' ? 'card-gold' : 'card-dark'"
            >
              <!-- 카드 상단 -->
              <div class="card-plate-top">
                <div class="card-chip-row">
                  <div class="chip-icon"></div>
                  <span class="card-name-label">{{ currentCard.cardName }}</span>
                </div>
                <div class="card-badge-wrap">
                  <span v-if="currentCard.representYn === 'Y'" class="rep-badge">
                    <i class="bi bi-star-fill"></i> 대표
                  </span>
                  <button
                    v-else
                    class="set-rep-btn"
                    @click.stop="makePrimaryCard(currentCard.cardId)"
                  >
                    대표 지정
                  </button>
                </div>
              </div>

              <!-- 카드 중단: 카드 번호 -->
              <div class="card-number-row">
                <span class="card-number">{{ currentCard.cardNum }}</span>
              </div>

              <!-- 카드 하단 -->
              <div class="card-plate-bottom">
                <span class="card-holder">{{ currentCard.holderName || 'KB PAY MEMBER' }}</span>
                <i class="bi bi-wifi nfc-icon"></i>
              </div>
            </div>

            <!-- 오른쪽 다음 버튼 -->
            <button class="card-nav-btn" :disabled="currentCardIndex === userCards.length - 1" @click="nextCard">
              <i class="bi bi-chevron-right"></i>
            </button>
          </div>

          <!-- 카드 인디케이터 -->
          <div class="card-dots">
            <span
              v-for="(c, i) in userCards"
              :key="i"
              class="card-dot"
              :class="{ active: i === currentCardIndex }"
            ></span>
          </div>

          <!-- 카드 정보 + 액션 -->
          <div class="card-actions-box">
            <div class="card-actions-row">
              <button class="card-action-btn" @click="openAddCardModal">
                <span class="action-icon-wrap"><i class="bi bi-plus-circle-fill"></i></span>
                <span>카드 추가</span>
              </button>
              <div class="action-divider"></div>
              <button class="card-action-btn" @click="openPinModal">
                <span class="action-icon-wrap accent"><i class="bi bi-contactless"></i></span>
                <span>결제 시작</span>
              </button>
            </div>
          </div>

          <!-- 결제 메인 버튼 -->
          <button class="big-pay-btn" @click="openPinModal">
            <i class="bi bi-credit-card-fill me-2"></i>
            무선 카드 결제 시작하기
            <i class="bi bi-arrow-right ms-2"></i>
          </button>
        </div>
      </div>

      <!-- ────────── [MODE B] 전자지갑 ────────── -->
      <div v-else class="wallet-section">

        <!-- 잔액 카드 -->
        <div class="balance-card">
          <div class="balance-card-top">
            <div class="balance-badge">
              <i class="bi bi-wallet2 me-1"></i> KB Pay Wallet
            </div>
            <button class="refresh-btn" @click="fetchWallet">
              <i class="bi bi-arrow-clockwise"></i>
            </button>
          </div>

          <div class="balance-display">
            <p class="balance-label">내 지갑 잔액</p>
            <h1 class="balance-amount">{{ formatCurrency(walletData?.balance) }}</h1>
          </div>

          <div class="balance-actions">
            <button class="balance-action-btn charge" @click="openChargeModal">
              <i class="bi bi-plus-lg me-1"></i> 충전하기
            </button>
            <router-link
              :to="{ path: '/remittance', query: { walletId: walletData?.walletId || 1 } }"
              class="balance-action-btn send"
            >
              <i class="bi bi-send-fill me-1"></i> 송금하기
            </router-link>
          </div>
        </div>

        <!-- 빠른 결제 섹션 -->
        <div class="section-header">
          <h6 class="section-title">빠른 결제</h6>
        </div>

        <div class="quick-pay-grid">
          <!-- 바코드 결제 -->
<!-- 바코드 결제 -->
          <button class="quick-pay-card" @click="openPaymentModal('barcode')">
            <div class="qp-icon-wrap amber">
              <img src="@/assets/barcode_icon.jpg" alt="바코드" class="pay-icon" />
            </div>
            <div class="qp-text">
              <strong>바코드 결제</strong>
              <span>보안 바코드 생성</span>
            </div>
          </button>

          <!-- QR코드 결제 -->
          <button class="quick-pay-card" @click="openPaymentModal('qr')">
            <div class="qp-icon-wrap blue">
              <img src="@/assets/qr_icon.jpg" alt="QR코드" class="pay-icon" />
            </div>
            <div class="qp-text">
              <strong>QR 코드 결제</strong>
              <span>보안 QR코드 생성</span>
            </div>
          </button>
        </div>

        <!-- 안내 배너 -->
        <div class="info-banner">
          <i class="bi bi-shield-check info-icon"></i>
          <div>
            <p class="info-title">보안 결제 시스템</p>
            <p class="info-desc">결제 코드는 1분마다 자동 갱신되어 안전합니다</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 알림 메시지 (토스트) -->
    <transition name="toast">
      <div v-if="statusMessage" :class="['toast-msg', isSuccess ? 'success' : 'error']">
        <i :class="['bi me-2', isSuccess ? 'bi-check-circle-fill' : 'bi-exclamation-circle-fill']"></i>
        <span>{{ statusMessage }}</span>
        <button class="toast-close" @click="statusMessage = ''">
          <i class="bi bi-x"></i>
        </button>
      </div>
    </transition>

    <!-- ══════════════════════════════════════════
         충전 바텀시트
    ══════════════════════════════════════════ -->
    <div v-if="showChargeModal" class="backdrop" @click.self="showChargeModal = false">
      <div class="bottom-sheet animate-up">
        <div class="sheet-handle" @click="showChargeModal = false"></div>

        <div class="sheet-header">
          <div class="sheet-title-wrap">
            <div class="sheet-icon yellow"><i class="bi bi-lightning-fill"></i></div>
            <h5 class="sheet-title">지갑 충전</h5>
          </div>
          <button class="btn-close shadow-none" @click="showChargeModal = false"></button>
        </div>

        <form @submit.prevent="executeCharge" class="sheet-body">
          <div class="form-group-kb">
            <label class="form-label-kb">출금 계좌</label>
            <select v-model="chargeForm.bankCode" class="select-kb">
              <option value="004">KB국민은행 (123-456-7890)</option>
              <option value="088">신한은행 (110-234-5678)</option>
              <option value="020">우리은행 (1002-345-6789)</option>
              <option value="011">NH농협은행 (302-1234-5678)</option>
              <option value="090">카카오뱅크 (3333-01-23456)</option>
            </select>
          </div>

          <div class="form-group-kb">
            <label class="form-label-kb">충전 금액</label>
            <div class="amount-input-wrap">
              <input
                type="number"
                v-model.number="chargeForm.amount"
                class="amount-input"
                placeholder="0"
                min="1000"
                step="1000"
                required
              />
              <span class="amount-unit">원</span>
            </div>
            <div class="quick-amount-row">
              <button type="button" class="quick-amt-btn" @click="addChargeAmount(10000)">+1만</button>
              <button type="button" class="quick-amt-btn" @click="addChargeAmount(30000)">+3만</button>
              <button type="button" class="quick-amt-btn" @click="addChargeAmount(50000)">+5만</button>
              <button type="button" class="quick-amt-btn" @click="addChargeAmount(100000)">+10만</button>
            </div>
          </div>

          <div class="form-group-kb">
            <label class="form-label-kb">메모 <span class="optional-tag">선택</span></label>
            <input type="text" v-model="chargeForm.memo" class="input-kb" placeholder="예: 용돈 충전" />
          </div>

          <button type="submit" class="submit-btn" :disabled="charging">
            <span v-if="charging" class="spinner-border spinner-border-sm me-2"></span>
            <i v-else class="bi bi-lightning-fill me-2"></i>
            충전 완료하기
          </button>
        </form>
      </div>
    </div>

    <!-- ══════════════════════════════════════════
         카드 등록 바텀시트
    ══════════════════════════════════════════ -->
    <div v-if="showAddCardModal" class="backdrop" @click.self="showAddCardModal = false">
      <div class="bottom-sheet animate-up">
        <div class="sheet-handle" @click="showAddCardModal = false"></div>

        <div class="sheet-header">
          <div class="sheet-title-wrap">
            <div class="sheet-icon dark"><i class="bi bi-credit-card-fill"></i></div>
            <h5 class="sheet-title">카드 등록</h5>
          </div>
          <button class="btn-close shadow-none" @click="showAddCardModal = false"></button>
        </div>

        <form @submit.prevent="submitAddCard" class="sheet-body">
          <div class="form-group-kb">
            <label class="form-label-kb">카드 상품</label>
            <select v-model="newCardForm.cardName" class="select-kb">
              <option value="KB국민 My WE:SH 카드">KB국민 My WE:SH 카드</option>
              <option value="KB국민 톡톡O 카드">KB국민 톡톡O 카드</option>
              <option value="KB국민 노리2 체크카드">KB국민 노리2 체크카드</option>
              <option value="KB국민 직장인보너스체크카드">KB국민 직장인보너스체크카드</option>
            </select>
          </div>

          <div class="form-group-kb">
            <label class="form-label-kb">카드 번호 (16자리)</label>
            <input
              type="text"
              v-model="newCardForm.cardNum"
              class="input-kb font-monospace"
              placeholder="9410-1234-5678-0000"
              required
            />
          </div>

          <div class="form-row-2">
            <div class="form-group-kb">
              <label class="form-label-kb">유효기간</label>
              <input type="text" v-model="newCardForm.expiryDate" class="input-kb text-center" placeholder="MM/YY" required />
            </div>
            <div class="form-group-kb">
              <label class="form-label-kb">CVC</label>
              <input type="password" v-model="newCardForm.cvv" class="input-kb text-center" maxlength="3" placeholder="•••" required />
            </div>
          </div>

          <div class="form-group-kb">
            <label class="form-label-kb">카드 비밀번호 앞 2자리</label>
            <input type="password" v-model="newCardForm.cardPassword" class="input-kb" maxlength="4" placeholder="••••" required />
          </div>

          <button type="submit" class="submit-btn">
            <i class="bi bi-shield-check me-2"></i>
            카드 안전 등록하기
          </button>
        </form>
      </div>
    </div>

    <!-- 결제 코드 모달 -->
    <PaymentCodeModal
      v-if="showPaymentModal"
      :userId="currentUserId"
      :initialTab="selectedPaymentMode"
      @close="showPaymentModal = false"
      @paymentSuccess="fetchWallet"
    />

    <!-- PIN 인증 모달 -->
    <PinAuthModal
      :show="showPinModal"
      :userId="currentUserId"
      @close="showPinModal = false"
      @success="handlePinSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import walletApi from '@/api/walletApi';
import cardApi from '@/api/cardApi';
import PaymentCodeModal from '@/components/wallet/PaymentCodeModal.vue';
import PinAuthModal from '@/components/auth/PinAuthModal.vue';
import WalletSection from '@/components/wallet/WalletSection.vue';

const authStore = useAuthStore();
const currentUserId = computed(() => authStore.userId || 1);
const walletData = ref(null);
const cardStatus = ref(null);
const primaryCard = ref(null);
const loading = ref(false);
const charging = ref(false);
const showChargeModal = ref(false);
const showPaymentModal = ref(false);
const showPinModal = ref(false);
const showAddCardModal = ref(false);
const isPinAuthenticated = ref(false);

const selectedPaymentMode = ref('barcode');
const statusMessage = ref('');
const isSuccess = ref(true);

const startScreenMode = ref(localStorage.getItem('kb_pay_start_mode') || 'WALLET');
const activePaymentTab = ref(startScreenMode.value === 'CARD' ? 'CARD' : 'WALLET');

const currentCardIndex = ref(0);
const userCards = ref([]);

const currentCard = computed(() => userCards.value[currentCardIndex.value] || {});

const timerSeconds = ref(60);
let timerInterval = null;

const startTimer = () => {
  stopTimer();
  timerSeconds.value = 60;
  timerInterval = setInterval(() => {
    if (timerSeconds.value > 0) timerSeconds.value--;
    else timerSeconds.value = 60;
  }, 1000);
};

const stopTimer = () => {
  if (timerInterval) { clearInterval(timerInterval); timerInterval = null; }
};

const openPinModal = () => { showPinModal.value = true; };

const handlePinSuccess = () => {
  isPinAuthenticated.value = true;
  showPinModal.value = false;
  statusMessage.value = 'PIN 인증 성공! 무선 결제가 시작되었습니다.';
  isSuccess.value = true;
  startTimer();
  setTimeout(() => { statusMessage.value = ''; }, 3000);
};

const toggleStartScreenMode = () => {
  const nextMode = startScreenMode.value === 'WALLET' ? 'CARD' : 'WALLET';
  startScreenMode.value = nextMode;
  activePaymentTab.value = nextMode;
  localStorage.setItem('kb_pay_start_mode', nextMode);
  if (nextMode === 'CARD') startTimer();
  else stopTimer();
  statusMessage.value = `기본 시작 화면이 '${nextMode === 'CARD' ? '카드 결제' : '지갑 결제'}'로 설정되었습니다.`;
  isSuccess.value = true;
  setTimeout(() => { statusMessage.value = ''; }, 2500);
};

const switchPaymentTab = (tab) => {
  activePaymentTab.value = tab;
  if (tab === 'CARD') startTimer();
  else stopTimer();
};

const prevCard = () => { if (currentCardIndex.value > 0) currentCardIndex.value--; };
const nextCard = () => { if (currentCardIndex.value < userCards.value.length - 1) currentCardIndex.value++; };

const newCardForm = reactive({ cardName: 'KB국민 My WE:SH 카드', cardNum: '', expiryDate: '', cvv: '', cardPassword: '' });

const openAddCardModal = () => {
  newCardForm.cardName = 'KB국민 My WE:SH 카드';
  newCardForm.cardNum = '';
  newCardForm.expiryDate = '';
  newCardForm.cvv = '';
  newCardForm.cardPassword = '';
  showAddCardModal.value = true;
};

const submitAddCard = async () => {
  try {
    const payload = {
      userId: currentUserId.value,
      accountId: 1,
      cardNum: newCardForm.cardNum,
      expiryDate: newCardForm.expiryDate,
      cvv: newCardForm.cvv,
      cardPassword: newCardForm.cardPassword,
      cardName: newCardForm.cardName,
      representYn: userCards.value.length === 0 ? 'Y' : 'N',
    };
    await cardApi.registerCard(payload);
    statusMessage.value = `'${newCardForm.cardName}' 등록 완료!`;
    isSuccess.value = true;
    showAddCardModal.value = false;
    await fetchCardStatusAndCards();
    setTimeout(() => { statusMessage.value = ''; }, 3000);
  } catch (err) {
    console.error('카드 등록 실패:', err);
    statusMessage.value = '카드 등록 중 오류가 발생했습니다.';
    isSuccess.value = false;
  }
};

const makePrimaryCard = async (cardId) => {
  try {
    await cardApi.setPrimaryCard(cardId, currentUserId.value);
    statusMessage.value = '대표 카드가 변경되었습니다!';
    isSuccess.value = true;
    await fetchCardStatusAndCards();
    setTimeout(() => { statusMessage.value = ''; }, 2500);
  } catch (err) {
    statusMessage.value = '대표 카드 변경 실패';
    isSuccess.value = false;
  }
};

const chargeForm = reactive({ bankCode: '004', accountNumber: '123-456-7890', amount: 50000, memo: '' });

const formatCurrency = (val) => {
  if (val === undefined || val === null) return '₩0';
  return '₩' + Number(val).toLocaleString('ko-KR');
};

const formatCardNumDisplay = (raw) => {
  if (!raw) return '•••• 9182';
  const clean = String(raw).replace(/\D/g, '');
  return clean.length >= 4 ? '•••• •••• •••• ' + clean.slice(-4) : raw;
};

const fetchCardStatusAndCards = async () => {
  try {
    const statusRes = await cardApi.getCardStatus(currentUserId.value);
    cardStatus.value = statusRes;
    if (statusRes && statusRes.hasRegisteredCard) {
      const dbCards = await walletApi.getUserCards(currentUserId.value);
      if (dbCards && dbCards.length > 0) {
        userCards.value = dbCards.map(c => ({
          cardId: c.cardId,
          cardName: c.cardName || 'KB국민 Custom Card',
          holderName: c.holderName || '김국민',
          cardNum: formatCardNumDisplay(c.cardNum),
          representYn: c.representYn || 'N',
        }));
        const primaryRes = await cardApi.getPrimaryCard(currentUserId.value);
        primaryCard.value = primaryRes;
        if (primaryRes && primaryRes.cardId) {
          const idx = userCards.value.findIndex(c => c.cardId === primaryRes.cardId);
          if (idx !== -1) currentCardIndex.value = idx;
        }
      } else { userCards.value = []; }
    } else { userCards.value = []; primaryCard.value = null; }
  } catch (err) {
    console.error('카드 조회 실패:', err);
    userCards.value = [];
    primaryCard.value = null;
  }
};

const fetchWallet = async () => {
  loading.value = true;
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

const addChargeAmount = (val) => { chargeForm.amount = (chargeForm.amount || 0) + val; };

const executeCharge = async () => {
  charging.value = true;
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
    statusMessage.value = `${Number(chargeForm.amount).toLocaleString('ko-KR')}원 충전 완료!`;
    showChargeModal.value = false;
    await fetchWallet();
    setTimeout(() => { statusMessage.value = ''; }, 3000);
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
  fetchCardStatusAndCards();
});

onUnmounted(() => { stopTimer(); });
</script>

<style scoped>
/* ═══════════════════════════════════════
   루트 레이아웃
═══════════════════════════════════════ */
.wallet-root {
  min-height: 100vh;
  background: #FFFFFF;
  padding-bottom: 80px;
}

/* ═══════════════════════════════════════
   히어로 배너 (밝은 KB 톤)
═══════════════════════════════════════ */
.hero-banner {
  background: linear-gradient(145deg, #FFD700 0%, #FFBC00 40%, #FFA500 100%);
  border-bottom: none;
  padding: 36px 20px 20px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(255, 188, 0, 0.3);
}
.hero-banner::before {
  content: '';
  position: absolute;
  top: -60px;
  right: -40px;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255,188,0,0.25) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}
.hero-inner {
  max-width: 480px;
  margin: 0 auto;
}
.hero-top-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.hero-sub {
  color: rgba(26, 26, 46, 0.65);
  font-size: 0.75rem;
  margin-bottom: 4px;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.hero-title {
  color: #1A1A2E;
  font-size: 1.45rem;
  font-weight: 900;
  margin: 0;
  letter-spacing: -0.5px;
}
.mode-toggle-btn {
  background: rgba(255,255,255,0.7);
  border: none;
  color: #1A1A2E;
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 0.76rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  backdrop-filter: blur(4px);
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  z-index: 10;
}
.mode-toggle-btn:hover {
  background: rgba(255,255,255,0.9);
  transform: translateY(-1px);
}
.mode-toggle-btn:active {
  transform: translateY(0);
}

/* 세그먼트 탭 */
.segment-wrap {
  display: flex;
  background: rgba(255,255,255,0.35);
  border-radius: 12px;
  padding: 4px;
  gap: 4px;
  backdrop-filter: blur(4px);
}
.segment-btn {
  flex: 1;
  padding: 10px 0;
  border: none;
  background: transparent;
  color: rgba(26,26,46,0.6);
  border-radius: 9px;
  font-size: 0.85rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.22s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.segment-btn.active {
  background: #ffffff;
  color: #1A1A2E;
  box-shadow: 0 3px 12px rgba(0,0,0,0.12);
  font-weight: 800;
}

/* ═══════════════════════════════════════
   본문
═══════════════════════════════════════ */
.page-body {
  max-width: 480px;
  margin: 0 auto;
  padding: 20px 16px 0;
}

/* ═══════════════════════════════════════
   카드 섹션
═══════════════════════════════════════ */
.card-section { display: flex; flex-direction: column; gap: 16px; }

/* 카드 없음 */
.empty-card-box {
  background: #fff;
  border-radius: 20px;
  padding: 48px 24px;
  text-align: center;
  box-shadow: 0 2px 16px rgba(0,0,0,0.06);
}
.empty-icon-wrap {
  width: 72px; height: 72px;
  background: #FFF8E1;
  border-radius: 20px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px;
  font-size: 2rem;
  color: #FFBC00;
}
.empty-card-box h5 { font-size: 1.1rem; font-weight: 800; color: #1A1A2E; margin-bottom: 8px; }
.empty-card-box p { color: #94A3B8; font-size: 0.875rem; line-height: 1.6; margin-bottom: 24px; }

/* 카드 스테이지 */
.card-stage {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.card-nav-btn {
  width: 36px; height: 36px;
  border-radius: 50%;
  border: none;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  color: #1A1A2E;
  transition: all 0.2s ease;
  flex-shrink: 0;
}
.card-nav-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.card-nav-btn:not(:disabled):hover { background: #FFBC00; box-shadow: 0 4px 16px rgba(255,188,0,0.3); }

/* 카드 플레이트 */
.card-plate {
  width: 280px;
  height: 170px;
  border-radius: 20px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 16px 40px rgba(0,0,0,0.25);
  transition: transform 0.2s ease;
}
.card-plate:hover { transform: translateY(-3px); }

.card-gold {
  background: linear-gradient(135deg, #1A1A2E 0%, #2D2D4A 50%, #1A1A2E 100%);
  border: 1.5px solid rgba(255,188,0,0.6);
}
.card-gold::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,188,0,0.08) 0%, transparent 60%);
}

.card-dark {
  background: linear-gradient(135deg, #2D3748 0%, #1A202C 100%);
  border: 1px solid rgba(255,255,255,0.1);
}

.card-plate-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  z-index: 1;
}
.card-chip-row { display: flex; align-items: center; gap: 10px; }
.chip-icon {
  width: 28px; height: 20px;
  background: linear-gradient(135deg, #FFE082 0%, #FFB300 100%);
  border-radius: 4px;
}
.card-name-label { color: rgba(255,255,255,0.85); font-size: 0.72rem; font-weight: 700; max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-badge-wrap { display: flex; align-items: center; }
.rep-badge {
  background: rgba(255,188,0,0.2);
  color: #FFBC00;
  border: 1px solid rgba(255,188,0,0.4);
  border-radius: 20px;
  padding: 2px 10px;
  font-size: 0.68rem;
  font-weight: 800;
  display: flex; align-items: center; gap: 4px;
}
.set-rep-btn {
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  color: rgba(255,255,255,0.7);
  border-radius: 20px;
  padding: 2px 10px;
  font-size: 0.68rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.set-rep-btn:hover { background: rgba(255,188,0,0.2); border-color: #FFBC00; color: #FFBC00; }

.card-number-row {
  text-align: center;
  position: relative;
  z-index: 1;
}
.card-number {
  color: rgba(255,255,255,0.95);
  font-size: 1.1rem;
  font-weight: 700;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
}

.card-plate-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}
.card-holder { color: rgba(255,255,255,0.6); font-size: 0.7rem; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; }
.nfc-icon { color: #FFBC00; font-size: 1.2rem; }

/* 카드 인디케이터 */
.card-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 12px;
}
.card-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #CBD5E1;
  transition: all 0.2s ease;
}
.card-dot.active {
  background: #FFBC00;
  width: 18px;
  border-radius: 3px;
}

/* 카드 액션 박스 */
.card-actions-box {
  background: #fff;
  border-radius: 16px;
  padding: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.card-actions-row {
  display: flex;
  align-items: center;
}
.card-action-btn {
  flex: 1;
  background: none;
  border: none;
  cursor: pointer;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 0.78rem;
  font-weight: 700;
  color: #475569;
  transition: all 0.2s ease;
  border-radius: 12px;
}
.card-action-btn:hover { background: #F8FAFC; color: #1A1A2E; }
.action-icon-wrap {
  width: 40px; height: 40px;
  border-radius: 12px;
  background: #F1F5F9;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.1rem;
  color: #475569;
}
.action-icon-wrap.accent {
  background: #FFF8E1;
  color: #FFBC00;
}
.action-divider {
  width: 1px; height: 40px;
  background: #E2E8F0;
}

/* 결제 메인 버튼 */
.big-pay-btn {
  width: 100%;
  background: linear-gradient(135deg, #FFBC00 0%, #FF9900 100%);
  color: #1A1A2E;
  border: none;
  border-radius: 16px;
  padding: 18px;
  font-size: 1rem;
  font-weight: 800;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(255,188,0,0.35);
  transition: all 0.2s ease;
  letter-spacing: -0.3px;
}
.big-pay-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(255,188,0,0.45);
}
.big-pay-btn:active { transform: translateY(0); }

/* ═══════════════════════════════════════
   공통 버튼
═══════════════════════════════════════ */
.btn-primary-kb {
  background: linear-gradient(135deg, #FFBC00 0%, #FF9900 100%);
  color: #1A1A2E;
  border: none;
  border-radius: 14px;
  padding: 14px 32px;
  font-size: 0.9rem;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(255,188,0,0.3);
  transition: all 0.2s ease;
}
.btn-primary-kb:hover { transform: translateY(-2px); box-shadow: 0 10px 28px rgba(255,188,0,0.4); }

/* ═══════════════════════════════════════
   지갑 섹션
═══════════════════════════════════════ */
.wallet-section { display: flex; flex-direction: column; gap: 16px; }

/* ── KB국민은행 스타일 실물 카드 ── */
.balance-card {
  background: linear-gradient(135deg, #342E28 0%, #1D1A17 100%);
  border: 1px solid rgba(255, 188, 0, 0.3);
  border-radius: 20px;
  padding: 22px 24px 20px;
  box-shadow: 0 10px 30px rgba(29, 26, 23, 0.35), 0 0 20px rgba(255, 188, 0, 0.12);
  position: relative;
  overflow: hidden;
  color: #FFFFFF;
  min-height: 180px;
  display: flex;
  flex-direction: column;
}
/* 카드 배경 원형 데코 (KB 골드 앰비언트 글로우) */
.balance-card::before {
  content: '';
  position: absolute;
  top: -50px; right: -50px;
  width: 200px; height: 200px;
  background: radial-gradient(circle, rgba(255,188,0,0.22) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}
.balance-card::after {
  content: '';
  position: absolute;
  bottom: -40px; left: -30px;
  width: 150px; height: 150px;
  background: radial-gradient(circle, rgba(255,188,0,0.12) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.balance-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.balance-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.82rem;
  font-weight: 900;
  color: #FFFFFF;
  letter-spacing: 0.3px;
}
.balance-badge i {
  font-size: 1rem;
  color: #FFBC00;
}
.refresh-btn {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: rgba(255, 255, 255, 0.8);
  width: 32px; height: 32px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s ease;
}
.refresh-btn:hover { background: rgba(255, 255, 255, 0.25); color: #fff; }

.balance-display {
  flex: 1;
  position: relative;
  z-index: 1;
  margin-bottom: 20px;
}
.balance-label {
  color: rgba(255, 255, 255, 0.65);
  font-size: 0.75rem;
  font-weight: 600;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.8px;
}
.balance-amount {
  color: #FFFFFF;
  font-size: 2.2rem;
  font-weight: 900;
  margin: 0;
  letter-spacing: -1px;
  line-height: 1;
  text-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.balance-actions {
  display: flex;
  gap: 10px;
  position: relative;
  z-index: 1;
}
.balance-action-btn {
  flex: 1;
  border: none;
  border-radius: 12px;
  padding: 12px;
  font-size: 0.85rem;
  font-weight: 800;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  transition: all 0.2s ease;
}
.balance-action-btn.charge {
  background: #FFBC00;
  color: #1A1A2E;
  box-shadow: 0 4px 14px rgba(255,188,0,0.4);
}
.balance-action-btn.charge:hover { transform: translateY(-1px); background: #e6a800; }
.balance-action-btn.send {
  background: rgba(255,255,255,0.18);
  color: #FFFFFF;
  border: 1.5px solid rgba(255,255,255,0.3);
}
.balance-action-btn.send:hover { background: rgba(255,255,255,0.28); }

/* 섹션 헤더 */
.section-header { padding: 0 4px; }
.section-title { font-size: 1rem; font-weight: 800; color: #1A1A2E; margin: 0; }

/* 빠른 결제 그리드 (가로 2열 배치) */
.quick-pay-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.quick-pay-card {
  background: linear-gradient(135deg, #F9FAFB 0%, #FFFFFF 100%);
  border: 1.5px solid #F1F5F9;
  border-radius: 18px;
  padding: 18px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 4px 14px rgba(0,0,0,0.04);
  width: 100%;
  text-align: center;
}
.quick-pay-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  border-color: #FFBC00;
}

.qp-icon-wrap {
  width: 56px; height: 56px;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.8rem;
  flex-shrink: 0;
}
.qp-icon-wrap.amber {
  background: linear-gradient(135deg, #FFBC00 0%, #FF9900 100%);
  color: #1A1A2E;
  border: 2px solid #FFBC00;
  box-shadow: 0 4px 12px rgba(255,188,0,0.25);
}

.qp-icon-wrap.blue {
  background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
  color: #1D4ED8;
  border: 2px solid #93C5FD;
  box-shadow: 0 4px 12px rgba(59,130,246,0.2);
}

.qp-text { text-align: center; }

.pay-icon {
  width: 48px;
  height: 48px;
  object-fit: contain;
  display: block;
}
.qp-text strong { display: block; font-size: 0.86rem; font-weight: 800; color: #1A1A2E; margin-bottom: 2px; }
.qp-text span { font-size: 0.72rem; color: #94A3B8; font-weight: 600; }
.qp-arrow { color: #CBD5E1; font-size: 0.9rem; }

/* 안내 배너 */
.info-banner {
  background: #EFF6FF;
  border: 1px solid #DBEAFE;
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.info-icon { font-size: 1.2rem; color: #3B82F6; flex-shrink: 0; margin-top: 2px; }
.info-title { font-size: 0.82rem; font-weight: 800; color: #1E3A5F; margin-bottom: 2px; }
.info-desc { font-size: 0.73rem; color: #64748B; margin: 0; }

/* ═══════════════════════════════════════
   토스트 메시지
═══════════════════════════════════════ */
.toast-msg {
  position: fixed;
  top: 20px; left: 50%;
  transform: translateX(-50%);
  z-index: 2000;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 40px;
  font-size: 0.85rem;
  font-weight: 700;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
  min-width: 220px;
  max-width: 340px;
  white-space: nowrap;
}
.toast-msg.success { background: #1A1A2E; color: #FFBC00; }
.toast-msg.error { background: #FEF2F2; color: #EF4444; border: 1px solid #FCA5A5; }
.toast-close { background: none; border: none; cursor: pointer; font-size: 1.1rem; opacity: 0.6; margin-left: auto; padding: 0; color: inherit; }

.toast-enter-active, .toast-leave-active { transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(-12px); }

/* ═══════════════════════════════════════
   바텀시트 공통
═══════════════════════════════════════ */
.backdrop {
  position: fixed; inset: 0;
  background: rgba(15,23,42,0.6);
  backdrop-filter: blur(8px);
  display: flex; align-items: flex-end; justify-content: center;
  z-index: 1060;
}
.bottom-sheet {
  background: #fff;
  width: 100%; max-width: 480px;
  border-top-left-radius: 28px;
  border-top-right-radius: 28px;
  padding-bottom: max(env(safe-area-inset-bottom), 24px);
  max-height: 92vh;
  overflow-y: auto;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.animate-up { animation: slideUp 0.35s cubic-bezier(0.16, 1, 0.3, 1) forwards; }

.sheet-handle {
  width: 40px; height: 5px;
  background: #E2E8F0;
  border-radius: 3px;
  margin: 14px auto 0;
  cursor: pointer;
}
.sheet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 4px;
}
.sheet-title-wrap { display: flex; align-items: center; gap: 12px; }
.sheet-icon {
  width: 40px; height: 40px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.1rem;
}
.sheet-icon.yellow { background: #FFF8E1; color: #FFBC00; }
.sheet-icon.dark { background: #F1F5F9; color: #1A1A2E; }
.sheet-title { font-size: 1.05rem; font-weight: 800; color: #1A1A2E; margin: 0; }
.sheet-body { padding: 16px 20px 0; display: flex; flex-direction: column; gap: 16px; }

/* 폼 요소 */
.form-group-kb { display: flex; flex-direction: column; gap: 6px; }
.form-label-kb { font-size: 0.78rem; font-weight: 700; color: #64748B; }
.optional-tag { font-weight: 500; color: #94A3B8; margin-left: 4px; }
.input-kb {
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  padding: 13px 16px;
  font-size: 0.95rem;
  font-weight: 600;
  color: #1A1A2E;
  outline: none;
  transition: border-color 0.2s ease;
  background: #FAFAFA;
  width: 100%;
}
.input-kb:focus { border-color: #FFBC00; background: #fff; box-shadow: 0 0 0 3px rgba(255,188,0,0.12); }
.select-kb {
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  padding: 13px 16px;
  font-size: 0.88rem;
  font-weight: 600;
  color: #1A1A2E;
  outline: none;
  background: #FAFAFA;
  cursor: pointer;
  transition: border-color 0.2s ease;
  width: 100%;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24'%3E%3Cpath fill='%2394A3B8' d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
}
.select-kb:focus { border-color: #FFBC00; box-shadow: 0 0 0 3px rgba(255,188,0,0.12); }

.amount-input-wrap { position: relative; }
.amount-input {
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  padding: 13px 50px 13px 16px;
  font-size: 1.4rem;
  font-weight: 900;
  color: #1A1A2E;
  outline: none;
  background: #FAFAFA;
  width: 100%;
  transition: border-color 0.2s ease;
}
.amount-input:focus { border-color: #FFBC00; background: #fff; box-shadow: 0 0 0 3px rgba(255,188,0,0.12); }
.amount-unit {
  position: absolute; right: 16px; top: 50%;
  transform: translateY(-50%);
  font-size: 0.9rem; font-weight: 700; color: #94A3B8;
}

.quick-amount-row { display: flex; gap: 6px; }
.quick-amt-btn {
  flex: 1;
  background: #F8FAFC;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  padding: 8px 0;
  font-size: 0.78rem;
  font-weight: 700;
  color: #475569;
  cursor: pointer;
  transition: all 0.15s ease;
}
.quick-amt-btn:hover { background: #FFF8E1; border-color: #FFBC00; color: #1A1A2E; }
.quick-amt-btn:active { transform: scale(0.96); }

.form-row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }

.submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #FFBC00 0%, #FF9900 100%);
  color: #1A1A2E;
  border: none;
  border-radius: 16px;
  padding: 17px;
  font-size: 1rem;
  font-weight: 800;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(255,188,0,0.35);
  transition: all 0.2s ease;
  margin-top: 4px;
  margin-bottom: 8px;
}
.submit-btn:hover { transform: translateY(-2px); box-shadow: 0 12px 32px rgba(255,188,0,0.45); }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }
</style>
