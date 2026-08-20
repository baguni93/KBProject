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
            <i class="fa-solid fa-clock"></i>
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
          <h1 class="text-30-bold">아직 재가입할 수 없어요</h1>

          <p class="text-15">
            회원 탈퇴 후 24시간이 지나면<br />다시 가입할 수 있어요.
          </p>
        </div>

        <!-- 안내 카드 -->
        <div class="guide-card">
          <div class="guide-icon">
            <i class="fa-solid fa-calendar-days"></i>
          </div>

          <div class="guide-text">
            <strong class="text-15-bold"> 재가입 가능 시간 </strong>

            <p class="text-13">{{ formattedRejoinAvailableAt }} 이후</p>
          </div>
        </div>
      </section>
    </main>

    <!-- 공통 하단 버튼 -->
    <div class="bottom-btn-area single">
      <button class="bottom-btn next-button" type="button" @click="goIntro">
        처음으로
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useSignupStore } from '@/stores/signup';

const router = useRouter();
const signupStore = useSignupStore();

const rejoinAvailableAt = sessionStorage.getItem('rejoinAvailableAt');

const formattedRejoinAvailableAt = computed(() => {
  if (!rejoinAvailableAt) return '';

  const date = new Date(rejoinAvailableAt);

  if (Number.isNaN(date.getTime())) return '';

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');

  return `${year}.${month}.${day} ${hours}:${minutes}`;
});

// 처음 화면 이동
const goIntro = async () => {
  sessionStorage.removeItem('rejoinAvailableAt');
  signupStore.reset();
  await router.replace('/intro');
};

onMounted(() => {
  if (!rejoinAvailableAt) router.replace('/intro');
});
</script>

<style scoped>
@import '@/components/common/common/common.css';
@import '@/components/common/common/layout.css';

.member-page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  background: linear-gradient(180deg, #fffdf8 0%, #ffffff 42%, #ffffff 100%);
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
  background: rgba(255, 205, 91, 0.12);
  animation: background-float-left 6s ease-in-out infinite;
}

.decoration-right {
  top: 240px;
  right: -110px;
  width: 200px;
  height: 200px;
  background: rgba(176, 164, 255, 0.055);
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
    circle-pulse 2.6s ease-in-out 0.9s infinite;
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
    #ffd268 0%,
    var(--color-primary) 65%,
    #f3a711 100%
  );
  box-shadow: 0 14px 30px rgba(255, 188, 46, 0.24);
  color: #ffffff;
  opacity: 0;
  transform: scale(0.58);
  animation: member-pop 0.58s cubic-bezier(0.34, 1.56, 0.64, 1) 0.12s forwards;
}

.member-icon i {
  font-size: 34px;
  opacity: 0;
  transform: scale(0.55) rotate(-8deg);
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
    deco-float 3.4s ease-in-out 1s infinite;
}

.deco-two {
  top: 42px;
  left: 3px;
  width: 7px;
  height: 7px;
  background: #9d90ff;
  animation:
    deco-pop 0.4s ease 0.48s forwards,
    deco-float 3.8s ease-in-out 1.1s infinite reverse;
}

.deco-three {
  right: 4px;
  bottom: 27px;
  width: 7px;
  height: 7px;
  background: #7bd6c7;
  animation:
    deco-pop 0.4s ease 0.56s forwards,
    deco-float 3.5s ease-in-out 1.15s infinite;
}

.deco-four {
  bottom: 9px;
  left: 25px;
  width: 8px;
  height: 8px;
  background: #ff9eaa;
  animation:
    deco-pop 0.4s ease 0.62s forwards,
    deco-float 3.9s ease-in-out 1.2s infinite reverse;
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
    sparkle-float 3.2s ease-in-out 1.1s infinite;
}

.sparkle-two {
  right: 15px;
  bottom: 6px;
  color: #a99df7;
  font-size: 9px;
  animation:
    sparkle-pop 0.45s ease 0.66s forwards,
    sparkle-float 3.6s ease-in-out 1.2s infinite reverse;
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
  background: linear-gradient(110deg, #fff9ec 0%, #fffdf8 70%, #faf8ff 100%);
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

/* 하단 버튼 등장 효과 */
.next-button {
  opacity: 0;
  animation: button-show 0.4s ease 0.9s forwards;
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
    transform: scale(1.07);
  }
}

@keyframes member-pop {
  0% {
    opacity: 0;
    transform: scale(0.58);
  }

  70% {
    opacity: 1;
    transform: scale(1.06);
  }

  100% {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes icon-show {
  from {
    opacity: 0;
    transform: scale(0.55) rotate(-8deg);
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
    transform: scale(1.25);
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
    transform: translateY(-4px);
  }
}

@keyframes sparkle-pop {
  0% {
    opacity: 0;
    transform: scale(0) rotate(-18deg);
  }

  70% {
    opacity: 1;
    transform: scale(1.2) rotate(8deg);
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
    transform: translateY(-4px) rotate(8deg);
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

/* 버튼 등장 */
@keyframes button-show {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
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
  .guide-icon,
  .next-button {
    opacity: 1;
    animation: none;
    transform: none;
  }
}
</style>
