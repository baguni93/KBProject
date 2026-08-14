<template>
  <div class="event-list-container">
    <div class="list-content-wrapper">
      <header class="list-header">
        <button class="back-btn" @click="goToEventPage">← 이벤트 리스트</button>
      </header>

      <!-- 이벤트 리스트 탭 (진행 중 / 참여 완료) -->
      <nav class="tab-menu">
        <button
          :class="['tab-btn', { active: currentTab === 'active' }]"
          @click="switchTab('active')"
        >
          진행 중
        </button>
        <button
          :class="['tab-btn', { active: currentTab === 'joined' }]"
          @click="switchTab('joined')"
        >
          참여완료
        </button>
      </nav>

      <!-- 참여완료 이벤트 탭일 때 -> 검색연월 변경 -->
      <div v-if="currentTab === 'joined'" class="date-picker-section">
        <div class="month-selector">
          <!-- 이전 달 이동 -->
          <button class="picker-nav-btn" @click="changeMonth(-1)">
            <i class="fa-solid fa-chevron-left"></i>
          </button>

          <!-- 현재 연월 -->
          <div class="current-month-display">
            {{ formatDisplayYearMonth(selectedYearMonth) }}
          </div>

          <!-- 다음 달 이동 -->
          <button class="picker-nav-btn" @click="changeMonth(1)">
            <i class="fa-solid fa-chevron-right"></i>
          </button>
        </div>
      </div>

      <!-- 진행 중 / 참여 완료 -->
      <main class="list-content">
        <template
          v-for="eventItem in eventList"
          :key="eventItem?.eventId || eventItem?.id"
        >
          <component
            v-if="eventItem"
            :is="currentTab === 'active' ? EventItem : EventHistoryItem"
            :event="eventItem"
            @click-action="onEventAction"
          />
        </template>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useEventStore } from '@/stores/event';
import eventApi from '@/api/eventApi';
import EventItem from '@/components/event/EventItem.vue';
import EventHistoryItem from '@/components/event/EventHistoryItem.vue';

// 유저 아이디
import { useAuthStore } from '@/stores/auth';
const authStore = useAuthStore();
const userId = authStore.userId ?? 1;

const router = useRouter();
const route = useRoute();

const currentTab = ref('active');
const eventList = ref([]);
const isLoading = ref(false);

const goToEventPage = () => {
  router.push('/event/main');
};

// 날짜 관련 헬퍼 함수
const formatDisplayYearMonth = (yearMonthStr) => {
  if (!yearMonthStr) return '';
  return yearMonthStr.replace('-', '.');
};

const getTodayYearMonth = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
};

const selectedYearMonth = ref(getTodayYearMonth());

// 2. 이벤트 리스트 조회 (진행 중)
const loadEventList = async () => {
  if (!userId) return;

  isLoading.value = true;
  try {
    const data = await eventApi.getEventList(userId);
    eventList.value = data;
  } catch (error) {
    console.error('이벤트 리스트 조회 실패:', error);
  } finally {
    isLoading.value = false;
  }
};

// 3. 참여완료 내역 조회
const loadJoinedEventList = async (targetMonth) => {
  if (!userId) return;

  isLoading.value = true;
  const monthToFetch = targetMonth || selectedYearMonth.value;
  try {
    const data = await eventApi.getJoinedEventList(userId, monthToFetch);
    eventList.value = data;
  } catch (error) {
    console.error('참여 내역 데이터 조회 실패:', error);
  } finally {
    isLoading.value = false;
  }
};

// 탭 변경
const switchTab = (tab) => {
  currentTab.value = tab;
  if (tab === 'active') {
    router.push({ path: '/event/list' });
  } else {
    router.push({
      path: '/event/list/joined',
      query: { yearMonth: selectedYearMonth.value },
    });
  }
};

// 월 변경
const changeMonth = (direction) => {
  const [year, month] = selectedYearMonth.value.split('-').map(Number);
  const targetDate = new Date(year, month - 1 + direction, 1);

  const nextYear = targetDate.getFullYear();
  const nextMonth = String(targetDate.getMonth() + 1).padStart(2, '0');
  const newYearMonth = `${nextYear}-${nextMonth}`;

  if (currentTab.value === 'joined') {
    router.push({
      path: '/event/list/joined',
      query: { yearMonth: newYearMonth },
    });
  } else {
    selectedYearMonth.value = newYearMonth;
  }
};

