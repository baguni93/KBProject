<template>
  <div class="main-page">
    <PageHeader title="나만의 카드" />

    <main class="content-area">
      <!-- 1. 히어로 섹션: 카드 컨셉 강조 (레이아웃 오버랩 및 패딩 수정) -->
      <section class="brand-hero">
        <img
          src="/images/card_edit_background/glass.png"
          alt="카드 배경"
          class="hero-bg-img"
        />
        <div class="hero-inner">
          <span class="badge">KB 나만의 체크카드</span>
          <h2 class="title text-20-bold">나만의 디자인으로 완성</h2>
          <p class="desc text-13">
            세상에 단 하나뿐인 디자인과<br />내가 직접 고르는 혜택으로 당당하게.
          </p>
        </div>
      </section>

      <!-- 2. AI 맞춤 혜택 찾기 유도 섹션 -->
      <section class="ai-pattern-box" @click="goToAiAnalysis">
        <div class="ai-left">
          <span class="ai-badge">AI 추천</span>
          <p class="ai-title text-15-bold">소비패턴 분석하고 맞춤 혜택 찾기</p>
          <p class="ai-sub text-13">
            나한테 꼭 맞는 혜택이 무엇인지<br />
            바로 확인해보세요.
          </p>
        </div>
        <div class="ai-right">
          <button class="ai-btn" type="button">분석하기 &gt;</button>
        </div>
      </section>

      <!-- 3. 국민은행 계좌 유도 섹션 (텍스트 잘림 및 높이 오류 수정) -->
      <section class="bank-account-box" @click="goToBanking">
        <div class="bank-left">
          <div class="app-icon-wrapper">
            <img
              src="https://play-lh.googleusercontent.com/OmJa1eJ_ONHHa9SRuhiI2Ouzy7Cb_rhoGF-JQC1pnWxOcMtnNGOs7XrwPoldEJ0ar5eztMqBPn79H7AQDeyk=w240-h480-rw"
              alt="KB스타뱅킹"
              class="bank-logo-img"
            />
          </div>
          <div class="bank-info">
            <span class="bank-badge">KB 스타뱅킹</span>
            <p class="bank-title text-15-bold">국민은행 계좌가 없으신가요?</p>
            <p class="bank-sub text-13">
              KB스타뱅킹 앱에서 쉽고 빠르게 개설하기
            </p>
          </div>
        </div>
        <div class="bank-right">
          <span class="link-arrow yellow-btn">이동</span>
        </div>
      </section>

      <!-- 4. 주요 안내 섹션 -->
      <section class="info-section">
        <h3 class="guide-title text-20-bold">카드 발급 가이드</h3>
        <ul class="guide-list">
          <li class="text-13">
            <strong>맞춤형 혜택:</strong> 나에게 꼭 필요한 혜택만 골라 담으세요.
          </li>
          <li class="text-13">
            <strong>디자인 커스텀:</strong> 트렌디한 패턴과 색상을 선택해보세요.
          </li>
          <li class="text-13">
            <strong>간편 신청:</strong> 디자인과 혜택 선택 후 즉시 신청이
            가능합니다.
          </li>
        </ul>
      </section>
    </main>

    <!-- 하단 고정 버튼 -->
    <div class="bottom-btn-area single">
      <button class="bottom-btn text-15-bold" type="button" @click="goToDesign">
        나만의 카드 만들기
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import { useCustomCardStore } from '@/stores/customcard';
import { useAuthStore } from '@/stores/auth.js';

const authStore = useAuthStore();
const userId = authStore.userId ?? 1;

const customCardStore = useCustomCardStore();
const router = useRouter();

const goToBanking = () => {
  window.location.href = 'https://www.kbstar.com';
};

const goToAiAnalysis = () => {
  router.push('/analysis');
};

const goToDesign = () => {
  router.push('/card/create/agreement');
};

customCardStore.load(userId);
</script>

