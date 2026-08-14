<template>
  <div
    class="image-slider brand-hero"
    @pointerdown="touchStart"
    @pointermove="touchMove"
    @pointerup="touchEnd"
    @pointercancel="touchEnd"
  >
    <div
      class="slider-track"
      :style="{
        transform: `translateX(-${currentIndex * 100}%)`,
      }"
    >
      <div v-for="(slide, index) in slides" :key="index" class="slide">
        <!-- 배경 이미지 -->
        <img :src="slide.bgImg" :alt="slide.altText" class="hero-bg-img" />

        <!-- 텍스트 영역 (검은색 배경 제거됨) -->
        <div class="hero-inner">
          <span class="badge">{{ slide.badgeText }}</span>
          <h2 class="title text-20-bold">{{ slide.title }}</h2>
          <p class="desc text-13" v-html="slide.desc"></p>
        </div>
      </div>
    </div>

    <!-- 하단 인디케이터 (점) -->
    <div v-if="slides.length > 1" class="indicator">
      <span
        v-for="(slide, index) in slides"
        :key="index"
        class="dot"
        :class="{ active: index === currentIndex }"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
  slides: {
    type: Array,
    default: () => [
      {
        bgImg: '/images/card_edit_background/glass.png',
        altText: '카드 배경 1',
        badgeText: 'KB 나만의 체크카드',
        title: '나만의 디자인으로 완성',
        desc: '세상에 단 하나뿐인 디자인과<br />내가 직접 고르는 혜택으로 당당하게.',
      },
      {
        bgImg: '/images/card_edit_background/glass.png',
        altText: '카드 배경 2',
        badgeText: 'EVENT & BENEFIT',
        title: '특별한 혜택을 담아보세요',
        desc: '매일 쓰는 일상 속에서<br />더 큰 할인과 포인트를 누려보세요.',
      },
    ],
  },
});

const currentIndex = ref(0);

const nextImage = (length) => {
  if (currentIndex.value < length - 1) {
    currentIndex.value++;
  }
};

const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
};

let startX = 0;
let endX = 0;

const touchStart = (e) => {
  startX = e.clientX;
  if (e.currentTarget.setPointerCapture) {
    e.currentTarget.setPointerCapture(e.pointerId);
  }
};

const touchMove = (e) => {
  endX = e.clientX;
};

const touchEnd = (e) => {
  const diff = startX - endX;
  const length = e.currentTarget.querySelectorAll('.slide').length;

  if (diff > 50) nextImage(length);
  if (diff < -50) prevImage();

  startX = 0;
  endX = 0;
};
</script>

<style scoped>
.text-20-bold {
  font-size: 18px;
  font-weight: 700;
}
.text-13 {
  font-size: 12px;
  font-weight: 500;
}

.image-slider.brand-hero {
  position: relative;
  width: 100%;
  height: 180px;
  min-height: 180px;
  border-radius: 8px;
  overflow: hidden;
  background: #111;
  touch-action: pan-y;
  user-select: none;
  flex-shrink: 0;
}

.slider-track {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease-in-out;
}

.slide {
  min-width: 100%;
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
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
  user-select: none;
  -webkit-user-drag: none;
}

.hero-inner {
  position: relative;
  z-index: 2;
  padding: 16px 20px;
  width: 100%;
  box-sizing: border-box;
  color: #ffffff;
  /* 배경색을 완전히 제거하고 텍스트 가독성을 위한 은은한 그림자 추가 */
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.7);
}

.badge {
  font-size: 11px;
  color: var(--color-primary, #ffbc2e);
  font-weight: 600;
  display: inline-block;
}

.title {
  margin: 4px 0 4px;
  color: #ffffff;
  line-height: 1.3;
}

.desc {
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.4;
}

.indicator {
  position: absolute;
  right: 20px;
  bottom: 16px;
  z-index: 3;
  display: flex;
  gap: 6px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  transition: background 0.3s;
}

.dot.active {
  background: #ffffff;
}
</style>
