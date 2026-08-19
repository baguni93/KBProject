<template>
  <main class="kb-mobile-page event-page">
    <PageHeader
      title="이벤트"
      :showBack="true"
      :customBack="true"
      @back="goToFinance"
    />

    <div class="event-content">
      <div class="event-main">

      <!-- 1. 사용자 포인트 조회 - 포인트 지갑과 동일한 일반 CSS 스타일 -->
      <section
        class="event-balance-card kb-card"
        role="button"
        tabindex="0"
        aria-label="포인트 지갑으로 이동"
        @click="goToPointWallet"
        @keydown.enter="goToPointWallet"
        @keydown.space.prevent="goToPointWallet"
      >
        <div class="event-balance-info">
          <div class="event-balance-label text-13-bold">내 포인트</div>

          <div class="event-balance-value text-28-bold">
            {{ userPoint.toLocaleString() }}<span class="text-18-bold">P</span>
          </div>

          <div class="event-balance-sub text-13">
            현금처럼 전환해서 사용할 수 있어요
          </div>
        </div>

        <div class="event-point-symbol" aria-hidden="true">P</div>
      </section>

      <!-- 2. 커스텀 카드 발급 바로가기 -->
      <section class="event-section">
        <EventMainCardBanner class="kb-card" />
      </section>

      <!-- 3. 이벤트 챌린지 -->
      <section v-if="challengeData" class="event-section">
        <EventMainChallenge
          class="kb-card"
          :challenge="challengeData"
          @claim-reward="handleClaimReward"
        />
      </section>

      <!-- 4. 현재 참여 가능 이벤트 -->
      <section class="event-section available-event-section">
        <div class="section-heading">
          <h2 class="text-18-bold">현재 참여 가능 이벤트</h2>
        </div>

        <div v-if="eventLists && eventLists.length > 0" class="event-card-list">
          <EventItem
            v-for="event in eventLists.slice(0, 3)"
            :key="event.eventId"
            v-bind="event"
            @clickAction="(payload) => onEventAction(payload)"
          />
        </div>

        <!-- 데이터 없을 시 안내문구 -->
        <div v-else class="no-event kb-card text-14">
          <p>현재 참여 가능한 이벤트가 없습니다.</p>
        </div>
      </section>

      <!-- 5. 전체 이벤트 리스트 조회 -->
      <div class="footer-action event-section">
        <button type="button" class="content-btn primary" @click="goToEventList">
          전체 이벤트 리스트 조회
        </button>
      </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import eventApi from '@/api/eventApi';
import PageHeader from '@/components/common/PageHeader.vue';
import EventMainCardBanner from '@/components/event/EventMainCardBanner.vue';
import EventMainChallenge from '@/components/event/EventMainChallenge.vue';
import EventItem from '@/components/event/EventItem.vue';
import { useFeedStore } from '@/stores/feed';
const feedStore = useFeedStore();
// 유저 아이디
import { useAuthStore } from '@/stores/auth';
const authStore = useAuthStore();
const userId = computed(() => authStore.userId);

const router = useRouter();

const userPoint = ref(0);
const challengeData = ref(null);
const eventLists = ref([]);

// 메인 페이지 데이터 로드
const fetchMainData = async () => {
  if (!userId.value) return;

  try {
    const [mainData, attendanceData] = await Promise.all([
      eventApi.getEventMain(userId.value),
      eventApi.getAttendanceEventList(userId.value),
    ]);

    userPoint.value = mainData.currentPoint || 0;
    eventLists.value = [
      ...(mainData.eventLists || []),
      ...(attendanceData || []),
    ];

    if (mainData?.userChallengeData?.length > 0) {
      challengeData.value = mainData.userChallengeData[0];
    } else challengeData.value = null;
  } catch (err) {
    console.error('데이터 로드 실패', err);
  }
};

onMounted(() => {
  fetchMainData();
});

watch(
  () => authStore.userId,
  (userId) => {
    if (userId) {
      fetchMainData();
    }
  },
);

// 이벤트 챌린지 보상 수령 처리
const handleClaimReward = async (challengeId) => {
  try {
    // API 호출
    const response = await eventApi.receiveChallengeReward(
      userId.value,
      challengeId,
    );

    if (response) {
      alert('보상 수령이 완료되었습니다.');
      await fetchMainData(); // 화면 데이터 갱신
    }
  } catch (error) {
    console.error('보상 수령 오류:', error);
    alert('보상 수령에 실패했습니다.');
  }
};

// 이벤트 참여/보상 수령 처리
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

  if (buttonStatus === 'COMPLETED' || buttonStatus === 'DAILY_LIMIT') {
    alert('이미 참여 완료된 이벤트입니다.');
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

    if (targetAction.msg) {
      alert(targetAction.msg);
    }

    // 메인 데이터 갱신
    await fetchMainData();
  } catch (error) {
    console.error('이벤트 처리 실패:', error);
    const errorMsg =
      error.response?.data?.message ||
      '이벤트 참여 처리 요청 중 오류가 발생했습니다.';
    alert(errorMsg);
  }
};

const goToFinance = () => {
  router.push('/finance');
};

const goToPointWallet = () => {
  router.push('/point-wallet');
};

const goToEventList = () => {
  router.push('/event/list');
};
</script>

