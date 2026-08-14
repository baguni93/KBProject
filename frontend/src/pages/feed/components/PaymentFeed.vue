<template>
  <article class="feed-item insta-feed-item">
    <!-- 헤더 -->
    <FeedHeader
      :user-name="feed.sender?.nickname"
      :time="feed.createdAt"
      :profile-image="feed.sender?.profileImageName"
      :visibility="feed.visibility"
      :user-id="feed.userId"
    />

    <!-- 이미지가 있을 때만 토글 -->
    <div v-if="feed.images?.length" class="image-section">
      <!-- 이미지 토글 -->
      <div class="image-toggle-area">
        <button
          class="image-toggle-btn"
          :class="{ active: showImages }"
          type="button"
          :aria-label="showImages ? '이미지 숨기기' : '이미지 보기'"
          @click="showImages = !showImages"
        >
          <span class="toggle-knob"></span>
        </button>
      </div>

      <!-- 이미지 -->
      <Transition name="image-collapse">
        <div v-show="showImages" class="image-wrapper">
          <FeedImageSlider :images="feed.images" />
        </div>
      </Transition>
    </div>

    <!-- 내용 -->
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
import { ref, computed } from 'vue';
import FeedHeader from './FeedHeader.vue';
import FeedTags from './FeedTags.vue';
import FeedImageSlider from '@/components/feed/FeedImageSlider.vue';

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

import config from '@/config/tags';

const feedConfig = computed(() => {
  return config.FEEDTYPE_MAP[props.feed.feedType];
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

  // 매장
  if (typeConfig.STORE && props.feed.transaction?.merchantName) {
    result.push({
      icon: typeConfig.STORE.icon,
      value: props.feed.transaction.merchantName,
    });
  }

  return result;
});

/* 이미지 표시 여부 */
const showImages = ref(true);

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

/* =========================================
   이미지 영역
========================================= */

.image-section {
  position: relative;
  width: 100%;
}

/* 토글 영역 */

.image-toggle-area {
  position: absolute;

  top: 0;
  right: 0;

  z-index: 10;
}

/* =========================================
   토글
========================================= */

.image-toggle-btn {
  position: relative;

  width: 38px;
  height: 22px;

  flex-shrink: 0;

  padding: 0;

  border: none;
  border-radius: 999px;

  background: #d1d5db;

  cursor: pointer;

  transition: background-color 0.2s ease;
}

/* ON */

.image-toggle-btn.active {
  background: #ffbc2e;
}

/* 동그라미 */

.toggle-knob {
  position: absolute;

  top: 3px;
  left: 3px;

  width: 16px;
  height: 16px;

  border-radius: 50%;

  background: #ffffff;

  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.18);

  transition: transform 0.2s ease;
}

/* ON → 오른쪽 */

.image-toggle-btn.active .toggle-knob {
  transform: translateX(16px);
}

/* 클릭 */

.image-toggle-btn:active .toggle-knob {
  transform: scale(0.9);
}

.image-toggle-btn.active:active .toggle-knob {
  transform: translateX(16px) scale(0.9);
}

/* =========================================
   이미지 Wrapper
========================================= */

.image-wrapper {
  width: 100%;

  overflow: hidden;

  border-radius: 16px;

  /*
   * 핵심
   * 이미지의 위쪽 위치는 고정하고
   * 아래쪽만 위로 올라가도록 함
   */
  transform-origin: top center;
}

/* =========================================
   이미지 접기 / 펼치기
========================================= */

.image-collapse-enter-active,
.image-collapse-leave-active {
  overflow: hidden;

  transition:
    max-height 0.35s ease,
    opacity 0.25s ease;

  /*
   * 위쪽 기준으로 유지
   */
  transform-origin: top center;
}

/* 펼쳐질 때 시작 */

.image-collapse-enter-from {
  max-height: 0;

  opacity: 0;
}

/* 펼쳐진 상태 */

.image-collapse-enter-to {
  max-height: 1000px;

  opacity: 1;
}

/* 닫힐 때 */

.image-collapse-leave-from {
  max-height: 1000px;

  opacity: 1;
}

/*
 * 핵심 부분
 *
 * max-height가
 * 1000px → 0px
 *
 * 으로 줄어들면서
 * 위쪽은 그대로 있고
 * 아래쪽이 위로 올라감
 */
.image-collapse-leave-to {
  max-height: 0;

  opacity: 0;
}

/* =========================================
   기존 이미지 영역
========================================= */

.insta-media-container {
  width: 100%;
  height: 280px;

  border-radius: 12px;

  overflow: hidden;

  background: #e2e8f0;

  margin-bottom: 12px;
}

.feed-img {
  width: 100%;
  height: 100%;

  object-fit: cover;

  display: block;
}

/* =========================================
   내용
========================================= */

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
