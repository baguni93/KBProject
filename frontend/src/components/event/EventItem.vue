<template>
  <div
    :class="[
      'event-item',
      { 'border-yellow': event.buttonStatus === 'REWARD_CLAIM' },
    ]"
    v-if="event"
  >
    <div class="event-img">
      <span class="img-icon">{{ getEventImg(event.eventName) }}</span>
    </div>

    <!-- 이벤트 요약정보 -->
    <div class="event-info">
      <div class="title-row">
        <h4 class="event-title">{{ event.eventName }}</h4>
        <span v-if="event.dDay" class="status-tag">{{ event.dDay }}</span>
      </div>
      <p class="event-desc">{{ event.eventDesc }}</p>

      <!-- 진행도 바 -->
      <div class="progress-container">
        <div class="progress-bar-group">
          <div
            v-for="index in event.eventTarget"
            :key="index"
            :class="['progress-segment', { active: index <= event.partCount }]"
          ></div>
        </div>
        <span class="event-level">Lv.{{ event.currentLevel || 1 }}</span>
      </div>
    </div>

    <!-- 배지 버튼 -->
    <div class="event-action">
      <span class="reward-points">+{{ event.rewardPoint }}P</span>
      <button
        :class="['status-btn', getStatusClass(event.buttonStatus)]"
        @click="handleButtonClick"
      >
        {{ getStatusText(event.buttonStatus) }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue';
import eventApi from '@/api/eventApi';

const emit = defineEmits(['clickAction']);

const props = defineProps({
  event: {
    type: Object,
    required: true,
    default: () => ({
      eventId: 0,
      eventName: '',
      tag: '',
      eventDesc: '',
      currentLevel: 0,
      rewardPoint: 0,
      buttonStatus: '',
      partCount: 0,
      reqCount: 0,
    }),
  },
});

const handleButtonClick = () => {
  emit('clickAction', {
    eventId: props.event.eventId,
    eventName: props.event.eventName || props.event.title,
    buttonStatus: props.event.buttonStatus,
  });
};

// 이벤트 배너 이미지 아이콘 처리
const getEventImg = (eventName) => {
  if (!event) return '📢';
  if (eventName.includes('출석')) return '☀️';
  if (eventName.includes('피드')) return '📄';
  if (eventName.includes('카드')) return '🎨';
  if (eventName.includes('결제') || eventName.includes('지갑')) return '💳';
  if (eventName.includes('정산')) return '📜';
  if (eventName.includes('박스') || eventName.includes('랜덤')) return '🎁';
  if (eventName.includes('분석')) return '📊';
  return '💡';
};

// 버튼 텍스트 변환 처리
const getStatusText = (status) => {
  switch (status) {
    case 'ATTENDANCE_READY':
      return '출석';
    case 'ATTENDANCE_COMPLETE':
      return '출석완료';
    case 'REWARD_CLAIM':
      return '보상받기';
    case 'PROGRESS':
      return '참여';
    case 'COMPLETE':
      return '참여완료';
    case 'READY':
      return '참여';
    default:
      return '참여';
  }
};

// 버튼 상태 디자인 처리
const getStatusClass = (status) => {
  if (
    status === 'ATTENDANCE_READY' ||
    status === 'REWARD_CLAIM' ||
    status === 'READY' ||
    status === 'PROGRESS'
  ) {
    return 'bg-yellow';
  }
  if (status === 'ATTENDANCE_COMPLETE' || status === 'COMPLETE') {
    return 'bg-gray';
  }
  return 'bg-yellow';
};

const getActiveSegmentsCount = computed(() => {
  if (!props.event) return 0;

  const current = props.event.partCount || 0;
  const target = props.event.reqCount || 1; // 기본 목표값

  if (current >= target) {
    return 3;
  }

  if (current === 0) {
    return 0;
  }

  // 비율 계산 후 3개 칸으로 치환
  const ratio = current / target;

  if (ratio >= 0.66) {
    return 2;
  }

  if (ratio > 0) {
    return 1;
  }

  return 0;
});
</script>

<style scoped>
/* 기존 스타일 명세 유지 */
.event-item {
  background-color: #f8f9fa;
  border-radius: 20px;
  padding: 16px;
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.01);
  border: 1px solid #f1f3f5;
  box-sizing: border-box;
}

.event-item.border-yellow {
  border: 2px solid #ffbc00;
  box-shadow: 0 4px 12px rgba(255, 188, 0, 0.12);
}

.event-img {
  margin-right: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background-color: #f5f5f5;
  border-radius: 16px;
}

.img-icon {
  font-size: 24px;
}

.event-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.event-title {
  font-size: 14px;
  font-weight: 700;
  color: #222222;
  margin: 0;
}

.status-tag {
  background-color: #ffbc00;
  color: #222222;
  font-size: 10px;
  font-weight: 800;
  padding: 2px 6px;
  border-radius: 6px;
}

.event-desc {
  font-size: 11px;
  color: #8c8c8c;
  margin: 4px 0 6px 0;
  line-height: 1.3;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.progress-bar-group {
  display: flex;
  gap: 4px;
  flex: 1;
}

.progress-segment {
  flex: 1;
  height: 4px;
  background-color: #e9ecef;
  border-radius: 2px;
}

.progress-segment.active {
  background-color: #00b050;
}

.border-yellow .progress-segment.active {
  background-color: #00b050;
}

.event-level {
  font-size: 10px;
  color: #adb5bd;
  font-weight: bold;
}

.event-action {
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  margin-left: 12px;
}

.reward-points {
  font-size: 14px;
  font-weight: bold;
  color: #ffbc00;
}

.status-btn {
  border: none;
  padding: 6px 14px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.status-btn.bg-yellow {
  background-color: #ffbc00;
  color: #222222;
}

.status-btn.bg-gray {
  background-color: #f5f5f5;
  color: #222222;
}
</style>
