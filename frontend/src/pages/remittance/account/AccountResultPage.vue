<template>
  <div class="step-content-wrap">
    <RemitResultStep
      remit-type="ACCOUNT"
      :remit-amount="displayAmount"
      :receiver-name="displayReceiver"
      :bank-name="displayBank"
      :account-number="displayAccount"
      :format-currency="formatCurrency"
      @reset-all="resetAndGoHome"
    />
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useRemittanceStore } from "@/stores/remittance";
import RemitResultStep from "@/components/remittance/RemitResultStep.vue";

const router = useRouter();
const route = useRoute();
const remittanceStore = useRemittanceStore();

const displayAmount = computed(() => {
  if (remittanceStore.remitAmount) return Number(remittanceStore.remitAmount);
  if (route.query.amount) return Number(route.query.amount);
  return 10000;
});

const displayReceiver = computed(() => {
  return remittanceStore.accountForm?.receiverName || route.query.receiverName || "테스트회원2";
});

const displayBank = computed(() => {
  const bCode = remittanceStore.accountForm?.bankCode;
  if (bCode) return remittanceStore.getBankName(bCode);
  return route.query.bankName || "신한은행";
});

const displayAccount = computed(() => {
  return remittanceStore.accountForm?.accountNumber || route.query.accountNumber || "110-222-111111";
});

const formatCurrency = (val) => {
  if (val === undefined || val === null) return "0";
  return Number(val).toLocaleString("ko-KR");
};

const resetAndGoHome = () => {
  remittanceStore.resetAll();
  router.push('/wallet');
};
</script>

<style scoped>
.step-content-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  position: relative;
  overflow: hidden;
}
</style>
