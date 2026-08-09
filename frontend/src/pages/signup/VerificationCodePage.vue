<template>
  <div class="signup-page">
    <!-- 1. 상단 영역 (Header + 뒤로가기 버튼) -->
    <header class="signup-header">
      <button class="back-button" type="button" @click="goBack">&lt;</button>
      <h1>인증번호 입력</h1>
      <p>문자로 받은 인증번호를 입력해 주세요.</p>
    </header>

    <!-- 2. 중앙 내용 영역 (휴대폰 본인인증 화면과 구조 통일) -->
    <main class="content-area">
      <div class="verification-info">
        <div class="timer-area">
          <span>입력시간</span>
          <VerificationTimer
            :key="timerKey"
            :seconds="signupStore.expiresIn"
            @expired="handleExpired"
          />
        </div>

        <button
          class="resend-button"
          :disabled="resending || resendCount >= 1"
          type="button"
          @click="resendCode"
        >
          {{ resending ? '재전송 중' : '재전송' }}
        </button>
      </div>

      <VerificationCodeInput v-model="verificationCode" :expired="expired" />

      <p v-if="signupStore.developmentCode" class="development-code">
        개발용 인증번호:
        {{ signupStore.developmentCode }}
      </p>

      <p v-if="expired && resendCount === 0" class="error-message">
        인증시간이 만료되었어요.<br />
        인증번호를 다시 받아 주세요.
      </p>

      <p v-else-if="expired && resendCount >= 1" class="error-message">
        인증 가능 횟수를 초과했어요.<br />
        본인인증을 다시 진행해 주세요.
      </p>

      <p v-else-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </p>
    </main>

    <!-- 3. 하단 버튼 영역 (위치 및 크기 고정) -->
    <div class="bottom-btn-area.single">
      <button
        v-if="expired && resendCount >= 1"
        class="bottom-btn"
        type="button"
        @click="restartVerification"
      >
        본인인증 다시 하기
      </button>

      <button
        v-else
        class="bottom-btn"
        :disabled="verificationCode.length !== 6 || loading || expired"
        type="button"
        @click="verifyCode"
      >
        {{ loading ? '확인 중...' : '확인' }}
      </button>
    </div>

    <!-- 로딩 오버레이 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <span>인증정보를 확인하고 있어요.</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import loginApi from '@/api/loginApi';
import { changePhoneNumber, changeUserName } from '@/api/userApi';
import VerificationCodeInput from '@/components/auth/VerificationCodeInput.vue';
import VerificationTimer from '@/components/auth/VerificationTimer.vue';
import { useAuthStore } from '@/stores/auth';
import { useSignupStore } from '@/stores/signup';

const router = useRouter();
const signupStore = useSignupStore();
const authStore = useAuthStore();

const verificationCode = ref('');
const errorMessage = ref('');
const loading = ref(false);
const resending = ref(false);
const expired = ref(false);
const timerKey = ref(0);
const resendCount = ref(0);

// 인증번호 오류 메시지 처리
const getVerificationErrorMessage = (error, fallbackMessage) => {
  if (!error.response)
    return '서버에 연결할 수 없습니다. 네트워크 상태를 확인해주세요.';

  const status = error.response.status;
  const serverMessage = error.response?.data?.message;

  if (status >= 500) return fallbackMessage;

  return serverMessage || fallbackMessage;
};

// 인증시간 만료
const handleExpired = () => {
  expired.value = true;
  verificationCode.value = '';
  errorMessage.value = '';
};

// PIN 재설정 인증 완료 처리
const handlePinReset = async () => {
  sessionStorage.setItem(
    'pinResetPhoneNumber',
    signupStore.phoneAuth.phoneNumber,
  );
  await router.push('/auth/pin-reset');
};

// 이름 변경 인증 완료 처리
const handleNameChange = async () => {
  const newUserName = sessionStorage.getItem('nameChangeNewUserName');

  if (!authStore.userId || !newUserName) {
    await router.replace('/setting/account-management/name');
    return;
  }

  await changeUserName(authStore.userId, {
    phoneNumber: signupStore.phoneAuth.phoneNumber,
    newUserName,
  });

  authStore.setUserName(newUserName);
  sessionStorage.removeItem('nameChangeNewUserName');
  signupStore.reset();

  await router.replace({
    path: '/setting/account-management/complete',
    query: { type: 'NAME_CHANGE' },
  });
};

// 휴대폰번호 변경 인증 완료 처리
const handlePhoneChange = async () => {
  if (!authStore.userId) {
    await router.replace('/intro');
    return;
  }

  await changePhoneNumber(authStore.userId, {
    newPhoneNumber: signupStore.phoneAuth.phoneNumber,
  });

  signupStore.reset();

  await router.replace({
    path: '/setting/account-management/complete',
    query: { type: 'PHONE_CHANGE' },
  });
};

