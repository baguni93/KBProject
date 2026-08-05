<template>
  <DefaultLayout :show-bottom-nav="showBottomNav">
    <RouterView />
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
  return route.meta.showBottomNav === true;
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
  try {
    client.activate();
  } catch (e) {
    console.log('WS activation bypass');
  }
});

onUnmounted(() => {
  try {
    client.deactivate();
  } catch (e) {
    console.log('WS deactivation bypass');
  }
});
</script>

<style scoped>
</style>
