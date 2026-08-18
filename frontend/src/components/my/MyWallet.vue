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

  <!-- 카드 결제 대기중 모달 (하단 결제 버튼 롱 프레스 탐지) -->
  <CardPaymentPendingModal v-model="showPendingModal" />
</template>

<script setup>
import { ref, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import WalletSection from '@/components/wallet/WalletSection.vue';
import ReceiptDetailModal from '@/components/transaction/ReceiptDetailModal.vue';
import CardPaymentPendingModal from '@/components/wallet/CardPaymentPendingModal.vue';

const authStore = useAuthStore();
const userId = computed(() => authStore.userId || 1);

const showReceiptModal = ref(false);
const showPendingModal = ref(false);
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
