<template>
  <div class="member-page">
    <main class="member-container">
      <section class="member-content">
        <div class="member-visual">
          <div class="visual-card">
            <span class="person person-left"></span>
            <span class="person person-center"></span>
            <span class="person person-right"></span>
          </div>

          <span class="confetti confetti-one"></span>
          <span class="confetti confetti-two"></span>
          <span class="confetti confetti-three"></span>
          <span class="confetti confetti-four"></span>
        </div>

        <h1>이미 가입된 회원이에요!</h1>

        <p class="member-description">
          등록한 간편비밀번호로<br />
          빠르고 안전하게 로그인해 주세요.
        </p>

        <section class="guide-list">
          <article class="guide-item">
            <div class="guide-icon">✓</div>

            <div>
              <strong>본인인증 완료</strong>
              <p>가입된 회원 정보를 확인했어요.</p>
            </div>
          </article>

          <article class="guide-item">
            <div class="guide-icon">●</div>

            <div>
              <strong>간편한 PIN 로그인</strong>
              <p>6자리 간편비밀번호로 로그인할 수 있어요.</p>
            </div>
          </article>

          <article class="guide-item">
            <div class="guide-icon">🔒</div>

            <div>
              <strong>안전한 금융 서비스</strong>
              <p>회원님의 금융 정보를 안전하게 보호해요.</p>
            </div>
          </article>
        </section>
      </section>

      <button class="bottom-button" type="button" @click="goLogin">
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
  if (!signupStore.phoneAuth.phoneNumber) {
    router.replace('/intro');
    return;
  }

  router.push('/auth/pin-login');
};
</script>

<style scoped>
.member-page {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  padding: 24px 0;
  background: #f4f4f4;
  overflow: auto;
}

.member-container {
  display: flex;
  flex: none;
  flex-direction: column;
  width: 390px;
  height: 844px;
  padding: 52px 28px 30px;
  background: #ffffff;
  overflow: hidden;
}

.member-content {
  text-align: center;
}

.member-visual {
  position: relative;
  width: 150px;
  height: 150px;
  margin: 18px auto 32px;
}

.visual-card {
  position: absolute;
  top: 28px;
  left: 50%;
  display: flex;
  width: 112px;
  height: 104px;
  align-items: center;
  justify-content: center;
  border-radius: 28px;
  background: linear-gradient(145deg, #fff4cf, #ffe59a);
  box-shadow: 0 18px 34px rgba(255, 188, 46, 0.22);
  transform: translateX(-50%);
}

.person {
  position: absolute;
  bottom: 24px;
  width: 30px;
  height: 38px;
  border-radius: 16px 16px 10px 10px;
  background: #5c6670;
}

.person::before {
  position: absolute;
  top: -19px;
  left: 5px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #7f8992;
  content: '';
}

.person-left {
  left: 17px;
  transform: scale(0.86);
}

.person-center {
  z-index: 2;
  left: 41px;
  background: #333333;
}

.person-center::before {
  background: #555555;
}

.person-right {
  right: 17px;
  transform: scale(0.86);
}

.confetti {
  position: absolute;
  width: 8px;
  height: 18px;
  border-radius: 4px;
}

.confetti-one {
  top: 18px;
  left: 18px;
  background: #ffbc2e;
  transform: rotate(-35deg);
}

.confetti-two {
  top: 12px;
  right: 22px;
  background: #6c8cff;
  transform: rotate(42deg);
}

.confetti-three {
  bottom: 16px;
  left: 12px;
  background: #63ca72;
  transform: rotate(30deg);
}

.confetti-four {
  right: 10px;
  bottom: 24px;
  background: #ff8a65;
  transform: rotate(-28deg);
}

.member-content h1 {
  margin: 0;
  color: #111111;
  font-size: 27px;
  font-weight: 800;
  letter-spacing: -0.7px;
}

.member-description {
  margin: 18px 0 0;
  color: #777777;
  font-size: 16px;
  font-weight: 500;
  line-height: 1.6;
}

.guide-list {
  margin-top: 48px;
  padding: 6px 18px;
  border-radius: 20px;
  background: #fafafa;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.05);
  text-align: left;
}

.guide-item {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 86px;
  border-bottom: 1px solid #eeeeee;
}

.guide-item:last-child {
  border-bottom: 0;
}

.guide-icon {
  display: flex;
  flex: none;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff2c9;
  color: #bc7d00;
  font-size: 17px;
  font-weight: 800;
}

.guide-item strong {
  display: block;
  color: #222222;
  font-size: 15px;
  font-weight: 700;
}

.guide-item p {
  margin: 5px 0 0;
  color: #888888;
  font-size: 12px;
  line-height: 1.4;
}

.bottom-button {
  width: 100%;
  height: 58px;
  margin-top: auto;
  border: 1px solid #d99900;
  border-radius: 12px;
  background: #ffbc2e;
  color: #111111;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
}

.bottom-button:active {
  background: #f2aa10;
}
</style>