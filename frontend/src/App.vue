<template>
  <DefaultLayout :show-bottom-nav="showBottomNav">
    <RouterView />
    <!-- 전역 공통 팝업 컴포넌트 -->
    <CommonModal
      :is-open="modalStore.isOpen"
      :title="modalStore.title"
      :message="modalStore.message"
      :confirm-text="modalStore.confirmText"
      :show-cancel="modalStore.showCancel"
      @confirm="modalStore.handleConfirm"
      @cancel="modalStore.handleCancel"
    />
  </DefaultLayout>
</template>

<script setup>
import { RouterView, useRoute } from 'vue-router';
import DefaultLayout from './components/layouts/DefaultLayout.vue';
import CommonModal from './components/common/CommonModal.vue';
import { computed } from 'vue';
import { useModalStore } from './stores/userModalStore.js';
const modalStore = useModalStore();

const route = useRoute();

const showBottomNav = computed(() => {
  return route.meta.showBottomNav === true;
});
</script>

<style scoped></style>
