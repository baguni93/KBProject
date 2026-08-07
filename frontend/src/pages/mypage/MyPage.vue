<template>
  <div class="page">
    <div>
      <HeaderButtons />
      <div class="profile-section">
        <ProfileHeader v-if="profile" :profile="profile" />
        <FriendMenuButton
          :friend-count="
            friendStore.friendList ? friendStore.friendList.length : 0
          "
        />
      </div>
      <MyPageTab v-model="currentTab" />
      <div class="main">
        <div v-if="currentTab === 'feed'">
          <SettlementSection />
          <FeedSection type="my" :user-id="userId || 1" />
        </div>
        <div v-else-if="currentTab === 'wallet'">
          <MyWallet />
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import MyPageTab from '@/components/my/MyPageTab.vue';
import MyWallet from '@/components/my/MyWallet.vue';
import HeaderButtons from '@/components/common/HeaderButtons.vue';
import SettlementSection from '@/components/my/SettlementSection.vue';
import FeedSection from '@/components/my/FeedSection.vue';
import ProfileHeader from '@/components/profile/ProfileHeader.vue';
import FriendMenuButton from '@/components/my/FriendMenuButton.vue';
import { useProfileStore } from '@/stores/profile';
import { useFriendStore } from '@/stores/friend';
import { onMounted, watch, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js';

const authStore = useAuthStore();
const userId = authStore.userId;
const route = useRoute();
const friendStore = useFriendStore();
const profileStore = useProfileStore();

const currentTab = ref(route.query.tab || 'feed');

// URL query 변경 감지
watch(
  () => route.query.tab,
  (tab) => {
    if (tab) {
      currentTab.value = tab;
    }
  },
);

const profile = ref({});

const load = async () => {
  try {
    profile.value = await profileStore.getProfile(userId);
    await friendStore.getFriendList(userId);
  } catch (e) {
    console.log('MyPage load bypass', e);
  }
};

onMounted(load);
</script>

<style scoped>
.profile-section {
  display: flex;
  flex-direction: column;
  align-items: center;

  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}
</style>