<style scoped>
/* 타이포그래피 공통 클래스 매칭 */
.text-20-bold {
  font-size: 18px;
  font-weight: 700;
}
.text-15-bold {
  font-size: 14px;
  font-weight: 700;
}
.text-13 {
  font-size: 12px;
  font-weight: 500;
}

.main-page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100dvh;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  padding: 12px;
}

.content-area {
  flex: 1;
  padding: 13px 5px 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 1. 히어로 섹션 수정 (글씨가 이미지 밖으로 넘치지 않도록 높이 구조 개선) */
.brand-hero {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  color: var(--color-text-white, #ffffff);
}

.hero-bg-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 1;
}

.hero-inner {
  position: relative;
  z-index: 2;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.75), rgba(0, 0, 0, 0.3));
  padding: 24px 20px;
}

.badge {
  font-size: 11px;
  color: var(--color-primary, #ffbc2e);
  font-weight: 600;
  display: inline-block;
}

.title {
  margin: 8px 0 6px;
  color: var(--color-text-white, #ffffff);
  line-height: 1.3;
}

.desc {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.4;
}

/* 2. AI 맞춤 혜택 유도 섹션 */
.ai-pattern-box {
  background: var(--color-bg-page, #ffffff);
  border: 1.5px solid var(--color-border-main, #dddddd);
  border-radius: 16px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: all 0.2s ease;
}

.ai-pattern-box:hover {
  border-color: var(--color-primary, #ffbc2e);
  transform: translateY(-2px);
}

.ai-left {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
  flex: 1;
}

.ai-badge {
  font-size: 11px;
  font-weight: 600;
  color: #d97706;
}

.ai-title {
  margin: 0;
  color: var(--color-text-main, #111111);
}

.ai-sub {
  margin: 0;
  color: var(--color-text-sub, #777777);
  line-height: 1.3;
}

.ai-right {
  flex-shrink: 0;
  margin-left: 10px;
}

.ai-btn {
  background-color: var(--color-primary, #ffbc2e);
  color: var(--color-text-main, #111111);
  border: 1px solid var(--color-primary-border, #cc9200);
  font-size: 11px;
  font-weight: 600;
  padding: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
  white-space: nowrap;
}

/* 3. 국민은행 계좌 유도 섹션 수정 (높이 찌그러짐 및 텍스트 넘침 현상 해결) */
.bank-account-box {
  background: var(--color-bg-page, #ffffff);
  padding: 16px;
  border-radius: 16px;
  border: 1.5px solid var(--color-border-main, #dddddd);
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: all 0.2s ease;
}

.bank-account-box:hover {
  border-color: var(--color-primary, #ffbc2e);
  transform: translateY(-2px);
}

.bank-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.app-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #fff8e1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border: 1px solid #fef3c7;
  flex-shrink: 0;
}

.bank-logo-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bank-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0; /* flex 자식 요소에서 텍스트 말줄임 및 내부 정렬 유지에 필수 */
}

.bank-badge {
  font-size: 11px;
  font-weight: 600;
  color: #d97706;
}

.bank-title {
  color: var(--color-text-main, #111111);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bank-sub {
  color: var(--color-text-sub, #777777);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.link-arrow.yellow-btn {
  font-size: 11px;
  font-weight: 600;
  color: #111111;
  background: var(--color-primary, #ffbc2e);
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid var(--color-primary-border, #cc9200);
  white-space: nowrap;
}

/* 가이드 섹션 */
.info-section {
  padding: 4px 0;
}

.guide-title {
  margin-bottom: 8px;
  color: var(--color-text-main, #111111);
}

.guide-list {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 0;
}

.guide-list li {
  color: var(--color-text-sub, #777777);
  padding-left: 14px;
  position: relative;
  line-height: 1.4;
}

.guide-list li strong {
  color: var(--color-text-main, #111111);
}

.guide-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--color-primary, #ffbc2e);
  font-weight: bold;
}
</style>
