<template>
  <div class="step-content-wrap">
    <DutchCreateSummaryStep
      v-model:dutch-room-title="remittanceStore.dutchRoomTitle"
      v-model:remit-memo="remittanceStore.remitMemo"
      v-model:selected-category-id="remittanceStore.selectedCategoryId"
      v-model:remit-visibility="remittanceStore.remitVisibility"
      :remit-amount="remittanceStore.remitAmount"
      :selected-dutch-friends="remittanceStore.selectedDutchFriends"
      :get-friend-name="remittanceStore.getFriendName"
      :category-list="remittanceStore.categoryList"
      :my-profile-name="myProfileName"
      :format-currency="remittanceStore.formatCurrency"
      @submit="submitRemittance"
    />
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRemittanceStore } from "@/stores/remittance";
import { useAuthStore } from "@/stores/auth";
import { useProfileStore } from "@/stores/profile";
import DutchCreateSummaryStep from "@/components/remittance/DutchCreateSummaryStep.vue";

const remittanceStore = useRemittanceStore();
const authStore = useAuthStore();
const profileStore = useProfileStore();

const myProfileName = computed(() => {
  return remittanceStore.myProfileName || profileStore.profile?.nickname || authStore.userName || "내 프로필";
});

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
