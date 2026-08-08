<template>
  <div class="signup-page">
    <main class="signup-container">
      <button class="back-button" type="button" @click="goBack">
        &lt;
      </button>

      <header class="signup-header">
        <h1>휴대폰 본인인증</h1>
        <p>본인 확인을 위해<br />정보를 입력해주세요.</p>
      </header>

      <PhoneAuthForm :initial-value="signupStore.phoneAuth" :loading="loading" @submit="sendCode" />

      <p v-if="errorMessage" class="page-error">
        {{ errorMessage }}
      </p>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import loginApi from '@/api/loginApi';
import PhoneAuthForm from '@/components/auth/PhoneAuthForm.vue';
import { useSignupStore } from '@/stores/signup';

const router = useRouter();
const signupStore = useSignupStore();
const loading = ref(false);
const errorMessage = ref('');

// 인증번호 발급
const sendCode = async (formData) => {
  try {
    loading.value = true;
    errorMessage.value = '';

    const requestData = { ...formData, verificationPurpose: signupStore.phoneAuth.verificationPurpose };
    const response = await loginApi.sendPhoneAuthCode(requestData);

    signupStore.setPhoneAuth(requestData);
    signupStore.setExpiresIn(response.expiresIn);
    signupStore.setDevelopmentCode(response.verificationCode);

    router.push('/signup/verification');
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '인증번호 발급에 실패했습니다.';
  } finally {
    loading.value = false;
  }
};

// 이전 화면
const goBack = () => {
  router.back();
};
</script>

<style scoped>
.signup-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.signup-container {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
  padding: 26px 28px 140px;
  background: #ffffff;
  box-sizing: border-box;
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
  cursor: pointer;
}

.signup-header {
  margin-bottom: 44px;
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
  font-size: 19px;
  font-weight: 600;
  line-height: 1.45;
}

.page-error {
  margin: 16px 0 0;
  color: #d32f2f;
  font-size: 14px;
}

/* PhoneAuthForm 안의 인증번호 받기 버튼 */
:deep(.submit-button) {
  position: absolute;
  right: 28px;
  bottom: 58px;
  left: 28px;
  width: auto;
  height: 58px;
  margin: 0;
}
</style>