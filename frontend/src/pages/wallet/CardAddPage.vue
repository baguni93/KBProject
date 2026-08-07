<template>
  <div class="card-add-root">

    <!-- ══════════════════════════════════════════
         상단 고정 헤더
    ══════════════════════════════════════════ -->
    <div class="card-add-header">
      <div class="header-inner">
        <button class="back-btn" @click="$router.push('/wallet')">
          <i class="bi bi-chevron-left me-1"></i> 지갑
        </button>
        <h4 class="header-title">KB국민카드 등록</h4>
        <button class="close-x-btn" @click="$router.push('/wallet')" title="닫기">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
    </div>

    <!-- ══════════════════════════════════════════
         본문 폼 (중간 가변 스크롤 영역)
    ══════════════════════════════════════════ -->
    <div class="card-add-body">
      <div class="form-card">
        
        <!-- KB국민카드 실물 3D 프리뷰 플레이트 (실물 카드 이미지 & 카드 별칭 실시간 반영) -->
        <div class="card-preview-plate">
          <div class="plate-background-overlay"></div>
          
          <div class="card-plate-header">
            <div class="chip-ic"></div>
            <span class="kb-card-badge"><i class="bi bi-shield-fill-check me-1"></i>KB국민카드</span>
          </div>

          <div class="real-card-img-wrap my-2">
            <img
              v-if="cardPreviewImg"
              :src="cardPreviewImg"
              :alt="cardForm.cardName"
              class="real-card-img fade-in"
              @error="handleImgError"
            />
          </div>

          <div class="card-preview-num">{{ cardForm.cardNum || '**** **** **** ****' }}</div>
          
          <div class="d-flex justify-content-between align-items-center mt-2">
            <span class="preview-name">{{ cardForm.cardAlias || cardForm.cardName || 'KB국민카드 상품을 선택하세요' }}</span>
            <span class="preview-expiry">{{ cardForm.expiry || 'MM/YY' }}</span>
          </div>
        </div>

        <form id="cardAddForm" @submit.prevent="submitCard">
          
          <!-- KB국민카드 전용 상품 선택 드롭다운 -->
          <div class="form-group mb-3">
            <label class="form-label">
              <i class="bi bi-credit-card-2-front-fill me-1 text-warning"></i>KB국민카드 상품 및 디자인 선택
            </label>
            <select v-model="cardForm.cardName" class="kb-select-field" required @change="updatePreviewImg">
              <option value="" disabled selected>등록할 KB국민카드 상품을 선택하세요</option>
              <option v-for="opt in kbCardOptions" :key="opt" :value="opt">
                {{ opt }}
              </option>
            </select>
          </div>

          <!-- 카드 별칭 (카드 이미지를 위한 사용자 정의 카드 이름) -->
          <div class="form-group mb-3">
            <label class="form-label">
              <i class="bi bi-tag-fill me-1 text-primary"></i>카드 별칭 (지갑 표기용 이름)
            </label>
            <input
              v-model="cardForm.cardAlias"
              type="text"
              class="kb-input"
              placeholder="예: 메인 KB 체크카드, 생활비 전용 카드"
              maxLength="20"
            />
          </div>

          <!-- 카드 번호 -->
          <div class="form-group mb-3">
            <label class="form-label">카드 번호 (16자리)</label>
            <input
              v-model="cardForm.cardNum"
              type="text"
              class="kb-input"
              placeholder="'-' 없이 16자리 숫자 입력"
              maxLength="19"
              required
              @input="formatCardNum"
            />
          </div>

          <div class="row g-2 mb-3">
            <div class="col-4">
              <label class="form-label">유효기간</label>
              <input
                v-model="cardForm.expiry"
                type="text"
                class="kb-input"
                placeholder="MM/YY"
                maxLength="5"
                required
                @input="formatExpiry"
              />
            </div>
            <div class="col-4">
              <label class="form-label">CVC 보안코드</label>
              <input
                v-model="cardForm.cvc"
                type="password"
                class="kb-input"
                placeholder="3자리"
                maxLength="3"
                required
              />
            </div>
            <div class="col-4">
              <label class="form-label">카드 비밀번호</label>
              <input
                v-model="cardForm.cardPassword"
                type="password"
                class="kb-input"
                placeholder="앞 2자리"
                maxLength="4"
                required
              />
            </div>
          </div>

          <!-- 보안 숫자 키패드 (HTML 캔버스 Screen 2-1 맞춤) -->
          <div class="keypad-container my-3 p-2 bg-light rounded-3 border">
            <div class="text-center text-muted small fw-bold mb-2">
              <i class="bi bi-shield-lock-fill text-warning me-1"></i>보안 키패드 입력
            </div>
            <div class="grid-keypad">
              <button type="button" class="key-btn" v-for="num in [1,2,3,4,5,6,7,8,9]" :key="num" @click="appendKeypad(num)">{{ num }}</button>
              <button type="button" class="key-btn action" @click="shuffleKeypad"><i class="bi bi-arrow-repeat"></i></button>
              <button type="button" class="key-btn" @click="appendKeypad(0)">0</button>
              <button type="button" class="key-btn action" @click="deleteKeypad"><i class="bi bi-backspace"></i></button>
            </div>
          </div>

          <div class="terms-agree-box mb-4 p-3 bg-light rounded-3 border">
            <div class="d-flex align-items-center mb-2 pb-2 border-bottom">
              <input type="checkbox" id="agreeAll" v-model="agreeTerms" class="form-check-input me-2">
              <label for="agreeAll" class="fw-bold text-dark small mb-0">약관 전체 동의 (필수)</label>
            </div>
            <div class="small text-muted space-y-1" style="font-size: 11px;">
              <div class="d-flex justify-content-between align-items-center mb-1">
                <span><i class="bi bi-check-circle-fill text-success me-1"></i>개인정보 수집 및 이용 동의 (필수)</span>
                <i class="bi bi-chevron-right text-muted" style="font-size: 9px;"></i>
              </div>
              <div class="d-flex justify-content-between align-items-center mb-1">
                <span><i class="bi bi-check-circle-fill text-success me-1"></i>고유식별정보 처리 동의 (필수)</span>
                <i class="bi bi-chevron-right text-muted" style="font-size: 9px;"></i>
              </div>
              <div class="d-flex justify-content-between align-items-center">
                <span><i class="bi bi-check-circle-fill text-success me-1"></i>카드결제 서비스 이용 약관 (필수)</span>
                <i class="bi bi-chevron-right text-muted" style="font-size: 9px;"></i>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>

    <!-- ══════════════════════════════════════════
         하단 고정 버튼 영역 (Flex 바닥 고정)
    ══════════════════════════════════════════ -->
    <div class="form-btn-row">
      <button type="button" class="cancel-btn" @click="$router.push('/wallet')">
        취소
      </button>
      <button type="submit" form="cardAddForm" class="submit-card-btn flex-1" :disabled="!isFormValid || submitting">
        <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
        카드 정보 입력 완료
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { registerCard } from '@/api/cardApi';
import { useAuthStore } from '@/stores/auth';
import AgreementCheckItem from '@/components/common/AgreementCheckItem.vue';

