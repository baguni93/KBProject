<template>
  <div class="my-wallet-wrapper">
    <WalletSection :userId="userId" />
  </div>
</template>

<script setup>
import WalletSection from '@/components/wallet/WalletSection.vue';
import { useUserStore } from '@/stores/user';
import { computed } from 'vue';

const userStore = useUserStore();
const userId = computed(() => userStore.userId || 1);
</script>

<style scoped>
.my-wallet-wrapper {
  padding: 0 16px;
}
</style>
