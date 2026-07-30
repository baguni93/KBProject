<template>
  <section class="section">
    <div class="section-header">
      <h3>정산 요청</h3>

      <button class="more-btn">전체보기 ></button>
    </div>

    <SettlementCard
      v-for="settlement in settlements"
      :key="settlement.settlementId"
      :settlement="settlement"
    />
  </section>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import SettlementCard from './SettlementCard.vue';
import api from '@/api/settlementApi.js';

const settlements = ref([]);

const query = reactive({
  userId: 3,
});

const load = async (query) => {
  try {
    settlements.value = await api.getMyList(query);
    console.log(settlements.value);
  } catch {}
};

onMounted(() => {
  load(query);
});
</script>

<style scoped>
.section {
  background: white;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
}

.more-btn {
  border: none;
  background: none;
  color: #999;
}
</style>
