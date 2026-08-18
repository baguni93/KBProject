<template>
  <FriendCard
    :profile-image-name="profileImageName"
    :nickname="nickname"
    @profile-click="goProfile"
  >
    <div class="status">친구 요청 대기중</div>

    <template #action>
      <button class="content-btn small primary" @click="cancelRequest">
        요청 취소
      </button>
    </template>
  </FriendCard>
</template>

<script setup>
import FriendCard from './FriendCard.vue';

const props = defineProps({
  requestId: {
    type: Number,
    default: 0,
  },

  profileImageName: {
    type: String,
    default: 'default.png',
  },

  nickname: {
    type: String,
    default: '사용자',
  },
  receiverId: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(['cancel', 'profile-click']);

const cancelRequest = () => {
  emit('cancel', props.requestId);
};

const goProfile = () => {
  emit('profile-click', props.receiverId);
};
</script>

<style scoped>
.status {
  margin-top: 4px;

  font-size: 13px;

  color: #999;
}
</style>
