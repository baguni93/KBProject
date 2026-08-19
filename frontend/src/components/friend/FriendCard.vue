<template>
  <div class="friend-card">
    <!-- 프로필 -->

    <img
      class="profile"
      :src="`/api/feeds/profile/${profileImageName}`"
      alt="프로필"
      @click="goProfile"
    />

    <!-- 정보 -->
    <div class="info" @click="goProfile">
      <div class="nickname">
        {{ nickname }}
      </div>

      <slot />
    </div>

    <!-- 오른쪽 영역 -->
    <div class="action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  profileImageName: {
    type: String,
    default: 'default.png',
  },

  nickname: {
    type: String,
    default: '사용자',
  },

  userId: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(['profile-click']);

const goProfile = () => {
  emit('profile-click', props.userId);
};
</script>

<style scoped>
.friend-card {
  display: flex;

  align-items: center;

  gap: 12px;

  padding: 14px 16px;

  margin-bottom: 15px;

  background: white;

  border-radius: 14px;

  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.profile {
  width: 45px;

  height: 45px;

  border-radius: 50%;

  object-fit: cover;
}

.info {
  flex: 1;

  display: flex;
  flex-direction: column;
  justify-content: center;
}

.nickname {
  font-size: 15px;

  font-weight: 600;
}

.action {
  position: relative;
}
</style>
