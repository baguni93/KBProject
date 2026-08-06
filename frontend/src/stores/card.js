import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { useAuthStore } from '@/stores/auth';

export const useCardStore = defineStore('card', () => {
    const authStore = useAuthStore();

    // 연결 카드 목록
    const cards = ref([]);

    // 로그인 회원번호
    const userId = computed(() => authStore.userId);

    // 연결 카드 개수
    const cardCount = computed(() => cards.value.length);

    // 연결 카드 목록 저장
    const setCards = (cardList) => {
        cards.value = Array.isArray(cardList) ? cardList : [];
    };

    // 카드 상태 초기화
    const reset = () => {
        cards.value = [];
    };

    return {
        cards,
        userId,
        cardCount,
        setCards,
        reset,
    };
});