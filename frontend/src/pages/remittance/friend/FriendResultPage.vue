<template>
  <div class="step-content-wrap">
    <RemitResultStep
      remit-type="FRIEND"
      :remit-amount="remittanceStore.remitAmount"
      :receiver-name="remittanceStore.selectedFriendObj?.name || remittanceStore.getFriendName(remittanceStore.selectedFriendId)"
      :remit-memo="remittanceStore.remitMemo"
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
  router.push('/remittance/friend');
};
</script>

<style scoped>
.step-content-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}
</style>
