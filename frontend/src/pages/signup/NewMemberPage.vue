<template>
  <div class="page-layout page-layout-top member-page">
    <!-- 배경 장식 -->
    <div class="background-decoration decoration-left"></div>
    <div class="background-decoration decoration-right"></div>

    <main class="page-content content-area">
      <section class="member-content">
        <!-- 메인 비주얼 -->
        <div class="member-visual">
          <div class="circle-background"></div>

          <div class="member-icon">
            <i class="fa-solid fa-user-plus"></i>
          </div>

          <!-- 주변 장식 -->
          <span class="deco deco-one"></span>
          <span class="deco deco-two"></span>
          <span class="deco deco-three"></span>
          <span class="deco deco-four"></span>

          <i class="fa-solid fa-star sparkle sparkle-one"></i>
          <i class="fa-solid fa-star sparkle sparkle-two"></i>
        </div>

        <!-- 메시지 -->
        <div class="member-message">
          <h1 class="text-30-bold">
            신규 회원이에요!
          </h1>

          <p class="text-15">
            간편비밀번호를 설정하고<br />
            Social Wallet을 시작해 보세요.
          </p>
        </div>

        <!-- 안내 카드 -->
        <div class="guide-card">
          <div class="guide-icon">
            <i class="fa-solid fa-lock"></i>
          </div>

          <div class="guide-text">
            <strong class="text-15-bold">
              간편비밀번호 설정
            </strong>

            <p class="text-13">
              로그인에 사용할 PIN을 등록해 주세요.
            </p>
          </div>
        </div>
      </section>
    </main>

    <!-- 공통 하단 버튼 -->
    <div class="bottom-btn-area single">
      <button
          class="bottom-btn"
          type="button"
          @click="goPin"
      >
        다음
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

// PIN 설정 화면 이동
const goPin = () => {
  router.push('/signup/pin');
};
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.member-page {
  position: relative;
  overflow: hidden;
  background: linear-gradient(
      180deg,
      #fffdf8 0%,
      #ffffff 42%,
      #ffffff 100%
  );
}

.content-area {
  position: relative;
  z-index: 2;
  min-height: 0;
  overflow-y: auto;
  padding-right: 2px;
}

.member-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 54px;
  text-align: center;
}

/* 배경 장식 */
.background-decoration {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.decoration-left {
  top: -90px;
  left: -110px;
  width: 220px;
  height: 220px;
  background: rgba(255, 205, 91, 0.13);
  animation: background-float-left 6s ease-in-out infinite;
}

.decoration-right {
  top: 240px;
  right: -110px;
  width: 200px;
  height: 200px;
  background: rgba(176, 164, 255, 0.06);
  animation: background-float-right 7s ease-in-out infinite;
}

/* 메인 비주얼 */
.member-visual {
  position: relative;
  width: 138px;
  height: 138px;
  margin-bottom: 28px;
}

.circle-background {
  position: absolute;
  inset: 4px;
  border-radius: 50%;
  background: linear-gradient(
      135deg,
      rgba(255, 188, 46, 0.17),
      rgba(255, 225, 155, 0.07)
  );
  opacity: 0;
  transform: scale(0.76);
  animation:
      circle-enter 0.5s ease 0.05s forwards,
      circle-pulse 2.5s ease-in-out 0.9s infinite;
}

.member-icon {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 2;
  display: flex;
  width: 90px;
  height: 90px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(
      135deg,
      #ffca52 0%,
      var(--color-primary) 65%,
      #f3a711 100%
  );
  box-shadow: 0 14px 30px rgba(255, 188, 46, 0.26);
  color: #ffffff;
  opacity: 0;
  transform: scale(0.55);
  animation: member-pop 0.58s cubic-bezier(0.34, 1.56, 0.64, 1) 0.12s forwards;
}

.member-icon i {
  font-size: 34px;
  opacity: 0;
  transform: scale(0.5) rotate(-12deg);
  animation: icon-show 0.3s ease 0.46s forwards;
}

/* 작은 장식 */
.deco {
  position: absolute;
  display: block;
  border-radius: 50%;
  opacity: 0;
  transform: scale(0);
}

.deco-one {
  top: 14px;
  right: 10px;
  width: 9px;
  height: 9px;
  background: #ffca52;
  animation:
      deco-pop 0.4s ease 0.4s forwards,
      deco-float 3.2s ease-in-out 1s infinite;
}

.deco-two {
  top: 42px;
  left: 3px;
  width: 7px;
  height: 7px;
  background: #9d90ff;
  animation:
      deco-pop 0.4s ease 0.48s forwards,
      deco-float 3.6s ease-in-out 1.1s infinite reverse;
}

.deco-three {
  right: 4px;
  bottom: 27px;
  width: 7px;
  height: 7px;
  background: #7bd6c7;
  animation:
      deco-pop 0.4s ease 0.56s forwards,
      deco-float 3.4s ease-in-out 1.15s infinite;
}

.deco-four {
  bottom: 9px;
  left: 25px;
  width: 8px;
  height: 8px;
  background: #ff9eaa;
  animation:
      deco-pop 0.4s ease 0.62s forwards,
      deco-float 3.8s ease-in-out 1.2s infinite reverse;
}

.sparkle {
  position: absolute;
  opacity: 0;
  transform: scale(0);
}

.sparkle-one {
  top: 2px;
  left: 31px;
  color: var(--color-primary);
  font-size: 11px;
  animation:
      sparkle-pop 0.45s ease 0.5s forwards,
      sparkle-float 3s ease-in-out 1.1s infinite;
}

.sparkle-two {
  right: 15px;
  bottom: 6px;
  color: #a99df7;
  font-size: 9px;
  animation:
      sparkle-pop 0.45s ease 0.66s forwards,
      sparkle-float 3.5s ease-in-out 1.2s infinite reverse;
}

/* 메시지 */
.member-message {
  opacity: 0;
  transform: translateY(16px);
  animation: content-up 0.48s ease 0.6s forwards;
}

.member-message h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.3;
  letter-spacing: -0.6px;
}

