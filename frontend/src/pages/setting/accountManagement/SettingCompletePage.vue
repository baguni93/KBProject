<template>
  <div class="page-layout complete-page">
    <!-- 배경 장식 -->
    <div class="background-decoration decoration-left"></div>
    <div class="background-decoration decoration-right"></div>

    <main class="page-content complete-container">
      <section class="complete-content">
        <div class="success-visual">
          <span class="particle particle-1"></span>
          <span class="particle particle-2"></span>
          <span class="particle particle-3"></span>
          <span class="particle particle-4"></span>
          <span class="particle particle-5"></span>
          <span class="particle particle-6"></span>

          <span class="spark spark-1">
            <i class="fa-solid fa-star"></i>
          </span>

          <span class="spark spark-2">
            <i class="fa-solid fa-star"></i>
          </span>

          <div class="success-glow"></div>

          <div class="success-circle">
            <i class="fa-solid fa-check"></i>
          </div>
        </div>

        <div class="complete-message">
          <h1 class="text-26-bold" v-html="completeInfo.title"></h1>

          <p
            class="complete-description text-15"
            v-html="completeInfo.description"
          ></p>
        </div>

        <div class="complete-badge">
          <i :class="completeInfo.icon"></i>
          <span>{{ completeInfo.badgeText }}</span>
        </div>
      </section>
    </main>

    <div class="bottom-btn-area single complete-button-area">
      <button class="bottom-btn" type="button" @click="complete">
        {{ completeInfo.buttonText }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const completeType = computed(() => String(route.query.type || 'PIN_CHANGE'));

// 완료 화면 정보
const completeInfo = computed(() => {
  const completeData = {
    NAME_CHANGE: {
      title: '이름 변경이<br />완료되었어요',
      description: '변경된 이름으로<br />서비스를 이용할 수 있어요.',
      icon: 'fa-regular fa-user',
      badgeText: '새 이름 적용 완료',
      buttonText: '확인',
      path: '/setting/account-management',
    },

    PHONE_CHANGE: {
      title: '휴대폰 번호 변경이<br />완료되었어요',
      description: '새로운 휴대폰 번호가<br />계정에 안전하게 등록되었어요.',
      icon: 'fa-solid fa-mobile-screen-button',
      badgeText: '새 번호 적용 완료',
      buttonText: '확인',
      path: '/setting/account-management',
    },

    PIN_CHANGE: {
      title: '간편비밀번호 변경이<br />완료되었어요',
      description: '이제 새로운 간편비밀번호로<br />서비스를 이용할 수 있어요.',
      icon: 'fa-solid fa-lock',
      badgeText: '새 비밀번호 적용 완료',
      buttonText: '확인',
      path: '/setting/account-management',
    },

    WITHDRAW: {
      title: '회원탈퇴가<br />완료되었어요',
      description: '그동안 서비스를<br />이용해 주셔서 감사합니다.',
      icon: 'fa-regular fa-hand',
      badgeText: '탈퇴 처리 완료',
      buttonText: '확인',
      path: '/intro',
    },
  };

  return completeData[completeType.value] || completeData.PIN_CHANGE;
});

// 완료 후 이동
const complete = async () => {
  await router.replace(completeInfo.value.path);
};
</script>

<style scoped>
@import '@/components/common/common/common.css';

.complete-page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  background: linear-gradient(
    180deg,
    #fffdf8 0%,
    var(--color-bg-page) 42%,
    var(--color-bg-page) 100%
  );
}

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
  margin-top: -46px;
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
  animation: backgroundFloatLeft 6s ease-in-out infinite;
}

.decoration-right {
  top: 280px;
  right: -110px;
  width: 210px;
  height: 210px;
  background: rgba(176, 164, 255, 0.05);
  animation: backgroundFloatRight 7s ease-in-out infinite;
}

/* =========================
   완료 애니메이션 영역
========================= */

.success-visual {
  position: relative;
  width: 180px;
  height: 180px;
  margin-bottom: 22px;
}

/* 뒤쪽 은은한 빛 */
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