watch(
  () => [route.path, route.query.yearMonth, userId],
  ([path, queryMonth, currentUserId]) => {
    if (!currentUserId) return;

    currentTab.value = path.includes('/joined') ? 'joined' : 'active';
    if (queryMonth) selectedYearMonth.value = queryMonth;

    currentTab.value === 'joined' ? loadJoinedEventList() : loadEventList();
  },
  { immediate: true },
);

// 4. 이벤트 참여/보상수령 액션 처리
const onEventAction = async ({
  eventId,
  eventName,
  rewardId,
  buttonStatus,
}) => {
  if (!eventId) return;

  if (!userId) {
    alert('올바른 사용자 정보가 아닙니다.');
    return;
  }

  const isAttendance = eventName?.includes('출석');

  const actionMap = {
    // 1. 이벤트 참여 시작 / 출석체크
    READY: {
      action: async () => {
        // 이벤트 시작 / 출석체크 시작 내역 생성 API 먼저 실행
        try {
          if (isAttendance) {
            await eventApi.joinAttendanceEvent(userId, eventId);
          } else {
            await eventApi.joinEvent(userId, eventId);
          }
        } catch (err) {
          console.warn('이미 참가 등록된 이벤트입니다.', err);
        }

        // 이벤트 참여이력 바로 생성되도록
        return await eventApi.createParticipation(userId, eventId);
      },
      msg: `[${eventName}] 이벤트 참여를 시작합니다.`,
    },

    // 2. 출석체크
    ATTENDANCE: {
      action: () => eventApi.joinAttendanceEvent(userId, eventId),
      msg: `[${eventName}] 출석체크가 완료되었습니다.`,
    },
    ATTENDANCE_READY: {
      action: () => eventApi.joinAttendanceEvent(userId, eventId),
      msg: `[${eventName}] 출석체크가 완료되었습니다.`,
    },

    // 3. 진행 중 이벤트
    PROGRESS: {
      action: () => eventApi.createParticipation(userId, eventId),
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
          ? eventApi.receiveAttendanceEventReward(eventId, userId, rewardId)
          : eventApi.receiveEventReward(eventId, userId, rewardId),
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

    const successMsg =
      targetAction.msg || `[${eventName}] 참여 처리가 완료되었습니다.`;
    if (targetAction.msg) alert(successMsg);

    if (
      currentTab?.value === 'joined' &&
      typeof loadJoinedEventList === 'function'
    ) {
      await loadJoinedEventList();
    } else if (typeof loadEventList === 'function') {
      await loadEventList();
    }
  } catch (error) {
    console.error('이벤트 처리 실패:', error);
    const errorMsg =
      error.response?.data?.message || '요청 처리 중 오류가 발생했습니다.';
    alert(errorMsg);
  }
};
</script>

<style scoped>
.event-list-container {
  max-width: 480px;
  margin: 0 auto;
  padding: 16px;
  background-color: #ffffff;
  box-sizing: border-box;
  /* min-height: 900px */
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.list-content {
  max-height: 580px;
  overflow-y: auto;
  padding-right: 12px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.list-content-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  flex: 1;
  padding-bottom: 20px;
}

.list-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
.back-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  margin-right: 12px;
  font-size: 18px;
  color: #222222;
  font-weight: 600;
}
.list-header h2 {
  font-size: 18px;
  font-weight: bold;
}

/* 탭 메뉴 디자인 */
.tab-menu {
  display: flex;
  border-bottom: 2px solid #e9ecef;
  margin-bottom: 16px;
}
.tab-btn {
  flex: 1;
  background: none;
  border: none;
  padding: 12px 0;
  font-size: 14px;
  color: #888;
  cursor: pointer;
  font-weight: bold;
  text-align: center;
}
.tab-btn.active {
  color: #ffb703;
  border-bottom: 2px solid #ffb703;
  margin-bottom: -2px;
}

.date-picker-section {
  margin-top: 4px;
  margin-bottom: 20px;
  width: 100%;
}

.month-selector {
  width: 100%;
  background-color: #f1f3f5;
  border-radius: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  box-sizing: border-box;
}

.current-month-display {
  font-size: 15px;
  font-weight: 800;
  color: #2b3a4a;
  letter-spacing: 0.5px;
  user-select: none;
}

.picker-nav-btn {
  background: none;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8a94a0;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.picker-nav-btn:hover {
  background-color: rgba(0, 0, 0, 0.04);
  color: #2b3a4a;
}

.picker-nav-btn:active {
  transform: scale(0.92);
}

.loading-box,
.empty-box {
  text-align: center;
  padding: 40px 0;
  color: #888;
  font-size: 14px;
}
</style>
