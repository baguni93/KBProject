<template>
  <div class="intro-page">
    <main class="page-layout page-layout-top intro-layout">
      <!-- 배경 장식 -->
      <div class="background-orb orb-one"></div>
      <div class="background-orb orb-two"></div>

      <!-- 상단 콘텐츠 -->
      <section class="page-content intro-content">
        <div class="intro-copy">
          <div class="service-badge text-13-bold">
            <span class="badge-dot"></span>
            SOCIAL WALLET
          </div>

          <h1 class="text-32-bold">
            결제는 순식간,<br />
            소통은 자연스럽게
          </h1>

          <p class="text-15">
            결제와 송금은 간편하게,<br />
            친구들과의 금융 생활은 더 즐겁게
          </p>
        </div>

        <!-- 지갑 비주얼 -->
        <div class="wallet-visual">
          <div class="visual-glow"></div>
          <div class="visual-ring ring-one"></div>
          <div class="visual-ring ring-two"></div>

          <!-- 뒤 카드 -->
          <div class="wallet-card back-card">
            <div class="back-card-top">
              <span class="card-label text-13-bold">SOCIAL</span>
              <span class="card-circle"></span>
            </div>

            <div class="back-card-line"></div>
          </div>

          <!-- 앞 카드 -->
          <div class="wallet-card front-card">
            <div class="card-top">
              <span class="card-chip">
                <span></span>
              </span>

              <span class="card-logo">W</span>
            </div>

            <div class="card-text">
              <strong class="text-18-bold"> Social Wallet </strong>

              <span class="text-13"> MZ세대를 위한 소셜 전자지갑 </span>
            </div>
          </div>

          <!-- 소셜 버블 -->
          <div class="message-bubble left-bubble">
            <i class="fa-solid fa-heart"></i>
          </div>

          <div class="message-bubble right-bubble">
            <span>₩</span>
          </div>

          <!-- 작은 장식 -->
          <span class="visual-dot dot-one"></span>
          <span class="visual-dot dot-two"></span>
          <span class="visual-dot dot-three"></span>
        </div>

        <!-- 기능 미리보기 -->
        <div class="feature-preview">
          <div class="feature-item">
            <div class="feature-icon">
              <i class="fa-solid fa-paper-plane"></i>
            </div>

            <span class="text-13"> 간편 송금 </span>
          </div>

          <span class="feature-divider"></span>

          <div class="feature-item">
            <div class="feature-icon">
              <i class="fa-solid fa-credit-card"></i>
            </div>

            <span class="text-13"> 간편 결제 </span>
          </div>

          <span class="feature-divider"></span>

          <div class="feature-item">
            <div class="feature-icon">
              <i class="fa-solid fa-user-group"></i>
            </div>

            <span class="text-13"> 친구와 함께 </span>
          </div>
        </div>
      </section>

      <!-- 공통 하단 버튼 -->
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
@import '@/components/common/common/common.css';
@import '@/components/common/common/layout.css';

.intro-page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
}

.intro-layout {
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, #fffdf8 0%, #ffffff 38%, #ffffff 100%);
}

.intro-content {
  position: relative;
  z-index: 2;
  overflow-y: auto;
}

/* 배경 장식 */
.background-orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(1px);
}

.orb-one {
  top: -120px;
  right: -120px;
  width: 280px;
  height: 280px;
  background: rgba(255, 188, 46, 0.09);
  animation: orb-float-one 7s ease-in-out infinite;
}

.orb-two {
  top: 390px;
  left: -130px;
  width: 240px;
  height: 240px;
  background: rgba(157, 144, 255, 0.045);
  animation: orb-float-two 8s ease-in-out infinite;
}

/* 상단 카피 */
.intro-copy {
  padding-top: 10px;
}

.service-badge {
  display: inline-flex;
  min-height: 30px;
  align-items: center;
  gap: 7px;
  padding: 0 13px;
  border: 1px solid rgba(255, 188, 46, 0.16);
  border-radius: 999px;
  background: rgba(255, 244, 215, 0.9);
  color: #a66d00;
  letter-spacing: 0.8px;
  opacity: 0;
  transform: translateY(8px);
  animation: content-show 0.45s ease 0.05s forwards;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(255, 188, 46, 0.12);
}

