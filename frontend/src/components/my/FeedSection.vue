<template>
  <section class="section">
    <div class="section-header">
      <h3>내 피드</h3>
    </div>

    <FeedCard v-for="feed in feeds" :key="feed.feedId" :feed="feed" />
  </section>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import FeedCard from '@/components/feed/FeedCard.vue';
import api from '@/api/feedApi';

const feeds = ref([]);

const query = reactive({
  userId: 3,
});

const load = async (query) => {
  try {
    feeds.value = await api.getMyList(query);
    console.log(feeds.value);
  } catch {}
};

onMounted(() => {
  load(query);
});
</script>

<style scoped>
.section-header {
  padding: 16px;
}

.section-header h3 {
  margin: 0;
}
</style>
