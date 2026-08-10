<template>
  <div class="container">
    <div class="event-main">
      <header class="main-header">
        <span class="event-title">이벤트</span>
      </header>

      <!-- 1. 사용자 포인트 조회 -->
      <PointView :point="userPoint" />

      <!-- 2. 커스텀 카드 발급 바로가기 -->
      <EventMainCardBanner />

      <!-- 3. 이벤트 챌린지 -->
      <EventMainChallenge v-if="challengeData" :challenge="challengeData" />

      <!-- 4. 현재 참여 가능 이벤트  -->
      <template v-if="activeEvents && activeEvents.length > 0">
        <div class="section-title-group">
          <i class="fa-regular fa-star header-icon"></i>
          <span class="sub-section-title">현재 참여 가능 이벤트</span>
        </div>
        <EventItem
          v-for="event in activeEvents.slice(0, 3)"
          :key="event.eventId"
          v-bind="event"
          @clickAction="(payload) => onEventAction(payload)"
        />
      </template>

      <!-- 데이터 없을 시 안내문구 -->
      <div v-else class="no-event">
        <p>현재 참여 가능한 이벤트가 없습니다.</p>
      </div>

      <!-- 5. 전체 이벤트 리스트 조회 -->
      <div class="footer-action">
        <button class="all-events-btn" @click="goToEventList">
          -> 전체 이벤트 리스트 조회
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import eventApi from '@/api/eventApi';
import PointView from '@/components/finance/PointView.vue';
import EventMainCardBanner from '@/components/event/EventMainCardBanner.vue';
import EventMainChallenge from '@/components/event/EventMainChallenge.vue';
import EventItem from '@/components/event/EventItem.vue';

/// 유저 아이디
import { useAuthStore } from '@/stores/auth';
const authStore = useAuthStore();
const userId = authStore.userId ?? 1;

const router = useRouter();

const userPoint = ref(0);
const challengeData = ref(null);
const activeEvents = ref([]);

// 메인 페이지 데이터 로드
const fetchMainData = async () => {
  if (!userId) return;

  try {
    //const dataOrigin = await eventApi.getEventMain(userId);

    const data = await eventApi.getEventList(userId);

    //console.log(' Origin : ', dataOrigin);
    console.log('참여 가능한 이벤트 리스트 조회 : ', data);
    activeEvents.value = data;

    userPoint.value = data.currentPoint || 0;

    challengeData.value = data.userChallenge || {
      // 이벤트 챌린지 default
      userChallengeLevel: 1,
      userChallengeExe: 0,
      userChallengeMaxExe: 1000,
    };

    const rawEvents = data.eventLists || [];

    console.log(rawEvents);

    // if (Array.isArray(rawEvents)) {
    //   activeEvents.value = rawEvents.filter((item) => {
    //     if (!item) return false;
    //     const eventStatus = item.buttonStatus;

    //     return (
    //       !eventStatus ||
    //       (eventStatus !== 'COMPLETE' && eventStatus !== 'ATTENDANCE')
    //     );
    //   });
    // } else {
    //   activeEvents.value = [];
    // }
  } catch (err) {
    console.error('데이터 로드 실패', err);
  }
};

onMounted(() => {
  fetchMainData();
});

// 이벤트 참여/보상 수령 처리
// 이벤트 참여/보상 수령 처리
const onEventAction = async ({
  eventId,
  eventName,
  rewardId,
  buttonStatus,
}) => {
  if (!eventId) return;

  if (!userId.value) {
    alert('올바른 사용자 정보가 아닙니다.');
    return;
  }

  if (buttonStatus === 'COMPLETED' || buttonStatus === 'DAILY_LIMIT') {
    alert('이미 참여 완료된 이벤트입니다.');
    return;
  }

  const isAttendance = eventName?.includes('출석');

  const actionMap = {
    // 1. 이벤트 참여 시작 / 출석체크
    READY: {
      action: () =>
        isAttendance
          ? eventApi.joinAttendanceEvent(eventId, userId.value)
          : eventApi.joinEvent(eventId, userId.value),
      msg: `[${eventName}] 이벤트 참여를 시작합니다.`,
    },

    // 2. 출석체크
    ATTENDANCE: {
      action: () => eventApi.joinAttendanceEvent(eventId, userId.value),
      msg: `[${eventName}] 출석체크가 완료되었습니다.`,
    },
    ATTENDANCE_READY: {
      action: () => eventApi.joinAttendanceEvent(eventId, userId.value),
      msg: `[${eventName}] 출석체크가 완료되었습니다.`,
    },

    // 3. 진행 중 이벤트
    PROGRESS: {
      action: () => eventApi.joinEvent(eventId, userId.value),
      msg: `[${eventName}] 이벤트 참여가 완료되었습니다.`,
    },

    // 4. 오늘자 참여 완료
    DAILY_LIMIT: {
      action: async () => {
        alert('이미 참여한 이벤트입니다.');
      },
      msg: null,
    },

    // 5. 목표 달성 후 보상 수령 (rewardId 전달 추가)
    REWARD_CLAIM: {
      action: () =>
        isAttendance
          ? eventApi.receiveAttendanceEventReward(
              eventId,
              userId.value,
              rewardId,
            )
          : eventApi.receiveEventReward(eventId, userId.value, rewardId),
      msg: `[${eventName}] 보상 수령이 완료되었습니다!`,
    },
  };

  const targetAction = actionMap[buttonStatus];

  if (!targetAction) {
    console.warn(`정의되지 않은 버튼 상태입니다: ${buttonStatus}`);
    return;
  }

  try {
    await targetAction.action();

    if (targetAction.msg) {
      alert(targetAction.msg);
    }

    // 메인 데이터 갱신
    if (typeof fetchMainData === 'function') {
      await fetchMainData();
    }
  } catch (error) {
    console.error('이벤트 처리 실패:', error);
    const errorMsg =
      error.response?.data?.message ||
      '이벤트 참여 처리 요청 중 오류가 발생했습니다.';
    alert(errorMsg);
  }
};

const goToEventList = () => {
  router.push('/event/list');
};
</script>

<style scoped>
.container {
  background-color: #f8f9fa;
  padding: 12px 12px 60px 12px;
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  min-height: auto;
  box-sizing: border-box;
}

.event-main {
  display: flex;
  flex-direction: column;
  gap: 16px; /* 배너 간격 */
  width: 100%;
  box-sizing: border-box;
}

/* 이벤트 최상단 타이틀 */
.event-title {
  font-size: 20px;
  color: #222222;
  font-weight: 900;
  margin: 4px 0 2px 4px;
}

.section-title-group {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 6px 0 2px 4px;
}

.header-icon {
  font-size: 15px;
  color: #ffb703;
}

.sub-section-title {
  font-size: 15px;
  font-weight: 800;
  color: #1e293b;
}

/* 전체 이벤트 리스트 조회 버튼 */
.all-events-btn {
  width: 100%;
  box-sizing: border-box;
  padding: 16px 20px;
  background-color: #ffbc00;
  border-radius: 16px;
  border: none;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  color: #222222;
  font-size: 15px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(255, 188, 0, 0.15);
  cursor: pointer;
  transition: background-color 0.2s ease;
  margin-top: 0;
}

.all-events-btn:hover {
  background-color: #e5a900;
}

/* 데이터 없을 경우 레이아웃 */
.no-event {
  background-color: #ffffff;
  border: 1px dashed #cbd5e1;
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  color: #64748b;
  font-size: 14px;
}

.footer-action {
  margin-top: 0;
}
</style>
