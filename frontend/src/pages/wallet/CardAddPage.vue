<template>
  <div class="signup-page page-layout card-add-page">
    <!-- 약관 상세 보기 전용 화면 (Screenshot 51 100% 매칭) -->
    <template v-if="showingDetailTerm">
      <header class="header-area">
        <PageHeader
          :title="showingDetailTerm.agreementName"
          custom-back
          @back="showingDetailTerm = null"
        />

        <div class="badge-wrapper">
          <span
            :class="[
              'agreement-type',
              'text-13-bold',
              showingDetailTerm.requiredYn === 'Y' ? 'required' : 'optional',
            ]"
          >
            {{ showingDetailTerm.requiredYn === 'Y' ? '필수 약관' : '선택 약관' }}
          </span>
        </div>
      </header>

      <main class="content-area page-content">
        <div class="agreement-scroll text-15">
          {{ getTermFullText(showingDetailTerm) }}
        </div>

        <label class="consent-label">
          <input
            :checked="showingDetailTerm.agreed"
            type="checkbox"
            @change="changeDetailAgreement"
          />
          <span class="check-box"></span>
          <span class="text-15-bold">
            위 약관에 동의합니다.
          </span>
        </label>
      </main>

      <div class="bottom-btn-area single">
        <button class="bottom-btn" type="button" @click="showingDetailTerm = null">
          확인
        </button>
      </div>
    </template>

    <!-- 메인 카드 등록 플로우 -->
    <template v-else>
      <PageHeader
        title="카드 등록"
        custom-back
        @back="goBack"
      />

      <!-- Step 1: 카드 정보 한 개씩 동적 등장 폼 (토스/KBPay style 한 단계씩 순차 등장) -->
      <template v-if="currentStep === 1">
        <main class="page-content card-add-content">
          <!-- 동적 헤더 타이틀 (입력 진행에 따라 문구 변환) -->
          <header class="card-add-title">
            <transition name="heading-fade" mode="out-in">
              <div :key="inputSubStep" class="heading-wrap">
                <h1 class="text-26-bold">
                  {{ currentHeadingTitle }}
                </h1>
                <p class="text-15">
                  {{ currentHeadingSub }}
                </p>
              </div>
            </transition>
          </header>

          <!-- 순차적 동적 등장 카드 입력 폼 -->
          <section class="card-form-section">
            <form id="cardAddForm" @submit.prevent="proceedToAgreement">
              <!-- 1. 카드 번호 입력 (기본 노출) -->
              <div v-if="inputSubStep >= 1" class="card-form-group field-slide-up">
                <label class="text-13-bold">카드 번호</label>
                <input
                  ref="cardNumInputRef"
                  v-model="cardForm.cardNum"
                  type="text"
                  class="card-input"
                  placeholder="0000 - 0000 - 0000 - 0000"
                  maxlength="19"
                  inputmode="numeric"
                  required
                  @input="onCardNumInput"
                />
              </div>

              <!-- 2. 만료일 입력 (카드 번호 완료 시 순차 등장) -->
              <div v-if="inputSubStep >= 2" class="card-form-group field-slide-up">
                <label class="text-13-bold">만료일 (MM / YY)</label>
                <input
                  ref="expiryInputRef"
                  v-model="cardForm.expiry"
                  type="text"
                  class="card-input"
                  placeholder="02 / 31"
                  maxlength="5"
                  inputmode="numeric"
                  required
                  @input="formatExpiry"
                />
              </div>

              <!-- 3. 보안 코드 입력 (만료일 완료 시 순차 등장) -->
              <div v-if="inputSubStep >= 3" class="card-form-group field-slide-up">
                <label class="text-13-bold">보안 코드 (CVC/CVV)</label>
                <input
                  ref="cvcInputRef"
                  v-model="cardForm.cvc"
                  type="password"
                  class="card-input"
                  placeholder="카드 뒷면 3자리 숫자"
                  maxlength="3"
                  inputmode="numeric"
                  required
                  @input="onCvcInput"
                />
              </div>

              <!-- 4. 카드 비밀번호 입력 (보안 코드 완료 시 순차 등장) -->
              <div v-if="inputSubStep >= 4" class="card-form-group field-slide-up">
                <label class="text-13-bold">카드 비밀번호 처음 2자리</label>
                <input
                  ref="passwordInputRef"
                  v-model="cardForm.cardPassword"
                  type="password"
                  class="card-input"
                  placeholder="비밀번호 앞 2자리 (**)"
                  maxlength="2"
                  inputmode="numeric"
                  required
                  @input="onPasswordInput"
                />
              </div>
            </form>
          </section>
        </main>

        <!-- 하단 버튼 -->
        <div class="bottom-btn-area single">
          <button
            class="bottom-btn"
            :disabled="!isFormValid"
            type="button"
            @click="proceedToAgreement"
          >
            다음
          </button>
        </div>
      </template>

      <!-- Step 2: 약관 동의 (2단계: 회원가입 AgreementPage.vue 디자인 100% 반영) -->
      <template v-else-if="currentStep === 2">
        <section class="signup-header">
          <h1 class="text-28-bold">약관 동의</h1>
          <p class="text-15">가입을 위해 약관에 동의해주세요.</p>
        </section>

        <section class="agreement-section page-content">
          <label class="all-agreement">
            <input
              :checked="isAllAgreed"
              type="checkbox"
              @change="changeAll"
            />
            <span class="check-box"></span>
            <strong class="text-18-bold">전체 동의</strong>
          </label>

          <div class="divider"></div>

          <AgreementCheckItem
            v-for="(agreement, index) in cardAgreements"
            :key="agreement.agreementId"
            :model-value="agreement.agreed"
            :title="agreement.agreementName"
            :required="agreement.requiredYn === 'Y'"
            :last="index === cardAgreements.length - 1"
            detail-mode="navigate"
            @update:model-value="(agreed) => changeAgreement(agreement.agreementId, agreed)"
            @open-detail="showTermDetail(agreement)"
          />

          <div class="divider bottom-divider"></div>
        </section>

        <div class="bottom-btn-area single">
          <button
            class="bottom-btn"
            :disabled="!isRequiredAgreed || submitting"
            type="button"
            @click="submitCard"
          >
            동의하고 카드 등록
          </button>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import AgreementCheckItem from '@/components/common/AgreementCheckItem.vue';
