import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import settlementApi from '@/api/settlementApi';

export const useSettlementStore = defineStore('settlement', () => {
  const settlements = ref([]);

  /* =========================
   * 전체 정산
   * ========================= */

  const allSettlements = computed(() => settlements.value);

  /* =========================
   * 진행 중 정산
   *
   * CANCEL 제외
   * ========================= */

  const activeSettlements = computed(() =>
    settlements.value.filter((settlement) => settlement.status !== 'CANCEL'),
  );

  /* =========================
   * 정산 조회
   * ========================= */

  const get = async (params) => {
    settlements.value = await settlementApi.get(params);
  };

  const getMyList = async (params) => {
    settlements.value = await settlementApi.getMyList(params);
    console.log(settlements.value);
  };

  /* =========================
   * 정산 취소
   * ========================= */

  const cancel = async (params) => {
    await settlementApi.cancel(params);

    const settlement = settlements.value.find(
      (s) => s.settlementId === params.settlementId,
    );

    if (settlement) {
      settlement.status = 'CANCEL';
    }
  };

  /* =========================
   * 정산 추가
   * ========================= */

  const addSettlement = (settlement) => {
    const exists = settlements.value.some(
      (x) => x.settlementId === settlement.settlementId,
    );

    if (!exists) {
      settlements.value.unshift(settlement);
    }
  };

  /* =========================
   * 정산 수정
   * ========================= */

  const updateSettlement = (settlement) => {
    const index = settlements.value.findIndex(
      (x) => x.settlementId === settlement.settlementId,
    );

    if (index !== -1) {
      settlements.value[index] = settlement;
    }
  };

  /* =========================
   * 정산 삭제
   * ========================= */

  const removeSettlement = (settlementId) => {
    settlements.value = settlements.value.filter(
      (x) => x.settlementId !== settlementId,
    );
  };

  /* =========================
   * 송금
   * ========================= */

  const payment = async (params) => {
    const settlement = await settlementApi.payment(params);

    updateSettlement(settlement);
  };

  /* =========================
   * 리마인드
   * ========================= */

  const remine = async (params) => {
    await settlementApi.remine(params);
  };

  return {
    settlements,
    allSettlements,
    activeSettlements,

    get,
    getMyList,
    cancel,
    payment,
    remine,

    updateSettlement,
    addSettlement,
    removeSettlement,
  };
});
