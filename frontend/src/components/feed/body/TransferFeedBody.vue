<template>
  <FeedImageSlider v-if="feed.images?.length" :images="feed.images" />
  <!-- <FeedTags
    v-if="feed.transaction?.category"
    :categoryName="feed.transaction.category.categoryName"
  /> -->
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
