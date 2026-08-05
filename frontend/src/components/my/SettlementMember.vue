<template>
  <div class="member">
    <router-link :to="memberDetailPath">
      <img
        :src="`/api/feeds/profile/${profileImageName}`"
        class="profile-img"
      />
    </router-link>

    <div class="info">
      <router-link :to="memberDetailPath" class="nickname-link">
        <strong class="nickname">
          {{ nickname }}
        </strong>
      </router-link>

      <div class="status" :class="{ complete: status === 'COMPLETE' }">
        {{ statusText }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
//test user Id
import { useUserStore } from '@/stores/user';
const userStore = useUserStore();
const myUserId = userStore.userId;

const props = defineProps({
  userId: {
    type: Number,
    required: true,
  },
  profileImageName: {
    type: String,
    required: true,
  },
  nickname: {
    type: String,
    required: true,
  },
  status: {
    type: String,
    required: true,
  },
});

const memberDetailPath = computed(() => {
  if (props.userId === myUserId) {
    return '/mypage';
  }

  return `/member/${props.userId}`;
});

const statusText = computed(() => {
  return props.status === 'COMPLETE' ? '완료' : '미납';
});
</script>

<style scoped>
.member {
  display: flex;
  align-items: center;
  gap: 10px;

  /* ===== 레이아웃 체크 (삭제 예정) ===== */
  /* background: rgba(114, 46, 209, 0.15); */
  /* border: 2px solid #722ed1; */
  /* ===== 레이아웃 체크 끝 ===== */
}

.profile-img {
  width: 40px;
  height: 40px;

  border-radius: 50%;
  object-fit: cover;
}

.nickname-link {
  color: inherit;
  text-decoration: none;
}

.nickname {
  font-size: 14px;
}

.status {
  font-size: 12px;
  color: #999;
}

.status.complete {
  color: #22c55e;
  font-weight: 700;
}
</style>
