<template>
  <div class="step-content-wrap">
    <FriendRemitSection
      v-model:keyword="remittanceStore.friendSearchKeyword"
      :recent-friends="remittanceStore.recentFriends"
      :friends="remittanceStore.filteredFriends"
      :selected-friend-id="remittanceStore.selectedFriendId"
      :get-profile-image-url="remittanceStore.getProfileImageUrl"
      @select-friend="selectFriendAndProceed"
    />
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useRemittanceStore } from "@/stores/remittance";
import FriendRemitSection from "@/components/remittance/FriendRemitSection.vue";

const router = useRouter();
const remittanceStore = useRemittanceStore();

const selectFriendAndProceed = (fId) => {
  remittanceStore.selectedFriendId = fId;
  const fObj = remittanceStore.friendList.find((f) => (f.id || f.friendId) === fId);
  remittanceStore.selectedFriendObj = fObj;
  remittanceStore.accountForm.receiverName = fObj ? (fObj.name || fObj.nickname || fObj.username) : "친구";
  router.push('/remittance/friend/amount');
};
</script>

<style scoped>
.step-content-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
}
</style>
