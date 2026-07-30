<template>
  <div class="my-page">
    <MyTab v-model="currentTab" />

    <FeedTab v-if="currentTab === 'feed'" />
    <WalletTab v-else />
  </div>
</template>

<script setup>
import { ref } from 'vue';

import MyTab from '@/components/my/MyTab.vue';
import FeedTab from '@/components/my/FeedTab.vue';
import WalletTab from '@/components/my/WalletTab.vue';

const currentTab = ref('feed');
</script>

<style scoped>
.my-page {
  display: flex;
  flex-direction: column;
}
</style>
