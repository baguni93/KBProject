<template>
  <div class="feed-detail-page">
    <PageHeader title="피드" />

    <div v-if="feed" class="detail-container">
      <FeedCard :feed="feed" />
    </div>

    <div v-else class="loading">피드를 불러오는 중...</div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import PageHeader from '@/components/common/PageHeader.vue';
import FeedCard from '@/components/feed/FeedCard.vue';

import feedApi from '@/api/feedApi';

const route = useRoute();

const feed = ref(null);

const loadFeed = async () => {
  const feedId = route.params.feedId;

  try {
    const response = await feedApi.getFeed(feedId);

    feed.value = response;
  } catch (error) {
    console.error('피드 조회 실패', error);
  }
};

onMounted(() => {
  loadFeed();
});
</script>

<style scoped>
.feed-detail-page {
  min-height: 100%;
  background: #fff;
}

.detail-container {
  padding: 16px;
}

.loading {
  text-align: center;
  padding: 50px;
  color: #888;
}
</style>