const router = useRouter();
const authStore = useAuthStore();
const submitting = ref(false);
const agreeTerms = ref(true);
const showCardAgreementDetail = ref(false);

// 로컬 폴더 이미지 매핑 (/images/cards/ 폴더 참조)
const kbCardImageMap = {
  'KB Pay 노리2 체크카드 (KB국민카드)': '/images/cards/nori2.png',
  'KB국민 톡톡MyPoint 카드': '/images/cards/toktok.png',
  'KB국민 굿데이 ALL 카드': '/images/cards/goodday.png',
  'KB국민 청춘대로 톡톡카드': '/images/cards/chungchun.png',
  'KB국민 My WEISH 카드': '/images/cards/weish.png',
  'KB국민 Easy Link 카드': '/images/cards/easylink.png',
};

const kbCardOptions = Object.keys(kbCardImageMap);

const cardForm = ref({
  cardName: 'KB Pay 노리2 체크카드 (KB국민카드)',
  cardAlias: '',
  cardNum: '',
  expiry: '',
  cvc: '',
  cardPassword: '',
});

const cardPreviewImg = ref(kbCardImageMap['KB Pay 노리2 체크카드 (KB국민카드)']);

const updatePreviewImg = () => {
  cardPreviewImg.value = kbCardImageMap[cardForm.value.cardName] || null;
};

const handleImgError = () => {
  cardPreviewImg.value = '/images/cards/default_card.jpg';
};

const isFormValid = computed(() => {
  return cardForm.value.cardName && cardForm.value.cardNum && cardForm.value.expiry && cardForm.value.cvc && cardForm.value.cardPassword && agreeTerms.value;
});

