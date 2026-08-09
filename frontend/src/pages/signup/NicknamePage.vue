<template>
  <main class="nickname-page">
    <section class="nickname-container">
      <button
          class="back-button"
          type="button"
          @click="goBack"
      >
        &lt;
      </button>

      <div
          class="signup-progress"
          aria-label="회원가입 진행 단계"
      >
        <span class="progress-step"></span>
        <span class="progress-line"></span>
        <span class="progress-step"></span>
        <span class="progress-line"></span>
        <span class="progress-step active"></span>
      </div>

      <div class="title-area">
        <h1>닉네임 설정</h1>

        <p>사용할 닉네임을 입력해주세요.</p>
      </div>

      <NicknameForm
          :submitting="submitting"
          @submit="handleSignup"
      />
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { signup } from '@/api/userApi';
import NicknameForm from '@/components/auth/NicknameForm.vue';
import { useSignupStore } from '@/stores/signup';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const signupStore = useSignupStore();
const authStore = useAuthStore();
const submitting = ref(false);

// 이전 화면
const goBack = () => {
  router.back();
};

// 회원가입
const handleSignup = async (nickname) => {
  if (!signupStore.pinConfirmed) {
    alert('PIN 확인이 필요합니다.');
    await router.push('/signup/pin');
    return;
  }

  const phoneNumber =
      signupStore.phoneAuth.phoneNumber;

  const pinPassword =
      signupStore.pin;

  const signupData = {
    userName:
    signupStore.phoneAuth.userName,
    birthDate:
    signupStore.phoneAuth.birthDate,
    phoneNumber,
    pinPassword,
    nickname,
    agreementIds:
        signupStore.agreements
            .filter(
                (agreement) =>
                    agreement.agreed,
            )
            .map(
                (agreement) =>
                    agreement.agreementId,
            ),
  };

  try {
    submitting.value = true;

    const data =
        await signup(signupData);

    // 회원가입 완료 후 자동 로그인
    await authStore.login({
      phoneNumber,
      pinPassword,
    });

    sessionStorage.setItem(
        'signupUserId',
        String(data.userId),
    );

    signupStore.reset();

    await router.push({
      path: '/signup/complete',
      query: {
        userId: data.userId,
      },
    });
  } catch (error) {
    alert(
        error.response?.data?.message
        || '회원가입에 실패했습니다.',
    );
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.nickname-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.nickname-container {
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
  margin-bottom: 34px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 28px;
  line-height: 1;
  cursor: pointer;
}

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
  background: #dddddd;
}

.progress-step.active {
  width: 44px;
  height: 12px;
  border-radius: 999px;
  background: #ffbc2e;
}

.progress-line {
  width: 38px;
  height: 1px;
  margin: 0 8px;
  background: #dddddd;
}

.title-area {
  margin-bottom: 48px;
}

.title-area h1 {
  margin: 0 0 20px;
  color: #111111;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.4;
}

.title-area p {
  margin: 0;
  color: #777777;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.35;
}
</style>