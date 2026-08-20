<template>
  <FriendCard
    :profile-image-name="profileImageName"
    :nickname="nickname"
    @profile-click="goProfile"
  >
    <!-- 상태 -->
    <div class="status">친구 요청</div>

    <!-- 버튼 -->
    <template #action>
      <div class="actions">
        <button class="content-btn small primary" @click="acceptRequest">
          수락
        </button>

        <button class="content-btn small" @click="rejectRequest">거절</button>
      </div>
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
  requesterId: {
    type: Number,
    default: 0,
  },

  profileImageName: {
    type: String,
    default: 'unknown.png',
  },

  nickname: {
    type: String,
    default: '사용자',
  },
});

const emit = defineEmits(['accept', 'reject', 'profile-click']);

const acceptRequest = () => {
  emit('accept', props.requestId);
};

const rejectRequest = () => {
  emit('reject', props.requestId);
};

const goProfile = () => {
  emit('profile-click', props.requesterId);
};
</script>

<style scoped>
.status {
  margin-top: 4px;

  font-size: 13px;

  color: #999;
}

.actions {
  display: flex;

  gap: 8px;
}

.accept {
  background: #ffbc2e;

  color: white;
}

.reject {
  background: #eee;

  color: #555;
}

.actions button:active {
  transform: scale(0.95);
}
</style>
