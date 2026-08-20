<template>
  <div class="page-layout account-page">
    <!-- 공통 상단 헤더 -->
    <PageHeader title="계좌 인증" custom-back @back="goBack" />

    <!-- 콘텐츠 -->
    <main class="page-content account-content">
      <!-- 페이지 제목 -->
      <header class="verification-header">
        <h1 class="text-26-bold">계좌 인증번호 입력</h1>

        <p class="text-15">
          계좌 거래내역에 표시된<br />
          인증번호 4자리를 입력해 주세요.
        </p>
      </header>

      <!-- 인증번호 입력 -->
      <section class="verification-section">
        <div
          :class="{
            error: !!errorMessage,
            disabled: resendRequired || verificationLocked,
          }"
          class="verification-input-area"
        >
          <input
            ref="verificationInput"
            :value="verificationCode"
            :disabled="resendRequired || verificationLocked || loading"
            class="verification-input"
            inputmode="numeric"
            maxlength="4"
            placeholder="인증번호 4자리"
            type="text"
            @input="changeVerificationCode"
          />

          <button
            v-if="verificationCode"
            class="clear-button"
            :disabled="resendRequired || verificationLocked || loading"
            type="button"
            aria-label="인증번호 전체 삭제"
            @click="clearVerificationCode"
          >
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <p
          v-if="accountStore.accountForm.developmentCode"
          class="development-code text-13"
        >
          계좌 인증번호: {{ accountStore.accountForm.developmentCode }}
        </p>

        <p v-if="errorMessage" class="error-message text-13">
          {{ errorMessage }}
        </p>
      </section>
    </main>

    <!-- 인증번호 재발급이 필요한 경우에만 하단 버튼 표시 -->
    <div v-if="resendRequired" class="bottom-btn-area single">
      <button
        class="bottom-btn"
        :disabled="loading"
        type="button"
        @click="resendVerification"
      >
        {{ loading ? '인증번호 재발급 중...' : '인증번호 다시 받기' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  confirmAccountVerification,
  connectAccount,
  resendAccountVerification,
} from '@/api/accountApi';
import PageHeader from '@/components/common/PageHeader.vue';
import { useAccountStore } from '@/stores/account';

const router = useRouter();
const accountStore = useAccountStore();

const verificationInput = ref(null);
const verificationCode = ref('');
const loading = ref(false);
const errorMessage = ref('');
const resendRequired = ref(false);
const verificationLocked = ref(false);

// 입력창 포커스
const focusInput = async () => {
  if (resendRequired.value || verificationLocked.value || loading.value) {
    return;
  }

  await nextTick();
  verificationInput.value?.focus();
};

// 인증번호 입력
const changeVerificationCode = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 4);

  verificationCode.value = value;
  errorMessage.value = '';

  accountStore.setVerificationCode(value);

  if (event.target.value !== value) {
    event.target.value = value;
  }
};

// 인증번호 전체 삭제
const clearVerificationCode = async () => {
  if (loading.value) return;

  verificationCode.value = '';
  errorMessage.value = '';

  accountStore.setVerificationCode('');

  await focusInput();
};

// 계좌 인증 및 연결
const confirmAndConnect = async () => {
  if (
    loading.value ||
    verificationCode.value.length !== 4 ||
    resendRequired.value ||
    verificationLocked.value
  ) {
    return;
  }

  const userId = accountStore.userId;
  const verificationId = accountStore.accountForm.verificationId;

  if (!userId || !verificationId) {
    await router.replace('/setting/account/connect');
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    // 인증번호 확인
    await confirmAccountVerification({
      verificationId,
      verificationCode: verificationCode.value,
    });

    // 계좌 연결
    await connectAccount({
      verificationId,
    });

    // 성공 시 완료 화면으로 자동 이동
    await router.replace('/setting/account/complete');
  } catch (error) {
    console.error(error);

    verificationCode.value = '';
    accountStore.setVerificationCode('');

    const message =
      error.response?.data?.message || '인증번호가 일치하지 않습니다.';

    errorMessage.value = message;

    // 최초 인증번호 5회 실패
    if (message.includes('인증번호를 다시 받아주세요')) {
      resendRequired.value = true;
    }

    // 재발급 후 5회 실패
    if (message.includes('5분 후 다시 시도해주세요')) {
      resendRequired.value = false;
      verificationLocked.value = true;
    }

    if (!resendRequired.value && !verificationLocked.value) {
      await focusInput();
    }
  } finally {
    loading.value = false;
  }
};

