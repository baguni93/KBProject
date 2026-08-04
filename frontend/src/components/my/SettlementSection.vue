<template>
  <section class="section">
    <div class="section-header">
      <div class="section-title">
        <span>내 정산</span>
      </div>
      <button class="more-btn" @click="goSettlementList">전체보기 ></button>
    </div>

    <!-- 정산 있음 -->
    <template v-if="visibleSettlements.length > 0">
      <div
        class="col-12 mb-3"
        v-for="settlement in visibleSettlements"
        :key="settlement.settlementId"
      >
        <SettlementCard :settlement="settlement" />
      </div>

      <button
        v-if="remainingCount > 0"
        class="more-settlement-btn"
        @click="goSettlementList"
      >
        + 정산 {{ remainingCount }}개 더 보기
      </button>
    </template>

    <!-- 정산 없음 -->
    <div v-else class="empty">진행 중인 정산이 없습니다.</div>
  </section>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

import SettlementCard from './SettlementCard.vue';
import { useSettlementStore } from '@/stores/settlement';

import { useUserStore } from '@/stores/user';

const router = useRouter();

const userStore = useUserStore();
const userId = userStore.userId;

const settlementStore = useSettlementStore();

const query = {
  userId,
};

const visibleSettlements = computed(() => {
  return settlementStore.activeSettlements.slice(0, 3);
});

const remainingCount = computed(() => {
  return Math.max(settlementStore.activeSettlements.length - 3, 0);
});

const goSettlementList = () => {
  router.push({
    name: 'settlement/list',
  });
};

onMounted(() => {
  settlementStore.getMyList(query);
});
</script>

<style scoped>
.section {
  padding: 0 0px 0px;
}

.section-header {
  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-top: 20px;
  margin-bottom: 30px;

  padding-bottom: 8px;

  border-bottom: 1px solid #eceff3;
}

.section-title {
  display: flex;
  padding: 10px;
  align-items: center;
}

.section-title span {
  font-size: 15px;

  font-weight: 600;

  color: #2d3748;

  letter-spacing: -0.2px;
}

.section-header h3 {
  margin: 0;

  font-size: 18px;
}

.more-btn {
  border: none;

  background: none;

  padding: 0;

  color: #2d3748;

  cursor: pointer;

  font-size: 12px;

  font-weight: 600;

  letter-spacing: -0.2px;
}

.more-settlement-btn {
  width: 100%;

  margin-top: 4px;

  padding: 12px;

  border: none;

  border-radius: 12px;

  background: #f5f5f5;

  font-size: 14px;

  font-weight: 600;

  cursor: pointer;
}

.empty {
  text-align: center;

  color: #999;

  padding: 30px 0;

  font-size: 14px;
}
</style>
