<template>
  <div class="step-content-wrap">
    <RemitAmountStep
      remit-type="DUTCH"
      :selected-friend-obj="null"
      :account-form="{}"
      :remit-amount="remittanceStore.remitAmount"
      :remit-amount-display="remittanceStore.remitAmount > 0 ? remittanceStore.formatCurrency(remittanceStore.remitAmount) : ''"
      :my-balance="remittanceStore.myBalance"
      :category-list="remittanceStore.categoryList"
      :selected-category-id="remittanceStore.selectedCategoryId"
      :selected-dutch-friends="remittanceStore.selectedDutchFriends"
      :get-friend-name="remittanceStore.getFriendName"
      :get-profile-image-url="remittanceStore.getProfileImageUrl"
      :get-friend-obj="remittanceStore.getFriendObj"
      :format-currency="remittanceStore.formatCurrency"
      @on-amount-input="handleAmountInput"
      @add-amount="addAmount"
      @set-all-balance="setAllBalance"
      @open-tx-select="openTxSelect"
      @update:selected-category-id="(id) => remittanceStore.selectedCategoryId = id"
    />

    <!-- 하단 고정 버튼 (상단 카드 좌우 여백과 100% 동일하게 확장) -->
    <div class="bottom-btn-area single">
      <button
        type="button"
        class="bottom-btn primary-button text-17-bold"
        :disabled="remittanceStore.remitAmount <= 0"
        @click="proceedToSummary"
      >
        다음
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useRemittanceStore } from "@/stores/remittance";
import { useAuthStore } from "@/stores/auth";
import { useProfileStore } from "@/stores/profile";
import { useModalStore } from "@/stores/userModalStore";
import RemitAmountStep from "@/components/remittance/RemitAmountStep.vue";

const router = useRouter();
const remittanceStore = useRemittanceStore();
const authStore = useAuthStore();
const profileStore = useProfileStore();
const modalStore = useModalStore();

const handleAmountInput = (e) => {
  const raw = e.target.value.replace(/[^0-9]/g, "");
  remittanceStore.remitAmount = raw ? parseInt(raw, 10) : 0;
};

const addAmount = (amt) => {
  remittanceStore.remitAmount = (remittanceStore.remitAmount || 0) + amt;
};

const setAllBalance = () => {
  remittanceStore.remitAmount = remittanceStore.myBalance;
};

const openTxSelect = async () => {
  await remittanceStore.loadUserTransactions();
  router.push('/remittance/dutch/transactions');
};

const proceedToSummary = () => {
  if (remittanceStore.remitAmount <= 0) {
    modalStore.showAlert("정산할 금액을 입력해주세요.", "금액 입력 안내");
    return;
  }

  // 결제내역을 선택하지 않고 직접 금액을 입력했거나 제목이 없는 경우 자동 생성
  if (!remittanceStore.selectedTxIds || remittanceStore.selectedTxIds.length === 0 || !remittanceStore.dutchRoomTitle) {
    const myNickname = profileStore.profile?.nickname || authStore.userName || "노랑지갑";
    const friendCount = remittanceStore.selectedDutchFriends ? remittanceStore.selectedDutchFriends.length : 0;
    if (friendCount > 0) {
      remittanceStore.dutchRoomTitle = `${myNickname} 외 ${friendCount}명`;
    } else {
      remittanceStore.dutchRoomTitle = myNickname;
    }
  }

  router.push('/remittance/dutch/summary');
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
