<template>
  <div class="step-content-wrap">
    <AccountRemitSection
      v-model:account-number="remittanceStore.accountForm.accountNumber"
      v-model:bank-code="remittanceStore.accountForm.bankCode"
      :bank-options="remittanceStore.bankOptions"
      :recent-accounts="remittanceStore.recentAccounts"
      :get-bank-logo-file-name="remittanceStore.getBankLogoFileName"
      :get-bank-name="remittanceStore.getBankName"
      :format-currency="remittanceStore.formatCurrency"
      @select-recent="selectRecentAccount"
      @proceed="proceedToAmount"
    />
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useRemittanceStore } from "@/stores/remittance";
import { useModalStore } from "@/stores/userModalStore";
import AccountRemitSection from "@/components/remittance/AccountRemitSection.vue";

const router = useRouter();
const remittanceStore = useRemittanceStore();
const modalStore = useModalStore();

const selectRecentAccount = (acc) => {
  remittanceStore.accountForm.bankCode = acc.bankCode || "004";
  remittanceStore.accountForm.accountNumber = acc.accountNumber || "";
  remittanceStore.accountForm.receiverName = acc.receiverName || acc.name || "수취인";
  proceedToAmount();
};

const proceedToAmount = () => {
  if (!remittanceStore.accountForm.accountNumber || !remittanceStore.accountForm.bankCode) {
    modalStore.showAlert("은행과 계좌번호를 입력해주세요.", "계좌 입력 안내");
    return;
  }
  router.push('/remittance/account/amount');
};
</script>

<style scoped>
.step-content-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
}
</style>
