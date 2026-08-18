<template>
  <div class="profile-page">
    <PageHeader title="프로필" />
    <ProfileHeader v-if="profile" :profile="profile">
      <FriendButton :user-id="userId" :member-user-id="memberUserId" />
    </ProfileHeader>

    <FeedSection type="member" :member-user-id="memberUserId" />
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import ProfileHeader from '@/components/profile/ProfileHeader.vue';
import FeedSection from '../feed/components/FeedSection.vue';
import FriendButton from '@/components/common/FriendButton.vue';
import { useProfileStore } from '@/stores/profile';
import { useAuthStore } from '@/stores/auth.js';
const authStore = useAuthStore();
const userId = authStore.userId;

const profileStore = useProfileStore();
const route = useRoute();
const memberUserId = Number(route.params.userId);

const profile = ref(null);

const loadProfile = async () => {
  profile.value = null;
  profile.value = await profileStore.getProfile(memberUserId);
};

onMounted(loadProfile);
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  background: var(--color-bg-screen);
  min-height: 100vh;
  box-sizing: border-box;
  padding: 16px 16px 20px 16px;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
.friend-page {
  padding: 20px;
}

/* 프로필 */

.profile-header {
  display: flex;
  align-items: center;

  gap: 20px;

  padding-bottom: 20px;

  border-bottom: 1px solid #eee;
}

.profile-image {
  width: 80px;
  height: 80px;

  border-radius: 50%;

  object-fit: cover;
}

.profile-info {
  display: flex;

  flex-direction: column;

  gap: 10px;
}

.profile-info h2 {
  margin: 0;

  font-size: 24px;
}

.friend-btn {
  width: 120px;

  height: 36px;

  border-radius: 18px;

  border: none;

  background: #3182f6;

  color: white;

  cursor: pointer;
}

.friend-btn.complete {
  background: #ddd;

  color: #555;
}

/* 피드 */

.feed-section {
  margin-top: 25px;
}

.feed-section h3 {
  font-size: 18px;

  margin-bottom: 15px;
}
</style>
