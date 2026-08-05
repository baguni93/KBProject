<template>
  <div class="event-main">
    <header class="main-header">
      <h4>이벤트</h4>
    </header>

    <!-- 1. 사용자 포인트 조회 -->
    <PointView :point="userPoint" />

    <!-- 2. 커스텀 카드 발급 바로가기 -->
    <EventMainCardBanner />

    <!-- 3. 이벤트 챌린지 -->
    <EventMainChallenge v-if="challengeData" :challenge="challengeData" />

    <!-- 4. 현재 참여 가능 이벤트 -->
    <div class="event-list-section">
      <p>현재 참여 가능 이벤트</p>

      <div v-if="activeEvents && activeEvents.length > 0" class="event-list">
        <EventItem
          v-for="event in activeEvents.slice(0, 3)"
          :key="event.eventId"
          :event="event"
          @clickAction="(payload) => onEventAction(payload)"
        />
      </div>

      <!-- 데이터 없음 안내문구 -->
      <div v-else class="no-event">
        <p>현재 참여 가능한 이벤트가 없습니다.</p>
      </div>
    </div>

    <!-- 5. 전체 이벤트 리스트 조회 -->
    <div class="footer-action">
      <button class="all-events-btn" @click="goToEventList">
        -> 전체 이벤트 리스트 조회
      </button>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import eventApi from '@/api/eventApi';
import PointView from '@/components/finance/PointView.vue';
import EventMainCardBanner from '@/components/event/EventMainCardBanner.vue';
import EventMainChallenge from '@/components/event/EventMainChallenge.vue';
import EventItem from '@/components/event/EventItem.vue';

const userPoint = ref(0);
const activeEvents = ref([]);
const challengeData = ref(null);

// 데이터 로드
onMounted(async () => {
  try {
    const data = await eventApi.getEventMainPage(currentUserId.value);

    console.log('백엔드 서버 응답 데이터:', data);

    userPoint.value = data.currentPoint; // 내 포인트

    challengeData.value = data.userChallenge || {
      userChallengeLevel: 1,
      userChallengeExe: 0,
      userChallengeMaxExe: 1000,
      status: 'PROCESS',
    };

    if (data && data.activeEvents && Array.isArray(data.activeEvents)) {
      activeEvents.value = data.activeEvents.filter((item) => {
        if (!item) return false;
        const eventStatus = item.buttonStatus;

        return (
          !eventStatus ||
          (eventStatus !== 'COMPLETE' && eventStatus !== 'ATTENDANCE_COMPLETE')
        );
      });
    } else {
      activeEvents.value = [];
    }
  } catch (err) {
    console.error('데이터 로드 실패', err);
  }
});

// 이벤트 참여 처리
const currentUserId = ref(1);

const onEventAction = async ({ eventId, eventName, buttonStatus }) => {
  if (!eventId) return;

  if (!currentUserId.value) {
    alert('올바른 사용자 정보가 아닙니다.');
    return;
  }

  const apiMap = {
    ATTENDANCE_READY: () => eventApi.joinEvent(eventId, currentUserId.value),
    READY: () => eventApi.joinEvent(eventId, currentUserId.value),
    PROGRESS: () => eventApi.joinEvent(eventId, currentUserId.value),
    REWARD_CLAIM: () =>
      eventApi.receiveEventReward(eventId, currentUserId.value),
  };

  if (!apiMap[buttonStatus]) return;

  try {
    // API 호출
    await apiMap[buttonStatus]();

    if (typeof loadActiveEvents === 'function') {
      await loadActiveEvents();
    }
    if (typeof loadEventMainPageData === 'function') {
      await loadEventMainPageData();
    }
  } catch (error) {
    console.error('이벤트 처리 실패:', error);
    const errorMsg =
      error.response?.data?.message || '요청 처리 중 오류가 발생했습니다.';
    alert(errorMsg);
  }
};

const router = useRouter();

const goToEventList = () => {
  router.push('/event/list');
};
</script>
<style scoped>
.event-main {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 480px;
  margin: 0 auto;
  box-sizing: border-box;
}
.event-list-container {
  max-width: 480px;
  margin: 0 auto;
  padding: 16px;
  background-color: #f8f9fa;
  box-sizing: border-box;
  min-height: 850px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.container {
  background-color: #f8f9fa;
  padding: 12px 12px 60px 12px;
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  min-height: 100vh;
  box-sizing: border-box;
  display: block;
}

.content-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card {
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
  width: 100%;
  box-sizing: border-box;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
  margin-bottom: 8px;
}

.summary-card {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

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
}

.all-events-btn:hover {
  background-color: #e5a900;
}
</style>
