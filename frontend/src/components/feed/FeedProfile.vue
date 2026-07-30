<template>
  <div>
    <!-- 더보기 -->
    <div v-if="feed.userId === userId" class="more-btn" @click="$emit('menu')">
      <i class="fa-solid fa-ellipsis"></i>
    </div>

    <!-- 작성자 -->
    <div class="profile">
      <img
        :src="`/images/profile/${feed.sender.profileImageName}`"
        class="profile-img"
      />

      <div>
        <strong class="nickname">{{ feed.sender.nickname }}</strong>

        <div class="date">
          {{ formatDate(feed.createdAt) }}

          <span class="text_dot">·</span>

          <span>
            <i :class="visibilityIcon"></i>
            {{ visibilityText }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

defineEmits(['menu']);

const props = defineProps({
  feed: {
    type: Object,
    required: true,
  },
  userId: {
    type: Number,
    required: true,
  },
});

const visibilityMap = {
  PUBLIC: {
    icon: 'fa-solid fa-earth-americas',
    text: '공개',
  },
  FRIEND: {
    icon: 'fa-solid fa-user-group',
    text: '친구',
  },
  PRIVATE: {
    icon: 'fa-solid fa-lock',
    text: '비공개',
  },
};

const visibilityIcon = computed(() => {
  return visibilityMap[props.feed.visibility]?.icon ?? '';
});

const visibilityText = computed(() => {
  return visibilityMap[props.feed.visibility]?.text ?? '';
});

const formatDate = (time) => {
  const now = new Date();
  const date = new Date(time);

  const diff = now - date;

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);

  if (seconds < 60) return '방금 전';
  if (minutes < 60) return `${minutes}분 전`;
  if (hours < 24) return `${hours}시간 전`;

  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
};
</script>

<style scoped>
.nickname {
  font-size: 18px;
}

.profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-img {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  object-fit: cover;
}

.date {
  font-size: 12px;
  color: #999;
}

.text_dot {
  margin: 0 1px;
}

.more-btn {
  position: absolute;
  top: 15px;
  right: 20px;

  font-size: 20px;
  color: #777;

  cursor: pointer;
  z-index: 10;
}
</style>
