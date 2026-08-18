<template>
  <section class="section">
    <!-- =========================
         1. 요청 방향 탭
         ========================= -->
    <div class="type-tab">
      <button
        class="type-tab-item"
        :class="{ active: selectedType === 'requested' }"
        @click="selectedType = 'requested'"
      >
        요청한 내역
      </button>

      <button
        class="type-tab-item"
        :class="{ active: selectedType === 'received' }"
        @click="selectedType = 'received'"
      >
        요청받은 내역
      </button>
    </div>

    <!-- =========================
         2. 상태 세그먼트
         ========================= -->
    <div class="status-area">
      <div class="status-segment">
        <!-- 진행 중 -->
        <button
          class="segment-item"
          :class="{ active: selectedStatus === 'progress' }"
          @click="selectedStatus = 'progress'"
        >
          <span>진행 중</span>

          <span v-if="progressCount > 0" class="segment-count">
            {{ progressCount }}
          </span>
        </button>

        <!-- 완료 -->
        <button
          class="segment-item"
          :class="{ active: selectedStatus === 'complete' }"
          @click="selectedStatus = 'complete'"
        >
          <span>완료</span>

          <span v-if="completeCount > 0" class="segment-count">
            {{ completeCount }}
          </span>
        </button>

        <!-- 취소 -->
        <button
          class="segment-item"
          :class="{ active: selectedStatus === 'cancel' }"
          @click="selectedStatus = 'cancel'"
        >
          <span>취소</span>

          <span v-if="cancelCount > 0" class="segment-count">
            {{ cancelCount }}
          </span>
        </button>
      </div>
    </div>

    <!-- =========================
         3. 정산 목록
         ========================= -->
    <template v-if="filteredSettlements.length > 0">
      <div
        v-for="settlement in filteredSettlements"
        :key="settlement.settlementId"
        class="settlement-item"
      >
        <SettlementNewSection :settlement="settlement" />
      </div>
    </template>

    <!-- =========================
         4. 정산 없음
         ========================= -->
    <div v-else class="empty">
      <div class="empty-icon">
        <span v-if="selectedStatus === 'progress'">✓</span>
        <span v-else-if="selectedStatus === 'complete'">✓</span>
        <span v-else>×</span>
      </div>

      <p>
        {{
          selectedStatus === 'progress'
            ? '진행 중인 정산이 없습니다.'
            : selectedStatus === 'complete'
              ? '완료된 정산이 없습니다.'
              : '취소된 정산이 없습니다.'
        }}
      </p>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';

import SettlementNewSection from './SettlementNewSection.vue';
import { useSettlementStore } from '@/stores/settlement';
import { useAuthStore } from '@/stores/auth.js';

/* =========================
 * Props
 *
 * initialType
 * requested : 내가 요청한 내역
 * received  : 내가 요청받은 내역
 *
 * initialStatus
 * progress : 진행 중
 * complete : 완료
 * cancel   : 취소
 * ========================= */

const props = defineProps({
  initialType: {
    type: String,
    default: 'requested',
  },

  initialStatus: {
    type: String,
    default: 'progress',
  },
});

/* =========================
 * Props 변경 감지
 * ========================= */

watch(
  () => props.initialType,
  (value) => {
    selectedType.value = value;
  },
);

watch(
  () => props.initialStatus,
  (value) => {
    selectedStatus.value = value;
  },
);

/* =========================
 * Store
 * ========================= */

const authStore = useAuthStore();
const settlementStore = useSettlementStore();

const userId = authStore.userId;

/* =========================
 * 현재 선택 상태
 * ========================= */

const selectedType = ref(props.initialType);
const selectedStatus = ref(props.initialStatus);

/* =========================
 * 전체 정산
 *
 * CANCEL 포함
 * ========================= */

const allSettlements = computed(() => {
  return settlementStore.allSettlements ?? [];
});

/* =========================
 * 요청한 내역
 * ========================= */

const requestedSettlements = computed(() => {
  return allSettlements.value.filter(
    (settlement) => settlement.requesterId === userId,
  );
});

/* =========================
 * 요청받은 내역
 * ========================= */

const receivedSettlements = computed(() => {
  return allSettlements.value.filter(
    (settlement) => settlement.requesterId !== userId,
  );
});

/* =========================
 * 현재 요청 방향
 * ========================= */

const typedSettlements = computed(() => {
  if (selectedType.value === 'requested') {
    return requestedSettlements.value;
  }

  return receivedSettlements.value;
});

/* =========================
 * 진행 중 정산
 *
 * COMPLETE / CANCEL 제외
 * ========================= */

