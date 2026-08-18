<template>
  <div class="app-container">
    <!-- 상단 검색바 & 알림 -->
    <header class="header-section">
      <div class="search-bar" @click="handleSearchClick">
        <i class="fa-solid fa-magnifying-glass search-icon"></i>
        <span class="placeholder-text">사용자 검색</span>
      </div>

      <button
        class="icon-square-btn"
        type="button"
        aria-label="알림"
        @click="goToNotification"
      >
        <i class="fa-regular fa-bell"></i>

        <span
          v-if="notificationStore.hasUnread"
          class="notification-dot"
          aria-hidden="true"
        ></span>
      </button>
    </header>

    <!-- 프로모션 배너 -->
    <section class="banner-section">
      <img
        src="/images/card_edit_background/glass.png"
        alt="카드 배경"
        class="banner-bg-img"
      />

      <div class="banner-content">
        <span class="banner-badge">KB 나만의 체크카드</span>

        <h2 class="banner-title">나만의 디자인으로 완성</h2>

        <p class="banner-desc">
          세상에 단 하나뿐인 디자인과<br />
          내가 직접 고르는 혜택으로 당당하게.
        </p>

        <button class="banner-btn" type="button" @click="handleBannerClick">
          나만의 카드 만들기
        </button>
      </div>
    </section>

    <p class="banner-subtext">
      나만의 디자인과 혜택으로 특별한 카드를 만들어보세요.
    </p>

    <!-- 피드 -->
    <FeedSection type="public" />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import FeedSection from './components/FeedSection.vue';
import { useNotificationStore } from '@/stores/notification';

const router = useRouter();

const notificationStore = useNotificationStore();

// 알림
const goToNotification = () => {
  router.push('/notification');
};

// 사용자 검색
const handleSearchClick = () => {
  router.push('/search');
};

// 카드 만들기
const handleBannerClick = () => {
  router.push('/card/create/intro');
};
</script>

<style scoped>
/* 전체 화면 컨테이너 */
.app-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  background: var(--color-bg-screen);
  min-height: 100vh;
  box-sizing: border-box;
  padding: 16px 16px 20px 16px;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 1. 상단 검색바 & 우측 버튼 (Grid로 위치 고정) */
.header-section {
  display: grid;
  grid-template-columns: 1fr 48px;
  align-items: center;
  gap: 10px;
  width: 100%;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 48px;
  padding: 0 16px;
  background: #e2edf6;
  border-radius: 24px;
  cursor: pointer;
  box-sizing: border-box;
}

.search-icon {
  color: #555;
  font-size: 16px;
  flex-shrink: 0;
}

.placeholder-text {
  color: #667085;
  font-size: 15px;
  font-weight: 500;
}

/* 2. 프로모션 배너 */
.banner-section {
  position: relative;

  width: 100%;
  min-height: 210px;

  border-radius: 18px;

  overflow: hidden;

  box-sizing: border-box;

  background: #f5f5f5;

  display: flex;
  align-items: center;
}

/* 배경 이미지 */
.banner-bg-img {
  position: absolute;
  inset: 0;

  width: 100%;
  height: 100%;

  object-fit: cover;

  z-index: 0;
}

/* 배경 위 살짝 오버레이 */
.banner-section::after {
  content: '';

  position: absolute;
  inset: 0;

  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.88) 0%,
    rgba(255, 255, 255, 0.55) 55%,
    rgba(255, 255, 255, 0.15) 100%
  );

  z-index: 1;
}

/* 내용 */
.banner-content {
  position: relative;

  z-index: 2;

  width: 100%;

  padding: 20px;

  box-sizing: border-box;
}

/* KB 나만의 체크카드 배지 */
.banner-badge {
  display: inline-flex;
  align-items: center;

  padding: 6px 12px;

  margin-bottom: 12px;

  border-radius: 10px;

  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.88) 0%,
    rgba(255, 255, 255, 0.55) 55%,
    rgba(255, 255, 255, 0.15) 100%
  );

  color: #6b4f00;

  font-size: 10px;
  font-weight: 700;

  border: 1px solid rgba(255, 193, 7, 0.35);

  box-shadow:
    0 3px 8px rgba(255, 193, 7, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);

  backdrop-filter: blur(4px);
}

/* 제목 */
.banner-title {
  margin: 0;

  color: #6b4f00;

  font-size: 18px;
  font-weight: 800;

  line-height: 1.35;

  letter-spacing: -0.4px;
}

/* 설명 */
.banner-desc {
  margin: 7px 0 12px;

  color: #6b4f00;

  font-size: 13px;
  font-weight: 500;

  line-height: 1.45;
}

.banner-btn {
  height: 40px;

  padding: 0 18px;

  border: none;
  border-radius: 11px;

  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.88) 0%,
    rgba(255, 255, 255, 0.55) 55%,
    rgba(255, 255, 255, 0.15) 100%
  );
  color: #5c4300;

  font-family: inherit;

  font-size: 12px;
  font-weight: 800;

  cursor: pointer;

  box-shadow:
    0 5px 14px rgba(255, 193, 7, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);

  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease,
    filter 0.15s ease;
}

.banner-btn:hover {
  filter: brightness(1.04);

  box-shadow:
    0 7px 18px rgba(255, 193, 7, 0.32),
    inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.banner-btn:active {
  transform: scale(0.96);

  box-shadow: 0 3px 8px rgba(255, 193, 7, 0.2);
}
.banner-section::after {
  content: '';

  position: absolute;
  inset: 0;

  background: linear-gradient(
    90deg,
    rgba(255, 252, 235, 0.92) 0%,
    rgba(255, 248, 215, 0.62) 50%,
    rgba(255, 255, 255, 0.12) 100%
  );

  z-index: 1;
}
/* 아래 설명 */
.banner-subtext {
  margin: 8px 4px 0;

  color: #999;

  font-size: 10px;

  line-height: 1.4;
}

/* 새 알림 점 */
.icon-square-btn {
  position: relative; /* ⭐ 추가 */

  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: #e2edf6;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  box-sizing: border-box;
  flex-shrink: 0;
}

.icon-square-btn i {
  color: #333;
  font-size: 18px;
}

.notification-dot {
  position: absolute;

  top: 6px;
  right: 6px;

  width: 8px;
  height: 8px;
  background: #ff3b30;
  border-radius: 50%;
}
</style>
