<template>
  <div class="request-list">
    <SentRequestCard
      v-for="request in requests"
      :key="request.requestId"
      :request-id="request.requestId"
      :profile-image-name="request.sender.profileImageName"
      :nickname="request.sender.nickname"
      :receiverId="request.receiverId"
      @cancel="cancelRequest"
      @profile-click="goProfile"
    />
    <div v-if="requests.length === 0" class="empty">
      보낸 친구 요청이 없습니다.
    </div>
  </div>
</template>

<script setup>
import SentRequestCard from './SentRequestCard.vue';

defineProps({
  requests: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['cancel', 'profile-click']);

const cancelRequest = (requestId) => {
  emit('cancel', requestId);
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