.member-message p {
  margin: 16px 0 0;
  color: var(--color-text-sub);
  line-height: 1.65;
}

/* 안내 카드 */
.guide-card {
  display: flex;
  width: 100%;
  align-items: center;
  margin-top: 36px;
  padding: 18px;
  border: 1px solid rgba(255, 188, 46, 0.15);
  border-radius: 18px;
  background: linear-gradient(
      110deg,
      #fff9ec 0%,
      #fffdf8 70%,
      #faf8ff 100%
  );
  box-sizing: border-box;
  text-align: left;
  opacity: 0;
  transform: translateY(14px);
  animation: content-up 0.48s ease 0.74s forwards;
}

.guide-icon {
  display: flex;
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  border-radius: 14px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(255, 188, 46, 0.12);
  color: #d9980d;
  font-size: 17px;
  animation: guide-float 2.8s ease-in-out 1.3s infinite;
}

.guide-text {
  min-width: 0;
}

.guide-text strong {
  display: block;
  color: var(--color-text-main);
}

.guide-text p {
  margin: 5px 0 0;
  color: var(--color-text-sub);
  line-height: 1.45;
}

/* 애니메이션 */
@keyframes circle-enter {
  from {
    opacity: 0;
    transform: scale(0.76);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes circle-pulse {
  0%,
  100% {
    opacity: 0.8;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.08);
  }
}

@keyframes member-pop {
  0% {
    opacity: 0;
    transform: scale(0.55);
  }

  70% {
    opacity: 1;
    transform: scale(1.08);
  }

  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes icon-show {
  from {
    opacity: 0;
    transform: scale(0.5) rotate(-12deg);
  }

  to {
    opacity: 1;
    transform: scale(1) rotate(0);
  }
}

@keyframes deco-pop {
  0% {
    opacity: 0;
    transform: scale(0);
  }

  70% {
    opacity: 1;
    transform: scale(1.3);
  }

  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes deco-float {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-5px);
  }
}

@keyframes sparkle-pop {
  0% {
    opacity: 0;
    transform: scale(0) rotate(-20deg);
  }

  70% {
    opacity: 1;
    transform: scale(1.25) rotate(10deg);
  }

  100% {
    opacity: 1;
    transform: scale(1) rotate(0);
  }
}

@keyframes sparkle-float {
  0%,
  100% {
    transform: translateY(0) rotate(0);
  }

  50% {
    transform: translateY(-4px) rotate(10deg);
  }
}

@keyframes content-up {
  from {
    opacity: 0;
    transform: translateY(16px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes guide-float {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-3px);
  }
}

@keyframes background-float-left {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(12px, 10px);
  }
}

@keyframes background-float-right {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(-10px, -8px);
  }
}

/* 모션 최소화 설정 대응 */
@media (prefers-reduced-motion: reduce) {
  .background-decoration,
  .circle-background,
  .member-icon,
  .member-icon i,
  .deco,
  .sparkle,
  .member-message,
  .guide-card,
  .guide-icon {
    opacity: 1;
    animation: none;
    transform: none;
  }
}
</style>