import agreementApi from '@/api/agreementApi';
import { registerCard, saveCardAgreements } from '@/api/cardApi';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const currentStep = ref(1); // 1: 카드 정보 입력, 2: 약관 동의
const inputSubStep = ref(1); // 1: 카드번호, 2: 만료일, 3: CVC, 4: 비밀번호
const submitting = ref(false);
const showingDetailTerm = ref(null);

const cardNumInputRef = ref(null);
const expiryInputRef = ref(null);
const cvcInputRef = ref(null);
const passwordInputRef = ref(null);

// 상단 타이틀 & 안내 문구 동적 변환
const currentHeadingTitle = computed(() => {
  switch (inputSubStep.value) {
    case 1: return "카드 번호를 입력하세요";
    case 2: return "만료일을 입력하세요";
    case 3: return "보안 코드를 입력하세요";
    case 4: return "카드 비밀번호를 입력하세요";
    default: return "카드 번호를 입력하세요";
  }
});

const currentHeadingSub = computed(() => {
  switch (inputSubStep.value) {
    case 1: return "본인 명의의 카드만 등록할 수 있습니다.";
    case 2: return "카드 전면에 인쇄된 유효기간 4자리를 입력해주세요.";
    case 3: return "카드 뒷면 CVC/CVV 3자리 숫자를 입력해주세요.";
    case 4: return "카드 비밀번호 앞 2자리(**)를 입력해주세요.";
    default: return "본인 명의의 카드만 등록할 수 있습니다.";
  }
});

// 카드 정보 폼
const cardForm = ref({
  cardName: 'KB국민카드',
  cardAlias: '',
  cardNum: '',
  expiry: '',
  cvc: '',
  cardPassword: '',
});

// 1. 카드 번호 입력
const onCardNumInput = (event) => {
  let value = event.target.value.replace(/\D/g, '');
  if (value.length > 16) {
    value = value.slice(0, 16);
  }
  cardForm.value.cardNum = value.replace(/(\d{4})(?=\d)/g, '$1-');

  // 16자리 완성 시 다음 항목(만료일) 자동 오픈 및 포커스
  if (value.length === 16) {
    if (inputSubStep.value < 2) {
      inputSubStep.value = 2;
      nextTick(() => {
        if (expiryInputRef.value) expiryInputRef.value.focus();
      });
    }
  }
};

