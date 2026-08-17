<template>
  <article class="feed-item insta-feed-item">
    <FeedHeader
      :user-name="feed.sender?.nickname"
      :time="feed.createdAt"
      :profile-image="feed.sender?.profileImageName"
      :visibility="feed.visibility"
      :user-id="feed.userId"
    />

    <div class="point-content-container">
      <div class="point-box-card">
        <!-- 이벤트 이미지 -->
        <img
          v-if="feed.event?.url"
          :src="feed.event.url"
          alt="이벤트 이미지"
          class="point-box-background"
        />

        <!-- 이미지 위 텍스트 -->
        <div class="point-box-overlay">
          <span class="point-box-badge"> Mission Clear! </span>

          <h3 class="point-box-title">{{ feed.content }}</h3>

          <p class="point-box-desc">{{ feed.event?.rewardPoint ?? 0 }}P 획득</p>
        </div>
      </div>

      <button class="content-btn primary" type="button" @click="handleOpen">
        나도 이벤트 하러가기
      </button>
    </div>

    <div class="insta-content-wrap">
      <slot name="actions" />

      <p class="feed-message insta-message"></p>
    </div>
    <FeedTags :tags="mytags" @tag-click="handleTagClick" />
  </article>
</template>

<script setup>
import FeedHeader from './FeedHeader.vue';
import FeedTags from './FeedTags.vue';
import { computed } from 'vue';

import { useRouter } from 'vue-router';
const router = useRouter();

const props = defineProps({
  feed: {
    type: Object,
    required: true,
  },

  tags: {
    type: Array,
    default: () => [],
  },
});
// event 경로 수정되어서 바꿨습니다.
const handleOpen = () => {
  router.push('/event');
};

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

.point-content-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  margin-bottom: 12px;
}

/* 이벤트 이미지 카드 */
.point-box-card {
  position: relative;

  width: 88%;
  height: 200px;
  margin: 0 auto;

  border: 1.5px solid #fde68a;
  border-radius: 12px;

  overflow: hidden;
  box-sizing: border-box;

  box-shadow: 0 3px 10px rgba(245, 158, 11, 0.08);
}

/* 이벤트 이미지 */
.point-box-background {
  position: absolute;

  inset: 0;

  width: 100%;
  height: 100%;

  object-fit: cover;

  display: block;
}

/* 이미지 위 어두운 오버레이 */
.point-box-card::after {
  content: '';

  position: absolute;

  inset: 0;

  background: rgba(0, 0, 0, 0.25);

  z-index: 1;

  pointer-events: none;
}

/* 이미지 위 내용 */
.point-box-overlay {
  position: relative;

  z-index: 2;

  width: 100%;
  height: 100%;

  display: flex;
  flex-direction: column;

  align-items: center;
  justify-content: center;

  text-align: center;

  gap: 4px;

  padding: 16px;

  box-sizing: border-box;
}

/* Mission Clear */
.point-box-badge {
  display: inline-block;

  background: #fef08a;
  color: #854d0e;

  font-size: 10px;
  font-weight: 700;

  padding: 3px 7px;

  border-radius: 6px;

  text-transform: uppercase;

  margin-bottom: 2px;
}

/* 제목 */
.point-box-title {
  font-size: 15px;
  font-weight: 700;

  color: #ffffff;

  margin: 0;

  line-height: 1.2;

  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.45);
}

/* 설명 */
.point-box-desc {
  font-size: 12px;

  color: #ffffff;

  margin: 0;

  line-height: 1.3;

  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.45);
}

/* 피드 내용 */
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
