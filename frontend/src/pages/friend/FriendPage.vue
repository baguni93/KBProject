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

onMounted(() => {
  friendStore.getFriendList(userId);
  friendStore.getRequestList(userId);
  friendStore.getSendRequestList(userId);
});
</script>

<style scoped>
/* 공통 페이지 규칙 */
.friend-page {
  padding: 16px;
}

/* 탭 */
.tabs {
  display: flex;

  border-bottom: 1px solid #eee;

  margin-top: 20px;
  margin-bottom: 20px;
}

.tabs button {
  flex: 1;

  border: none;

  background: none;

  padding: 12px;

  font-size: 15px;

  cursor: pointer;

  color: #777;
}

.tabs button.active {
  color: #4f46e5;

  font-weight: bold;

  border-bottom: 2px solid #4f46e5;
}
</style>
