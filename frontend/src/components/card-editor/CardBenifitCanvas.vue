<template>
  <div class="benefit-selection-wrapper">
    <!-- 1. 상단 히어로 섹션 (메인 페이지 .brand-hero와 동일한 둥글기 및 구조 적용) -->
    <section class="brand-hero">
      <img
        src="/images/card_edit_background/glass.png"
        alt="카드 배경"
        class="hero-bg-img"
      />
      <div class="hero-inner">
        <span class="badge">{{ currentBanner.badge }}</span>
        <h2 class="title text-20-bold" v-html="currentBanner.title"></h2>
        <p class="desc text-13" v-html="currentBanner.desc"></p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
const props = defineProps({
  tab: {
    type: String,
    default: 'all',
  },
});

const bannerData = {
  all: {
    badge: 'KB 나만의 체크카드',
    title: '나만의 혜택, <br />나만의 디자인으로 완성',
    desc: '세상에 단 하나뿐인 디자인과<br />내가 직접 고르는 혜택으로 당당하게.',
  },
  lifestyle: {
    badge: 'KB 디지털/구독 혜택',
    title: '매달 빠져나가는 구독료,<br />반값으로 줄여보세요',
    desc: '넷플릭스, 유튜브, 스타벅스 등<br />MZ세대 필수 디지털 혜택 모음.',
  },
  shopping: {
    badge: 'KB 쇼핑/뷰티 혜택',
    title: '올영·무신사 쇼핑할 때<br />놓칠 수 없는 할인',
    desc: '패션 플랫폼부터 뷰티 스토어까지<br />트렌디한 쇼퍼를 위한 맞춤 혜택.',
  },
  daily: {
    badge: 'KB 생활/교통 혜택',
    title: '출퇴근 교통비부터<br />편의점까지 알뜰하게',
    desc: '매일 쓰는 대중교통과 일상 소비에서<br />실속 있게 챙기는 생활 할인.',
  },
  basic: {
    badge: 'KB 기본 혜택',
    title: '기본에 충실한<br />알찬 혜택 모음',
    desc: '언제 어디서나 누리는<br />기본적인 할인 혜택입니다.',
  },
  photo: {
    badge: 'KB 포토 혜택',
    title: '내 사진 카드 전용<br />특별 맞춤 혜택',
    desc: '특별한 디자인과 함께 즐기는<br />프리미엄 혜택.',
  },
};

const currentBanner = computed(() => {
  return bannerData[props.tab] || bannerData.all;
});
</script>

<style scoped>
/* 메인 페이지와 완전히 일치하는 타이포그래피 */
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

/* 에디터 내부 스크롤 영역과 간격 맞춤 (메인 .content-area의 gap: 16px 일치) */
.benefit-selection-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-sizing: border-box;
}

/* 1. 히어로 섹션 (메인 .brand-hero와 동일한 둥글기 20px 적용) */
.brand-hero {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  color: var(--color-text-white, #ffffff);
  flex-shrink: 0;
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
  box-sizing: border-box;
}

.badge {
  font-size: 11px;
  color: var(--color-primary, #ffbc2e);
  font-weight: 600;
  display: inline-block;
}

.title {
  margin: 8px 0 6px 0;
  color: var(--color-text-white, #ffffff);
  line-height: 1.3;
}

.desc {
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.4;
  margin: 0;
}

/* 2. AI 분석 유도 섹션 (메인 .ai-pattern-box와 패딩, 보더, 둥글기 16px 완벽 일치) */
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
  flex-shrink: 0;
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

/* 3. 혜택 리스트 영역 (위 섹션들과 동일한 디자인 시스템의 박스 규격 사용) */
.benefit-list-section {
  display: flex;
  flex-direction: column;
}

.card-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px; /* 메인 페이지 간격과 통일 */
}

.benefit-card-item {
  background-color: var(--color-bg-page, #ffffff);
  padding: 16px;
  border-radius: 16px;
  border: 1.5px solid var(--color-border-main, #dddddd);
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: all 0.2s ease;
}

.benefit-card-item:hover {
  border-color: var(--color-primary, #ffbc2e);
  transform: translateY(-2px);
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-badge {
  font-size: 11px;
  font-weight: 600;
  color: #d97706;
  background-color: #fef3c7;
  padding: 2px 6px;
  border-radius: 4px;
}

.select-indicator {
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
}

.card-name {
  color: var(--color-text-main, #111111);
  margin: 0 0 12px 0;
}

.benefit-box {
  background-color: #f9fafb;
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  border: 1px solid #f3f4f6;
}

.benefit-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.benefit-text {
  color: var(--color-text-sub, #777777);
}

.highlight {
  color: #d97706;
  font-weight: 700;
}
</style>
