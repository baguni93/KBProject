<template>
  <div class="card-add-root">

    <!-- ══════════════════════════════════════════
         상단 헤더 (나가는 버튼 X 및 뒤로가기)
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

    <!-- 본문 폼 -->
    <div class="card-add-body">
      <div class="form-card">
        
        <!-- KB국민카드 실물 3D 프리뷰 플레이트 (공식 CDN 실물 카드 이미지 적용!) -->
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
            <span class="preview-name">{{ cardForm.cardName || 'KB국민카드 상품을 선택하세요' }}</span>
            <span class="preview-expiry">{{ cardForm.expiry || 'MM/YY' }}</span>
          </div>
        </div>

        <form @submit.prevent="submitCard">
          
          <!-- KB국민카드 전용 상품 선택 드롭다운 -->
          <div class="form-group mb-3">
            <label class="form-label">
              <i class="bi bi-credit-card-2-front-fill me-1 text-warning"></i>KB국민카드 상품 선택
            </label>
            <select v-model="cardForm.cardName" class="kb-select-field" required @change="updatePreviewImg">
              <option value="" disabled selected>등록할 KB국민카드 상품을 선택하세요</option>
              <option v-for="opt in kbCardOptions" :key="opt" :value="opt">
                {{ opt }}
              </option>
            </select>
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
            <div class="col-6">
              <label class="form-label">유효기간</label>
              <input
                v-model="cardForm.expiry"
                type="text"
                class="kb-input"
                placeholder="MM/YY"
                maxLength="5"
                required
              />
            </div>
            <div class="col-6">
              <label class="form-label">CVC 보안코드</label>
              <input
                v-model="cardForm.cvc"
                type="password"
                class="kb-input"
                placeholder="뒷면 3자리"
                maxLength="3"
                required
              />
            </div>
          </div>

          <div class="terms-agree-box mb-4">
            <label class="agree-label">
              <input v-model="agreeTerms" type="checkbox" required />
              <span>KB국민카드 결제 서비스 약관 및 개인정보 제공에 동의합니다.</span>
            </label>
          </div>

          <!-- 버튼 그룹 (취소/나가기 & 등록완료) -->
          <div class="form-btn-row">
            <button type="button" class="cancel-btn" @click="$router.push('/wallet')">
              취소 (나가기)
            </button>
            <button type="submit" class="submit-card-btn flex-1" :disabled="!isFormValid || submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              KB국민카드 등록 완료
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import cardApi from '@/api/cardApi';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const submitting = ref(false);
const agreeTerms = ref(true);

// 로컬 폴더 이미지 매핑 (/images/cards/ 폴더 참조)
// 이미지 파일을 public/images/cards/ 폴더에 넣어주세요
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
  cardNum: '',
  expiry: '',
  cvc: '',
});

const cardPreviewImg = ref(kbCardImageMap['KB Pay 노리2 체크카드 (KB국민카드)']);

const updatePreviewImg = () => {
  cardPreviewImg.value = kbCardImageMap[cardForm.value.cardName] || null;
};

const handleImgError = () => {
  cardPreviewImg.value = '/images/cards/default_card.jpg';
};

const isFormValid = computed(() => {
  return cardForm.value.cardName && cardForm.value.cardNum && agreeTerms.value;
});

const formatCardNum = (e) => {
  let val = e.target.value.replace(/\D/g, '');
  if (val.length > 16) val = val.slice(0, 16);
  cardForm.value.cardNum = val.replace(/(\d{4})(?=\d)/g, '$1-');
};

const submitCard = async () => {
  if (!isFormValid.value || submitting.value) return;
  submitting.value = true;
  try {
    const payload = {
      userId: authStore.userId,
      cardName: cardForm.value.cardName,
      cardNum: cardForm.value.cardNum,
    };
    await cardApi.registerCard(payload);

    // 등록 성공 시 카드번호 → 선택한 카드명/이미지 localStorage에 저장
    // (백엔드 DB가 cardName을 cardNum 컬럼으로 조회하여 "Custom Card"로 내려오는 문제 보완)
    const rawNum = cardForm.value.cardNum.replace(/[-\s]/g, '');
    const saved = JSON.parse(localStorage.getItem('kbCardSelections') || '{}');
    saved[rawNum] = {
      name: cardForm.value.cardName,
      img: cardPreviewImg.value,
    };
    localStorage.setItem('kbCardSelections', JSON.stringify(saved));

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
  min-height: 100vh;
  background-color: #f4f5f8;
  font-family: 'Pretendard', -apple-system, BlinkMacSystemFont, system-ui, sans-serif;
  color: #222;
  padding-bottom: 50px;
}

.card-add-header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: #ffffff;
  border-bottom: 1px solid #ebebeb;
}

.header-inner {
  max-width: 500px;
  margin: 0 auto;
  height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-btn {
  background: transparent;
  border: none;
  font-size: 14px;
  font-weight: 700;
  color: #475569;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 17px;
  font-weight: 800;
  margin: 0;
  color: #0f172a;
}

.close-x-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  color: #64748b;
  cursor: pointer;
  padding: 4px;
}

.card-add-body {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px 16px;
}

.form-card {
  background: #ffffff;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.card-preview-plate {
  position: relative;
  background: linear-gradient(135deg, #1d4ed8 0%, #1e293b 100%);
  border-radius: 20px;
  padding: 20px;
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.22);
  overflow: hidden;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-plate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 2;
}

.chip-ic {
  width: 32px;
  height: 22px;
  background: #f59e0b;
  border-radius: 5px;
}

.kb-card-badge {
  background: #ffbc00;
  color: #111;
  font-size: 11px;
  font-weight: 900;
  padding: 3px 8px;
  border-radius: 8px;
}

.real-card-img-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 75px;
  z-index: 2;
}

.real-card-img {
  max-height: 75px;
  max-width: 120px;
  object-fit: contain;
  filter: drop-shadow(0 4px 10px rgba(0, 0, 0, 0.25));
}

.card-preview-num {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 2px;
  font-family: monospace;
  z-index: 2;
}

.preview-name { font-size: 13px; font-weight: 700; z-index: 2; }
.preview-expiry { font-size: 12px; opacity: 0.8; z-index: 2; }

.form-label {
  font-size: 13px;
  font-weight: 700;
  color: #475569;
  margin-bottom: 6px;
  display: block;
}

.kb-input, .kb-select-field {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  background-color: #ffffff;
}

.terms-agree-box {
  background: #f8fafc;
  border-radius: 12px;
  padding: 12px;
  border: 1px solid #e2e8f0;
}

.agree-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #475569;
  font-weight: 600;
  cursor: pointer;
}

.agree-label input {
  width: 16px;
  height: 16px;
  accent-color: #ffbc00;
}

.form-btn-row {
  display: flex;
  gap: 10px;
}

.cancel-btn {
  background: #f1f5f9;
  color: #475569;
  border: none;
  border-radius: 14px;
  padding: 14px 20px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.submit-card-btn {
  background: #ffbc00;
  color: #111;
  border: none;
  border-radius: 14px;
  padding: 14px 0;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}

.submit-card-btn:disabled {
  background: #e2e8f0;
  color: #94a3b8;
  cursor: not-allowed;
}

.flex-1 { flex: 1; }

.fade-in { animation: fadeIn 0.25s ease-in-out; }

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.96); }
  to { opacity: 1; transform: scale(1); }
}
</style>
