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
      <span class="bottom-label">획득 보상</span>
      <span class="bottom-value">
        +{{ event.rewardPoint }} P
        <span class="exp-text">| +{{ event.rewardExp }} exp</span>
      </span>
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
      eventDesc: '',
      eventImgName: '',
      rewardId: 0,
      recvId: 0,
      rewardPoint: 0,
      rewardExp: 0,
      receivedAt: '',
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
const getEventImg = (eventName) => {
  const name = eventName || '';

  if (name.includes('출석')) return '☀️';
  if (name.includes('피드')) return '📄';
  if (name.includes('카드')) return '🎨';
  if (name.includes('결제') || name.includes('지갑')) return '💳';
  if (name.includes('정산')) return '📜';
  if (name.includes('박스') || name.includes('랜덤')) return '🎁';
  if (name.includes('분석')) return '📊';

  return '💡';
};
</script>

<style scoped>
.history-item-card {
  width: 100%;
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
  flex-shrink: 0;
}

.img-icon {
  font-size: 22px;
}

.event-info {
  display: flex;
  flex-direction: column;
  text-align: left;
  flex: 1;
  min-width: 0;
  margin-right: 8px;
}

.event-title {
  font-size: 14px;
  font-weight: 700;
  color: #2b3a4a;
  margin: 0 0 2px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.event-date {
  font-size: 12px;
  color: #adb5bd;
  font-weight: 500;
  margin: 0;
}

/* 참여완료 배지  */
.complete-tag {
  margin-left: auto;
  align-self: center;
  background-color: #e8f5e9;
  color: #2e7d32;
  font-size: 11px;
  font-weight: bold;
  padding: 4px 8px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
  flex-shrink: 0;
}

.complete-txt {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
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

.exp-text {
  font-size: 13px;
  color: #bdbebd;
  font-weight: 500;
  margin-left: 4px;
}
</style>
