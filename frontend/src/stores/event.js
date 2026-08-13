import { defineStore } from 'pinia';
import { ref } from 'vue';
import eventApi from '@/api/eventApi';

export const useEventStore = defineStore('event', () => {
  // 사용자 포인트
  const userPoint = ref(0);
  // 챌린지 데이터
  const challengeData = ref(null);
  //

  const updateUserPoint = (addedPoint) => {
    userPoint.value += addedPoint;
  };

  return {
    userPoint,
    challengeData,
    updateUserPoint,
  };
});
