<template>
  <div class="componet">
    <FriendCard
      v-for="user in searchResult"
      :key="user.userId"
      :profile-image-name="user.profileImageName"
      :nickname="user.nickname"
      :user-id="user.userId"
      @profile-click="goProfile"
    >
      <span class="desc" v-if="user.friend === true"> 친구</span>
    </FriendCard>
  </div>
</template>

<script setup>
import FriendCard from '../friend/FriendCard.vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js';
const authStore = useAuthStore();
const userId = authStore.userId;

const router = useRouter();

const props = defineProps({
  searchResult: {
    type: Array,
    required: true,
  },
});

const goProfile = (searchUserId) => {
  if (searchUserId === userId) {
    router.push('/mypage');
  } else {
    router.push(`/member/${searchUserId}`);
  }
};
</script>

<style scoped>
.componet {
  margin-top: 15px;
}
.desc {
  font-size: 12px;
}
</style>
