<template>
  <div class="intro-page">
    <main class="intro-container">
      <!-- 1. 상단 / 내용 영역 -->
      <section class="intro-content">
        <div class="service-badge">SOCIAL WALLET</div>

        <h1>
          결제는 순식간,<br />
          소통은 자연스럽게
        </h1>

        <p>
          결제와 송금은 간편하게,<br />
          친구들과의 금융 생활은 더 즐겁게
        </p>

        <div class="wallet-visual">
          <div class="visual-glow"></div>

          <div class="wallet-card back-card">
            <span class="card-label">SOCIAL</span>
            <span class="card-circle"></span>
          </div>

          <div class="wallet-card front-card">
            <div class="card-top">
              <span class="card-chip"></span>
              <span class="card-logo">W</span>
            </div>

            <div class="card-text">
              <strong>Social Wallet</strong>
              <span>MZ세대를 위한 소셜 전자지갑</span>
            </div>
          </div>

          <div class="message-bubble left-bubble">
            <span>♥</span>
          </div>

          <div class="message-bubble right-bubble">
            <span>₩</span>
          </div>
        </div>
      </section>

      <!-- 3. 하단 버튼 영역 -->
      <div class="bottom-btn-area.single">
        <button class="bottom-btn" type="button" @click="start">
          시작하기
        </button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

// 회원가입 시작
const start = () => {
  router.push('/signup/agreement');
};
</script>

<style scoped>
.intro-page {
  width: 100%;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow: hidden;
  padding: 12px;
}

.intro-container {
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 상단과 하단을 양쪽 끝으로 배치 */
  width: 100%;
  height: 100%;
  padding: 40px 28px 30px;
  background: #ffffff;
  box-sizing: border-box;
}

.intro-content {
  display: flex;
  flex-direction: column;
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

.service-badge {
  display: inline-flex;
  align-self: flex-start;
  align-items: center;
  min-height: 30px;
  padding: 0 14px;
  border-radius: 18px;
  background: #fff3cf;
  color: #b77700;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 1px;
}

.intro-content h1 {
  margin: 26px 0 0;
  color: #111111;
  font-size: 32px;
  font-weight: 800;
  line-height: 1.3;
  letter-spacing: -1px;
}

.intro-content p {
  margin: 22px 0 0;
  color: #777777;
  font-size: 17px;
  font-weight: 600;
  line-height: 1.6;
}

.wallet-visual {
  position: relative;
  width: 100%;
  height: 260px;
  margin-top: 24px;
}

.visual-glow {
  position: absolute;
  top: 30px;
  left: 50%;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(255, 188, 46, 0.32) 0%,
    rgba(255, 188, 46, 0.12) 45%,
    rgba(255, 188, 46, 0) 72%
  );
  transform: translateX(-50%);
}

.wallet-card {
  position: absolute;
  left: 50%;
  width: 230px;
  height: 142px;
  border-radius: 20px;
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.14);
  box-sizing: border-box;
}

.back-card {
  top: 50px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 20px;
  background: #222222;
  color: #ffffff;
  transform: translateX(-56%) rotate(-8deg);
}

.front-card {
  top: 82px;
  padding: 20px;
  background: linear-gradient(135deg, #ffc43d, #ffad12);
  color: #111111;
  transform: translateX(-44%) rotate(7deg);
}

.card-label {
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 1px;
}

.card-circle {
  width: 26px;
  height: 26px;
  border: 2px solid rgba(255, 255, 255, 0.7);
  border-radius: 50%;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-chip {
  width: 34px;
  height: 24px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.65);
}

.card-logo {
  display: flex;
  width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #111111;
  color: #ffffff;
  font-size: 14px;
  font-weight: 800;
}

.card-text {
  display: flex;
  flex-direction: column;
  margin-top: 26px;
}

.card-text strong {
  font-size: 18px;
  font-weight: 800;
}

.card-text span {
  margin-top: 4px;
  font-size: 11px;
  font-weight: 600;
}

.message-bubble {
  position: absolute;
  z-index: 3;
  display: flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  border: 4px solid #ffffff;
  border-radius: 50%;
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.14);
  font-size: 18px;
  font-weight: 800;
  box-sizing: border-box;
}

.left-bubble {
  top: 40px;
  left: 10px;
  background: #ffffff;
  color: #ffb516;
}

.right-bubble {
  right: 10px;
  bottom: 20px;
  background: #222222;
  color: #ffbc2e;
}
</style>
