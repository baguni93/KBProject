<template>
  <div>
    <img :src="feed.event?.url" class="image-wrapper" />
  </div>

  <button class="event-btn" @click="goEvent">
    <span class="icon">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <path
          d="M8 6L16 12L8 18"
          stroke="white"
          stroke-width="2.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
    </span>

    <span class="text">나도 이벤트 하러가기</span>
  </button>
</template>

<script setup>
const props = defineProps({
  feed: {
    type: Object,
    required: true,
  },
});

const goEvent = () => {
  console.log('이벤트 페이지 이동');
  // router.push('/event')
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
.event-btn {
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

  background: linear-gradient(90deg, #ff5a7a, #ff8ea1);

  color: white;

  cursor: pointer;

  box-shadow: 0 8px 20px rgba(255, 90, 122, 0.25);

  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease,
    opacity 0.15s ease;

  -webkit-tap-highlight-color: transparent;
}

/* 클릭 애니메이션 */
.event-btn:active {
  transform: scale(0.96);

  opacity: 0.85;

  box-shadow: 0 4px 10px rgba(255, 90, 122, 0.2);
}

/* PC hover */
.event-btn:hover {
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

/* 작은 화면 */
@media (max-width: 360px) {
  .event-btn {
    width: 90%;
  }

  .text {
    max-width: 170px;
  }
}
</style>