// 4자리 입력 완료 시 자동 인증
watch(verificationCode, async (code) => {
  if (
    code.length !== 4 ||
    loading.value ||
    resendRequired.value ||
    verificationLocked.value
  ) {
    return;
  }

  await confirmAndConnect();
});

// 계좌 인증번호 재발급
const resendVerification = async () => {
  const userId = accountStore.userId;
  const verificationId = accountStore.accountForm.verificationId;

  if (!userId || !verificationId || loading.value) {
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    const response = await resendAccountVerification(verificationId);

    accountStore.accountForm.developmentCode = response.verificationCode;

    verificationCode.value = '';
    accountStore.setVerificationCode('');

    resendRequired.value = false;

    await nextTick();
  } catch (error) {
    console.error(error);

    const message =
      error.response?.data?.message || '인증번호 재발급에 실패했습니다.';

    errorMessage.value = message;

    if (message.includes('5분 후 다시 시도해주세요')) {
      resendRequired.value = false;
      verificationLocked.value = true;
    }
  } finally {
    loading.value = false;

    if (!resendRequired.value && !verificationLocked.value) {
      await focusInput();
    }
  }
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(() => {
  if (!accountStore.accountForm.verificationId) {
    router.replace('/setting/account/connect');
    return;
  }

  focusInput();
});
</script>

<style scoped>
@import '@/components/common/common/common.css';
@import '@/components/common/common/layout.css';

.account-page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  background: var(--color-bg-page);
}

/* 콘텐츠 */
.account-content {
  overflow-y: auto;
  box-sizing: border-box;
}

/* 페이지 제목 */
.verification-header {
  flex-shrink: 0;
  margin-top: 24px;
}

.verification-header h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.verification-header p {
  margin: 14px 0 0;
  color: var(--color-text-sub);
  line-height: 1.6;
}

/* 인증번호 입력 */
.verification-section {
  margin-top: 52px;
}

/* 한 줄 인증번호 입력 영역 */
.verification-input-area {
  position: relative;
  display: flex;
  width: 100%;
  height: 58px;
  align-items: center;
  border-bottom: 2px solid var(--color-border-main);
  box-sizing: border-box;
  transition: border-color 0.2s ease;
}

.verification-input-area:focus-within {
  border-color: var(--color-primary);
}

.verification-input-area.error {
  border-color: var(--color-error);
}

.verification-input-area.disabled {
  border-color: var(--color-border-main);
  opacity: 0.6;
}

.verification-input {
  width: 100%;
  height: 100%;
  padding: 0 48px 0 2px;
  border: 0;
  outline: none;
  background: transparent;
  color: var(--color-text-main);
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 4px;
  box-sizing: border-box;
}

.verification-input::placeholder {
  color: var(--color-text-muted);
  font-size: 15px;
  font-weight: 400;
  letter-spacing: -0.2px;
}

.verification-input:disabled {
  color: var(--color-text-disabled);
  cursor: not-allowed;
}

/* 전체 삭제 버튼 */
.clear-button {
  position: absolute;
  top: 50%;
  right: 4px;
  display: flex;
  width: 30px;
  height: 30px;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: var(--color-bg-screen);
  color: var(--color-text-sub);
  font-size: 14px;
  cursor: pointer;
  transform: translateY(-50%);
}

.clear-button:active:not(:disabled) {
  background: var(--color-bg-disabled);
}

.clear-button:disabled {
  cursor: not-allowed;
}

/* 개발용 인증번호 */
.development-code {
  margin: 16px 0 0;
  color: var(--color-text-sub);
  line-height: 1.5;
  text-align: left;
}

/* 오류 메시지 */
.error-message {
  margin: 10px 0 0;
  color: var(--color-error);
  line-height: 1.5;
  text-align: left;
}
</style>
