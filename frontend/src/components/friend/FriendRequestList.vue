<template>
  <div class="request-list">
    <FriendRequestCard
      v-for="request in requests"
      :key="request.requestId"
      :request-id="request.requestId"
      :profile-image-name="request.receiver.profileImageName"
      :nickname="request.receiver.nickname"
      :requesterId="request.requesterId"
      @accept="acceptRequest"
      @reject="rejectRequest"
      @profile-click="goProfile"
    />

    <EmptyList v-if="requests.length === 0" desc="받은 친구 요청이 없습니다." />
  </div>
</template>

<script setup>
import FriendRequestCard from './FriendRequestCard.vue';
import EmptyList from '@/components/common/EmptyList.vue';

const props = defineProps({
  requests: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['accept', 'reject', 'profile-click']);

const acceptRequest = (requestId) => {
  emit('accept', requestId);
};

const rejectRequest = (requestId) => {
  emit('reject', requestId);
};

const goProfile = (targetId) => {
  emit('profile-click', targetId);
};
</script>

<style scoped>
.request-list {
  padding-top: 10px;
}

.empty {
  text-align: center;

  color: #999;

  padding: 30px 0;

  font-size: 14px;
}
</style>
