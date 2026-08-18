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
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js';
import { useModalStore } from '@/stores/userModalStore.js';
import { computed, onMounted } from 'vue';

const modalStroe = useModalStore();
const authStore = useAuthStore();
const userId = authStore.userId;
const route = useRoute();
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

const acceptRequest = async (id) => {
  const res = await modalStroe.showConfirm('친구 요청을 수락하시겠습니까?');

  if (!res) {
    return;
  }

  await friendStore.accept(userId, id);

  modalStroe.showSuccess('친구 요청 수락 완료');
};

const rejectRequest = async (id) => {
  const res = await modalStroe.showConfirm('친구 요청을 거절하시겠습니까?');

  if (!res) {
    return;
  }

  await friendStore.reject(userId, id);

  modalStroe.showAlert('친구 요청 거절 완료');
};

const cancelRequest = async (id) => {
  const res = await modalStroe.showConfirm('친구 요청을 취소하시겠습니까?');

  if (!res) {
    return;
  }

  await friendStore.cancel(userId, id);

  modalStroe.showAlert('친구 요청 취소 완료');
};

const goProfile = (targetId) => {
  if (targetId === userId) {
    router.push('/mypage');
  } else {
    router.push(`/member/${targetId}`);
  }
};

onMounted(async () => {
  await friendStore.getRequestList(userId);
  const requestId = Number(route.query.requestId);

  console.log('requestId:', requestId, typeof requestId);
  console.log('requestList:', friendStore.requestList);

  if (!requestId) {
    return;
  }

  const request = friendStore.requestList.find(
    (item) => Number(item.requestId) === requestId,
  );

  console.log('찾은 request:', request);

  if (!request) {
    await modalStroe.showAlert('이미 처리된 친구 요청입니다.', '알림');
    router.replace('/friends?tab=request');
  }
});
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
