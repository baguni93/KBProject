<template>
  <div class="history-item-card" v-if="event">
    <div class="card-top">
      <div class="event-img">
        <span class="img-icon">{{ getEventImg(event.eventName) }}</span>
      </div>

      <div class="event-info">
        <h4 class="event-title">{{ event.eventName }}</h4>
        <p class="event-date">{{ formatToFullDate(event.joinedDate) }}</p>
      </div>

      <div class="complete-tag">
        <p class="complete-txt">
          <i class="fa-solid fa-check check-icon"></i> 참여완료
        </p>
      </div>
    </div>

    <div class="card-bottom">
      <span class="bottom-label">획득 포인트</span>
      <span class="bottom-value">+{{ event.rewardPoint }} P</span>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue';

defineProps({
  event: {
    type: Object,
    required: true,
    default: () => ({
      eventId: 0,
      eventName: '',
      joinedDate: '',
      rewardPoint: 0,
    }),
  },
});

const formatToFullDate = (dateSrc) => {
  if (!dateSrc) return '';
  const date = new Date(dateSrc);
  if (isNaN(date.getTime())) return dateSrc;

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}.${month}.${day}`;
};

// 이벤트 배너 이미지 아이콘 처리
// 임시로 이모지 사용
const getEventImg = (title) => {
  if (!title) return '📢';
  if (title.includes('출석')) return '📅';
  if (title.includes('피드')) return '📝';
  if (title.includes('카드')) return '🎨';
  if (title.includes('결제') || title.includes('지갑')) return '💳';
  if (title.includes('소비') || title.includes('분석')) return '📊';
  return '💡';
};
</script>

<style scoped>
.history-item-card {
  background-color: #f8f9fa;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-sizing: border-box;
}
.card-top {
  display: flex;
  align-items: center;
  position: relative;
  width: 100%;
}

.event-img {
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background-color: #fffde7;
  border-radius: 12px;
}

.img-icon {
  font-size: 22px;
}

.event-info {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.event-title {
  font-size: 15px;
  font-weight: 700;
  color: #2b3a4a;
  margin: 0 0 2px 0;
}

.event-date {
  font-size: 12px;
  color: #adb5bd;
  font-weight: 500;
}

.complete-tag {
  margin-left: auto;
  align-self: flex-start;
  background-color: #e8f5e9;
  color: #2e7d32;
  font-size: 11px;
  font-weight: bold;
  padding: 4px 10px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.check-icon {
  font-size: 10px;
}

.card-bottom {
  width: 100%;
  background-color: #ffffff;
  border-radius: 12px;
  padding: 10px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
}

.bottom-label {
  font-size: 12px;
  color: #8a94a0;
  font-weight: 500;
}

.bottom-value {
  font-size: 15px;
  font-weight: 800;
  color: #ffbc00;
}
</style>
