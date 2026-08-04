<template>
  <div class="friend-page">
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
    <!-- <FriendRequestList v-else :requests="friendStore.requestList" /> -->

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

//test user Id
import { useUserStore } from '@/stores/user';
const userStore = useUserStore();
const userId = userStore.userId;

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
.friend-page {
  padding: 20px;
}

/* 상단 */
.header {
  display: flex;
  align-items: center;
  gap: 15px;

  margin-bottom: 20px;
}

.header i {
  cursor: pointer;
}

.header h2 {
  margin: 0;
}

/* 탭 */
.tabs {
  display: flex;

  border-bottom: 1px solid #eee;

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
