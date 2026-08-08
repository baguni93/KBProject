<template>
  <div class="signup-page">
    <!-- 1. 상단 영역 (Header + 뒤로가기 버튼) -->
    <header class="signup-header">
      <button class="back-button" type="button" @click="goBack">&lt;</button>
      <h1>휴대폰 본인인증</h1>
      <p>본인 확인을 위해 정보를 입력해주세요.</p>
    </header>

    <!-- 2. 중앙 내용 영역 (내용이 길어지면 내부 스크롤) -->
    <main class="content-area">
      <PhoneAuthForm
        ref="phoneAuthFormRef"
        :initial-value="signupStore.phoneAuth"
        :loading="loading"
        @submit="sendCode"
      />

      <p v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </p>
    </main>

    <!-- 3. 하단 버튼 영역 (버튼 크기 및 위치 고정) -->
    <div class="bottom-btn-area.single">
      <button
        class="bottom-btn"
        :disabled="loading"
        type="button"
        @click="handleButtonClick"
      >
        인증번호 받기
      </button>
    </div>
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
const phoneAuthFormRef = ref(null);

// 인증번호 발급
const sendCode = async (formData) => {
  try {
    loading.value = true;
    errorMessage.value = '';

    const requestData = {
      ...formData,
      verificationPurpose: signupStore.phoneAuth.verificationPurpose,
    };
    const response = await loginApi.sendPhoneAuthCode(requestData);

    signupStore.setPhoneAuth(requestData);
    signupStore.setExpiresIn(response.expiresIn);
    signupStore.setDevelopmentCode(response.verificationCode);

    router.push('/signup/verification');
  } catch (error) {
    console.error(error);
    errorMessage.value =
      error.response?.data?.message || '인증번호 발급에 실패했습니다.';
  } finally {
    loading.value = false;
  }
};

// 하단 버튼 클릭 시 폼 제출 함수 실행
const handleButtonClick = () => {
  if (phoneAuthFormRef.value?.submitForm) {
    phoneAuthFormRef.value.submitForm();
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
  height: 100dvh;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  /* 💡 하단 패딩을 20px -> 40px로 늘려 버튼 위치를 위로 올립니다 */
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

/* 2. 중앙 내용 영역 (내용이 길어지면 내부 스크롤) */
.content-area {
  flex: 1;
  min-height: 0;
  margin-top: 28px;
  overflow-y: auto;
  box-sizing: border-box;
}

.error-message {
  margin: 16px 0 0;
  color: #d32f2f;
  font-size: 14px;
}

/* 3. 하단 버튼 영역 */
.button-area {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 16px;
  background: #ffffff;
}

.next-btn {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  background: #ffc400;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.next-btn:active:not(:disabled) {
  background: #f3aa0b;
}

.next-btn:disabled {
  background: #e0e0e0;
  color: #9e9e9e;
  cursor: not-allowed;
}
</style>
