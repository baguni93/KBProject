<template>
  <div class="page-layout card-add-page">
    <!-- 공통 상단 헤더 -->
    <PageHeader
        title="카드 등록"
        custom-back
        @back="goBack"
    />

    <!-- 콘텐츠 -->
    <main class="page-content card-add-content">
      <header class="card-add-title">
        <h1 class="text-26-bold">
          카드 번호를 입력하세요
        </h1>

        <p class="text-15">
          본인 명의의 카드만 등록할 수 있습니다.
        </p>
      </header>

      <!-- 카드 입력 폼 -->
      <section class="card-form-section">
        <form
            id="cardAddForm"
            @submit.prevent="submitCard"
        >
          <!-- 카드 번호 -->
          <div class="card-form-group">
            <label class="text-13-bold">
              카드 번호
            </label>

            <input
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

          <!-- 만료일 -->
          <div class="card-form-group">
            <label class="text-13-bold">
              만료일 (MM / YY)
            </label>

            <input
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

          <!-- 보안 코드 -->
          <div class="card-form-group">
            <label class="text-13-bold">
              보안 코드 (CVC/CVV)
            </label>

            <input
                v-model="cardForm.cvc"
                type="password"
                class="card-input"
                placeholder="카드 뒷면 3자리 숫자"
                maxlength="3"
                inputmode="numeric"
                required
            />
          </div>

          <!-- 카드 비밀번호 -->
          <div class="card-form-group">
            <label class="text-13-bold">
              카드 비밀번호 처음 2자리
            </label>

            <input
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
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import { registerCard } from '@/api/cardApi';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const submitting = ref(false);

const cardForm = ref({
  cardName: 'KB국민카드',
  cardAlias: '',
  cardNum: '',
  expiry: '',
  cvc: '',
  cardPassword: '',
});

const cardPreviewImg = ref('');

// 카드번호 입력 처리
const onCardNumInput = (event) => {
  let value = event.target.value.replace(/\D/g, '');

  if (value.length > 16) {
    value = value.slice(0, 16);
  }

  cardForm.value.cardNum =
      value.replace(/(\d{4})(?=\d)/g, '$1-');
};

// 비밀번호 2자리 완성 시 자동 제출
const onPasswordInput = () => {
  const cleanPassword =
      cardForm.value.cardPassword.replace(/\D/g, '');

  cardForm.value.cardPassword =
      cleanPassword.slice(0, 2);

  if (
      cleanPassword.length === 2 &&
      isFormValid.value &&
      !submitting.value
  ) {
    submitCard();
  }
};

// 입력값 검증
const isFormValid = computed(() => {
  const cleanNum =
      cardForm.value.cardNum.replace(/\D/g, '');

  const cleanExpiry =
      cardForm.value.expiry.replace(/\D/g, '');

  const cleanCvc =
      cardForm.value.cvc.replace(/\D/g, '');

  const cleanPassword =
      cardForm.value.cardPassword.replace(/\D/g, '');

  return (
      cleanNum.length === 16 &&
      cleanExpiry.length === 4 &&
      cleanCvc.length === 3 &&
      cleanPassword.length === 2
  );
});

// 만료일 입력
const formatExpiry = (event) => {
  let value = event.target.value.replace(/\D/g, '');

  if (value.length > 4) {
    value = value.slice(0, 4);
  }

  if (value.length >= 3) {
    cardForm.value.expiry =
        `${value.slice(0, 2)}/${value.slice(2)}`;
  } else {
    cardForm.value.expiry = value;
  }
};

// 카드 등록
const submitCard = async () => {
  if (!isFormValid.value || submitting.value) return;

  submitting.value = true;

  try {
    let rawExpiry =
        (cardForm.value.expiry || '')
            .trim()
            .replace(/[^0-9/]/g, '');

    if (rawExpiry.includes('/')) {
      const parts = rawExpiry.split('/');

      let mm = parts[0].padStart(2, '0');
      let yy = parts[1] || '28';

      if (yy.length === 4) {
        yy = yy.slice(-2);
      }

      rawExpiry =
          `${mm}/${yy}`.slice(0, 5);
    } else if (rawExpiry.length === 4) {
      rawExpiry =
          `${rawExpiry.slice(0, 2)}/${rawExpiry.slice(2)}`;
    } else {
      rawExpiry = '12/28';
    }

    const payload = {
      userId: authStore.userId || 1,
      cardName: cardForm.value.cardName,
      cardNum:
          cardForm.value.cardNum ||
          '9410-1234-5678-9999',
      expiryDate: rawExpiry,
      cvv:
          (cardForm.value.cvc || '777')
              .slice(0, 3),
      cardPassword:
          (cardForm.value.cardPassword || '1234')
              .padEnd(4, '0'),
      cardImageName:
          cardPreviewImg.value || '',
    };

    await registerCard(payload);

    await router.replace(
        '/setting/card/add/complete'
    );
  } catch (error) {
    console.error(
        '카드 등록 실패:',
        error
    );

    const message =
        error.response?.data?.message ||
        error.message ||
        '카드 등록에 실패했습니다.';

    alert(`카드 등록 실패: ${message}`);
  } finally {
    submitting.value = false;
  }
};

// 이전 화면
const goBack = () => {
  router.back();
};
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.card-add-page {
  background: var(--color-bg-page);
}

/* 콘텐츠 */
.card-add-content {
  overflow-y: auto;
  box-sizing: border-box;
}

/* 페이지 제목 */
.card-add-title {
  flex-shrink: 0;
  margin-top: 38px;
}

.card-add-title h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.card-add-title p {
  margin: 14px 0 0;
  color: var(--color-text-sub);
  line-height: 1.6;
}

/* 카드 입력 폼 */
.card-form-section {
  margin-top: 44px;
}

#cardAddForm {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 입력 박스 */
.card-form-group {
  display: flex;
  flex-direction: column;
  padding: 16px 20px;
  border: 1px solid var(--color-border-main);
  border-radius: 18px;
  background: var(--color-bg-page);
  box-sizing: border-box;
  transition:
      border-color 0.2s ease,
      box-shadow 0.2s ease;
}

.card-form-group:focus-within {
  border-color: var(--color-primary);
  box-shadow:
      0 0 0 3px
      rgba(255, 188, 46, 0.12);
}

.card-form-group label {
  margin-bottom: 6px;
  color: var(--color-text-sub);
}

/* 입력 */
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