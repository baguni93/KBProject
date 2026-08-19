<template>
  <div class="step-content-wrap">
    <RemitAmountStep
      remit-type="FRIEND"
      :selected-friend-obj="remittanceStore.selectedFriendObj"
      :my-balance="remittanceStore.myBalance"
      :my-account-name="remittanceStore.primaryAccountName || 'KB국민 주거래통장'"
      :total-available-balance="remittanceStore.totalAvailableBalance"
      :is-exceed-balance="remittanceStore.isExceedBalance"
      :is-auto-charge-needed="remittanceStore.isAutoChargeNeeded"
      :auto-charge-amount="remittanceStore.autoChargeAmount"
      :remit-amount="remittanceStore.remitAmount"
      :remit-amount-display="remittanceStore.remitAmount > 0 ? remittanceStore.formatCurrency(remittanceStore.remitAmount) : ''"
      :get-bank-logo-file-name="remittanceStore.getBankLogoFileName"
      :get-bank-name="remittanceStore.getBankName"
      :get-friend-name="remittanceStore.getFriendName"
      :format-currency="remittanceStore.formatCurrency"
      @on-amount-input="handleAmountInput"
      @add-amount="addAmount"
      @set-all-balance="setAllBalance"
    />

    <!-- 하단 고정 버튼 (상단 카드 좌우 여백과 100% 동일하게 확장) -->
    <div class="bottom-btn-area single">
      <button
        type="button"
        class="bottom-btn primary-button text-17-bold"
        :disabled="remittanceStore.remitAmount <= 0 || remittanceStore.isExceedBalance"
        @click="proceedToFeed"
      >
        다음
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useRemittanceStore } from "@/stores/remittance";
import RemitAmountStep from "@/components/remittance/RemitAmountStep.vue";

const router = useRouter();
const remittanceStore = useRemittanceStore();

const handleAmountInput = (e) => {
  const raw = e.target.value.replace(/[^0-9]/g, "");
  remittanceStore.remitAmount = raw ? parseInt(raw, 10) : 0;
};

const addAmount = (val) => {
  remittanceStore.remitAmount += val;
};

const setAllBalance = () => {
  remittanceStore.remitAmount = remittanceStore.myBalance;
};

const proceedToFeed = () => {
  if (remittanceStore.isExceedBalance) {
    alert("출금 가능 잔액을 초과했습니다.");
    return;
  }
  if (remittanceStore.remitAmount > 0) {
    router.push('/remittance/friend/feed');
  }
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
  padding: 16px 0 32px;
  background: #ffffff;
  z-index: 100;
  box-sizing: border-box;
}

.bottom-btn-area.single .bottom-btn {
  width: 100%;
  height: 52px;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 700;
  border: none;
  background-color: #ffbc2e;
  color: #111111;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bottom-btn-area.single .bottom-btn:disabled {
  background-color: #e2e8f0;
  color: #a0aec0;
  cursor: not-allowed;
}
</style>
