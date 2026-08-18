<template>
  <div class="app-container">
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
          <FeedSection type="my" />
        </div>
        <div v-if="currentTab === 'settlement'">
          <SettlementSection
            :initial-type="settlementType"
            :initial-status="settlementState"
          />
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
import FeedSection from '../feed/components/FeedSection.vue';
import ProfileHeader from '@/components/profile/ProfileHeader.vue';
import FriendMenuButton from '@/components/my/FriendMenuButton.vue';

import { useProfileStore } from '@/stores/profile';
import { useFriendStore } from '@/stores/friend';
import { onMounted, watch, ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js';

const authStore = useAuthStore();
const userId = authStore.userId;

const route = useRoute();

const friendStore = useFriendStore();
const profileStore = useProfileStore();

/* =========================
 * 현재 마이페이지 탭
 * ========================= */

const currentTab = ref(route.query.tab || 'feed');

/* =========================
 * 정산 탭 초기 상태
 *
 * type:
 * requested = 요청한 내역
 * received  = 요청받은 내역
 *
 * state:
 * progress = 진행 중
 * complete = 완료
 * ========================= */

const settlementType = computed(() => {
  return route.query.type || 'requested';
});

const settlementState = computed(() => {
  return route.query.state || 'progress';
});

/* =========================
 * URL query 변경 감지
 * ========================= */

watch(
  () => route.query.tab,
  (tab) => {
    if (tab) {
      currentTab.value = tab;
    }
  },
);

const profile = ref({});

/* =========================
 * 프로필 / 친구 목록
 * ========================= */

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
.app-container {
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

.profile-section {
  display: flex;
  flex-direction: column;
  align-items: center;

  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  margin-top: 20px;
  margin-bottom: 10px;

  padding-bottom: 8px;

  border-bottom: 1px solid #eceff3;
}

.section-title {
  display: flex;
  padding: 10px;
  align-items: center;
}

.section-title span {
  font-size: 15px;
  font-weight: 600;
  color: #2d3748;
  letter-spacing: -0.2px;
}
</style>
