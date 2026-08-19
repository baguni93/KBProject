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

    <!-- 하단 고정 버튼 (팀 공통 규격 100% 일치) -->
    <div class="bottom-btn-area single">
      <button
        type="button"
        class="bottom-btn primary-button text-17-bold"
        :disabled="!canProceed"
        @click="proceedToAmount"
      >
        다음
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useRemittanceStore } from "@/stores/remittance";
import { useModalStore } from "@/stores/userModalStore";
import AccountRemitSection from "@/components/remittance/AccountRemitSection.vue";
import remittanceApi from "@/api/remittanceApi";

const router = useRouter();
const remittanceStore = useRemittanceStore();
const modalStore = useModalStore();

const canProceed = computed(() => {
  const clean = (remittanceStore.accountForm.accountNumber || "").replace(/[^0-9]/g, "");
  return clean.length >= 7 && !!remittanceStore.accountForm.bankCode;
});

const selectRecentAccount = (acc) => {
  remittanceStore.accountForm.bankCode = acc.bankCode || "004";
  remittanceStore.accountForm.accountNumber = acc.accountNumber || "";
  remittanceStore.accountForm.receiverName = acc.receiverName || acc.name || "수취인";
  proceedToAmount();
};

const proceedToAmount = async () => {
  if (!remittanceStore.accountForm.accountNumber || !remittanceStore.accountForm.bankCode) {
    modalStore.showAlert("은행과 계좌번호를 입력해주세요.", "계좌 입력 안내");
    return;
  }

  try {
    const res = await remittanceApi.verifyBankAccount(
      remittanceStore.accountForm.bankCode,
      remittanceStore.accountForm.accountNumber
    );
    if (res && res.ownerName) {
      remittanceStore.accountForm.receiverName = res.ownerName;
    }
  } catch (e) {
    console.log("예금주 실명 조회 폴백:", e);
  }

  router.push('/remittance/account/amount');
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.step-content-wrap {
  display: flex;
  flex-direction: column;
  position: relative;
  min-height: 100%;
  padding-bottom: 90px;
  box-sizing: border-box;
}

.bottom-btn-area.single {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  background: #ffffff;
  z-index: 10;
}

.bottom-btn {
  width: 100%;
  height: 52px;
  border-radius: 14px;
  background-color: #ffbc2e;
  color: #1e293b;
  border: none;
  font-size: 17px;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bottom-btn:disabled {
  background-color: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}
</style>
