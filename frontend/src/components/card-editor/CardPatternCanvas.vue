<template>
  <div
    class="card"
    :class="{ active: isSelected }"
    :style="{
      background: cardBackground,
      '--active-color': activeBorderColor /* 동적으로 외곽선 색상 전달 */,
    }"
  >
    <!-- 선택 표시 (오른쪽 상단 하얀 점) -->
    <span v-if="isSelected" />

    <!-- 패턴 레이어 -->
    <div
      v-if="currentPattern"
      class="pattern"
      :style="{ backgroundImage: `url(${currentPattern})` }"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useCardEditorStore } from '@/stores/cardEditorStore';

const props = defineProps({
  pattern: {
    type: String,
    default: '',
  },
});

const cardStore = useCardEditorStore();

const currentPattern = computed(() => props.pattern || cardStore.pattern);

// 현재 패턴이 선택된 상태인지 체크
const isSelected = computed(() => {
  return props.pattern && cardStore.pattern === props.pattern;
});

// 카드 바탕 배경
const cardBackground = computed(() => {
  if (cardStore.image)
    return `url(${cardStore.image}) center / cover no-repeat`;

  if (cardStore.gradient) return cardStore.gradient;

  return cardStore.color || '#1e40af';
});

// [추가] active 테두리에 쓰일 색상 (노란색/카드색 등 스토어 색상 반영)
const activeBorderColor = computed(() => {
  return cardStore.color || '#FFB000'; // 기본 노란색 포인트 또는 현재 카드 색상
});
</script>

<style scoped>
.card {
  width: 100%;
  height: 100%;
  border-radius: 14px;
  padding: 0;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
  cursor: pointer;

  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);

  /* [컬러 피커와 동일] 호버 & 선택 트랜지션 */
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

/* [컬러 피커와 100% 동일한 Hover 효과] */
.card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.18);
}

.card:active {
  transform: translateY(-1px);
}

/* [컬러 피커와 100% 동일한 Active 효과] */
.card.active {
  transform: scale(1.05);

  /* 흰색 링 + 카드의 active-color 링 이중 효과 */
  box-shadow:
    0 0 0 3px white,
    0 0 0 5px var(--active-color),
    0 8px 20px rgba(0, 0, 0, 0.18);
}

/* [컬러 피커와 100% 동일한 선택 하얀 점] */
.card span {
  position: absolute;
  width: 10px;
  height: 10px;
  right: 8px;
  top: 8px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
  z-index: 2;
}

.pattern {
  position: absolute;
  inset: 0;
  background-repeat: repeat;
  background-position: center;
  background-size: 35px;
  opacity: 0.6;
  pointer-events: none;
}
</style>
