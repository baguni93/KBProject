<template>
  <div class="complete-page">
    <main class="complete-container">
      <section class="complete-content">
        <div class="complete-visual">
          <div class="check-circle">✓</div>

          <span class="confetti confetti-one"></span>
          <span class="confetti confetti-two"></span>
          <span class="confetti confetti-three"></span>
          <span class="confetti confetti-four"></span>
          <span class="confetti confetti-five"></span>
          <span class="confetti confetti-six"></span>
        </div>

        <h1>간편비밀번호가<br />재설정되었어요!</h1>

        <p>
          새로운 간편비밀번호로<br />
          안전하게 로그인해 주세요.
        </p>

        <section class="guide-area">
          <div class="guide-icon">🔒</div>

          <div>
            <strong>새로운 PIN이 적용되었어요</strong>
            <p>이전 간편비밀번호는 더 이상 사용할 수 없어요.</p>
          </div>
        </section>
      </section>

      <button class="login-button" type="button" @click="goLogin">
        로그인
      </button>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useSignupStore } from '@/stores/signup';

const router = useRouter();
const signupStore = useSignupStore();

// PIN 로그인 화면 이동
const goLogin = () => {
  const phoneNumber = sessionStorage.getItem('pinResetPhoneNumber');

  if (!phoneNumber) {
    router.replace('/intro');
    return;
  }

  sessionStorage.setItem('pinLoginPhoneNumber', phoneNumber);
  sessionStorage.removeItem('pinResetPhoneNumber');
  sessionStorage.removeItem('pinResetCompleted');

  signupStore.reset();
  router.replace('/auth/pin-login');
};
</script>

<style scoped>
.complete-page {
  width: 100%;
  min-height: 100%;
  padding: 58px 28px 30px;
  background: #ffffff;
}

.complete-container {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.complete-content {
  text-align: center;
}

.complete-visual {
  position: relative;
  width: 170px;
  height: 170px;
  margin: 34px auto 36px;
}

.check-circle {
  position: absolute;
  top: 34px;
  left: 50%;
  display: flex;
  width: 104px;
  height: 104px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffbc2e;
  box-shadow: 0 20px 38px rgba(255, 188, 46, 0.28);
  color: #ffffff;
  font-size: 54px;
  font-weight: 500;
  transform: translateX(-50%);
}

.confetti {
  position: absolute;
  width: 7px;
  height: 18px;
  border-radius: 4px;
}

.confetti-one {
  top: 16px;
  left: 22px;
  background: #ffbc2e;
  transform: rotate(-35deg);
}

.confetti-two {
  top: 8px;
  right: 28px;
  background: #6e8cff;
  transform: rotate(38deg);
}

.confetti-three {
  top: 76px;
  left: 2px;
  background: #67ca76;
  transform: rotate(54deg);
}

.confetti-four {
  top: 72px;
  right: 0;
  background: #f08ebf;
  transform: rotate(-48deg);
}

.confetti-five {
  bottom: 12px;
  left: 27px;
  background: #ff8a65;
  transform: rotate(26deg);
}

.confetti-six {
  right: 30px;
  bottom: 8px;
  background: #ffbc2e;
  transform: rotate(-26deg);
}

.complete-content h1 {
  margin: 0;
  color: #111111;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.8px;
}

.complete-content > p {
  margin: 20px 0 0;
  color: #777777;
  font-size: 16px;
  font-weight: 500;
  line-height: 1.65;
}

.guide-area {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 54px;
  padding: 18px;
  border-radius: 18px;
  background: #fff9e9;
  text-align: left;
}

.guide-icon {
  display: flex;
  flex: none;
  width: 46px;
  height: 46px;
  align-items: center;
  justify-content: center;
  border-radius: 15px;
  background: #ffbc2e;
  font-size: 20px;
}

.guide-area strong {
  display: block;
  color: #222222;
  font-size: 14px;
  font-weight: 800;
}

.guide-area p {
  margin: 6px 0 0;
  color: #888888;
  font-size: 11px;
  line-height: 1.5;
}

.login-button {
  width: 100%;
  height: 58px;
  margin-top: auto;
  border: 1px solid #cc9200;
  border-radius: 12px;
  background: #ffbc2e;
  color: #111111;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
}

.login-button:active {
  background: #f2aa10;
}
</style>