<style scoped>
.event-page {
  min-height: 100%;
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

/* 소비분석과 동일한 공용 헤더 좌우 여백 */
.event-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

/* 소비분석/포인트지갑과 동일한 좌우 여백 */
.event-content {
  padding: 12px 24px 26px;
}

.event-main {
  width: 100%;
  box-sizing: border-box;
}

/* 공용 kb-section과 같은 섹션 간격 */
.event-section {
  margin-top: 18px;
}

.event-main > .event-balance-card:first-child {
  margin-top: 0;
}

/* 이벤트 화면 내 포인트 카드 - PointWalletPage 스타일 */
.event-balance-card {
  min-height: 132px;
  padding: 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border: 1px solid rgba(255, 188, 46, 0.22);
  background: linear-gradient(135deg, #fff4c6 0%, #fffaf0 100%);
  box-shadow: 0 5px 18px rgba(30, 30, 30, 0.05);
  cursor: pointer;
}

.event-balance-info {
  min-width: 0;
}

.event-balance-label {
  color: #6c654f;
}

.event-balance-value {
  margin-top: 6px;
  color: var(--color-text-main);
  line-height: 1.15;
  letter-spacing: -1px;
}

.event-balance-value span {
  margin-left: 3px;
}

.event-balance-sub {
  margin-top: 8px;
  color: var(--color-text-sub);
  line-height: 1.45;
  word-break: keep-all;
}

.event-point-symbol {
  width: 60px;
  height: 60px;
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-text-white);
  font-size: 30px;
  font-weight: 600;
  box-shadow:
    inset 0 -3px 0 rgba(0, 0, 0, 0.08),
    0 6px 14px rgba(242, 170, 16, 0.18);
}

/* Finance의 '금융 서비스' 제목 규격 */
.section-heading {
  margin: 0 3px 12px;
}

.section-heading h2 {
  margin: 0;
  color: var(--color-text-main);
}

.event-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ===== 하위 이벤트 컴포넌트 공용 UI 덮어쓰기 =====
   하위 파일을 수정하지 않고 이 페이지 안에서만 공용 규격 적용 */

/* 커스텀 카드/이벤트 챌린지: 실제 kb-card 클래스 사용
   기존 다크 배경은 유지하고 카드 외곽 규격은 공용 kb-card 기준으로 통일 */
.event-page :deep(.banner-card.kb-card),
.event-page :deep(.challenge-card.kb-card) {
  border: 0;
  border-radius: 18px;
  box-shadow: 0 4px 16px rgba(30, 30, 30, 0.07);
}

.event-page :deep(.banner-card) {
  min-height: 90px;
  padding: 16px;
}

.event-page :deep(.banner-title) {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  word-break: keep-all;
}

.event-page :deep(.banner-desc) {
  margin: 0;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.45;
  word-break: keep-all;
}

/* 이벤트 챌린지: 커스텀 카드와 같은 타이포 기준 */

.event-page :deep(.challenge-background) {
  padding: 16px 16px 10px;
}

.event-page :deep(.label-text) {
  font-size: 15px;
  font-weight: 600;
}

.event-page :deep(.reward-point-badge) {
  font-size: 13px;
  font-weight: 600;
}

.event-page :deep(.percentage-info) {
  font-size: 13px;
  font-weight: 600;
}

.event-page :deep(.lv-text),
.event-page :deep(.xp-counter),
.event-page :deep(.success-tag) {
  font-size: 12px;
  font-weight: 500;
}

.event-page :deep(.level-reward-btn) {
  width: 100%;
  min-height: 44px;
  padding: 0 16px;
  border: none;
  border-radius: 10px;
  background: var(--color-primary);
  color: var(--color-text-main);
  font-size: 13px;
  font-weight: 700;
  box-shadow: none;
}

/* 진행 이벤트 카드: 공용 kb-card 외형 */
.event-page :deep(.event-item) {
  padding: 16px;
  border: 0;
  border-radius: 18px;
  background: var(--color-bg-page);
  box-shadow: 0 4px 16px rgba(30, 30, 30, 0.07);
}

.event-page :deep(.event-item.border-yellow) {
  border: 0;
  box-shadow: 0 4px 16px rgba(30, 30, 30, 0.07);
}

.event-page :deep(.event-title) {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.35;
}

.event-page :deep(.event-desc) {
  margin: 4px 0 7px;
  color: var(--color-text-sub);
  font-size: 12px;
  font-weight: 400;
  line-height: 1.45;
  word-break: keep-all;
}

.event-page :deep(.event-level),
.event-page :deep(.reward-points),
.event-page :deep(.reward-exp) {
  font-size: 12px;
  font-weight: 500;
}

/* 이벤트 참여 버튼을 공용 content-btn.small 규격으로 맞춤 */
.event-page :deep(.action-btn) {
  width: auto;
  min-width: 62px;
  height: auto;
  padding: 7px 14px;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
}

.event-page :deep(.action-btn.bg-yellow) {
  background: var(--color-primary);
  color: var(--color-text-main);
}

.event-page :deep(.action-btn.bg-gray) {
  border: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
  color: var(--color-text-body);
}

.no-event {
  padding: 20px;
  color: var(--color-text-sub);
  text-align: center;
}

.no-event p {
  margin: 0;
}

.footer-action {
  margin-bottom: 0;
}
</style>
