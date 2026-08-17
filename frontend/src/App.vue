<template>
  <DefaultLayout :show-bottom-nav="showBottomNav">
    <RouterView />
  </DefaultLayout>
  <!-- 삼성페이 스타일 결제 대기중 모달 (하단 결제 버튼 롱프레스 독립 감지) -->
  <CardPaymentPendingModal />
</template>

<script setup>
import { RouterView, useRoute } from 'vue-router';
import DefaultLayout from './components/layouts/DefaultLayout.vue';
import CardPaymentPendingModal from '@/components/wallet/CardPaymentPendingModal.vue';
import { computed } from 'vue';

const route = useRoute();

const showBottomNav = computed(() => {
  return route.meta.showBottomNav === true;
});
</script>

<style scoped></style>
