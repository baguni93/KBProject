<template>
  <div class="step-content-wrap">
    <RemitResultStep
      :remit-type="remittanceStore.remitType === 'DUTCH_PAY' ? 'DUTCH_PAY' : 'DUTCH_CREATE'"
      :remit-amount="remittanceStore.remitAmount"
      :selected-dutch-friends="remittanceStore.selectedDutchFriends"
      :dutch-room-title="remittanceStore.dutchRoomTitle"
      :receiver-name="remittanceStore.accountForm.receiverName || remittanceStore.selectedFriendObj?.nickname || '친구'"
      :remit-memo="remittanceStore.remitMemo"
      :get-friend-name="remittanceStore.getFriendName"
      :format-currency="remittanceStore.formatCurrency"
      @reset-all="resetAndGoHome"
    />
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useRemittanceStore } from "@/stores/remittance";
import RemitResultStep from "@/components/remittance/RemitResultStep.vue";

const router = useRouter();
const remittanceStore = useRemittanceStore();

const resetAndGoHome = () => {
  remittanceStore.resetAll();
};
</script>

<style scoped>
.step-content-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
}
</style>
