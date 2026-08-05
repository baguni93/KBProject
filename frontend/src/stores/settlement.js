import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import settlementApi from '@/api/settlementApi';

export const useSettlementStore = defineStore('settlement', () => {
  const settlements = ref([]);

  const activeSettlements = computed(() =>
    settlements.value.filter((settlement) => settlement.status !== 'CANCEL'),
  );

  const get = async (params) => {
    settlements.value = await settlementApi.get(params);
  };

  const getMyList = async (params) => {
    settlements.value = await settlementApi.getMyList(params);
  };

  const cancel = async (params) => {
    await settlementApi.cancel(params);

    const settlement = settlements.value.find(
      (s) => s.settlementId === params.settlementId,
    );

    if (settlement) {
      settlement.status = 'CANCEL';
    }
  };

  const updateSettlement = (settlement) => {
    const index = settlements.value.findIndex(
      (x) => x.settlementId === settlement.settlementId,
    );

    if (index !== -1) {
      settlements.value[index] = settlement;
    }
  };

  const payment = async (params) => {
    await settlementApi.payment(params);

    const settlement = settlements.value.find(
      (s) => s.settlementId === params.settlementId,
    );

    if (!settlement) return;

    const member = settlement.members.find(
      (member) => member.userId === params.userId,
    );

    if (member) {
      member.status = 'COMPLETE';
    }
  };

  const remine = async (params) => {
    await settlementApi.remine(params);
  };

  return {
    settlements,
    activeSettlements,
    get,
    getMyList,
    cancel,
    payment,
    remine,
    updateSettlement,
  };
});
