<template>
  <div
    class="col-12 mb-3"
    v-for="feed in feedStore.publicFeeds"
    :key="feed.feedId"
  >
    <FeedCard :feed="feed" />
  </div>
</template>

<script setup>
import FeedCard from '@/components/feed/FeedCard.vue';
import { useFeedStore } from '@/stores/feed';
const feedStore = useFeedStore();
</script>

<style scoped></style>
