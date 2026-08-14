<template>
  <div class="page-layout verification-page">
    <!-- 상단 헤더 그대로 유지 -->
    <PageHeader
        custom-back
        @back="goBack"
    />

    <!-- 페이지 내용 -->
    <main class="page-content verification-content">
      <!-- 제목 -->
      <header class="verification-header">
        <h1 class="text-30-bold">
          인증번호 입력
        </h1>

        <p class="text-15">
          문자로 받은 인증번호를 입력해 주세요.
        </p>
      </header>

      <!-- 인증번호 영역 -->
      <section class="verification-section">
        <!-- 인증번호 / 남은 시간 / X -->
        <div
            :class="{
              error: !!errorMessage || expired || attemptExceeded,
              disabled: expired || attemptExceeded,
            }"
            class="verification-input-area"
        >
          <div class="input-row">
            <input
                ref="verificationInput"
                :value="verificationCode"
                :disabled="expired || attemptExceeded"
                class="verification-input"
                inputmode="numeric"
                maxlength="6"
                pattern="[0-9]*"
                autocomplete="one-time-code"
                placeholder="인증번호 6자리"
                type="text"
                @input="changeVerificationCode"
            />

            <div class="input-actions">
              <!-- 남은 시간 -->
              <div class="input-timer text-13">
                <VerificationTimer
                    :key="timerKey"
                    :seconds="signupStore.expiresIn"
                    @expired="handleExpired"
                />
              </div>

              <!-- 전체 삭제 -->
              <button
                  v-if="verificationCode"
                  :disabled="expired || attemptExceeded"
                  aria-label="인증번호 전체 삭제"
                  class="clear-button"
                  type="button"
                  @click="clearVerificationCode"
              >
                <i class="fa-solid fa-xmark"></i>
              </button>
            </div>
          </div>
        </div>

        <!-- 오류 메시지 -->
        <div
            v-if="attemptExceeded || expired || errorMessage"
            class="message-area"
            aria-live="polite"
        >
          <p
              v-if="attemptExceeded"
              class="error-message text-13"
          >
            {{ errorMessage }}
          </p>

          <p
              v-else-if="expired && resendCount === 0"
              class="error-message text-13"
          >
            인증시간이 만료되어 재전송이 필요해요.
          </p>

          <p
              v-else-if="expired && resendCount >= 1"
              class="error-message text-13"
          >
            인증 가능 횟수를 초과했어요.<br />
            본인인증을 다시 진행해 주세요.
          </p>

          <p
              v-else-if="errorMessage"
              class="error-message text-13"
          >
            {{ errorMessage }}
          </p>
        </div>

        <!-- 개발용 인증번호 / 재전송 -->
        <div class="verification-sub-row">
          <p
              v-if="signupStore.developmentCode"
              class="development-code"
          >
            <span>개발용 인증번호</span>
            <strong>{{ signupStore.developmentCode }}</strong>
          </p>

          <button
              :disabled="resending || resendCount >= 1"
              class="resend-button text-13-bold"
              type="button"
              @click="resendCode"
          >
            {{ resending ? '재전송 중...' : '인증번호 재전송' }}
          </button>
        </div>
      </section>
    </main>

    <!-- 본인인증 다시 시작 -->
    <div
        v-if="
          (expired && resendCount >= 1) ||
          (attemptExceeded && resendCount >= 1)
        "
        class="bottom-btn-area single"
    >
      <button
          class="bottom-btn"
          type="button"
          @click="restartVerification"
      >
        본인인증 다시 하기
      </button>
    </div>

    <!-- 로딩 -->
    <div
        v-if="loading"
        class="loading-overlay"
    >
      <div class="loading-spinner"></div>

      <span class="text-15-bold">
        인증정보를 확인하고 있어요.
      </span>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import loginApi from '@/api/loginApi';
import {
  changePhoneNumber,
  changeUserName,
} from '@/api/userApi';
import VerificationTimer from '@/components/auth/VerificationTimer.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import { useAuthStore } from '@/stores/auth';
import { useSignupStore } from '@/stores/signup';

const router = useRouter();
const signupStore = useSignupStore();
const authStore = useAuthStore();

const verificationInput = ref(null);

const verificationCode = ref('');
const errorMessage = ref('');

const loading = ref(false);
const resending = ref(false);

const expired = ref(false);
const attemptExceeded = ref(false);

const timerKey = ref(0);
const resendCount = ref(0);

// 인증번호 입력창 포커스
const focusInput = async () => {
  if (expired.value || attemptExceeded.value) return;

  await nextTick();
  verificationInput.value?.focus();
};

// 인증번호 입력
const changeVerificationCode = (event) => {
  const value = event.target.value
      .replace(/[^0-9]/g, '')
      .slice(0, 6);

  verificationCode.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) {
    event.target.value = value;
  }
};

