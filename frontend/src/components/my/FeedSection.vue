<template>
  <section class="section">
    <template v-if="feeds.length > 0">
      <div class="col-12 mb-3" v-for="feed in feeds" :key="feed.feedId">
        <FeedCard :feed="feed" />
      </div>
    </template>

    <!-- 피드 없음 -->
    <div v-else class="empty">아직 공유한 피드가 없어요.</div>
  </section>
</template>

<script setup>
import { onMounted, computed, ref } from 'vue';
import FeedCard from '@/components/feed/FeedCard.vue';
import { useFeedStore } from '@/stores/feed';

const feedStore = useFeedStore();

const props = defineProps({
  type: {
    type: String,
    default: 'my',
  },

  userId: {
    type: Number,
    required: true,
  },

  memberUserId: {
    type: Number,
    default: null,
  },
});

const feeds = computed(() => {
  return props.type === 'my' ? myFeeds.value : memberFeeds.value;
});

// 내 피드
const myFeeds = ref([]);

//맴버 피드
const memberFeeds = ref([]);

onMounted(async () => {
  if (props.type === 'my') {
    const list = await feedStore.getMyList({
      userId: props.userId,
    });
    myFeeds.value = list || [];
  } else {
    memberFeeds.value = await feedStore.getMemberList({
      userId: props.userId,
      memberUserId: props.memberUserId,
    });
  }
});
</script>

<style scoped>
.section {
  padding: 12px 0px 0px;
}

.section-header {
  display: flex;

  justify-content: space-between;

  align-items: center;

  margin-top: 20px;
  margin-bottom: 30px;

  padding-bottom: 8px;

  border-bottom: 1px solid #eceff3;
}

.section-title {
  display: flex;
  padding: 10px;
  align-items: center;
}

.section-title span {
  font-size: 15px;

  font-weight: 600;

  color: #2d3748;

  letter-spacing: -0.2px;
}

.section-header h3 {
  margin: 0;

  font-size: 18px;
}

.empty {
  text-align: center;

  color: #999;

  padding: 30px 0;

  font-size: 14px;
}
</style>
