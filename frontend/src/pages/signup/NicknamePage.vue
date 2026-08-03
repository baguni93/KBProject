<template>
  <main class="nickname-page">
    <section class="nickname-container">
      <div class="title-area">
        <h1>
          사용할 닉네임을<br />
          입력해주세요
        </h1>

        <p>닉네임은 회원가입 후에도 변경할 수 있어요.</p>
      </div>

      <NicknameForm :submitting="submitting" @submit="handleSignup" />
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

    const data = await signup(signupData);

    // 회원가입 완료 후 자동 로그인
    await authStore.login({
      phoneNumber,
      pinPassword,
    });

    sessionStorage.setItem('signupUserId', String(data.userId));

    signupStore.reset();

    await router.push({
      path: '/signup/complete',
      query: { userId: data.userId },
    });
  } catch (error) {
    alert(error.response?.data?.message || '회원가입에 실패했습니다.');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.nickname-page {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  padding: 24px 0;
  background: #f4f4f4;
  overflow: auto;
}

.nickname-container {
  display: flex;
  flex: none;
  flex-direction: column;
  width: 390px;
  height: 844px;
  padding: 26px 28px 32px;
  background: #ffffff;
  overflow: hidden;
}

.title-area {
  margin-bottom: 48px;
}

.title-area h1 {
  margin: 0;
  font-size: 28px;
  line-height: 1.4;
}

.title-area p {
  margin: 12px 0 0;
  color: #777777;
  font-size: 14px;
}
</style>