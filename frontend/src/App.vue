<template>
  <DefaultLayout :show-bottom-nav="showBottomNav">
    <!-- v-slot을 사용하여 현재 라우트의 컴포넌트를 직접 제어합니다 -->
    <RouterView v-slot="{ Component }">
      <!-- mode="out-in"으로 이전 페이지가 완전히 사라진 후 새 페이지가 나타나게 합니다 -->
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </RouterView>
  </DefaultLayout>
</template>

<script setup>
import { RouterView, useRoute } from 'vue-router';
import DefaultLayout from './components/layouts/DefaultLayout.vue';
import { onMounted, onUnmounted, computed } from 'vue';
import client from '@/websocket';
import { useNotificationStore } from '@/stores/notification';
import { useSettlementStore } from '@/stores/settlement';

const route = useRoute();

const showBottomNav = computed(() => {
  return route.meta.showBottomNav !== false;
});

const settlementStore = useSettlementStore();
const notificationStore = useNotificationStore();

client.onConnect = () => {
  console.log('웹소켓 연결 성공');

  client.subscribe('/user/queue/notifications', (message) => {
    const notification = JSON.parse(message.body);
    console.log(JSON.parse(message.body));
    notificationStore.addNotification(notification);
  });

  client.subscribe('/user/queue/settlements', (message) => {
    const settlement = JSON.parse(message.body);
    console.log(JSON.parse(message.body));
    settlementStore.updateSettlement(settlement);
  });
};

client.onDisconnect = () => {
  console.log('웹소켓 연결 종료');
};

onMounted(() => {
  client.activate();
});

onUnmounted(() => {
  client.deactivate();
});
</script>

<style scoped>
/* 부드러운 투명도 전환 애니메이션 효과 정의 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.08s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
