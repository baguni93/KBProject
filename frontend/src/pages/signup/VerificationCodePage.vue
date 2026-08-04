<template>
  <div class="signup-page">
    <main class="signup-container">
      <button class="back-button" type="button" @click="goBack">&lt;</button>

      <header class="signup-header">
        <h1>인증번호 입력</h1>
        <p>
          문자로 받은 인증번호를<br />
          입력해 주세요.
        </p>
      </header>

      <section class="verification-section">
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
          개발용 인증번호: {{ signupStore.developmentCode }}
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
      </section>

      <button
          v-if="expired && resendCount >= 1"
          class="confirm-button"
          type="button"
          @click="restartVerification"
      >
        본인인증 다시 하기
      </button>

      <button
          v-else
          class="confirm-button"
          :disabled="verificationCode.length !== 6 || loading || expired"
          type="button"
          @click="verifyCode"
      >
        {{ loading ? '확인 중...' : '확인' }}
      </button>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import loginApi from '@/api/loginApi';
import VerificationCodeInput from '@/components/auth/VerificationCodeInput.vue';
import VerificationTimer from '@/components/auth/VerificationTimer.vue';
import { useSignupStore } from '@/stores/signup';

const router = useRouter();
const signupStore = useSignupStore();

const verificationCode = ref('');
const errorMessage = ref('');
const loading = ref(false);
const resending = ref(false);
const expired = ref(false);
const timerKey = ref(0);
const resendCount = ref(0);

// 인증시간 만료
const handleExpired = () => {
  expired.value = true;
  verificationCode.value = '';
  errorMessage.value = '';
};

// 인증번호 확인
const verifyCode = async () => {
  try {
    loading.value = true;
    errorMessage.value = '';

    await loginApi.verifyPhoneAuthCode({
      phoneNumber: signupStore.phoneAuth.phoneNumber,
      verificationCode: verificationCode.value,
      verificationPurpose: signupStore.phoneAuth.verificationPurpose,
    });

    signupStore.setVerificationCode(verificationCode.value);

    if (signupStore.phoneAuth.verificationPurpose === 'PIN_RESET') {
      sessionStorage.setItem('pinResetPhoneNumber', signupStore.phoneAuth.phoneNumber);
      router.push('/auth/pin-reset');
      return;
    }

    const signupResponse = await loginApi.checkSignupStatus({
      phoneNumber: signupStore.phoneAuth.phoneNumber,
    });

    signupStore.setMemberStatus(signupResponse.memberStatus);

    if (signupResponse.existingMember) {
      sessionStorage.setItem('pinLoginPhoneNumber', signupStore.phoneAuth.phoneNumber);
      router.push('/signup/existing-member');
      return;
    }

    router.push('/signup/new-member');
  } catch (error) {
    console.error(error);
    errorMessage.value = '인증번호가 일치하지 않습니다.';
  } finally {
    loading.value = false;
  }
};

// 인증번호 재발급
const resendCode = async () => {
  if (resendCount.value >= 1) return;

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
    errorMessage.value = '인증번호 재전송에 실패했습니다.';
  } finally {
    resending.value = false;
  }
};

// 본인인증 다시 시작
const restartVerification = () => {
  signupStore.setVerificationCode('');
  signupStore.setDevelopmentCode('');
  router.push('/signup/check');
};

// 이전 화면
const goBack = () => {
  router.back();
};
</script>

<style scoped>
.signup-page {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  padding: 24px 0;
  background: #f4f4f4;
  overflow: auto;
}

.signup-container {
  display: flex;
  flex: none;
  flex-direction: column;
  width: 390px;
  height: 844px;
  padding: 26px 28px 30px;
  background: #ffffff;
  overflow: hidden;
}

.back-button {
  align-self: flex-start;
  margin-bottom: 28px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 28px;
  line-height: 1;
}

.signup-header h1 {
  margin: 0 0 20px;
  color: #111111;
  font-size: 28px;
  font-weight: 700;
}

.signup-header p {
  margin: 0;
  color: #777777;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.5;
}

.verification-section {
  margin-top: 60px;
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

.confirm-button {
  width: 100%;
  height: 58px;
  margin-top: auto;
  border: 1px solid #cc9200;
  border-radius: 10px;
  background: #ffbc2e;
  color: #111111;
  font-size: 18px;
  font-weight: 700;
}

.confirm-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #999999;
  cursor: not-allowed;
}
</style>