const progressSettlements = computed(() => {
  return typedSettlements.value.filter(
    (settlement) =>
      settlement.status !== 'COMPLETE' && settlement.status !== 'CANCEL',
  );
});

/* =========================
 * 완료 정산
 * ========================= */

const completeSettlements = computed(() => {
  return typedSettlements.value.filter(
    (settlement) => settlement.status === 'COMPLETE',
  );
});

/* =========================
 * 취소 정산
 * ========================= */

const cancelSettlements = computed(() => {
  return typedSettlements.value.filter(
    (settlement) => settlement.status === 'CANCEL',
  );
});

/* =========================
 * 진행 중 개수
 * ========================= */

const progressCount = computed(() => {
  return progressSettlements.value.length;
});

/* =========================
 * 완료 개수
 * ========================= */

const completeCount = computed(() => {
  return completeSettlements.value.length;
});

/* =========================
 * 취소 개수
 * ========================= */

const cancelCount = computed(() => {
  return cancelSettlements.value.length;
});

/* =========================
 * 현재 선택된 정산
 * ========================= */

const filteredSettlements = computed(() => {
  switch (selectedStatus.value) {
    case 'progress':
      return progressSettlements.value;

    case 'complete':
      return completeSettlements.value;

    case 'cancel':
      return cancelSettlements.value;

    default:
      return progressSettlements.value;
  }
});

/* =========================
 * 정산 조회
 * ========================= */

onMounted(() => {
  settlementStore.getMyList({
    userId,
  });
});
</script>

<style scoped>
/* =================================================
 * 전체
 * ================================================= */

.section {
  width: 100%;
  box-sizing: border-box;
}

/* =================================================
 * 요청한 / 요청받은 탭
 * ================================================= */

.type-tab {
  position: relative;

  display: flex;

  width: 100%;
  height: 48px;

  border-bottom: 1px solid #eceff3;
}

.type-tab-item {
  position: relative;

  flex: 1;

  border: none;
  background: transparent;

  color: #9ca3af;

  font-size: 14px;
  font-weight: 500;
  letter-spacing: -0.2px;

  cursor: pointer;

  transition:
    color 0.2s ease,
    font-weight 0.2s ease;
}

.type-tab-item.active {
  color: #222;
  font-weight: 700;
}

.type-tab-item.active::after {
  content: '';

  position: absolute;

  left: 18%;
  right: 18%;
  bottom: -1px;

  height: 2px;

  border-radius: 999px;

  background: #ffcc00;
}

/* =================================================
 * 상태 영역
 * ================================================= */

.status-area {
  display: flex;
  justify-content: flex-end;

  padding: 12px 0;
}

/* =================================================
 * 상태 세그먼트
 * ================================================= */

.status-segment {
  display: flex;
  align-items: center;

  width: 210px;
  height: 34px;

  padding: 3px;

  box-sizing: border-box;

  background: #f3f4f6;

  border-radius: 9px;
}

/* =================================================
 * 세그먼트 버튼
 * ================================================= */

.segment-item {
  flex: 1;

  height: 28px;

  display: flex;
  align-items: center;
  justify-content: center;

  gap: 4px;

  border: none;
  border-radius: 7px;

  background: transparent;

  color: #9ca3af;

  font-size: 11px;
  font-weight: 600;

  cursor: pointer;

  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.segment-item.active {
  background: #ffffff;

  color: #374151;

  box-shadow:
    0 1px 2px rgba(0, 0, 0, 0.05),
    0 1px 3px rgba(0, 0, 0, 0.04);
}

/* =================================================
 * 상태 개수
 * ================================================= */

.segment-count {
  min-width: 16px;
  height: 16px;

  padding: 0 4px;

  display: inline-flex;
  align-items: center;
  justify-content: center;

  box-sizing: border-box;

  border-radius: 999px;

  background: #e5e7eb;

  color: #6b7280;

  font-size: 9px;
  font-weight: 700;
}

.segment-item.active .segment-count {
  background: #fff3bf;
  color: #a16207;
}

/* =================================================
 * 정산 카드
 * ================================================= */

.settlement-item {
  margin-bottom: 12px;
}

.settlement-item:last-child {
  margin-bottom: 0;
}

/* =================================================
 * Empty
 * ================================================= */

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  padding: 42px 0;

  color: #9ca3af;
}

.empty-icon {
  width: 40px;
  height: 40px;

  margin-bottom: 10px;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 50%;

  background: #f5f5f5;

  color: #9ca3af;

  font-size: 18px;
  font-weight: 600;
}

.empty p {
  margin: 0;

  font-size: 13px;
  color: #9ca3af;
}
</style>
