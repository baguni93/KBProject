<template>
  <div class="page-layout account-page">
    <!-- 공통 상단 헤더 -->
    <PageHeader
        custom-back
        @back="goBack"
    />

    <main class="page-content account-content">
      <!-- 계좌 정보 제목 -->
      <header class="account-header">
        <div class="selected-bank">
          <img
              v-if="accountStore.accountForm.bankLogoUrl"
              :alt="accountStore.accountForm.bankName"
              :src="accountStore.accountForm.bankLogoUrl"
          />

          <strong>{{ accountStore.accountForm.bankName }}</strong>
        </div>

        <h1 class="text-26-bold">
          계좌정보를 입력해 주세요
        </h1>

        <p class="account-description text-15">
          본인 명의의 계좌만 연결할 수 있어요.
        </p>
      </header>

      <!-- 계좌 정보 입력 -->
      <form
          class="account-form"
          @submit.prevent="requestVerification"
      >
        <label for="accountHolder">예금주</label>

        <div class="readonly-field">
          <input
              id="accountHolder"
              :value="accountHolder"
              class="readonly-input"
              placeholder="회원 실명을 불러오고 있어요"
              type="text"
              readonly
          />

          <span
              v-if="userLoading"
              class="field-loading"
          >
            조회 중
          </span>
        </div>

        <p class="field-guide text-13">
          로그인한 회원의 실명으로만 계좌를 연결할 수 있어요.
        </p>

        <label for="accountNumber">계좌번호</label>

        <input
            id="accountNumber"
            :value="accountNumber"
            inputmode="numeric"
            maxlength="20"
            placeholder="'-' 없이 숫자만 입력해 주세요"
            type="text"
            @input="changeAccountNumber"
        />

        <p
            v-if="errorMessage"
            class="error-message text-13"
        >
          {{ errorMessage }}
        </p>
      </form>
    </main>

    <!-- 공통 하단 버튼 -->
    <div class="bottom-btn-area single">
      <button
          class="bottom-btn"
          :disabled="!canSubmit || loading || userLoading"
          type="button"
          @click="requestVerification"
      >
        {{ loading ? '인증 요청 중...' : '인증번호 받기' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { requestAccountVerification } from '@/api/accountApi';
import { getAccountByBankCode } from '@/api/userApi';
import { getUserInfo } from '@/api/userApi';
import PageHeader from '@/components/common/PageHeader.vue';
import { useAccountStore } from '@/stores/account';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const accountStore = useAccountStore();
const authStore = useAuthStore();

const accountHolder = ref('');
const accountNumber = ref(accountStore.accountForm.accountNumber || '');
const loading = ref(false);
const userLoading = ref(false);
const errorMessage = ref('');

// 인증 요청 가능 여부
const canSubmit = computed(() => {
  return (
      accountHolder.value.length > 0 &&
      accountNumber.value.length >= 8 &&
      !!accountStore.accountForm.bankCode
  );
});

// 로그인 회원 실명 조회
const loadAccountHolder = async () => {
  const userId = authStore.userId || accountStore.userId;

  if (!userId) {
    await router.replace('/intro');
    return;
  }

  try {
    userLoading.value = true;
    errorMessage.value = '';

    const userInfo = await getUserInfo();

    accountHolder.value = userInfo.userName || '';

    if (!accountHolder.value) {
      errorMessage.value = '회원 실명을 확인할 수 없습니다.';
      return;
    }

    authStore.setUserName(accountHolder.value);
  } catch (error) {
    console.error(error);

    errorMessage.value = error.response?.data?.message || '회원 실명을 불러오지 못했습니다.';
  } finally {
    userLoading.value = false;
  }
};

// 계좌번호 입력
const changeAccountNumber = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 20);

  accountNumber.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) event.target.value = value;
};

// 인증번호 발급
const requestVerification = async () => {
  if (!canSubmit.value || loading.value || userLoading.value) return;

  const userId = authStore.userId || accountStore.userId;

  if (!userId) {
    await router.replace('/intro');
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    const requestData = {
      bankCode: accountStore.accountForm.bankCode,
      accountNumber: accountNumber.value,
      accountHolder: accountHolder.value,
    };

    const response = await requestAccountVerification(requestData);

    accountStore.setAccountInfo(requestData);
    accountStore.setVerification(response);

    await router.push('/setting/account/verification');
  } catch (error) {
    console.error(error);

    errorMessage.value = error.response?.data?.message || '계좌 인증번호 발급에 실패했습니다.';
  } finally {
    loading.value = false;
  }
};

//박우진 추가
const accountInfo = async () => {
  console.log(authStore.userId);

  const response = await getAccountByBankCode(
      authStore.userId,
      accountStore.accountForm.bankCode,
  );

  accountNumber.value = response.accountNumber;
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(async () => {
  if (!accountStore.accountForm.bankCode) {
    await router.replace('/setting/account/connect');
    return;
  }

  await accountInfo();
  await loadAccountHolder();
});
</script>

<style scoped>
@import '@/components/common/common/common.css';

/* ========================================
   계좌 페이지
======================================== */

.account-page {
  background: var(--color-bg-page);
}

/* ========================================
   콘텐츠
======================================== */

.account-content {
  overflow-y: auto;
  box-sizing: border-box;
}

.account-header {
  flex-shrink: 0;
  margin-top: 24px;
}

/* ========================================
   선택 은행
======================================== */

.selected-bank {
  display: inline-flex;
  align-items: center;
  gap: 10px;

  margin-bottom: 28px;
  padding: 9px 14px;

  border-radius: 20px;
  background: #fff7dc;
}

.selected-bank img {
  display: block;
  width: 26px;
  height: 26px;
  object-fit: contain;
}

.selected-bank strong {
  color: var(--color-text-main);
  font-size: 14px;
  font-weight: 700;
}

/* ========================================
   제목
======================================== */

.account-header h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
}

.account-description {
  margin: 16px 0 0;
  color: var(--color-text-sub);
  line-height: 1.5;
}

/* ========================================
   계좌 입력
======================================== */

.account-form {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  margin-top: 54px;
}

.account-form label {
  margin: 0 0 10px;
  color: var(--color-text-main);
  font-size: 15px;
  font-weight: 700;
}

.account-form input {
  width: 100%;
  height: 54px;
  padding: 0 16px;
  border: 1px solid var(--color-border-main);
  border-radius: 10px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
  font-size: 16px;
  outline: none;
  box-sizing: border-box;
}

.account-form input::placeholder {
  color: var(--color-text-disabled);
}

.account-form input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

/* ========================================
   예금주
======================================== */

.readonly-field {
  position: relative;
}

.readonly-input {
  padding-right: 70px !important;
  border-color: var(--color-bg-disabled) !important;
  background: #f7f7f7 !important;
  color: var(--color-text-sub) !important;
  cursor: default;
}

.readonly-input:focus {
  border-color: var(--color-bg-disabled) !important;
  box-shadow: none !important;
}

.field-loading {
  position: absolute;
  top: 50%;
  right: 16px;
  color: var(--color-text-muted);
  font-size: 12px;
  transform: translateY(-50%);
}

/* ========================================
   안내 문구
======================================== */

.field-guide {
  margin: 9px 0 28px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

/* ========================================
   에러
======================================== */

.error-message {
  margin: 14px 0 0;
  color: var(--color-error);
  line-height: 1.5;
}
</style>