.intro-copy h1 {
  margin: 24px 0 0;
  color: var(--color-text-main);
  line-height: 1.28;
  letter-spacing: -1px;
  opacity: 0;
  transform: translateY(12px);
  animation: content-show 0.5s ease 0.16s forwards;
}

.intro-copy p {
  margin: 18px 0 0;
  color: var(--color-text-sub);
  font-weight: 400;
  line-height: 1.65;
  opacity: 0;
  transform: translateY(10px);
  animation: content-show 0.5s ease 0.26s forwards;
}

/* 지갑 비주얼 */
.wallet-visual {
  position: relative;
  width: 100%;
  height: 270px;
  margin-top: 20px;
  opacity: 0;
  transform: translateY(18px);
  animation: visual-show 0.65s cubic-bezier(0.22, 1, 0.36, 1) 0.35s forwards;
}

.visual-glow {
  position: absolute;
  top: 24px;
  left: 50%;
  width: 250px;
  height: 250px;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(255, 188, 46, 0.29) 0%,
    rgba(255, 188, 46, 0.11) 42%,
    rgba(255, 188, 46, 0) 72%
  );
  transform: translateX(-50%);
  animation: glow-breathe 3.5s ease-in-out 1s infinite;
}

.visual-ring {
  position: absolute;
  left: 50%;
  border: 1px solid rgba(255, 188, 46, 0.12);
  border-radius: 50%;
  transform: translateX(-50%);
}

.ring-one {
  top: 50px;
  width: 205px;
  height: 205px;
}

.ring-two {
  top: 29px;
  width: 246px;
  height: 246px;
  opacity: 0.55;
}

/* 카드 공통 */
.wallet-card {
  position: absolute;
  left: 50%;
  width: 232px;
  height: 144px;
  border-radius: 21px;
  box-sizing: border-box;
  transform-origin: center;
}

