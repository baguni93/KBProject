<template>
  <div class="step-content-wrap">
    <RemitResultStep
      remit-type="ACCOUNT"
      :remit-amount="remittanceStore.remitAmount"
      :receiver-name="remittanceStore.accountForm.receiverName || '수취인'"
      :bank-name="remittanceStore.getBankName(remittanceStore.accountForm.bankCode)"
      :account-number="remittanceStore.accountForm.accountNumber"
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
  router.push('/remittance/account');
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
