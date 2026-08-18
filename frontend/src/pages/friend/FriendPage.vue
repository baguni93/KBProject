<template>
  <div class="page friend-page">
    <PageHeader title="친구" />

    <!-- 탭 -->
    <div class="tabs">
      <button
        :class="{ active: currentTab === 'list' }"
        @click="currentTab = 'list'"
      >
        친구 목록
      </button>

      <button
        :class="{ active: currentTab === 'request' }"
        @click="currentTab = 'request'"
      >
        친구 요청
      </button>
    </div>

    <!-- 친구 목록 -->
    <FriendList
      v-if="currentTab === 'list'"
      :friends="friendStore.friendList"
    />

    <!-- 친구 요청 -->
    <FriendRequest
      v-if="currentTab === 'request'"
      :received="friendStore.requestList"
      :sent="friendStore.sendRequestList"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useFriendStore } from '@/stores/friend';
import PageHeader from '@/components/common/PageHeader.vue';
import FriendList from '@/components/friend/FriendList.vue';
import FriendRequest from '@/components/friend/FriendRequest.vue';
import { useAuthStore } from '@/stores/auth.js';

const authStore = useAuthStore();
const userId = authStore.userId;

const route = useRoute();
const friendStore = useFriendStore();

const currentTab = ref(route.query.tab || 'list');

watch(
  () => route.query.tab,
  (tab) => {
    currentTab.value = tab || 'list';
  },
);

watch(
  () => friendStore.friendStatusVersion,
  async () => {
    await Promise.all([
      friendStore.getFriendList(userId),
      friendStore.getRequestList(userId),
      friendStore.getSendRequestList(userId),
    ]);
  },
);

onMounted(async () => {
  await friendStore.getFriendList(userId);
  await friendStore.getRequestList(userId);
  await friendStore.getSendRequestList(userId);
});
</script>

<style scoped>
/* 공통 페이지 */
.friend-page {
  width: 100%;
  min-height: 100%;
  padding: 0 20px 30px;
  background: var(--color-bg-screen);
}

/* =========================
 * 탭
 * ========================= */

.tabs {
  position: relative;

  display: flex;

  height: 52px;

  margin-top: 20px;
  margin-bottom: 20px;

  background: #fff;

  border-bottom: 1px solid #ececec;
}

.tabs button {
  flex: 1;

  border: none;
  background: transparent;

  padding: 0;

  font-size: 15px;
  font-weight: 500;

  color: #999;

  cursor: pointer;

  transition:
    color 0.2s ease,
    font-weight 0.2s ease;
}

.tabs button.active {
  color: #222;
  font-weight: 700;
}

/* 활성 탭 밑줄 */
.tabs::after {
  content: '';

  position: absolute;

  bottom: 0;
  left: 0;

  width: 50%;
  height: 3px;

  background: #ffcc00;

  transition: transform 0.25s ease;
}

/* 친구 요청 선택 */
.tabs:has(button:nth-child(2).active)::after {
  transform: translateX(100%);
}

/* =========================
 * 기타
 * ========================= */

.empty {
  color: #999;
}
</style>
