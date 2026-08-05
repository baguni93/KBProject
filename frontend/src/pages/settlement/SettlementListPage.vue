<template>
  <section class="page">
    <PageHeader title="정산 전체보기" />

    <!-- 탭 -->
    <div class="tabs">
      <button
        :class="{ active: currentTab === 'active' }"
        @click="currentTab = 'active'"
      >
        미완료
      </button>

      <button
        :class="{ active: currentTab === 'complete' }"
        @click="currentTab = 'complete'"
      >
        완료
      </button>
    </div>

    <!-- 미완료 -->
    <div v-if="currentTab === 'active'" class="settlement-list">
      <SettlementCard
        v-for="settlement in activeSettlements"
        :key="settlement.settlementId"
        :settlement="settlement"
      />

      <div v-if="activeSettlements.length === 0" class="empty">
        진행 중인 정산이 없습니다
      </div>
    </div>

    <!-- 완료 -->
    <div v-else class="settlement-list">
      <SettlementCard
        v-for="settlement in completedSettlements"
        :key="settlement.settlementId"
        :settlement="settlement"
      />

      <div v-if="completedSettlements.length === 0" class="empty">
        완료된 정산이 없습니다
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import PageHeader from '@/components/common/PageHeader.vue';
import SettlementCard from '@/components/my/SettlementCard.vue';
import { useSettlementStore } from '@/stores/settlement';
import { useUserStore } from '@/stores/user';

const settlementStore = useSettlementStore();
const userStore = useUserStore();

const userId = userStore.userId;

const currentTab = ref('active');

onMounted(() => {
  settlementStore.getMyList({
    userId,
  });
});

const activeSettlements = computed(() =>
  settlementStore.settlements.filter((item) => item.status !== 'COMPLETE'),
);

const completedSettlements = computed(() =>
  settlementStore.settlements.filter((item) => item.status === 'COMPLETE'),
);
</script>

<style scoped>
.page {
  padding: 20px;
}

.tabs {
  display: flex;

  border-bottom: 1px solid #eee;

  margin-bottom: 20px;
}

.tabs button {
  flex: 1;

  border: none;

  background: none;

  padding: 12px;

  font-size: 15px;

  color: #777;

  cursor: pointer;
}

.tabs button.active {
  color: #4f46e5;

  font-weight: 600;

  border-bottom: 2px solid #4f46e5;
}

.settlement-list {
  display: flex;

  flex-direction: column;

  gap: 16px;
}

.empty {
  text-align: center;

  color: #999;

  padding: 30px 0;

  font-size: 14px;
}
</style>
