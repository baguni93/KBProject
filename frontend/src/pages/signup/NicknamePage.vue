<template>
  <div class="page-layout nickname-page">
    <!-- 공통 상단 헤더 -->
    <PageHeader
        custom-back
        @back="goBack"
    />

    <!-- 콘텐츠 -->
    <main class="page-content nickname-content">
      <!-- 회원가입 진행 단계 -->
      <div class="signup-progress" aria-label="회원가입 진행 단계">
        <span class="progress-step"></span>
        <span class="progress-line"></span>
        <span class="progress-step"></span>
        <span class="progress-line"></span>
        <span class="progress-step active"></span>
      </div>

      <!-- 제목 -->
      <header class="nickname-header">
        <h1 class="text-30-bold">
          닉네임 설정
        </h1>

        <p class="text-15">
          사용할 닉네임을 입력해 주세요.
        </p>
      </header>

      <!-- 닉네임 입력 -->
      <section class="nickname-section">
        <NicknameForm
            ref="nicknameFormRef"
            :submitting="submitting"
            @valid-change="handleValidChange"
            @submit="handleSignup"
        />
      </section>
    </main>

    <!-- 공통 하단 버튼 -->
    <div class="bottom-btn-area single">
      <button
          class="bottom-btn"
          type="button"
          :disabled="!nicknameValid || submitting"
          @click="handleSubmit"
      >
        {{ submitting ? '가입 중' : '회원가입' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { signup } from '@/api/userApi';
import NicknameForm from '@/components/auth/NicknameForm.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import { useSignupStore } from '@/stores/signup';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const signupStore = useSignupStore();
const authStore = useAuthStore();

const submitting = ref(false);
const nicknameValid = ref(false);
const nicknameFormRef = ref(null);

// 이전 화면
const goBack = () => {
  router.back();
};

// 닉네임 사용 가능 여부
const handleValidChange = (valid) => {
  nicknameValid.value = valid;
};

// 회원가입 버튼
const handleSubmit = () => {
  nicknameFormRef.value?.submitForm();
};

// 회원가입
const handleSignup = async (nickname) => {
  if (!signupStore.pinConfirmed) {
    alert('PIN 확인이 필요합니다.');
    await router.push('/signup/pin');
    return;
  }

  const phoneNumber = signupStore.phoneAuth.phoneNumber;
  const pinPassword = signupStore.pin;

  const signupData = {
    userName: signupStore.phoneAuth.userName,
    birthDate: signupStore.phoneAuth.birthDate,
    phoneNumber,
    pinPassword,
    nickname,
    agreementIds: signupStore.agreements
        .filter((agreement) => agreement.agreed)
        .map((agreement) => agreement.agreementId),
  };

  try {
    submitting.value = true;

    // 회원가입
    const data = await signup(signupData);

    // 회원가입 완료 후 자동 로그인
    await authStore.login({
      phoneNumber,
      pinPassword,
    });

    // 회원가입 사용자 ID 임시 저장
    sessionStorage.setItem('signupUserId', String(data.userId));

    // 가입 완료 화면 이동
    await router.push({
      path: '/signup/complete',
      query: { userId: data.userId },
    });
  } catch (error) {
    console.error('회원가입 또는 자동 로그인 실패:', error);
    alert(error.response?.data?.message || '회원가입에 실패했습니다.');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";
@import "@/components/common/common/button.css";

.nickname-page {
  position: relative;
  background: var(--color-bg-page);
}

.nickname-content {
  padding-top: 30px;
  overflow-y: auto;
}

/* 회원가입 진행 단계 */
.signup-progress {
  display: flex;
  align-items: center;
  align-self: flex-start;
  margin-bottom: 40px;
}

.progress-step {
  flex-shrink: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-border-main);
}

.progress-step.active {
  width: 44px;
  height: 12px;
  border-radius: 999px;
  background: var(--color-primary);
}

.progress-line {
  width: 38px;
  height: 1px;
  margin: 0 8px;
  background: var(--color-border-main);
}

/* 제목 */
.nickname-header {
  text-align: left;
}

.nickname-header h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.nickname-header p {
  margin: 14px 0 0;
  color: var(--color-text-sub);
  line-height: 1.6;
}

/* 닉네임 입력 */
.nickname-section {
  margin-top: 52px;
}
</style>