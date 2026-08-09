<template>
  <div>
    <div class="profile">
      <router-link :to="memberDetailPath">
        <img
          :src="`/api/feeds/profile/${profileImageName}`"
          class="profile-img"
        />
      </router-link>

      <div>
        <router-link :to="memberDetailPath" class="nickname-link">
          <strong class="nickname">{{ nickname }}</strong>
        </router-link>

        <div class="date">
          {{ formatRelativeDate(createdAt) }}

          <template v-if="showVisibility">
            ·
            <VisibilityBadge :visibility="visibility" />
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import VisibilityBadge from '../common/VisibilityBadge.vue';
import { formatRelativeDate } from '@/util/data';
import { useAuthStore } from '@/stores/auth.js';
const authStore = useAuthStore();
const myUserId = authStore.userId;

const props = defineProps({
  userId: {
    type: Number,
    required: true,
  },
  profileImageName: {
    type: [String, Object],
    required: false,
    default: null,
  },
  nickname: {
    type: String,
    required: true,
  },
  createdAt: {
    type: [Number, String],
    required: true,
  },
  visibility: {
    type: String,
    default: 'PUBLIC',
  },
  showVisibility: {
    type: Boolean,
    default: true,
  },
});

const memberDetailPath = computed(() => {
  if (props.userId === myUserId) {
    return '/mypage';
  }

  return `/member/${props.userId}`;
});
</script>

<style scoped>
.profile {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
  margin-top: -5px;
}

.profile-img {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  object-fit: cover;
}

.nickname-link {
  color: inherit;
  text-decoration: none;
}

.nickname {
  font-size: 15px;
}

.date {
  display: flex;
  align-items: center;
  gap: 4px;

  font-size: 12px;
  color: #999;
}
</style>
