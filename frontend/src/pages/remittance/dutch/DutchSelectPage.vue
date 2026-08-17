<template>
  <div class="step-content-wrap">
    <DutchRemitSection
      v-model:keyword="remittanceStore.friendSearchKeyword"
      :selected-friends="remittanceStore.selectedDutchFriends"
      :friends="remittanceStore.filteredFriends"
      :my-profile-image-url="remittanceStore.myProfileImageUrl"
      :get-profile-image-url="remittanceStore.getProfileImageUrl"
      :get-friend-obj="remittanceStore.getFriendObj"
      :get-friend-name="remittanceStore.getFriendName"
      @toggle-friend="toggleDutchFriend"
      @remove-friend="removeDutchFriend"
    />

    <!-- 하단 고정 버튼 (팀 공통 규격 100% 통일) -->
    <div class="bottom-btn-area single">
      <button
        type="button"
        class="bottom-btn primary-button text-17-bold"
        :disabled="remittanceStore.selectedDutchFriends.length === 0"
        @click="proceedToTransactions"
      >
        {{ remittanceStore.selectedDutchFriends.length > 0 ? `${remittanceStore.selectedDutchFriends.length}명 선택 완료` : "참여자 선택" }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { useRemittanceStore } from "@/stores/remittance";
import DutchRemitSection from "@/components/remittance/DutchRemitSection.vue";

const router = useRouter();
const remittanceStore = useRemittanceStore();

const toggleDutchFriend = (fId) => {
  const idx = remittanceStore.selectedDutchFriends.indexOf(fId);
  if (idx > -1) {
    remittanceStore.selectedDutchFriends.splice(idx, 1);
  } else {
    remittanceStore.selectedDutchFriends.push(fId);
  }
};

const removeDutchFriend = (fId) => {
  const idx = remittanceStore.selectedDutchFriends.indexOf(fId);
  if (idx > -1) {
    remittanceStore.selectedDutchFriends.splice(idx, 1);
  }
};

const proceedToTransactions = async () => {
  if (remittanceStore.selectedDutchFriends.length === 0) {
    alert("정산에 함께할 친구를 선택해주세요.");
    return;
  }
  router.push('/remittance/dutch/amount');
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
  padding: 16px 24px 32px;
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
