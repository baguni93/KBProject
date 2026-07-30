<template>
  <div>
    <div class="row">
      <div class="col-12 mb-3" v-for="feed in feeds" :key="feed.feedId">
        <FeedCard :feed="feed" />
      </div>
    </div>
  </div>
</template>

<script setup>
import api from '@/api/feedApi';
import { useRoute, useRouter } from 'vue-router';
import FeedCard from '@/components/feed/FeedCard.vue';
import { ref, reactive, computed, watch } from 'vue';
import { onMounted } from 'vue';

console.log('Feed Public');

const cr = useRoute();
const router = useRouter();
const feeds = ref([]);
const query = reactive({
  userId: 3,
});

const load = async (query) => {
  try {
    feeds.value = await api.getList(query);
    console.log(feeds.value);
  } catch {}
};

onMounted(() => {
  load(query);
});
</script>

<style scoped></style>
