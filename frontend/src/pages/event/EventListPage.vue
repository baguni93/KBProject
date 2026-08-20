<template>
  <main class="kb-mobile-page event-list-page">
    <PageHeader
      title="이벤트 리스트"
      :showBack="true"
      :customBack="true"
      @back="goToEventPage"
    />

    <!-- 소비분석의 1/3/12개월 탭처럼 헤더 바로 아래, 좌우 끝까지 배치 -->
    <nav class="event-list-tabs" aria-label="이벤트 목록 구분">
      <button
        v-for="tab in EVENT_LIST_TABS"
        :key="tab.value"
        type="button"
        class="event-list-tab-btn"
        :class="{ active: currentTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </nav>

    <div class="event-list-content">
      <div class="list-content-wrapper">

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
  </main>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import eventApi from '@/api/eventApi';
import PageHeader from '@/components/common/PageHeader.vue';
import EventItem from '@/components/event/EventItem.vue';
import EventHistoryItem from '@/components/event/EventHistoryItem.vue';
import { useFeedStore } from '@/stores/feed';
const feedStore = useFeedStore();
// 유저 아이디
import { useAuthStore } from '@/stores/auth';
const authStore = useAuthStore();
const userId = computed(() => authStore.userId);

const router = useRouter();
const route = useRoute();

const EVENT_LIST_TABS = [
  { value: 'active', label: '진행 중' },
  { value: 'joined', label: '참여완료' },
];

const currentTab = ref('active');
const eventList = ref([]);
const isLoading = ref(false);

const goToEventPage = () => {
  router.push('/event');
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
  if (!userId.value) return;

  isLoading.value = true;
  try {
    const [eventData, attendanceData] = await Promise.all([
      eventApi.getEventList(userId.value),
      eventApi.getAttendanceEventList(userId.value),
    ]);
    eventList.value = [...(eventData || []), ...(attendanceData || [])];
  } catch (error) {
    console.error('이벤트 리스트 조회 실패:', error);
  } finally {
    isLoading.value = false;
  }
};

