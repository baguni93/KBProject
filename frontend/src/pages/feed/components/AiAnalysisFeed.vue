<template>
  <article class="feed-item insta-feed-item">
    <FeedHeader
      :user-name="feed.sender?.nickname"
      :time="feed.createdAt"
      :profile-image="feed.sender?.profileImageName"
      :visibility="feed.visibility"
      :user-id="feed.userId"
    />

    <div class="ai-content-container">
      <div class="ai-analysis-box">
        <div class="ai-box-header">
          <span class="ai-badge">
            <i class="fa-solid fa-wand-magic-sparkles"></i>
            {{ feed.analysis?.title || '절약 마스터' }}
          </span>
        </div>

        <div class="ai-content-body">
          <div class="ai-icon-pulse">
            <i class="fa-solid fa-brain"></i>
          </div>

          <p class="ai-analysis-text">
            {{ feed.analysis?.content || feed.content }}
          </p>
        </div>
      </div>

      <button class="content-btn primary" type="button" @click="handleReport">
        Ai 소비패턴 하러가기
      </button>
    </div>

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

const handleReport = () => {
  console.log('AI 소비 분석 리포트 보기');
  router.push('/analysis');
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

.ai-content-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  margin-bottom: 12px;
}

.ai-analysis-box {
  width: 100%;

  background: linear-gradient(135deg, #f8fafc 0%, #edf2f7 100%);

  border: 1.5px solid #cbd5e1;
  border-radius: 14px;

  padding: 16px;
  box-sizing: border-box;

  display: flex;
  flex-direction: column;
  gap: 12px;

  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.05);
}

.ai-box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ai-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;

  background: linear-gradient(135deg, #6366f1, #4f46e5);

  color: #ffffff;

  font-size: 11px;
  font-weight: 700;

  padding: 4px 10px;
  border-radius: 12px;

  box-shadow: 0 2px 5px rgba(99, 102, 241, 0.2);
}

.ai-content-body {
  display: flex;
  align-items: center;
  gap: 12px;

  background: #ffffff;

  padding: 12px;
  border-radius: 10px;

  border: 1px solid #e2e8f0;
}

.ai-icon-pulse {
  width: 36px;
  height: 36px;

  background: #e0e7ff;
  color: #4f46e5;

  border-radius: 50%;

  display: flex;
  align-items: center;
  justify-content: center;

  font-size: 16px;

  flex-shrink: 0;
}

.ai-analysis-text {
  font-size: 13px;
  color: #1e293b;

  margin: 0;

  line-height: 1.4;
  font-weight: 500;
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
  font-size: 15px;
  color: #111;

  margin: 0;
  line-height: 1.4;
}

.insta-message {
  padding-left: 0;
}
</style>
