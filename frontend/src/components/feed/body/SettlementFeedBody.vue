<template>
  <!-- <FeedTags
    v-if="feed.settlement?.category.categoryName"
    :categoryName="feed.settlement.category.categoryName"
  /> -->
</template>

<script setup>
import FeedTags from '../FeedTags.vue';
defineProps({
  feed: {
    type: Object,
    required: true,
  },
});
</script>
