<template>
  <div>
    <img :src="feed.card?.url" class="image-wrapper" />
  </div>

  <button class="custom-card-btn" @click="issueCard">
    <span class="icon">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <rect
          x="4"
          y="5"
          width="16"
          height="14"
          rx="1"
          stroke="white"
          stroke-width="2"
        />
        <line x1="4" y1="9" x2="20" y2="9" stroke="white" stroke-width="2" />
      </svg>
    </span>

    <span class="text"> 해당 커스텀 카드 발급 받기 </span>
  </button>
</template>

<script setup>
import { useCardEditorStore } from '@/stores/cardEditorStore';
const cardEditorStore = useCardEditorStore();
import { useRouter } from 'vue-router';
const router = useRouter();

const props = defineProps({
  feed: {
    type: Object,
    required: true,
  },
});

const issueCard = async () => {
  console.log('커스텀 카드 발급 버튼 클릭');
  console.log('해당 피드 ID :', props.feed.feedId);
  console.log('해당 피드 TargetID:', props.feed.targetId);
  await cardEditorStore.getCustomCard(props.feed.userId, props.feed.targetId);

  router.push('/card/create/agreement');
  // 카드 발급 API 호출 예정
};
</script>

<style scoped>
.image-wrapper {
  width: 65%;

  display: block;

  margin: 0 auto;

  border-radius: 16px;

  object-fit: contain;
}

/* 버튼 */
.custom-card-btn {
  position: relative;

  width: 85%;
  max-width: 420px;

  height: clamp(48px, 8vw, 58px);

  margin: 16px auto 0;

  border: none;

  border-radius: 18px;

  display: flex;

  align-items: center;

  justify-content: center;

  gap: clamp(6px, 2vw, 12px);

  padding: 0 clamp(12px, 4vw, 24px);

  background: linear-gradient(90deg, #3478ff, #48d1c8);

  color: white;

  cursor: pointer;

  box-shadow: 0 8px 20px rgba(50, 120, 255, 0.25);

  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease,
    opacity 0.15s ease;

  -webkit-tap-highlight-color: transparent;
}

/* 클릭 애니메이션 */
.custom-card-btn:active {
  transform: scale(0.96);

  opacity: 0.85;

  box-shadow: 0 4px 10px rgba(50, 120, 255, 0.2);
}

/* PC hover */
.custom-card-btn:hover {
  filter: brightness(1.05);
}

/* 아이콘 */
.icon {
  display: flex;

  align-items: center;

  flex-shrink: 0;

  width: clamp(18px, 5vw, 24px);
}

.icon svg {
  width: 100%;

  height: auto;
}

/* 텍스트 */
.text {
  font-size: clamp(12px, 3.8vw, 16px);

  font-weight: 600;

  white-space: nowrap;

  overflow: hidden;

  text-overflow: ellipsis;

  text-align: center;
}

/* 화살표 */
.arrow {
  position: absolute;

  right: clamp(12px, 4vw, 20px);

  display: flex;

  align-items: center;
}

.arrow svg {
  width: clamp(14px, 4vw, 20px);

  height: auto;
}

/* 작은 화면 */
@media (max-width: 360px) {
  .custom-card-btn {
    width: 90%;
  }

  .text {
    max-width: 170px;
  }
}
</style>
