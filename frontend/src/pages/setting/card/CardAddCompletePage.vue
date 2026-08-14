<template>
  <div class="page-layout page-layout-top complete-page">
    <!-- 배경 장식 -->
    <div class="background-decoration decoration-left"></div>
    <div class="background-decoration decoration-right"></div>

    <main class="page-content complete-container">
      <section class="complete-content">
        <!-- 완료 애니메이션 -->
        <div class="success-visual">
          <span class="particle particle-1"></span>
          <span class="particle particle-2"></span>
          <span class="particle particle-3"></span>
          <span class="particle particle-4"></span>
          <span class="particle particle-5"></span>
          <span class="particle particle-6"></span>

          <!-- 별 장식 -->
          <span class="spark spark-1">
            <i class="fa-solid fa-star"></i>
          </span>

          <span class="spark spark-2">
            <i class="fa-solid fa-star"></i>
          </span>

          <!-- 체크 뒤 glow -->
          <div class="success-glow"></div>

          <!-- 메인 체크 -->
          <div class="success-circle">
            <i class="fa-solid fa-check"></i>
          </div>
        </div>

        <!-- 완료 메시지 -->
        <div class="complete-message">
          <h1 class="text-30-bold">
            카드 등록이<br />
            완료되었어요!
          </h1>

          <p class="complete-description text-15">
            등록한 카드로 결제 서비스를<br />
            바로 이용할 수 있어요.
          </p>
        </div>

        <!-- 완료 상태 -->
        <div class="complete-badge">
          <i class="fa-regular fa-credit-card"></i>
          <span>카드 등록 완료</span>
        </div>
      </section>
    </main>

    <!-- 하단 2개 버튼 -->
    <div class="bottom-btn-area double complete-button-area">
      <button
          class="bottom-btn secondary-button complete-button"
          type="button"
          @click="goPayment"
      >
        결제 홈
      </button>

      <button
          class="bottom-btn primary-button complete-button"
          type="button"
          @click="goCardList"
      >
        카드 관리
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

// 결제 화면으로 이동
const goPayment = async () => {
  await router.replace('/wallet');
};

// 연결 카드 관리 화면으로 이동
const goCardList = async () => {
  await router.replace('/setting/cards');
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.complete-page {
  position: relative;
  overflow: hidden;
  background:
      linear-gradient(
          180deg,
          #fffdf8 0%,
          var(--color-bg-page) 42%,
          var(--color-bg-page) 100%
      );
}

/* =========================
   콘텐츠
========================= */

.complete-container {
  position: relative;
  z-index: 2;
  justify-content: center;
  overflow: hidden;
}

.complete-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: -42px;
  text-align: center;
}

/* =========================
   배경 장식
========================= */

.background-decoration {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.decoration-left {
  top: -100px;
  left: -120px;
  width: 240px;
  height: 240px;
  background: rgba(255, 188, 46, 0.1);
  animation: background-float-left 6s ease-in-out infinite;
}

.decoration-right {
  top: 280px;
  right: -110px;
  width: 210px;
  height: 210px;
  background: rgba(176, 164, 255, 0.05);
  animation: background-float-right 7s ease-in-out infinite;
}

/* =========================
   완료 애니메이션
========================= */

.success-visual {
  position: relative;
  width: 180px;
  height: 180px;
  margin-bottom: 22px;
}

.success-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 126px;
  height: 126px;
  border-radius: 50%;
  background: rgba(255, 188, 46, 0.16);
  transform: translate(-50%, -50%);
  animation: glow 2.2s ease-in-out 0.8s infinite;
}

.success-circle {
  position: absolute;
  z-index: 2;
  top: 50%;
  left: 50%;
  display: flex;
  width: 102px;
  height: 102px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background:
      linear-gradient(
          145deg,
          #ffd15c,
          var(--color-primary)
      );
  box-shadow:
      0 16px 34px rgba(255, 188, 46, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.5);
  color: var(--color-text-white);
  font-size: 42px;
  transform: translate(-50%, -50%) scale(0);
  animation:
      success-pop
      0.55s
      cubic-bezier(0.34, 1.56, 0.64, 1)
      forwards;
}

.success-circle i {
  opacity: 0;
  transform: scale(0.5) rotate(-15deg);
  animation: check-appear 0.35s ease 0.42s forwards;
}

/* =========================
   파티클
========================= */

.particle {
  position: absolute;
  z-index: 1;
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
  opacity: 0;
}

.particle-1 {
  top: 28px;
  left: 28px;
  animation: particle-pop 0.55s ease 0.35s forwards;
}

.particle-2 {
  top: 18px;
  right: 36px;
  width: 6px;
  height: 6px;
  background: #9d90ff;
  animation: particle-pop 0.55s ease 0.5s forwards;
}

.particle-3 {
  top: 82px;
  right: 8px;
  width: 10px;
  height: 10px;
  background: #7bd6c7;
  animation: particle-pop 0.55s ease 0.4s forwards;
}

.particle-4 {
  right: 30px;
  bottom: 24px;
  width: 7px;
  height: 7px;
  background: #ff9eaa;
  animation: particle-pop 0.55s ease 0.6s forwards;
}

