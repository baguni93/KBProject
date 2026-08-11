<template>
  <div ref="cardRef" class="card" :style="{ background: cardBackground }">
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
    <!-- 카드 기본 정보 고정 레이어 -->
    <div class="card-content">
      <div class="text-chip-container">
        <div class="bank-name">KB 국민카드</div>
        <div class="card-name">{{ currentCardData.cardName }}</div>

        <!-- 💡 화살표가 칩 왼쪽에 오도록 순서 변경 -->
        <div v-if="currentCardData.cardChip" class="chip-wrapper">
          <span class="chip-arrow" aria-hidden="true">&lt;</span>
          <img
            :src="currentCardData.cardChip"
            class="card-chip-img"
            alt="card chip"
            draggable="false"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
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
    cardChip: cardStore.cardChip,
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
  border-radius: 10px;
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

/* .custom-emoji-item.is-text-sticker {
  width: 90px;
  height: 50px;
}

.custom-emoji-item.is-text-sticker img {
  width: 100%;
  height: 100%;
} */

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

.bank-name {
  font-size: 12px;
  font-weight: 700;
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
/* 💡 텍스트와 칩을 묶는 컨테이너 스타일 (신규) */
.card-content {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px; /* 양쪽 여백 균일하게 */
  bottom: 20px;
  z-index: 15;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 위쪽(이름)과 아래쪽(칩) 분리 */
}

/* 텍스트와 칩을 묶어주는 컨테이너 */
.text-chip-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px; /* 💡 카드 이름과 IC칩 사이의 간격 */
}

/* 은행명 스타일 (필요시 추가) */
.bank-name {
  font-size: 11px;
  font-weight: 500;
  margin-bottom: -12px; /* 은행명과 카드 이름 사이를 더 가깝게 붙임 */
  margin-top: -10px;
  margin-left: -5px;
}

/* 카드 이름 텍스트 */
.card-name {
  opacity: 0.9;
  font-size: 10px;
  font-weight: 700;
  margin: 0;
  margin-left: -5px;
}

/* IC칩 이미지 위치 및 크기 */
.card-chip-img {
  width: 40px; /* 실제 카드 칩 표준 크기 */
  height: auto;
  object-fit: contain;
  pointer-events: none;
  margin-top: 4px; /* 이름과의 미세 간격 조정 */
}

/* 💡 칩과 화살표를 감싸는 래퍼 */
.chip-wrapper {
  display: flex;
  align-items: center; /* 세로 정렬 중앙 */
  gap: 3px; /* 화살표와 칩 사이의 간격 */

  /* 🛠️ 전체 위치 미세 조정 (가로, 세로) */
  transform: translate(-5px, 0px);
  opacity: 0.8;
}

/* 화살표 기호 스타일 */
.chip-arrow {
  font-size: 11px;
  font-weight: 900;
  color: white;
  opacity: 0.85;
  transform: scaleY(1.4);
  pointer-events: none;

  /* 🛠️ 화살표만 따로 위치를 칩에 더 붙이거나 띄우고 싶을 때 사용 */
  /* margin-right: -2px; */
}
</style>