/* 뒤 카드 */
.back-card {
  top: 48px;
  padding: 19px;
  background: linear-gradient(145deg, #292929 0%, #111111 100%);
  box-shadow: 0 18px 38px rgba(0, 0, 0, 0.14);
  color: #ffffff;
  transform: translateX(-57%) rotate(-8deg);
  animation: back-card-float 4s ease-in-out 1s infinite;
}

.back-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.card-label {
  color: rgba(255, 255, 255, 0.88);
  letter-spacing: 1px;
}

.card-circle {
  width: 25px;
  height: 25px;
  border: 2px solid rgba(255, 255, 255, 0.72);
  border-radius: 50%;
}

.back-card-line {
  width: 62px;
  height: 5px;
  margin-top: 56px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
}

/* 앞 카드 */
.front-card {
  top: 83px;
  z-index: 2;
  padding: 19px;
  background: linear-gradient(135deg, #ffd56a 0%, #ffc43d 43%, #ffab13 100%);
  box-shadow:
    0 20px 38px rgba(210, 141, 0, 0.2),
    0 7px 14px rgba(0, 0, 0, 0.08);
  color: var(--color-text-main);
  transform: translateX(-43%) rotate(7deg);
  animation: front-card-float 3.6s ease-in-out 1.1s infinite;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-chip {
  position: relative;
  display: block;
  width: 35px;
  height: 25px;
  overflow: hidden;
  border: 1px solid rgba(120, 75, 0, 0.1);
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.65);
}

.card-chip::before,
.card-chip::after,
.card-chip span {
  position: absolute;
  background: rgba(144, 94, 0, 0.16);
  content: '';
}

.card-chip::before {
  top: 0;
  bottom: 0;
  left: 16px;
  width: 1px;
}

.card-chip::after {
  top: 11px;
  right: 0;
  left: 0;
  height: 1px;
}

.card-chip span {
  top: 0;
  bottom: 0;
  left: 7px;
  width: 1px;
}

.card-logo {
  display: flex;
  width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(17, 17, 17, 0.92);
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
}

.card-text {
  display: flex;
  flex-direction: column;
  margin-top: 21px;
}

.card-text strong {
  color: #171717;
}

.card-text span {
  margin-top: 3px;
  color: rgba(17, 17, 17, 0.65);
}

/* 소셜 버블 */
.message-bubble {
  position: absolute;
  z-index: 4;
  display: flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  border: 4px solid rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  box-sizing: border-box;
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.12);
  font-size: 17px;
  font-weight: 700;
}

.left-bubble {
  top: 42px;
  left: 3px;
  background: #ffffff;
  color: #ffb516;
  animation: bubble-left 3.3s ease-in-out 1.2s infinite;
}

.right-bubble {
  right: 4px;
  bottom: 18px;
  background: #222222;
  color: var(--color-primary);
  animation: bubble-right 3.6s ease-in-out 1.3s infinite;
}

/* 작은 장식 */
.visual-dot {
  position: absolute;
  z-index: 3;
  display: block;
  border-radius: 50%;
}

.dot-one {
  top: 27px;
  right: 48px;
  width: 7px;
  height: 7px;
  background: #9d90ff;
  animation: dot-float 3s ease-in-out infinite;
}

.dot-two {
  bottom: 27px;
  left: 47px;
  width: 8px;
  height: 8px;
  background: #7bd6c7;
  animation: dot-float 3.5s ease-in-out 0.4s infinite reverse;
}

.dot-three {
  top: 87px;
  left: 17px;
  width: 5px;
  height: 5px;
  background: #ff9eaa;
  animation: dot-float 3.2s ease-in-out 0.7s infinite;
}

/* 기능 미리보기 */
.feature-preview {
  display: flex;
  width: 100%;
  min-height: 62px;
  align-items: center;
  justify-content: space-around;
  margin-top: -2px;
  padding: 9px 12px;
  border: 1px solid rgba(0, 0, 0, 0.045);
  border-radius: 18px;
  background: rgba(250, 250, 250, 0.74);
  box-sizing: border-box;
  opacity: 0;
  transform: translateY(10px);
  animation: content-show 0.45s ease 0.8s forwards;
}

.feature-item {
  display: flex;
  min-width: 72px;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  color: var(--color-text-sub);
}

.feature-icon {
  display: flex;
  width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  background: #fff4d7;
  color: #c48700;
  font-size: 12px;
}

.feature-divider {
  width: 1px;
  height: 26px;
  background: var(--color-divider);
}

/* 등장 */
@keyframes content-show {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes visual-show {
  from {
    opacity: 0;
    transform: translateY(18px) scale(0.97);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 움직임 */
@keyframes glow-breathe {
  0%,
  100% {
    opacity: 0.82;
    transform: translateX(-50%) scale(1);
  }

  50% {
    opacity: 1;
    transform: translateX(-50%) scale(1.08);
  }
}

@keyframes front-card-float {
  0%,
  100% {
    transform: translateX(-43%) translateY(0) rotate(7deg);
  }

  50% {
    transform: translateX(-43%) translateY(-4px) rotate(6deg);
  }
}

@keyframes back-card-float {
  0%,
  100% {
    transform: translateX(-57%) translateY(0) rotate(-8deg);
  }

  50% {
    transform: translateX(-57%) translateY(3px) rotate(-7deg);
  }
}

@keyframes bubble-left {
  0%,
  100% {
    transform: translateY(0) rotate(-4deg);
  }

  50% {
    transform: translateY(-6px) rotate(4deg);
  }
}

@keyframes bubble-right {
  0%,
  100% {
    transform: translateY(0) rotate(3deg);
  }

  50% {
    transform: translateY(-5px) rotate(-3deg);
  }
}

@keyframes dot-float {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-5px);
  }
}

@keyframes orb-float-one {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(-12px, 12px);
  }
}

@keyframes orb-float-two {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(14px, -10px);
  }
}

/* 작은 화면 */
@media (max-height: 700px) {
  .intro-copy h1 {
    margin-top: 18px;
  }

  .intro-copy p {
    margin-top: 12px;
  }

  .wallet-visual {
    height: 235px;
    margin-top: 10px;
    transform: scale(0.92);
    transform-origin: top center;
  }

  .feature-preview {
    margin-top: -15px;
  }
}

/* 모션 최소화 */
@media (prefers-reduced-motion: reduce) {
  .background-orb,
  .service-badge,
  .intro-copy h1,
  .intro-copy p,
  .wallet-visual,
  .visual-glow,
  .wallet-card,
  .message-bubble,
  .visual-dot,
  .feature-preview {
    opacity: 1;
    animation: none;
  }
}
</style>
