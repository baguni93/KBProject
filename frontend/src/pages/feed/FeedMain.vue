<template>
  <div class="feed-page">
    <!-- 검색 -->
    <SearchBar />

    <InviteBanner />

    <!-- 피드 -->

    <FeedList />
  </div>
</template>

<script setup>
import FeedList from '@/components/feed/FeedList.vue';
import SearchBar from '@/components/search/SearchBar.vue';
import InviteBanner from '@/components/common/InviteBanner.vue';
import { onMounted } from 'vue';
import { useFeedStore } from '@/stores/feed';
const feedStore = useFeedStore();

//test user Id
import { useUserStore } from '@/stores/user';
const userStore = useUserStore();
const userId = userStore.userId;

//JWT 처리
onMounted(() => {
  feedStore.getList({
    userId,
  });
});
</script>

<style scoped>
.feed-page {
  width: 100%;
  background: white;
}

/* 검색 영역 */
:deep(.search-bar) {
  margin-bottom: 14px;
}
</style>
