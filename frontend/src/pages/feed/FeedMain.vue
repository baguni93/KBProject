<template>
  <div class="kb-mobile-page page">
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
import { useAuthStore } from '@/stores/auth';
import HeaderButtons from '@/components/common/HeaderButtons.vue';

const feedStore = useFeedStore();
const authStore = useAuthStore();
const userId = authStore.userId;

//JWT 처리
onMounted(() => {
  const userId = authStore.userId || 1;
  feedStore.getList({
    userId,
  });
});
</script>

<style scoped>
/* 검색 영역 */
.page {
  background: var(--color-bg-screen);
  padding-bottom: 30px;
}
:deep(.search-bar) {
  margin-bottom: 14px;
}
</style>