// 회원가입 인증 완료 처리
const handleSignup = async () => {
  const signupResponse = await loginApi.checkSignupStatus({
    phoneNumber: signupStore.phoneAuth.phoneNumber,
  });

  signupStore.setMemberStatus(signupResponse.memberStatus);

  if (signupResponse.existingMember) {
    sessionStorage.setItem(
      'pinLoginPhoneNumber',
      signupStore.phoneAuth.phoneNumber,
    );
    await router.push('/signup/existing-member');
    return;
  }

  await router.push('/signup/new-member');
};

// 인증번호 확인
const verifyCode = async () => {
  if (loading.value || expired.value) return;

  errorMessage.value = '';

  try {
    loading.value = true;

    const verificationPurpose = signupStore.phoneAuth.verificationPurpose;

    await loginApi.verifyPhoneAuthCode({
      phoneNumber: signupStore.phoneAuth.phoneNumber,
      verificationCode: verificationCode.value,
      verificationPurpose,
    });

    signupStore.setVerificationCode(verificationCode.value);

    if (verificationPurpose === 'PIN_RESET') {
      await handlePinReset();
      return;
    }

    if (verificationPurpose === 'NAME_CHANGE') {
      await handleNameChange();
      return;
    }

    if (verificationPurpose === 'PHONE_CHANGE') {
      await handlePhoneChange();
      return;
    }

    await handleSignup();
  } catch (error) {
    console.error(error);

    errorMessage.value = getVerificationErrorMessage(
      error,
      '인증번호가 일치하지 않습니다.',
    );

    verificationCode.value = '';
  } finally {
    loading.value = false;
  }
};

// 인증번호 재발급
const resendCode = async () => {
  if (resendCount.value >= 1 || resending.value) return;

  try {
    resending.value = true;
    errorMessage.value = '';

    const response = await loginApi.resendPhoneAuthCode({
      phoneNumber: signupStore.phoneAuth.phoneNumber,
      verificationPurpose: signupStore.phoneAuth.verificationPurpose,
    });

    signupStore.setExpiresIn(response.expiresIn);
    signupStore.setDevelopmentCode(response.verificationCode);

    resendCount.value += 1;

    verificationCode.value = '';
    expired.value = false;
    timerKey.value += 1;
  } catch (error) {
    console.error(error);

    errorMessage.value = getVerificationErrorMessage(
      error,
      '인증번호 재전송에 실패했습니다.',
    );
  } finally {
    resending.value = false;
  }
};

// 본인인증 다시 시작
const restartVerification = async () => {
  const verificationPurpose = signupStore.phoneAuth.verificationPurpose;

  signupStore.setVerificationCode('');
  signupStore.setDevelopmentCode('');

  if (verificationPurpose === 'NAME_CHANGE') {
    await router.replace('/setting/account-management/name');
    return;
  }

  if (verificationPurpose === 'PHONE_CHANGE') {
    await router.replace('/setting/account-management/phone');
    return;
  }

  await router.push('/signup/check');
};

// 이전 화면
const goBack = async () => {
  const verificationPurpose = signupStore.phoneAuth.verificationPurpose;

  if (verificationPurpose === 'NAME_CHANGE') {
    await router.replace('/setting/account-management/name');
    return;
  }

  if (verificationPurpose === 'PHONE_CHANGE') {
    await router.replace('/setting/account-management/phone');
    return;
  }

  router.back();
};
</script>

<style scoped>
.signup-page {
  width: 100%;
  height: 100vh;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  /* 💡 버튼 위치를 일치시키기 위해 하단 패딩을 70px로 적용합니다 */
  padding: 36px 24px 70px;
  background: #ffffff;
}

/* 1. 상단 헤더 영역 */
.signup-header {
  flex-shrink: 0;
}

.back-button {
  align-self: flex-start;
  margin-bottom: 16px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 26px;
  line-height: 1;
  cursor: pointer;
}

.signup-header h1 {
  margin: 0 0 16px;
  color: #111111;
  font-size: 28px;
  font-weight: 700;
}

.signup-header p {
  margin: 0;
  color: #777777;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.4;
}

/* 2. 중앙 내용 영역 */
.content-area {
  flex: 1;
  min-height: 0;
  margin-top: 28px;
  overflow-y: auto;
  box-sizing: border-box;
  padding-right: 2px;
}

.verification-section {
  display: flex;
  flex-direction: column;
}

.verification-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.timer-area {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #222222;
  font-size: 14px;
  font-weight: 600;
}

.development-code {
  margin: 14px 0 0;
  color: #777777;
  font-size: 13px;
}

.resend-button {
  padding: 6px 12px;
  border: 1px solid #999999;
  border-radius: 16px;
  background: #ffffff;
  color: #444444;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.resend-button:disabled {
  border-color: #dddddd;
  color: #aaaaaa;
  cursor: not-allowed;
}

.error-message {
  margin: 16px 0 0;
  color: #e53935;
  font-size: 14px;
  line-height: 1.5;
}

/* 로딩 오버레이 */
.loading-overlay {
  position: absolute;
  inset: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.86);
  color: #333333;
  font-size: 15px;
  font-weight: 700;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 4px solid #eeeeee;
  border-top-color: #ffbc2e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
