<template>
  <div class="request-section">
    <div class="section-title">
      <span>받은 친구 요청</span>
    </div>

    <FriendRequestList
      :requests="received"
      @accept="acceptRequest"
      @reject="rejectRequest"
      @profile-click="goProfile"
    />

    <div class="section-title">
      <span>보낸 친구 요청</span>
    </div>

    <SentRequestList
      :requests="sent"
      @cancel="cancelRequest"
      @profile-click="goProfile"
    />
  </div>
</template>

<script setup>
import FriendRequestList from './FriendRequestList.vue';
import SentRequestList from './SentRequestList.vue';
import { useFriendStore } from '@/stores/friend';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js';
const authStore = useAuthStore();
const userId = authStore.userId;

const router = useRouter();
const friendStore = useFriendStore();

defineProps({
  received: {
    type: Array,
    default: () => [],
  },

  sent: {
    type: Array,
    default: () => [],
  },
});

const acceptRequest = (id) => {
  friendStore.accept(userId, id);
};

const rejectRequest = (id) => {
  friendStore.reject(userId, id);
};

const cancelRequest = (id) => {
  friendStore.cancel(userId, id);
};

const goProfile = (targetId) => {
  if (targetId === userId) {
    router.push('/mypage');
  } else {
    router.push(`/member/${targetId}`);
  }
};
</script>

<style scoped>
.request-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-title {
  display: flex;
  align-items: center;

  margin-top: 20px;
  margin-bottom: 12px;
  padding-bottom: 8px;

  border-bottom: 1px solid #eceff3;
}

.section-title:first-child {
  margin-top: 0;
}

.section-title span {
  font-size: 15px;
  font-weight: 600;
  color: #2d3748;
  letter-spacing: -0.2px;
}
</style>