/* 메인 체크 원 */
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
  background: linear-gradient(145deg, #ffd15c, var(--color-primary));
  box-shadow:
    0 16px 34px rgba(255, 188, 46, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  color: var(--color-text-white);
  font-size: 42px;
  transform: translate(-50%, -50%) scale(0);
  animation: successPop 0.55s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.success-circle i {
  opacity: 0;
  transform: scale(0.5) rotate(-15deg);
  animation: checkAppear 0.35s ease 0.42s forwards;
}

/* =========================
   주변 파티클
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
  animation: particlePop 0.55s ease 0.35s forwards;
}

.particle-2 {
  top: 18px;
  right: 36px;
  width: 6px;
  height: 6px;
  animation: particlePop 0.55s ease 0.5s forwards;
}

.particle-3 {
  top: 82px;
  right: 8px;
  width: 10px;
  height: 10px;
  animation: particlePop 0.55s ease 0.4s forwards;
}

.particle-4 {
  right: 30px;
  bottom: 24px;
  width: 7px;
  height: 7px;
  animation: particlePop 0.55s ease 0.6s forwards;
}

.particle-5 {
  bottom: 26px;
  left: 30px;
  width: 6px;
  height: 6px;
  animation: particlePop 0.55s ease 0.48s forwards;
}

.particle-6 {
  top: 92px;
  left: 6px;
  width: 9px;
  height: 9px;
  animation: particlePop 0.55s ease 0.58s forwards;
}

/* =========================
   별 장식
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
  animation: sparkPop 0.55s ease 0.55s forwards;
}

.spark-2 {
  right: 46px;
  bottom: 17px;
  font-size: 10px;
  animation: sparkPop 0.55s ease 0.7s forwards;
}

/* =========================
   완료 텍스트
========================= */

.complete-message {
  opacity: 0;
  transform: translateY(16px);
  animation: contentUp 0.5s ease 0.55s forwards;
}

.complete-message h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.42;
  letter-spacing: -0.7px;
}

.complete-description {
  margin: 14px 0 0;
  color: var(--color-text-sub);
  font-weight: 400;
  line-height: 1.65;
}

/* =========================
   완료 상태 뱃지
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
  animation: contentUp 0.45s ease 0.72s forwards;
}

.complete-badge i {
  color: var(--color-primary-active);
  font-size: 12px;
}

/* =========================
   하단 버튼 등장
========================= */

.complete-button-area {
  border: 0 !important;
  outline: 0 !important;
  background: transparent !important;
  box-shadow: none !important;

  opacity: 0;
  transform: translateY(14px);

  animation: buttonUp 0.45s ease 0.85s forwards;
}

.complete-button-area::before,
.complete-button-area::after {
  display: none !important;
}

/* =========================
   애니메이션
========================= */

/* 메인 원 등장 */
@keyframes successPop {
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

/* 체크 등장 */
@keyframes checkAppear {
  from {
    opacity: 0;
    transform: scale(0.5) rotate(-15deg);
  }

  to {
    opacity: 1;
    transform: scale(1) rotate(0);
  }
}

/* 주변 원 등장 */
@keyframes particlePop {
  0% {
    opacity: 0;
    transform: scale(0);
  }

  60% {
    opacity: 1;
    transform: scale(1.4);
  }

  100% {
    opacity: 0.65;
    transform: scale(1);
  }
}

/* 별 등장 */
@keyframes sparkPop {
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

/* 은은한 원 효과 */
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

/* 텍스트 등장 */
@keyframes contentUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 버튼 등장 */
@keyframes buttonUp {
  from {
    opacity: 0;
    transform: translateY(14px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 배경 왼쪽 움직임 */
@keyframes backgroundFloatLeft {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(12px, 10px);
  }
}

/* 배경 오른쪽 움직임 */
@keyframes backgroundFloatRight {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(-10px, -8px);
  }
}

/* =========================
   모션 최소화 설정 대응
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
  .complete-button-area {
    opacity: 1;
    animation: none;
    transform: none;
  }

  .success-circle {
    transform: translate(-50%, -50%);
  }

  .success-glow {
    transform: translate(-50%, -50%);
  }
}

/* =========================
   작은 화면
========================= */

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
