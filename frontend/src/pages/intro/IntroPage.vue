<template>
  <div class="intro-page">
    <main class="page-layout page-layout-top">
      <!-- 상단 콘텐츠 -->
      <section class="page-content">
        <div class="service-badge text-13-bold">
          SOCIAL WALLET
        </div>

        <h1 class="text-32-bold">
          결제는 순식간,<br />
          소통은 자연스럽게
        </h1>

        <p class="text-15-bold">
          결제와 송금은 간편하게,<br />
          친구들과의 금융 생활은 더 즐겁게
        </p>

        <div class="wallet-visual">
          <div class="visual-glow"></div>

          <div class="wallet-card back-card">
            <span class="card-label text-13-bold">SOCIAL</span>
            <span class="card-circle"></span>
          </div>

          <div class="wallet-card front-card">
            <div class="card-top">
              <span class="card-chip"></span>
              <span class="card-logo">W</span>
            </div>

            <div class="card-text">
              <strong class="text-18-bold">Social Wallet</strong>
              <span class="text-13">MZ세대를 위한 소셜 전자지갑</span>
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

      <!-- 하단 버튼 -->
      <div class="bottom-btn-area single">
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
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.intro-page {
  width: 100%;
  height: 100dvh;
  box-sizing: border-box;
  background: var(--color-bg-page);
  overflow: hidden;
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
  letter-spacing: 1px;
}

.page-content h1 {
  margin: 26px 0 0;
  color: var(--color-text-main);
  line-height: 1.3;
  letter-spacing: -1px;
}

.page-content p {
  margin: 22px 0 0;
  color: var(--color-text-sub);
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
  box-sizing: border-box;
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.14);
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
  color: var(--color-text-main);
  transform: translateX(-44%) rotate(7deg);
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
  font-weight: 600;
}

.card-text {
  display: flex;
  flex-direction: column;
  margin-top: 26px;
}

.card-text span {
  margin-top: 4px;
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
  box-sizing: border-box;
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.14);
  font-size: 18px;
  font-weight: 600;
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
  color: var(--color-primary);
}
</style>