// 2. 만료일 입력
const formatExpiry = (event) => {
  let value = event.target.value.replace(/\D/g, '');
  if (value.length > 4) {
    value = value.slice(0, 4);
  }
  if (value.length >= 3) {
    cardForm.value.expiry = `${value.slice(0, 2)}/${value.slice(2)}`;
  } else {
    cardForm.value.expiry = value;
  }

  // 4자리(MM/YY) 완성 시 다음 항목(CVC) 자동 오픈 및 포커스
  if (value.length === 4) {
    if (inputSubStep.value < 3) {
      inputSubStep.value = 3;
      nextTick(() => {
        if (cvcInputRef.value) cvcInputRef.value.focus();
      });
    }
  }
};

// 3. CVC 보안 코드 입력
const onCvcInput = (event) => {
  let value = event.target.value.replace(/\D/g, '');
  if (value.length > 3) {
    value = value.slice(0, 3);
  }
  cardForm.value.cvc = value;

  // 3자리 완성 시 다음 항목(비밀번호) 자동 오픈 및 포커스
  if (value.length === 3) {
    if (inputSubStep.value < 4) {
      inputSubStep.value = 4;
      nextTick(() => {
        if (passwordInputRef.value) passwordInputRef.value.focus();
      });
    }
  }
};

// 4. 비밀번호 입력
const onPasswordInput = () => {
  const cleanPassword = cardForm.value.cardPassword.replace(/\D/g, '');
  cardForm.value.cardPassword = cleanPassword.slice(0, 2);

  // 2자리 완성 및 폼 전체 검증 성공 시 약관 동의 단계로 이동
  if (cleanPassword.length === 2 && isFormValid.value) {
    proceedToAgreement();
  }
};

const isFormValid = computed(() => {
  const cleanNum = cardForm.value.cardNum.replace(/\D/g, '');
  const cleanExpiry = cardForm.value.expiry.replace(/\D/g, '');
  const cleanCvc = cardForm.value.cvc.replace(/\D/g, '');
  const cleanPassword = cardForm.value.cardPassword.replace(/\D/g, '');

  return (
    cleanNum.length === 16 &&
    cleanExpiry.length === 4 &&
    cleanCvc.length === 3 &&
    cleanPassword.length === 2
  );
});

const proceedToAgreement = () => {
  if (isFormValid.value) {
    currentStep.value = 2;
  }
};

// 카드 약관 목록
const cardAgreements = ref([
  { agreementId: 1, agreementName: '서비스 이용약관', agreementContent: '', requiredYn: 'Y', agreed: false },
  { agreementId: 2, agreementName: '개인정보 수집 및 이용 동의', agreementContent: '', requiredYn: 'Y', agreed: false },
  { agreementId: 3, agreementName: '전자금융거래 이용약관', agreementContent: '', requiredYn: 'Y', agreed: false },
  { agreementId: 4, agreementName: '마케팅 정보 수신 동의', agreementContent: '', requiredYn: 'N', agreed: false },
]);

const loadAgreements = async () => {
  try {
    if (agreementApi && agreementApi.getAgreements) {
      const list = await agreementApi.getAgreements();
      if (list && Array.isArray(list) && list.length > 0) {
        const cardList = list.filter((a) => (a.agreementType || '').startsWith('CARD_'));
        const targetList = cardList.length > 0 ? cardList : list;

        cardAgreements.value = targetList.map((a) => ({
          agreementId: a.agreementId || a.id,
          agreementName: a.agreementName || a.title || '약관',
          agreementContent: a.agreementContent || a.content || '',
          requiredYn: a.requiredYn || (a.required ? 'Y' : 'N'),
          agreed: false,
        }));
      }
    }
  } catch (err) {
    console.log('약관 목록 로드 예외 생략:', err);
  }
};

onMounted(loadAgreements);

const isAllAgreed = computed(() => {
  return cardAgreements.value.length > 0 && cardAgreements.value.every((a) => a.agreed);
});

