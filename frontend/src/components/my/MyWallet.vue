<template>
  <div class="my-wallet-wrapper">
    <WalletSection :userId="userId" @open-receipt="onOpenReceipt" />
  </div>

  <!-- 영수증 상세 모달 -->
  <ReceiptDetailModal
    :show="showReceiptModal"
    :transactionId="selectedTransactionId"
    @close="showReceiptModal = false"
  />
</template>

<script setup>
import { ref, computed } from 'vue';
import WalletSection from '@/components/wallet/WalletSection.vue';
import ReceiptDetailModal from '@/components/transaction/ReceiptDetailModal.vue';
// import { useUserStore } from '@/stores/user';

// const userStore = useUserStore();
// const userId = computed(() => userStore.userId || 1);

const showReceiptModal = ref(false);
const selectedTransactionId = ref(null);

const onOpenReceipt = (transactionId) => {
  selectedTransactionId.value = transactionId;
  showReceiptModal.value = true;
};
</script>

<style scoped>
.my-wallet-wrapper {
  padding: 0 16px;
}
</style>
