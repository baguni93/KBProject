<template>
  <div class="step-content-wrap">
    <DutchCreateSummaryStep
      v-model:dutch-room-title="remittanceStore.dutchRoomTitle"
      v-model:remit-memo="remittanceStore.remitMemo"
      :remit-amount="remittanceStore.remitAmount"
      :selected-dutch-friends="remittanceStore.selectedDutchFriends"
      :get-friend-name="remittanceStore.getFriendName"
      :image-preview-url="remittanceStore.imagePreviewUrl"
      :format-currency="remittanceStore.formatCurrency"
      @file-change="remittanceStore.handleFileChange"
      @remove-file="remittanceStore.removeSelectedFile"
      @submit="submitRemittance"
    />
  </div>
</template>

<script setup>
import { useRemittanceStore } from "@/stores/remittance";
import DutchCreateSummaryStep from "@/components/remittance/DutchCreateSummaryStep.vue";

const remittanceStore = useRemittanceStore();

const submitRemittance = () => {
  remittanceStore.showConfirmModal = true;
};
</script>

<style scoped>
.step-content-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
}
</style>