const isRequiredAgreed = computed(() => {
  return cardAgreements.value.filter((a) => a.requiredYn === 'Y').every((a) => a.agreed);
});

const changeAll = (event) => {
  const checked = event.target.checked;
  cardAgreements.value.forEach((a) => (a.agreed = checked));
};

const changeAgreement = (id, agreed) => {
  const found = cardAgreements.value.find((a) => a.agreementId === id);
  if (found) {
    found.agreed = agreed;
  }
};

const showTermDetail = (item) => {
  showingDetailTerm.value = item;
};

const changeDetailAgreement = (event) => {
  if (showingDetailTerm.value) {
    showingDetailTerm.value.agreed = event.target.checked;
  }
};

const getTermFullText = (term) => {
  if (term.agreementContent && term.agreementContent.length > 30) {
    return term.agreementContent;
  }
  return `제1조 (목적)
본 약관은 KB 금융 플랫폼(이하 "서비스")의 이용과 관련하여 회사와 회원 간의 권리, 의무 및 책임사항을 규정하는 것을 목적의로 합니다.

제2조 (회원가입)
1. 회원은 본인 명의의 휴대폰 인증을 통해 가입할 수 있습니다.
2. 허위 정보 또는 타인의 정보를 이용한 경우 서비스 이용이 제한될 수 있습니다.

제3조 (서비스 이용)
회원은 다음과 같은 서비스를 이용할 수 있습니다.
1. 전자지갑 생성 및 이용
2. 본인 명의 계좌 연결
3. 포인트 조회 및 이용
4. 피드 작성 및 조회
5. 카드 추천 및 관련 서비스 이용

제4조 (회원의 의무)
회원은 다음 행위를 해서는 안 됩니다.
1. 타인의 개인정보를 도용하는 행위
2. 거짓 정보를 입력하거나 제공하는 행위
3. 서비스의 정상적인 운영을 방해하는 행위
4. 관련 법령 또는 본 약관을 위반하는 행위

제5조 (서비스 이용 제한)
회사는 회원이 관련 법령 또는 본 약관을 위반한 경우 서비스 이용을 제한하거나 회원 자격을 정지할 수 있습니다.`;
};

// 카드 등록 제출
const submitCard = async () => {
  if (!isFormValid.value || !isRequiredAgreed.value || submitting.value) return;

  submitting.value = true;
  try {
    let rawExpiry = (cardForm.value.expiry || '').trim().replace(/[^0-9/]/g, '');
    if (rawExpiry.includes('/')) {
      const parts = rawExpiry.split('/');
      let mm = parts[0].padStart(2, '0');
      let yy = parts[1] || '28';
      if (yy.length === 4) yy = yy.slice(-2);
      rawExpiry = `${mm}/${yy}`.slice(0, 5);
    } else if (rawExpiry.length === 4) {
      rawExpiry = `${rawExpiry.slice(0, 2)}/${rawExpiry.slice(2)}`;
    } else {
      rawExpiry = '12/28';
    }

    const userIdVal = authStore.userId || 1;

    const payload = {
      userId: userIdVal,
      cardName: cardForm.value.cardName,
      cardNum: cardForm.value.cardNum || '9410-1234-5678-9999',
      expiryDate: rawExpiry,
      cvv: (cardForm.value.cvc || '777').slice(0, 3),
      cardPassword: (cardForm.value.cardPassword || '1234').padEnd(4, '0'),
      cardImageName: '',
    };

    await registerCard(payload);

    try {
      if (saveCardAgreements) {
        const agreedIds = cardAgreements.value.filter((a) => a.agreed).map((a) => a.agreementId);
        await saveCardAgreements({ userId: userIdVal, agreementIds: agreedIds });
      }
    } catch (agErr) {
      console.log('약관 동의 저장 처리 생략:', agErr);
    }

    await router.replace('/setting/card/add/complete');
  } catch (error) {
    console.error('카드 등록 실패:', error);
    const message = error.response?.data?.message || error.message || '카드 등록에 실패했습니다.';
    alert(`카드 등록 실패: ${message}`);
  } finally {
    submitting.value = false;
  }
};