const formatCardNum = (e) => {
  let val = e.target.value.replace(/\D/g, '');
  if (val.length > 16) val = val.slice(0, 16);
  cardForm.value.cardNum = val.replace(/(\d{4})(?=\d)/g, '$1-');
};

const formatExpiry = (e) => {
  let val = e.target.value.replace(/\D/g, '');
  if (val.length > 4) val = val.slice(0, 4);
  if (val.length >= 3) {
    cardForm.value.expiry = `${val.slice(0, 2)}/${val.slice(2)}`;
  } else {
    cardForm.value.expiry = val;
  }
};

const appendKeypad = (num) => {
  if (cardForm.value.cardPassword.length < 2) {
    cardForm.value.cardPassword += String(num);
  } else if (cardForm.value.cvc.length < 3) {
    cardForm.value.cvc += String(num);
  }
};

const deleteKeypad = () => {
  if (cardForm.value.cvc.length > 0) {
    cardForm.value.cvc = cardForm.value.cvc.slice(0, -1);
  } else if (cardForm.value.cardPassword.length > 0) {
    cardForm.value.cardPassword = cardForm.value.cardPassword.slice(0, -1);
  }
};

const shuffleKeypad = () => {
  // 시각적 피드백
};

const submitCard = async () => {
  if (!isFormValid.value || submitting.value) return;
  submitting.value = true;
  try {
    const payload = {
      userId: authStore.userId,
      cardName: cardForm.value.cardName,
      cardAlias: cardForm.value.cardAlias || cardForm.value.cardName,
      cardImgUrl: cardPreviewImg.value,
      cardNum: cardForm.value.cardNum,
      expiryDate: cardForm.value.expiry,
      cvv: cardForm.value.cvc,
      cardPassword: cardForm.value.cardPassword,
    };
    await registerCard(payload);
    router.push('/wallet');
  } catch (err) {
    console.error('카드 등록 실패:', err);
    const msg = err.response?.data?.message || err.message || '카드 등록에 실패했습니다.';
    alert(`카드 등록 실패: ${msg}`);
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.card-add-root {
  display: flex;
  flex-direction: column;
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 0;
  background: #ffffff;
  color: #222222;
  box-sizing: border-box;
  overflow: hidden;
}

/* 상단 헤더 */
.card-add-header {
  flex-shrink: 0;
  position: relative;
  z-index: 50;
  width: 100%;
  height: 44px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
}

.header-inner {
  display: grid;
  grid-template-columns: 70px 1fr 38px;
  width: 100%;
  height: 44px;
  align-items: center;
  padding: 0 28px;
  box-sizing: border-box;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-self: start;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 14px;
  font-weight: 600;
  line-height: 1;
  cursor: pointer;
}

.back-btn i {
  font-size: 18px;
}

.header-title {
  margin: 0;
  color: #222222;
  font-size: 17px;
  font-weight: 700;
  text-align: center;
}

.close-x-btn {
  display: flex;
  width: 38px;
  height: 38px;
  align-items: center;
  justify-content: center;
  justify-self: end;
  padding: 0;
  border: 0;
  background: transparent;
  color: #777777;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

/* 본문 (중간 가변 독립 스크롤) */
.card-add-body {
  flex: 1;
  min-height: 0;
  width: 100%;
  padding: 24px 28px 40px;
  background: #ffffff;
  box-sizing: border-box;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.card-add-body::-webkit-scrollbar {
  display: none;
}

.form-card {
  width: 100%;
  margin: 0;
  padding: 0;
  border: 0;
  border-radius: 0;
  background: #ffffff;
  box-shadow: none;
  box-sizing: border-box;
}

/* 카드 미리보기 */
.card-preview-plate {
  position: relative;
  display: flex;
  width: 100%;
  min-height: 200px;
  flex-direction: column;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 20px;
  border-radius: 18px;
  background: linear-gradient(
      135deg,
      #1d4ed8 0%,
      #1e293b 100%
  );
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.18);
  color: #ffffff;
  box-sizing: border-box;
  overflow: hidden;
}

.plate-background-overlay {
  position: absolute;
  inset: 0;
  background:
      radial-gradient(
          circle at top right,
          rgba(255, 255, 255, 0.14),
          transparent 42%
      );
  pointer-events: none;
}

.card-plate-header {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chip-ic {
  width: 32px;
  height: 22px;
  border-radius: 5px;
  background: #f5c242;
}

.kb-card-badge {
  padding: 4px 9px;
  border-radius: 999px;
  background: #ffbc2e;
  color: #111111;
  font-size: 11px;
  font-weight: 800;
}

.real-card-img-wrap {
  position: relative;
  z-index: 2;
  display: flex;
  height: 76px;
  align-items: center;
  justify-content: center;
  margin: 8px 0;
}

.real-card-img {
  display: block;
  max-width: 125px;
  max-height: 76px;
  object-fit: contain;
  filter: drop-shadow(
      0 5px 10px rgba(0, 0, 0, 0.25)
  );
}

.card-preview-num {
  position: relative;
  z-index: 2;
  overflow: hidden;
  color: #ffffff;
  font-family: monospace;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 1.5px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-name {
  position: relative;
  z-index: 2;
  overflow: hidden;
  max-width: 75%;
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-expiry {
  position: relative;
  z-index: 2;
  flex: none;
  color: rgba(255, 255, 255, 0.82);
  font-size: 12px;
}

/* 폼 */
.form-group {
  margin-bottom: 22px !important;
}

.form-label {
  display: block;
  margin: 0 0 9px;
  color: #333333;
  font-size: 13px;
  font-weight: 800;
}

.kb-input,
.kb-select-field {
  width: 100%;
  height: 52px;
  padding: 0 14px;
  border: 1px solid #dddddd;
  border-radius: 10px;
  background: #ffffff;
  color: #222222;
  font-family: inherit;
  font-size: 14px;
  font-weight: 500;
  outline: none;
  box-sizing: border-box;
}

.kb-input::placeholder {
  color: #aaaaaa;
  font-weight: 400;
}

.kb-input:focus,
.kb-select-field:focus {
  border-color: #ffbc2e;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.kb-select-field {
  padding-right: 36px;
  cursor: pointer;
}

/* 유효기간, CVC, 비밀번호 영역 */
.row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  margin: 0 0 22px !important;
}

.col-4 {
  width: auto;
  min-width: 0;
  padding: 0;
}

.row .form-label {
  overflow: hidden;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row .kb-input {
  min-width: 0;
  padding: 0 10px;
  font-size: 13px;
}

/* 약관 */
.terms-agree-box {
  min-height: 64px;
  padding: 4px 0;
  border-top: 1px solid #dddddd;
  border-bottom: 1px solid #dddddd;
}

/* 하단 고정 버튼 영역 */
.form-btn-row {
  flex-shrink: 0;
  position: relative;
  z-index: 20;
  display: grid;
  grid-template-columns: 0.8fr 1.6fr;
  gap: 10px;
  padding: 12px 28px 24px;
  background: #ffffff;
  border-top: 1px solid #f0f0f0;
  margin: 0;
}

.cancel-btn,
.submit-card-btn {
  width: 100%;
  height: 52px;
  padding: 0;
  border-radius: 10px;
  font-family: inherit;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}

.cancel-btn {
  border: 1px solid #bbbbbb;
  background: #ffffff;
  color: #444444;
}

.submit-card-btn {
  border: 1px solid #cc9200;
  background: #ffbc2e;
  color: #111111;
}

.cancel-btn:active {
  background: #f5f5f5;
}

.submit-card-btn:active:not(:disabled) {
  background: #f2aa10;
}

.submit-card-btn:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #aaaaaa;
  cursor: not-allowed;
}

.flex-1 {
  min-width: 0;
}

/* 보안 키패드 스타일 */
.grid-keypad {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
}

.key-btn {
  height: 38px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-weight: 700;
  font-size: 14px;
  color: #1e293b;
  cursor: pointer;
  transition: background 0.15s ease;
}

.key-btn:active {
  background: #e2e8f0;
}

.key-btn.action {
  background: #f1f5f9;
  color: #64748b;
  font-size: 12px;
}

/* 애니메이션 */
.fade-in {
  animation: fade-in 0.25s ease-in-out;
}

@media (max-width: 360px) {
  .header-inner {
    padding-right: 20px;
    padding-left: 20px;
  }

  .card-add-body {
    padding-right: 20px;
    padding-left: 20px;
  }

  .form-btn-row {
    padding-right: 20px;
    padding-left: 20px;
  }

  .row {
    gap: 6px;
  }

  .row .kb-input {
    padding: 0 8px;
    font-size: 12px;
  }
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: scale(0.96);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>