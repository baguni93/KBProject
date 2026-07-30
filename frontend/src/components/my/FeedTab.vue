<template>
  <div class="feed-tab">
    <SettlementSection />

    <!-- <FeedSection /> -->
  </div>
</template>

<script setup>
import SettlementSection from './SettlementSection.vue';
import FeedSection from './FeedSection.vue';
</script>

<style scoped>
.feed-tab {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