// 인증번호 전체 삭제
const clearVerificationCode = async () => {
  verificationCode.value = '';
  errorMessage.value = '';

  await focusInput();
};

// 서버 오류 메시지
const getVerificationErrorMessage = (
    error,
    fallbackMessage,
) => {
  if (!error.response) {
    return '서버에 연결할 수 없습니다. 네트워크 상태를 확인해주세요.';
  }

  return (
      error.response?.data?.message ||
      fallbackMessage
  );
};

// 인증시간 만료
const handleExpired = () => {
  expired.value = true;
  verificationCode.value = '';
  errorMessage.value = '';
};

// PIN 재설정 인증 완료
const handlePinReset = async () => {
  sessionStorage.setItem(
      'pinResetPhoneNumber',
      signupStore.phoneAuth.phoneNumber,
  );

  await router.push('/auth/pin-reset');
};

// 이름 변경 인증 완료
const handleNameChange = async () => {
  const newUserName =
      sessionStorage.getItem(
          'nameChangeNewUserName',
      );

  if (!authStore.userId || !newUserName) {
    await router.replace(
        '/setting/account-management/name',
    );
    return;
  }

  await changeUserName({
    phoneNumber:
    signupStore.phoneAuth.phoneNumber,
    newUserName,
  });

  authStore.setUserName(newUserName);

  sessionStorage.removeItem(
      'nameChangeNewUserName',
  );

  signupStore.reset();

  await router.replace({
    path: '/setting/account-management/complete',
    query: {
      type: 'NAME_CHANGE',
    },
  });
};

// 휴대폰번호 변경 인증 완료
const handlePhoneChange = async () => {
  if (!authStore.userId) {
    await router.replace('/intro');
    return;
  }

  const tokenData =
      await changePhoneNumber({
        newPhoneNumber:
        signupStore.phoneAuth.phoneNumber,
      });

  authStore.updateTokens(tokenData);

  signupStore.reset();

  await router.replace({
    path: '/setting/account-management/complete',
    query: {
      type: 'PHONE_CHANGE',
    },
  });
};

// 회원가입 인증 완료
const handleSignup = async () => {
  const signupResponse =
      await loginApi.checkSignupStatus({
        phoneNumber:
        signupStore.phoneAuth.phoneNumber,
      });

  signupStore.setMemberStatus(
      signupResponse.memberStatus,
  );

  if (signupResponse.existingMember) {
    sessionStorage.setItem(
        'pinLoginPhoneNumber',
        signupStore.phoneAuth.phoneNumber,
    );

    await router.push(
        '/signup/existing-member',
    );

    return;
  }

  await router.push('/signup/new-member');
};

// 인증번호 확인
const verifyCode = async () => {
  if (
      loading.value ||
      expired.value ||
      attemptExceeded.value
  ) {
    return;
  }

  errorMessage.value = '';

  try {
    loading.value = true;

    const verificationPurpose =
        signupStore.phoneAuth
            .verificationPurpose;

    await loginApi.verifyPhoneAuthCode({
      phoneNumber:
      signupStore.phoneAuth.phoneNumber,
      verificationCode:
      verificationCode.value,
      verificationPurpose,
    });

    signupStore.setVerificationCode(
        verificationCode.value,
    );

    if (
        verificationPurpose ===
        'PIN_RESET'
    ) {
      await handlePinReset();
      return;
    }

    if (
        verificationPurpose ===
        'NAME_CHANGE'
    ) {
      await handleNameChange();
      return;
    }

    if (
        verificationPurpose ===
        'PHONE_CHANGE'
    ) {
      await handlePhoneChange();
      return;
    }

    await handleSignup();
  } catch (error) {
    console.error(error);

    const message =
        getVerificationErrorMessage(
            error,
            '인증번호가 일치하지 않습니다.',
        );

    errorMessage.value = message;

    if (
        message.includes(
            '입력 가능 횟수를 초과',
        )
    ) {
      attemptExceeded.value = true;
    }

    verificationCode.value = '';

    if (!attemptExceeded.value) {
      await focusInput();
    }
  } finally {
    loading.value = false;
  }
};

// 6자리 입력 완료 시 자동 인증
watch(
    verificationCode,
    async (code) => {
      if (
          code.length !== 6 ||
          loading.value ||
          expired.value ||
          attemptExceeded.value
      ) {
        return;
      }

      await verifyCode();
    },
);

// 인증번호 재전송
const resendCode = async () => {
  if (
      resendCount.value >= 1 ||
      resending.value
  ) {
    return;
  }

  try {
    resending.value = true;
    errorMessage.value = '';

    const response =
        await loginApi.resendPhoneAuthCode({
          phoneNumber:
          signupStore.phoneAuth.phoneNumber,
          verificationPurpose:
          signupStore.phoneAuth
              .verificationPurpose,
        });

    signupStore.setExpiresIn(
        response.expiresIn,
    );

    signupStore.setDevelopmentCode(
        response.verificationCode,
    );

    resendCount.value += 1;

    verificationCode.value = '';
    expired.value = false;
    attemptExceeded.value = false;

    timerKey.value += 1;

    await focusInput();
  } catch (error) {
    console.error(error);

    errorMessage.value =
        getVerificationErrorMessage(
            error,
            '인증번호 재전송에 실패했습니다.',
        );
  } finally {
    resending.value = false;
  }
};