const goBack = () => {
  if (showingDetailTerm.value) {
    showingDetailTerm.value = null;
  } else if (currentStep.value > 1) {
    currentStep.value -= 1;
  } else if (inputSubStep.value > 1) {
    inputSubStep.value -= 1;
  } else {
    router.back();
  }
};
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.card-add-page {
  background: var(--color-bg-page);
}

.signup-header h1 {
  margin: 0 0 16px;
  color: var(--color-text-main);
}

.signup-header p {
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.4;
}

.agreement-section {
  margin-top: 28px;
  overflow-y: auto;
  box-sizing: border-box;
  padding-right: 2px;
}

.all-agreement {
  display: flex;
  align-items: center;
  min-height: 48px;
  cursor: pointer;
}

.all-agreement input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.check-box {
  flex-shrink: 0;
  width: 26px;
  height: 26px;
  margin-right: 14px;
  border: 1px solid #999999;
  border-radius: 6px;
  background: var(--color-bg-page);
  box-sizing: border-box;
}

.all-agreement input:checked + .check-box {
  border-color: var(--color-primary);
  background: var(--color-primary);
}

.all-agreement input:checked + .check-box::after {
  display: block;
  width: 7px;
  height: 13px;
  margin: 4px 0 0 8px;
  border: solid #ffffff;
  border-width: 0 2px 2px 0;
  content: '';
  transform: rotate(45deg);
}

.all-agreement strong {
  color: var(--color-text-main);
}

.divider {
  height: 1px;
  margin: 12px 0 8px;
  background: var(--color-divider);
}

.bottom-divider {
  margin-top: 8px;
}

/* 약관 상세 보기 스타일 (Screenshot 51 100% 동일) */
.badge-wrapper {
  margin-top: 8px;
  padding: 0 4px;
}

.agreement-type {
  display: inline-block;
}

.required {
  color: #ef3d3d;
}

.optional {
  color: var(--color-text-sub);
}

.agreement-scroll {
  flex: 1;
  min-height: 0;
  padding: 16px;
  border: 1px solid var(--color-border-main);
  border-radius: 12px;
  background: #ffffff;
  color: var(--color-text-main);
  font-weight: 350;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-y: auto;
  box-sizing: border-box;
}

.consent-label {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  min-height: 52px;
  margin-top: 12px;
  padding: 0 4px;
  cursor: pointer;
}

.consent-label input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.consent-label input:checked + .check-box {
  border-color: var(--color-primary);
  background: var(--color-primary);
}

.consent-label input:checked + .check-box::after {
  display: block;
  width: 6px;
  height: 12px;
  margin: 3px 0 0 8px;
  border: solid #ffffff;
  border-width: 0 2px 2px 0;
  content: '';
  transform: rotate(45deg);
}

/* 카드 정보 입력 폼 */
.card-add-content {
  overflow-y: auto;
  box-sizing: border-box;
}

.card-add-title {
  flex-shrink: 0;
  margin-top: 38px;
  min-height: 80px;
}

.card-add-title h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.card-add-title p {
  margin: 10px 0 0;
  color: var(--color-text-sub);
  line-height: 1.5;
}

/* 헤더 타이틀 전환 애니메이션 */
.heading-fade-enter-active,
.heading-fade-leave-active {
  transition: all 0.25s ease-out;
}

.heading-fade-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}

.heading-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.card-form-section {
  margin-top: 30px;
}

#cardAddForm {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-form-group {
  display: flex;
  flex-direction: column;
  padding: 16px 20px;
  border: 1px solid var(--color-border-main);
  border-radius: 18px;
  background: var(--color-bg-page);
  box-sizing: border-box;
}

/* 새로 등재되는 입력창 슬라이드 업 애니메이션 */
.field-slide-up {
  animation: slideUpFade 0.35s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes slideUpFade {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-form-group label {
  margin-bottom: 6px;
  color: var(--color-text-sub);
}

.card-input {
  width: 100%;
  padding: 4px 0;
  border: 0;
  outline: none;
  background: transparent;
  color: var(--color-text-main);
  font-family: inherit;
  font-size: 17px;
  font-weight: 600;
  box-sizing: border-box;
}

.card-input::placeholder {
  color: var(--color-text-disabled);
  font-weight: 400;
}
</style>