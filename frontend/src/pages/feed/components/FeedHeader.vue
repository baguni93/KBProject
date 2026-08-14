<template>
  <div class="feed-header">
    <div class="avatar-container" @click.stop="handleMemberPage">
      <img
        v-if="profileImage"
        :src="`/api/feeds/profile/${profileImage}`"
        :alt="userName"
        class="profile-image"
      />

      <i v-else class="fa-solid fa-user"></i>
    </div>

    <div class="feed-info">
      <p class="feed-user-text">
        <strong>{{ userName }}</strong>
      </p>

      <span class="feed-time">
        {{ formatRelativeDate(time) }}

        <i v-if="visibilityIcon" :class="visibilityIcon"></i>

        {{ visibilityText }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { formatRelativeDate } from '@/util/data';
import config from '@/config/feed';
import { useRouter } from 'vue-router';
const router = useRouter();

import { useAuthStore } from '@/stores/auth.js';
const authStore = useAuthStore();
const myUserId = authStore.userId;

const props = defineProps({
  userName: {
    type: String,
    default: '',
  },

  userId: {
    type: [Number, String],
    default: null,
  },

  time: {
    type: String,
    default: '',
  },

  profileImage: {
    type: String,
    default: '',
  },

  visibility: {
    type: String,
    default: '',
  },
});

const visibilityMap = config.visibilityMap;

const visibilityIcon = computed(() => {
  return visibilityMap[props.visibility]?.icon ?? '';
});

const visibilityText = computed(() => {
  return visibilityMap[props.visibility]?.text ?? '';
});

const handleMemberPage = () => {
  if (props.userId === myUserId) {
    router.push('/mypage');
    return;
  }
  router.push(`/member/${props.userId}`);
};
</script>

<style scoped>
.feed-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.avatar-container {
  width: 36px;
  height: 36px;

  flex-shrink: 0;

  border-radius: 50%;
  overflow: hidden;

  background: #cbd5e1;

  display: flex;
  align-items: center;
  justify-content: center;

  color: #94a3b8;

  cursor: pointer;

  position: relative;
  z-index: 20;
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;

  display: block;
}

.feed-info {
  min-width: 0;
  flex: 1;
}

.feed-user-text {
  margin: 0;

  font-size: 14px;
  color: #111827;

  line-height: 1.4;
}

.feed-user-text strong {
  font-weight: 700;
}

.feed-time {
  display: flex;
  align-items: center;

  gap: 5px;

  margin-top: 3px;

  font-size: 10px;
  color: #94a3b8;
}
</style>
