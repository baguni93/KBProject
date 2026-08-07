<template>
  <div class="profile-page">
    <PageHeader title="프로필" />
    <ProfileHeader v-if="profile" :profile="profile">
      <FriendButton :user-id="userId" :member-user-id="memberUserId" />
    </ProfileHeader>

    <FeedSection
      type="member"
      :user-id="userId"
      :member-user-id="memberUserId"
    />
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import ProfileHeader from '@/components/profile/ProfileHeader.vue';
import FeedSection from '@/components/my/FeedSection.vue';
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
  width: 100%;
  padding: 20px;
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