// 3. 참여완료 내역 조회
const loadJoinedEventList = async (targetMonth) => {
  if (!userId.value) return;

  isLoading.value = true;
  const monthToFetch = targetMonth || selectedYearMonth.value;
  try {
    const data = await eventApi.getJoinedEventList(userId.value, monthToFetch);
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
  () => [route.path, route.query.yearMonth, userId.value],
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
  eventType,
  eventCategory,
  rewardId,
  buttonStatus,
}) => {
  if (!eventId) return;

  if (!userId.value) {
    alert('올바른 사용자 정보가 아닙니다.');
    return;
  }

  const isAttendance = eventType === 'ATTENDANCE';
  const missionRoutes = {
    FEED: '/feed',
    CARD: '/card/create/intro',
    WALLET: '/wallet',
    SETTLEMENT: '/settlement',
    RANDOMBOX: '/point-wallet/random-box',
    ANALYSIS: '/analysis',
  };

  const actionMap = {
    // 1. 이벤트 참여 시작 / 출석체크
    READY: {
      action: async () => {
        return isAttendance
          ? eventApi.joinAttendanceEvent(userId.value, eventId)
          : eventApi.joinEvent(userId.value, eventId);
      },
      msg: isAttendance
        ? `[${eventName}] 출석체크가 완료되었습니다.`
        : `[${eventName}] 이벤트 참여를 시작합니다.`,
    },

    // 2. 출석체크
    ATTENDANCE: {
      action: () => eventApi.joinAttendanceEvent(userId.value, eventId),
      msg: `[${eventName}] 출석체크가 완료되었습니다.`,
    },
    ATTENDANCE_READY: {
      action: () => eventApi.joinAttendanceEvent(userId.value, eventId),
      msg: `[${eventName}] 출석체크가 완료되었습니다.`,
    },

    // 3. 진행 중 이벤트
    PROGRESS: {
      action: async () => {
        const targetRoute = missionRoutes[eventCategory];
        if (!targetRoute) throw new Error('이벤트 이동 경로가 없습니다.');
        await router.push(targetRoute);
      },
      msg: null,
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
      action: async () => {
        // 1. 보상 수령
        if (isAttendance) {
          await eventApi.receiveAttendanceEventReward(
            userId.value,
            eventId,
            rewardId,
          );
        } else {
          await eventApi.receiveEventReward(userId.value, eventId, rewardId);
        }

        // 2. 보상 수령 후 피드 생성
        const fromData = feedStore.createRequestDTO({
          targetId: eventId,
          feedType: 'EVENT',
          visibility: 'PUBLIC',
        });

        await feedStore.createFeed(fromData);
      },

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
.event-list-page {
  min-height: 100%;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

/* 소비분석과 동일한 공용 헤더 좌우 여백 */
.event-list-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

/* 소비분석 1/3/12개월 탭처럼 헤더에 바로 붙고 좌우 끝까지 */
.event-list-tabs {
  width: 100%;
  height: 44px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  box-sizing: border-box;
  border-bottom: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
}

.event-list-tab-btn {
  position: relative;
  min-width: 0;
  height: 44px;
  padding: 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  border: none;
  background: transparent;
  color: var(--color-text-sub);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
}

.event-list-tab-btn.active {
  color: var(--color-text-main);
  font-weight: 600;
}

.event-list-tab-btn.active::after {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 3px;
  border-radius: 3px 3px 0 0;
  background: var(--color-primary);
  content: '';
}

/* 목록 영역은 소비분석/포인트지갑과 같은 좌우 24px */
.event-list-content {
  padding: 12px 24px 8px;
}

.list-content-wrapper {
  width: 100%;
}

/* 내부 스크롤/고정 높이 제거 */
.list-content {
  width: 100%;
  height: auto;
  max-height: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: visible;
}

.date-picker-section {
  width: 100%;
  margin: 4px 0 18px;
}

.month-selector {
  width: 100%;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  border-radius: 18px;
  background: var(--color-bg-page);
  box-shadow: 0 4px 16px rgba(30, 30, 30, 0.07);
}

.current-month-display {
  color: var(--color-text-main);
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
  user-select: none;
}

.picker-nav-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--color-text-sub);
  font-size: 12px;
  cursor: pointer;
}

/* ===== 하위 이벤트 카드 공용 UI 덮어쓰기 ===== */

/* 진행 중 카드: kb-card 외형 */
.event-list-page :deep(.event-item) {
  padding: 16px;
  border: 0;
  border-radius: 18px;
  background: var(--color-bg-page);
  box-shadow: 0 4px 16px rgba(30, 30, 30, 0.07);
}

.event-list-page :deep(.event-item.border-yellow) {
  border: 0;
  box-shadow: 0 4px 16px rgba(30, 30, 30, 0.07);
}

.event-list-page :deep(.event-title) {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.35;
}

.event-list-page :deep(.event-desc) {
  margin: 4px 0 7px;
  color: var(--color-text-sub);
  font-size: 12px;
  font-weight: 400;
  line-height: 1.45;
  word-break: keep-all;
}

.event-list-page :deep(.event-level),
.event-list-page :deep(.reward-points),
.event-list-page :deep(.reward-exp) {
  font-size: 12px;
  font-weight: 500;
}

.event-list-page :deep(.action-btn) {
  width: auto;
  min-width: 62px;
  height: auto;
  padding: 7px 14px;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
}

.event-list-page :deep(.action-btn.bg-yellow) {
  background: var(--color-primary);
  color: var(--color-text-main);
}

.event-list-page :deep(.action-btn.bg-gray) {
  border: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
  color: var(--color-text-body);
}

/* 참여완료 카드도 kb-card 외형과 공용 타이포 규격 */
.event-list-page :deep(.history-item-card) {
  width: 100%;
  margin-bottom: 0;
  padding: 16px;
  border: 0;
  border-radius: 18px;
  background: var(--color-bg-page);
  box-shadow: 0 4px 16px rgba(30, 30, 30, 0.07);
}

.event-list-page :deep(.history-item-card .event-title) {
  margin: 0 0 2px;
  color: var(--color-text-main);
  font-size: 14px;
  font-weight: 600;
}

.event-list-page :deep(.event-date) {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 400;
}

.event-list-page :deep(.complete-tag) {
  font-size: 12px;
  font-weight: 500;
}

.event-list-page :deep(.card-bottom) {
  padding: 10px 14px;
  border-radius: 12px;
  background: var(--color-bg-screen);
}

.event-list-page :deep(.bottom-label) {
  color: var(--color-text-sub);
  font-size: 12px;
  font-weight: 400;
}

.event-list-page :deep(.bottom-value) {
  color: #f1ad00;
  font-size: 15px;
  font-weight: 600;
}

.event-list-page :deep(.exp-text) {
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 500;
}

.loading-box,
.empty-box {
  padding: 40px 0;
  color: var(--color-text-sub);
  font-size: 14px;
  font-weight: 500;
  text-align: center;
}
</style>