// 본인인증 다시 시작
const restartVerification = async () => {
  const verificationPurpose =
      signupStore.phoneAuth
          .verificationPurpose;

  signupStore.setVerificationCode('');
  signupStore.setDevelopmentCode('');

  if (
      verificationPurpose ===
      'NAME_CHANGE'
  ) {
    await router.replace(
        '/setting/account-management/name',
    );
    return;
  }

  if (
      verificationPurpose ===
      'PHONE_CHANGE'
  ) {
    await router.replace(
        '/setting/account-management/phone',
    );
    return;
  }

  await router.push('/signup/check');
};

// 이전 화면
const goBack = async () => {
  const verificationPurpose =
      signupStore.phoneAuth
          .verificationPurpose;

  if (
      verificationPurpose ===
      'NAME_CHANGE'
  ) {
    await router.replace(
        '/setting/account-management/name',
    );
    return;
  }

  if (
      verificationPurpose ===
      'PHONE_CHANGE'
  ) {
    await router.replace(
        '/setting/account-management/phone',
    );
    return;
  }

  router.back();
};

onMounted(() => {
  focusInput();
});
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.verification-page {
  position: relative;
  background: var(--color-bg-page);
}

.verification-content {
  padding-top: 24px;
  overflow-y: auto;
}

/* 제목 */
.verification-header {
  flex-shrink: 0;
}

.verification-header h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.6px;
}

.verification-header p {
  margin: 14px 0 0;
  color: var(--color-text-sub);
  font-weight: 400;
  line-height: 1.6;
}

/* 인증번호 영역 */
.verification-section {
  margin-top: 48px;
}

/* 인증번호 입력 영역 */
.verification-input-area {
  width: 100%;
  border-bottom: 2px solid var(--color-border-main);
  box-sizing: border-box;
  transition:
      border-color 0.2s ease,
      opacity 0.2s ease;
}

.verification-input-area:focus-within {
  border-color: var(--color-primary);
}

.verification-input-area.error {
  border-color: var(--color-error);
}

.verification-input-area.disabled {
  opacity: 0.55;
}

/* 입력값 / 남은시간 / X */
.input-row {
  position: relative;
  display: flex;
  width: 100%;
  height: 58px;
  align-items: center;
}

.verification-input {
  width: 100%;
  height: 100%;
  padding: 0 118px 0 0;
  border: 0;
  outline: none;
  background: transparent;
  color: var(--color-text-main);
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 5px;
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

/* 남은 시간 + X */
.input-actions {
  position: absolute;
  top: 50%;
  right: 2px;
  display: flex;
  align-items: center;
  gap: 12px;
  transform: translateY(-50%);
}

.input-timer {
  color: var(--color-error);
  font-weight: 700;
  line-height: 1;
  white-space: nowrap;
}

/* X 버튼 */
.clear-button {
  display: flex;
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: var(--color-bg-screen);
  color: var(--color-text-sub);
  font-size: 13px;
  cursor: pointer;
}

.clear-button:active:not(:disabled) {
  background: var(--color-bg-disabled);
}

.clear-button:disabled {
  cursor: not-allowed;
}

/* 개발용 인증번호 + 재전송 */
.verification-sub-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 12px;
}

/* 개발용 인증번호만 12px */
.development-code {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 5px;
  margin: 0;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 400;
  line-height: 1.4;
  letter-spacing: -0.3px;
  white-space: nowrap;
}

.development-code strong {
  color: var(--color-text-sub);
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0;
}

/* 인증번호 재전송 */
.resend-button {
  flex-shrink: 0;
  min-height: 28px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-text-sub);
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.resend-button:active:not(:disabled) {
  opacity: 0.6;
}

.resend-button:disabled {
  color: var(--color-text-disabled);
  cursor: not-allowed;
  text-decoration: none;
}

/* 오류 영역 */
.message-area {
  margin-top: 12px;
}

.error-message {
  margin: 0;
  color: var(--color-error);
  line-height: 1.5;
}

/* 로딩 */
.loading-overlay {
  position: absolute;
  z-index: 200;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.86);
  color: var(--color-text-main);
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 4px solid var(--color-bg-disabled);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 360px) {
  .verification-input {
    padding-right: 108px;
    font-size: 22px;
  }

  .input-actions {
    gap: 9px;
  }

  .verification-sub-row {
    gap: 10px;
  }

  .development-code {
    gap: 4px;
  }
}
</style>