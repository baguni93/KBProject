<template>
  <div>
    <HeaderButtons />
    <div class="profile-section">
      <ProfileHeader v-if="profile" :profile="profile" />
      <FriendMenuButton :friend-count="friendStore.friendList.length" />
    </div>
    <MyPageTab v-model="currentTab" />
    <div class="main">
      <template v-if="currentTab === 'feed'">
        <SettlementSection />
        <FeedSection type="my" :user-id="userId" />
      </template>
      <template v-else-if="currentTab === 'wallet'">
        <MyWallet />
      </template>
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

const route = useRoute();
const friendStore = useFriendStore();
const profileStore = useProfileStore();

//test user Id
import { useUserStore } from '@/stores/user';
const userStore = useUserStore();
const userId = userStore.userId;

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

// onMounted(async () => {
//   //profileStore.clearProfile();
//   await profileStore.getProfile(userId);
//   await friendStore.getFriendList(userId);
// });

const profile = ref({});

const load = async () => {
  profile.value = await profileStore.getProfile(userId);
  await friendStore.getFriendList(userId);
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
