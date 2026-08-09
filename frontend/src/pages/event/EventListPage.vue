<template>
  <div class="event-list-container">
    <div class="list-content-wrapper">
      <header class="list-header">
        <button class="back-btn" @click="$router.back()"><- 전체 이벤트</button>
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

      <!-- 진행 중 / 참여내역 -->
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
import { ref, onMounted, computed, watch } from 'vue';
import eventApi from '@/api/eventApi';
import EventItem from '@/components/event/EventItem.vue';
import EventHistoryItem from '@/components/event/EventHistoryItem.vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();
const route = useRoute();

const currentTab = ref('active'); // 진행 중 이벤트 탭 디폴트
const eventList = ref([]);
const currentUserId = ref(1);
const isLoading = ref(false);

const formatDisplayYearMonth = (yearMonthStr) => {
  if (!yearMonthStr) return '';
  return yearMonthStr.replace('-', '.');
};
// 현재 년월 초기값 설정
const getTodayYearMonth = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
};
const selectedYearMonth = ref(getTodayYearMonth());

// 1. 진행 중 이벤트 가져오기 (EVT-002)
const loadActiveEvents = async () => {
  isLoading.value = true;
  try {
    const data = await eventApi.getActiveEventList();
    eventList.value = data;
  } catch (error) {
    console.error('진행 중 이벤트 데이터 조회 실패:', error);
  } finally {
    isLoading.value = false;
  }
};

// 2. 참여 내역 가져오기 (EVT-003)
const loadJoinedEvents = async (targetMonth) => {
  isLoading.value = true;
  const monthToFetch = targetMonth || selectedYearMonth.value;
  try {
    const data = await eventApi.getJoinedEventList(
      currentUserId.value,
      monthToFetch,
    );
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

  // 참여완료 탭에서만
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
  () => [route.path, route.query.yearMonth],
  ([path, queryMonth]) => {
    currentTab.value = path.includes('/joined') ? 'joined' : 'active';
    if (queryMonth) selectedYearMonth.value = queryMonth;

    currentTab.value === 'joined' ? loadJoinedEvents() : loadActiveEvents();
  },
  { immediate: true },
);

// 이벤트 참여 처리

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

  if (!apiMap[buttonStatus]) {
    console.warn(`정의되지 않은 버튼 상태입니다: ${buttonStatus}`);
    return;
  }

  try {
    // API 호출 실행
    await apiMap[buttonStatus]();

    alert(`[${eventName}] 이벤트 참여 완료되었습니다.`);

    // 리스트 새로고침
    if (typeof loadActiveEvents === 'function') {
      await loadActiveEvents();
    }
  } catch (error) {
    console.error('이벤트 처리 실패:', error);
    const errorMsg =
      error.response?.data?.message || '요청 처리 중 오류가 발생했습니다.';
    alert(errorMsg);
  }
};

// onMounted(() => {});
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
