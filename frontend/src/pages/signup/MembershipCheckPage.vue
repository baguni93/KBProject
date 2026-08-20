<template>
  <div class="signup-page page-layout">
    <!-- 공통 상단바 -->
    <PageHeader custom-back @back="goBack" />

    <!-- 페이지 제목 -->
    <header class="signup-header">
      <Transition name="title-slide" mode="out-in">
        <h1 :key="currentStep" class="text-26-bold">
          {{ stepTitle }}
        </h1>
      </Transition>
    </header>

    <!-- 입력 영역 -->
    <main class="content-area page-content">
      <PhoneAuthForm
        ref="phoneAuthFormRef"
        :initial-value="signupStore.phoneAuth"
        :loading="loading"
        @step-change="handleStepChange"
        @phone-valid-change="handlePhoneValidChange"
        @submit="sendCode"
      />

      <p v-if="errorMessage" class="error-message text-13">
        {{ errorMessage }}
      </p>
    </main>

    <!-- 휴대폰번호 입력 완료 시에만 버튼 -->
    <Transition name="button-fade">
      <div
        v-if="currentStep === 4 && phoneValid"
        class="bottom-btn-area single"
      >
        <button
          class="bottom-btn"
          :disabled="loading"
          type="button"
          @click="handleButtonClick"
        >
          인증번호 받기
        </button>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import loginApi from '@/api/loginApi';
import PhoneAuthForm from '@/components/auth/PhoneAuthForm.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import { useSignupStore } from '@/stores/signup';

const router = useRouter();
const signupStore = useSignupStore();

const loading = ref(false);
const errorMessage = ref('');
const phoneAuthFormRef = ref(null);
const currentStep = ref(1);
const phoneValid = ref(false);

// 단계별 제목
const stepTitle = computed(() => {
  switch (currentStep.value) {
    case 1:
      return '이름을 입력해주세요.';
    case 2:
      return '생년월일을 입력해주세요.';
    case 3:
      return '통신사를 선택해주세요.';
    case 4:
      return '휴대폰 번호를 입력해주세요.';
    default:
      return '휴대폰 본인인증';
  }
});

// 단계 변경
const handleStepChange = (step) => {
  currentStep.value = step;
  errorMessage.value = '';

  if (step !== 4) phoneValid.value = false;
};

// 휴대폰번호 유효성 변경
const handlePhoneValidChange = (valid) => {
  phoneValid.value = valid;
};

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

// 인증번호 받기
const handleButtonClick = () => {
  phoneAuthFormRef.value?.submitForm();
};

// 이전
const goBack = () => {
  const handled = phoneAuthFormRef.value?.previousStep();

  if (!handled) router.back();
};
</script>

<style scoped>
@import '@/components/common/common/common.css';
@import '@/components/common/common/layout.css';

.signup-page {
  background: var(--color-bg-page);
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
}

.signup-header {
  min-height: 36px;
  flex-shrink: 0;
  margin-top: 24px;
  overflow: hidden;
}

.signup-header h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
}

.content-area {
  margin-top: 36px;
  overflow-x: hidden;
  overflow-y: auto;
  box-sizing: border-box;
}

.error-message {
  margin: 16px 0 0;
  color: var(--color-error);
}

/* 제목 단계 전환 */
.title-slide-enter-active,
.title-slide-leave-active {
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}

.title-slide-enter-from {
  opacity: 0;
  transform: translateX(16px);
}

.title-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

/* 하단 버튼 등장 */
.button-fade-enter-active,
.button-fade-leave-active {
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}

.button-fade-enter-from,
.button-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (prefers-reduced-motion: reduce) {
  .title-slide-enter-active,
  .title-slide-leave-active,
  .button-fade-enter-active,
  .button-fade-leave-active {
    transition: none;
  }
}
</style>