.particle-5 {
  bottom: 26px;
  left: 30px;
  width: 6px;
  height: 6px;
  background: #9d90ff;
  animation: particle-pop 0.55s ease 0.48s forwards;
}

.particle-6 {
  top: 92px;
  left: 6px;
  width: 9px;
  height: 9px;
  background: #7bd6c7;
  animation: particle-pop 0.55s ease 0.58s forwards;
}

/* =========================
   별
========================= */

.spark {
  position: absolute;
  z-index: 1;
  color: #ffd65c;
  opacity: 0;
}

.spark-1 {
  top: 18px;
  left: 70px;
  font-size: 12px;
  animation: spark-pop 0.55s ease 0.55s forwards;
}

.spark-2 {
  right: 46px;
  bottom: 17px;
  color: #a99df7;
  font-size: 10px;
  animation: spark-pop 0.55s ease 0.7s forwards;
}

/* =========================
   텍스트
========================= */

.complete-message {
  opacity: 0;
  transform: translateY(16px);
  animation: content-up 0.5s ease 0.55s forwards;
}

.complete-message h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.3;
  letter-spacing: -0.7px;
}

.complete-description {
  margin: 16px 0 0;
  color: var(--color-text-sub);
  font-weight: 400;
  line-height: 1.65;
}

/* =========================
   완료 뱃지
========================= */

.complete-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  margin-top: 26px;
  padding: 10px 15px;
  border: 1px solid rgba(255, 188, 46, 0.22);
  border-radius: 999px;
  background: rgba(255, 188, 46, 0.1);
  color: #9a6900;
  font-size: 13px;
  font-weight: 500;
  opacity: 0;
  transform: translateY(10px);
  animation: content-up 0.45s ease 0.72s forwards;
}

.complete-badge i {
  color: var(--color-primary-active);
  font-size: 12px;
}

/* =========================
   하단 2개 버튼
   common.css의
   .bottom-btn-area.double 사용
========================= */

.complete-button-area {
  position: relative;
  z-index: 3;
}

/*
  버튼 크기 / 높이 / 폰트 크기는
  common.css의 .bottom-btn을 그대로 사용합니다.
*/
.complete-button {
  opacity: 0;
  transform: translateY(10px);
  animation: button-show 0.45s ease forwards;
}

/* 왼쪽 버튼 등장 */
.complete-button:nth-child(1) {
  animation-delay: 0.82s;
}

/* 오른쪽 버튼 등장 */
.complete-button:nth-child(2) {
  animation-delay: 0.9s;
}

/* 왼쪽 - 보조 버튼 */
.secondary-button {
  border: 1px solid var(--color-border-main);
  background: #ffffff;
  color: var(--color-text-main);
}

.secondary-button:active {
  background: var(--color-bg-screen);
}

/* 오른쪽 - 메인 버튼 */
.primary-button {
  border: none;
  background: var(--color-primary);
  color: var(--color-text-main);
}

.primary-button:active {
  background: var(--color-primary-active);
}

/* =========================
   애니메이션
========================= */

@keyframes success-pop {
  0% {
    transform: translate(-50%, -50%) scale(0);
  }

  70% {
    transform: translate(-50%, -50%) scale(1.08);
  }

  100% {
    transform: translate(-50%, -50%) scale(1);
  }
}

@keyframes check-appear {
  from {
    opacity: 0;
    transform: scale(0.5) rotate(-15deg);
  }

  to {
    opacity: 1;
    transform: scale(1) rotate(0);
  }
}

@keyframes particle-pop {
  0% {
    opacity: 0;
    transform: scale(0);
  }

  60% {
    opacity: 1;
    transform: scale(1.4);
  }

  100% {
    opacity: 0.7;
    transform: scale(1);
  }
}

@keyframes spark-pop {
  0% {
    opacity: 0;
    transform: scale(0) rotate(-40deg);
  }

  60% {
    opacity: 1;
    transform: scale(1.4) rotate(12deg);
  }

  100% {
    opacity: 0.75;
    transform: scale(1) rotate(0);
  }
}

@keyframes glow {
  0%,
  100% {
    opacity: 0.55;
    transform: translate(-50%, -50%) scale(0.95);
  }

  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.1);
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

@keyframes button-show {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
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

/* =========================
   모션 최소화
========================= */

@media (prefers-reduced-motion: reduce) {
  .background-decoration,
  .success-circle,
  .success-circle i,
  .success-glow,
  .particle,
  .spark,
  .complete-message,
  .complete-badge,
  .complete-button {
    opacity: 1;
    animation: none;
  }

  .background-decoration,
  .particle,
  .spark,
  .complete-message,
  .complete-badge,
  .complete-button {
    transform: none;
  }

  .success-circle {
    transform: translate(-50%, -50%);
  }

  .success-glow {
    transform: translate(-50%, -50%);
  }
}

@media (max-width: 360px) {
  .success-visual {
    width: 160px;
    height: 160px;
  }

  .success-circle {
    width: 94px;
    height: 94px;
    font-size: 38px;
  }

  .success-glow {
    width: 116px;
    height: 116px;
  }
}
</style>