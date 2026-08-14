<template>
  <div class="page-layout logout-page">
    <PageHeader title="" :show-back="false" />

    <main class="page-content logout-container">
      <section class="logout-content">
        <!-- 로그아웃 비주얼 -->
        <div class="logout-visual">
          <div class="visual-glow"></div>
          <div class="visual-background"></div>

          <div class="logout-icon-box">
            <i class="fa-solid fa-arrow-right-from-bracket"></i>
          </div>

          <span class="deco deco-one"></span>
          <span class="deco deco-two"></span>
          <span class="deco deco-three"></span>
          <span class="deco deco-four"></span>
        </div>

        <!-- 로그아웃 메시지 -->
        <div class="logout-message">
          <h2 class="text-26-bold">로그아웃하시겠어요?</h2>

          <p class="logout-description">
            현재 기기에서만 로그아웃됩니다.<br />
            언제든 다시 로그인할 수 있어요.
          </p>
        </div>

        <!-- 안내 -->
        <div class="logout-notice">
          <div class="notice-icon">
            <i class="fa-solid fa-shield-halved"></i>
          </div>

          <div class="notice-text">
            <strong class="text-13-bold">걱정하지 않으셔도 돼요</strong>

            <p>
              로그아웃해도 계정 정보와 이용 내역은<br />
              안전하게 보관됩니다.
            </p>
          </div>
        </div>
      </section>
    </main>

    <!-- 하단 버튼 -->
    <div class="bottom-btn-area double logout-buttons">
      <button
          class="content-btn secondary logout-action-btn"
          type="button"
          :disabled="loading"
          @click="goBack"
      >
        취소
      </button>

      <button
          class="content-btn primary logout-action-btn"
          type="button"
          :disabled="loading"
          @click="handleLogout"
      >
        <i v-if="!loading" class="fa-solid fa-arrow-right-from-bracket"></i>
        {{ loading ? '로그아웃 중...' : '로그아웃' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);

// 이전 화면
const goBack = () => {
  router.back();
};

// 로그아웃
const handleLogout = async () => {
  if (loading.value) return;

  try {
    loading.value = true;
    await authStore.logout();
    await router.replace('/intro');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.logout-page {
  position: relative;
  background: var(--color-bg-page);
}

.logout-container {
  overflow: hidden;
}

.logout-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-bottom: 74px;
  text-align: center;
}

/* 로그아웃 비주얼 */
.logout-visual {
  position: relative;
  width: 150px;
  height: 150px;
  margin-bottom: 30px;
}

.visual-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 116px;
  height: 116px;
  border-radius: 50%;
  background: rgba(255, 188, 46, 0.12);
  transform: translate(-50%, -50%);
  animation: glowPulse 2.4s ease-in-out infinite;
}

.visual-background {
  position: absolute;
  inset: 12px;
  border-radius: 42px;
  background: linear-gradient(145deg, #fff9ea 0%, #fff1c7 100%);
  transform: rotate(8deg);
  animation: backgroundFloat 4s ease-in-out infinite;
}

.logout-icon-box {
  position: absolute;
  z-index: 2;
  top: 34px;
  left: 34px;
  display: flex;
  width: 82px;
  height: 82px;
  align-items: center;
  justify-content: center;
  border-radius: 26px;
  background: linear-gradient(145deg, #ffc744 0%, var(--color-primary-active) 100%);
  box-shadow: 0 14px 30px rgba(255, 188, 46, 0.26);
  color: var(--color-text-white);
  font-size: 31px;
  animation: iconFloat 2.8s ease-in-out infinite;
}

/* 장식 */
.deco {
  position: absolute;
  z-index: 3;
  display: block;
  border-radius: 50%;
}

.deco-one {
  top: 16px;
  right: 15px;
  width: 9px;
  height: 9px;
  background: var(--color-primary);
  animation: decoFloat 3s ease-in-out infinite;
}

.deco-two {
  bottom: 22px;
  left: 12px;
  width: 7px;
  height: 7px;
  background: #9d90ff;
  animation: decoFloat 3.6s ease-in-out infinite reverse;
}

.deco-three {
  right: 7px;
  bottom: 33px;
  width: 8px;
  height: 8px;
  background: #7bd6c7;
  animation: decoFloat 3.2s ease-in-out infinite;
}

.deco-four {
  top: 36px;
  left: 7px;
  width: 6px;
  height: 6px;
  background: #ff9ea8;
  animation: decoFloat 4s ease-in-out infinite reverse;
}

/* 메시지 */
.logout-message {
  opacity: 0;
  transform: translateY(12px);
  animation: contentUp 0.45s ease 0.15s forwards;
}

.logout-message h2 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.6px;
}

.logout-description {
  margin: 14px 0 0;
  color: var(--color-text-muted);
  font-size: 15px;
  font-weight: 400;
  line-height: 1.65;
}

/* 안내 영역 */
.logout-notice {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 13px;
  margin-top: 32px;
  padding: 16px;
  border: 1px solid rgba(255, 188, 46, 0.22);
  border-radius: 16px;
  background: rgba(255, 188, 46, 0.08);
  box-sizing: border-box;
  text-align: left;
  opacity: 0;
  transform: translateY(12px);
  animation: contentUp 0.45s ease 0.28s forwards;
}

.notice-icon {
  display: flex;
  flex: none;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff1c7;
  color: var(--color-primary-active);
  font-size: 17px;
}

.notice-text {
  min-width: 0;
}

.notice-text strong {
  display: block;
  color: var(--color-text-main);
}

.notice-text p {
  margin: 5px 0 0;
  color: var(--color-text-sub);
  font-size: 13px;
  font-weight: 400;
  line-height: 1.5;
}

/* 하단 버튼 */
.logout-buttons {
  flex-shrink: 0;
  padding-top: 16px;
  background: var(--color-bg-page);
  opacity: 0;
  transform: translateY(10px);
  animation: contentUp 0.45s ease 0.4s forwards;
}

.logout-action-btn {
  height: 52px;
  border-radius: 14px;
  font-size: 15px;
}

.logout-action-btn.secondary {
  border: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
}

.logout-action-btn.primary {
  border: 1px solid var(--color-primary-border);
  box-shadow: 0 7px 16px rgba(255, 188, 46, 0.14);
}

.logout-action-btn i {
  font-size: 13px;
}

/* 애니메이션 */
@keyframes iconFloat {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-5px);
  }
}

@keyframes backgroundFloat {
  0%,
  100% {
    transform: rotate(8deg) translateY(0);
  }

  50% {
    transform: rotate(5deg) translateY(3px);
  }
}

@keyframes glowPulse {
  0%,
  100% {
    opacity: 0.55;
    transform: translate(-50%, -50%) scale(0.95);
  }

  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.08);
  }
}

@keyframes decoFloat {
  0%,
  100% {
    transform: translateY(0) scale(1);
  }

  50% {
    transform: translateY(-5px) scale(1.12);
  }
}

@keyframes contentUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .visual-glow,
  .visual-background,
  .logout-icon-box,
  .deco,
  .logout-message,
  .logout-notice,
  .logout-buttons {
    opacity: 1;
    animation: none;
    transform: none;
  }

  .visual-glow {
    transform: translate(-50%, -50%);
  }

  .visual-background {
    transform: rotate(8deg);
  }
}

@media (max-width: 360px) {
  .logout-visual {
    width: 138px;
    height: 138px;
  }

  .logout-icon-box {
    top: 31px;
    left: 31px;
    width: 76px;
    height: 76px;
    font-size: 28px;
  }

  .visual-glow {
    width: 108px;
    height: 108px;
  }
}
</style>