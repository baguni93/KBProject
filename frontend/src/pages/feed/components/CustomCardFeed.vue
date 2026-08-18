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

      <!-- 카드 발급 -->
      <button class="custom-card-btn" type="button" @click="handleOrder">
        <span class="icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
            <rect
              x="4"
              y="5"
              width="16"
              height="14"
              rx="1"
              stroke="white"
              stroke-width="2"
            />

            <line
              x1="4"
              y1="9"
              x2="20"
              y2="9"
              stroke="white"
              stroke-width="2"
            />
          </svg>
        </span>

        <span class="text"> 해당 커스텀 카드 발급 받기 </span>
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

import { useCardEditorStore } from '@/stores/cardEditorStore';
import tags from '@/config/tags';

const router = useRouter();
const cardEditorStore = useCardEditorStore();

const props = defineProps({
  feed: {
    type: Object,
    required: true,
  },
});

/* =========================
   커스텀 카드 발급
========================= */

const handleOrder = async () => {
  console.log('커스텀 카드 발급 버튼 클릭');
  console.log('해당 피드 ID :', props.feed.feedId);
  console.log('해당 피드 TargetID:', props.feed.targetId);

  await cardEditorStore.getCustomCard(props.feed.userId, props.feed.targetId);

  router.push('/card/create/agreement');
};

/* =========================
   태그
========================= */

const feedConfig = computed(() => {
  return tags.FEEDTYPE_MAP[props.feed.feedType];
});

const mytags = computed(() => {
  const typeConfig = feedConfig.value;

  if (!typeConfig) {
    return [];
  }

  const result = [];

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
/* =========================
   피드
========================= */

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

/* =========================
   카드 영역
========================= */

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

  background: #ffffff;

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

/* =========================
   카드 이미지 없음
========================= */

.card-image-empty {
  width: 100%;
  height: 100%;

  display: flex;
  align-items: center;
  justify-content: center;

  color: #999999;

  font-size: 13px;
}

/* =========================
   커스텀 카드 발급 버튼
========================= */

.custom-card-btn {
  display: flex;
  align-items: center;
  justify-content: center;

  width: 88%;
  height: 44px;

  padding: 0 16px;

  margin: 0 auto;

  border: none;
  border-radius: 12px;

  background: linear-gradient(90deg, #3478ff, #48d1c8);

  color: #ffffff;

  font-family: inherit;
  font-size: 14px;
  font-weight: 600;

  cursor: pointer;
  box-sizing: border-box;

  gap: 8px;

  box-shadow: 0 5px 14px rgba(50, 120, 255, 0.18);

  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease,
    filter 0.15s ease;

  -webkit-tap-highlight-color: transparent;
}

/* 아이콘 */

.custom-card-btn .icon {
  display: flex;
  align-items: center;
  justify-content: center;

  flex-shrink: 0;
}

.custom-card-btn .icon svg {
  width: 20px;
  height: 20px;
}

/* 텍스트 */

.custom-card-btn .text {
  display: flex;
  align-items: center;
  justify-content: center;

  line-height: 1;

  white-space: nowrap;
}

/* PC hover */

.custom-card-btn:hover {
  filter: brightness(1.05);

  box-shadow: 0 7px 16px rgba(50, 120, 255, 0.22);
}

/* 클릭 */

.custom-card-btn:active {
  transform: scale(0.98);

  box-shadow: 0 3px 8px rgba(50, 120, 255, 0.18);
}
</style>
