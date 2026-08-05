<template>
  <div
    class="col-12 mb-3"
    v-for="feed in displayedFeeds"
    :key="feed.feedId"
  >
    <FeedCard :feed="feed" />
  </div>
</template>

<script setup>
import { computed } from 'vue';
import FeedCard from '@/components/feed/FeedCard.vue';
import { useFeedStore } from '@/stores/feed';
const feedStore = useFeedStore();

const displayedFeeds = computed(() => {
  const localFeeds = JSON.parse(localStorage.getItem('user_created_feeds') || '[]');
  const storeFeeds = feedStore.publicFeeds || [];
  return [...localFeeds, ...storeFeeds];
});
</script>

<style scoped></style>
