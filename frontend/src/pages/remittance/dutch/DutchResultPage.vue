<template>
  <div class="step-content-wrap">
    <RemitResultStep
      remit-type="DUTCH"
      :remit-amount="remittanceStore.remitAmount"
      :selected-dutch-friends="remittanceStore.selectedDutchFriends"
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
