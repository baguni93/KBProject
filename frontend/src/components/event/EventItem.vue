<template>
  <div
    :class="[
      'event-item',
      { 'border-yellow': currentEvent.buttonStatus === 'REWARD_CLAIM' },
    ]"
    v-if="currentEvent"
  >
    <div class="event-img">
      <span class="img-icon">{{ getEventImg(currentEvent.eventName) }}</span>
    </div>

    <!-- 이벤트 요약정보 -->
    <div class="event-info">
      <div class="title-row">
        <h4 class="event-title">{{ currentEvent.eventName }}</h4>
        <span v-if="currentEvent.dDay" class="status-tag">{{
          currentEvent.dDay
        }}</span>
      </div>
      <p class="event-desc">{{ currentEvent.eventDesc }}</p>

      <!-- 진행도 바 -->
      <div class="progress-container">
        <div class="progress-bar-group">
          <div
            v-for="index in currentEvent.eventTarget || 1"
            :key="index"
            :class="[
              'progress-segment',
              { active: index <= (currentEvent.currentTargetCount || 0) },
            ]"
          ></div>
        </div>
        <span class="event-level">Lv.{{ currentEvent.eventLevel || 1 }}</span>
      </div>
    </div>

    <!-- 배지 버튼 -->
    <div class="event-action">
      <!-- 리워드 포인트P / 챌린지 경험치 -->
      <div class="reward-info">
        <span class="reward-points">+{{ currentEvent.rewardPoint }}P</span>
        <span v-if="currentEvent.rewardExp" class="reward-exp">
          +{{ currentEvent.rewardExp }}EXP
        </span>
      </div>
      <button
        :class="['action-btn', getStatusClass(currentEvent)]"
        :disabled="getEventStatus(currentEvent).disabled"
        @click="handleButtonClick"
      >
        {{ getStatusText(currentEvent) }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue';

const emit = defineEmits(['clickAction']);

// props 관련 수정함, 보완 필요
const props = defineProps({
  event: {
    type: Object,
    default: null,
  },
  eventId: [Number, String],
  eventName: String,
  eventDesc: String,
  eventType: String,
  eventStatus: String,
  eventImgName: String,
  eventTarget: Number,
  currentTargetCount: Number,
  eventLevel: Number,
  eventDailyLimitCount: Number,
  isDailyLimitReached: [Boolean, String, Number],
  startAt: String,
  endAt: String,
  rewardId: [Number, String],
  rewardPoint: Number,
  rewardExp: Number,
  completed: [Boolean, String],
  joined: [Boolean, String],
  rewardReceived: [Boolean, String],
  dDay: String,
});

const currentEvent = computed(() => {
  if (props.event) return props.event;
  return props;
});

// 이벤트 배너 이미지 아이콘 처리
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

// 배지 버튼 상태 판단
const getEventStatus = (item) => {
  if (!item)
    return {
      text: '참여',
      styleClass: 'bg-yellow',
      status: 'READY',
      disabled: false,
    };

  const isRewardReceived =
    item.rewardReceived === true || item.rewardReceived === 'true';
  const isCompleted = item.completed === true || item.completed === 'true';
  const isJoined = item.joined === true || item.joined === 'true';
  const isDailyLimitReached =
    item.isDailyLimitReached === true ||
    item.isDailyLimitReached === 'true' ||
    item.isDailyLimitReached === 1;

  // 1. 보상 수령 완료
  if (isRewardReceived) {
    return {
      text: '수령완료',
      styleClass: 'bg-gray',
      status: 'COMPLETED',
      disabled: true,
    };
  }

  // 2. 조건 달성 (보상 미수령)
  if (isCompleted) {
    return {
      text: '보상받기',
      styleClass: 'bg-yellow',
      status: 'REWARD_CLAIM',
      disabled: false,
    };
  }

  // 3. 일일 제한 달성
  if (isDailyLimitReached) {
    return {
      text: '참여완료',
      styleClass: 'bg-gray',
      status: 'DAILY_LIMIT',
      disabled: true,
    };
  }

  // 4. 참여하기
  if (isJoined) {
    return {
      text: '참여중',
      styleClass: 'bg-gray',
      status: 'PROGRESS',
      disabled: true,
    };
  }

  // 5. 기본 참여 가능
  return {
    text: '참여',
    styleClass: 'bg-yellow',
    status: 'READY',
    disabled: false,
  };
};

const getStatusText = (item) => getEventStatus(item).text;
const getStatusClass = (item) => getEventStatus(item).styleClass;

const handleButtonClick = () => {
  const statusInfo = getEventStatus(currentEvent.value);

  if (statusInfo.disabled) return;

  emit('clickAction', {
    eventId: currentEvent.value.eventId,
    eventName: currentEvent.value.eventName,
    rewardId: currentEvent.value.rewardId,
    buttonStatus: statusInfo.status,
  });
};
</script>

<style scoped>
.event-item {
  background-color: #ffffff;
  border-radius: 20px;
  padding: 16px;
  display: flex;
  align-items: center;
  margin-bottom: 0;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.04);
  border: 1px solid #e2e8f0;
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

.reward-info {
  display: flex;
  flex-direction: column; /* 세로 정렬,  */
  align-items: flex-end; /* 우측 정렬 */
  gap: 2px;
}

.reward-points {
  font-size: 14px;
  font-weight: bold;
  color: #ffbc00;
}

.reward-exp {
  font-size: 13px;
  color: #bdbebd;
  font-weight: 500;
  margin-left: 4px;
}

.action-btn {
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

.action-btn.bg-yellow {
  background-color: #ffbc00;
  color: #222222;
}

.action-btn.bg-gray {
  background-color: #f5f5f5;
  color: #222222;
}
</style>
