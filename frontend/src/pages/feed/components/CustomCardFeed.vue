<template>
  <article class="feed-item insta-feed-item">
    <FeedHeader
      :user-name="feed.sender?.nickname"
      :time="feed.createdAt"
      :profile-image="feed.sender?.profileImageName"
      :visibility="feed.visibility"
      :user-id="feed.userId"
    />

    <!-- 카드 이미지 -->
    <div class="custom-card-content-container">
      <div class="feed-card-preview-container">
        <img
          v-if="feed.card?.url"
          :src="feed.card.url"
          alt="Custom Card Preview"
          class="feed-img card-preview-img"
        />

        <div v-else class="card-image-empty">카드 이미지가 없습니다.</div>
      </div>

      <!-- 카드 주문 -->
      <button class="content-btn primary" type="button" @click="handleOrder">
        해당 커스텀 카드 발급 받기
      </button>
    </div>

    <!-- 피드 내용 -->
    <div class="insta-content-wrap">
      <slot name="actions" />

      <p class="feed-message insta-message">
        {{ feed.content }}
      </p>
    </div>

    <FeedTags :tags="mytags" @tag-click="handleTagClick" />
  </article>
</template>

<script setup>
import FeedHeader from './FeedHeader.vue';
import FeedTags from './FeedTags.vue';
import { useRouter } from 'vue-router';
import { computed } from 'vue';
const router = useRouter();

import { useCardEditorStore } from '@/stores/cardEditorStore';
const cardEditorStore = useCardEditorStore();

const props = defineProps({
  feed: {
    type: Object,
    required: true,
  },
});

const handleOrder = async () => {
  console.log('커스텀 카드 발급 버튼 클릭');
  console.log('해당 피드 ID :', props.feed.feedId);
  console.log('해당 피드 TargetID:', props.feed.targetId);
  await cardEditorStore.getCustomCard(props.feed.userId, props.feed.targetId);

  router.push('/card/create/agreement');
  // 카드 발급 API 호출 예정
};

//태그
import tags from '@/config/tags';
const feedConfig = computed(() => {
  return tags.FEEDTYPE_MAP[props.feed.feedType];
});

const mytags = computed(() => {
  const typeConfig = feedConfig.value;

  if (!typeConfig) {
    return [];
  }

  const result = [];

  // 기본 태그
  typeConfig.values?.forEach((item) => {
    result.push({
      value: item.value,
      route: item.route,
    });
  });
  return result;
});

const handleTagClick = (tag) => {
  if (!tag.route) {
    return;
  }

  router.push(tag.route);
};
</script>

<style scoped>
.feed-item {
  width: 100%;
}

.insta-feed-item {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  box-sizing: border-box;
}

.custom-card-content-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  margin-bottom: 12px;
}

.feed-card-preview-container {
  width: 100%;
  height: 200px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.feed-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.card-preview-img {
  object-fit: contain;
}

.custom-primary-btn {
  display: flex;
  align-items: center;
  justify-content: center;

  width: 100%;
  height: 42px;

  border-radius: 12px;

  background-color: var(--color-primary, #ffbc2e);

  border: 1px solid var(--color-primary-border, #cc9200);

  color: #111111;

  font-size: 14px;
  font-weight: 700;

  cursor: pointer;
  box-sizing: border-box;

  transition:
    background-color 0.2s ease,
    transform 0.1s ease;
}

.custom-primary-btn:hover {
  background-color: var(--color-primary-active, #f2aa10);
}

.custom-primary-btn:active {
  transform: scale(0.98);
}

.insta-content-wrap {
  padding-left: 0;
}

.feed-message {
  font-size: 13px;
  color: #111;
  margin: 0;
  line-height: 1.4;
  margin-top: 5px;
}

.insta-message {
  padding-left: 0;
}
</style>
