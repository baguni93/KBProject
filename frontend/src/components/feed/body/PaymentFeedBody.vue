<template>
  <FeedImageSlider v-if="feed.images?.length" :images="feed.images" />
  <FeedTags :feed="feed" />
</template>

<script setup>
import FeedTags from '../FeedTags.vue';
import FeedImageSlider from '../FeedImageSlider.vue';
defineProps({
  feed: {
    type: Object,
    required: true,
  },
});
</script>
