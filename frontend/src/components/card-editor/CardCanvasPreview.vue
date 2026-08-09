<template>
  <div class="card" :style="{ background: cardBackground }">
    <!-- 패턴 레이어 -->
    <div
      v-if="currentCardData.pattern"
      class="pattern"
      :style="{ backgroundImage: `url(${currentCardData.pattern})` }"
    />

    <!-- 드로잉 이미지 렌더링 레이어 -->
    <div
      v-if="currentCardData.savedDrawingImage"
      class="drawing-image-layer"
      :style="{ backgroundImage: `url(${currentCardData.savedDrawingImage})` }"
    />

    <!-- 텍스트 레이어 (읽기 전용) -->
    <div class="custom-text-wrapper">
      <div
        v-for="item in currentCardData.texts || []"
        :key="item.id"
        class="custom-text-item"
        :style="{
          fontFamily: item.font,
          color: item.color,
          fontSize: item.size,
          fontWeight: item.isBold ? 'bold' : 'normal',
          left: `${item.x ?? 50}%`,
          top: `${item.y ?? 50}%`,
          transform: `translate(-50%, -50%) rotate(${item.rotation ?? 0}deg)`,
        }"
      >
        <span>{{ item.text }}</span>
      </div>
    </div>

    <!-- 이모지 레이어 (읽기 전용) -->
    <div class="custom-emoji-wrapper">
      <div
        v-for="item in currentCardData.emojis || []"
        :key="item.id"
        class="custom-emoji-item"
        :class="{
          'is-text-sticker': item.emojiObj?.emoji?.includes('text_'),
        }"
        :style="{
          left: `${item.x ?? 50}%`,
          top: `${item.y ?? 50}%`,
          transform: `translate(-50%, -50%) rotate(${item.rotation ?? 0}deg)`,
        }"
      >
        <img :src="item.emojiObj?.emoji" alt="" draggable="false" />
      </div>
    </div>

    <!-- 카드 기본 정보 고정 레이어 -->
    <div class="card-content">
      <div class="card-name">{{ currentCardData.cardName }}</div>
      <div class="card-bottom">
        <span>{{ currentCardData.cardNumber }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useCardEditorStore } from '@/stores/cardEditorStore';

const cardStore = useCardEditorStore();

// 💡 Pinia의 history가 단일 객체이므로 배열 방식 대신 단일 객체 기준으로 변경
const currentCardData = computed(() => {
  if (cardStore.history) {
    return cardStore.history;
  }
  // 히스토리가 아직 없다면 현재 스토어 상태 반환
  return {
    cardName: cardStore.cardName,
    cardNumber: cardStore.cardNumber,
    color: cardStore.color,
    gradient: cardStore.gradient,
    image: cardStore.image,
    pattern: cardStore.pattern,
    texts: cardStore.texts,
    emojis: cardStore.emojis,
    savedDrawingImage: cardStore.savedDrawingImage,
  };
});

// 카드 배경 계산 (히스토리 데이터 기준
const cardBackground = computed(() => {
  const data = currentCardData.value;
  if (data.image) return `url(${data.image}) center / cover no-repeat`;
  if (data.gradient) return data.gradient;
  return data.color || '#1e40af';
});
</script>
<style scoped>
.card {
  width: 280px;
  height: 175px;
  border-radius: 17px;
  padding: 15px;
  box-sizing: border-box;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.pattern {
  position: absolute;
  inset: 0;
  background-repeat: repeat;
  background-position: center;
  background-size: 80px;
  opacity: 0.5;
  border-radius: 17px;
}

.drawing-image-layer {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  border-radius: 17px;
}

.custom-emoji-wrapper,
.custom-text-wrapper {
  position: absolute;
  inset: 0;
}

.custom-emoji-item {
  position: absolute;
  transform: translate(-50%, -50%);
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.custom-emoji-item img {
  width: 36px;
  height: 36px;
  object-fit: contain;
  display: block;
}

.custom-emoji-item.is-text-sticker {
  width: 90px;
  height: 50px;
}

.custom-emoji-item.is-text-sticker img {
  width: 100%;
  height: 100%;
}

.custom-text-item {
  position: absolute;
  transform: translate(-50%, -50%);
  white-space: nowrap;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
  line-height: 1.2;
  padding: 4px 6px;
}

.card-content {
  position: relative;
  z-index: 15;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}

.card-name {
  font-size: 10px;
  font-weight: 700;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  font-size: 8px;
}
</style>
