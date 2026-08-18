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
      <!-- 이벤트 이미지 카드 -->
      <div class="point-box-card">
        <img
          src="/images/event/event_sample.png"
          alt="이벤트 이미지"
          class="point-box-background"
        />

        <!-- 이미지 위 어두운 오버레이 -->
        <div class="point-box-overlay-dark"></div>

        <!-- 이미지 위 텍스트 -->
        <div class="point-box-overlay">
          <span class="point-box-badge"> 이벤트 완료 </span>

          <h3 class="point-box-title">
            {{ feed.event?.eventName }}
          </h3>

          <div class="point-box-title-reward">
            <strong>{{ feed.event?.rewardPoint ?? 0 }} 포인트 획득 !</strong>
          </div>
        </div>

        <!-- 오른쪽 위 포인트 장식 -->
        <div class="point-floating-icon">
          <i class="fa-solid fa-star"></i>
        </div>
      </div>

      <!-- 이벤트 참여 버튼 -->
      <button class="event-join-btn" type="button" @click="handleOpen">
        <span class="event-btn-icon">
          <i class="fa-solid fa-gift"></i>
        </span>

        <span class="event-btn-text"> 나도 참여해서 포인트 받기 </span>

        <span class="ai-btn-arrow">
          <i class="fa-solid fa-arrow-right"></i>
        </span>
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

import tags from '@/config/tags';

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

// 태그
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
   이벤트 영역
========================= */

.point-content-container {
  display: flex;
  flex-direction: column;

  gap: 10px;

  width: 100%;

  margin-bottom: 12px;
}

/* =========================
   이벤트 카드
========================= */

.point-box-card {
  position: relative;

  width: 88%;
  height: 200px;

  margin: 0 auto;

  border-radius: 12px;

  overflow: hidden;

  box-sizing: border-box;

  border: 1.5px solid #e8b2f3;

  box-shadow:
    0 5px 16px rgba(245, 158, 11, 0.12),
    0 2px 6px rgba(0, 0, 0, 0.05);
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

/* 이미지 어두운 오버레이 */

.point-box-overlay-dark {
  position: absolute;

  inset: 0;

  z-index: 1;
}

/* =========================
   카드 내용
========================= */

.point-box-overlay {
  position: absolute;
  inset: 0;

  z-index: 2;

  width: 100%;
  height: 100%;

  padding: 0;

  box-sizing: border-box;
}

/* Mission Clear */

.point-box-badge {
  position: absolute;

  top: 10px;
  left: 20%;

  transform: translateX(-50%);

  display: inline-flex;
  align-items: center;
  gap: 3px;

  padding: 5px 10px;

  border-radius: 10px;

  background: linear-gradient(135deg, #ff7eb3 0%, #ff5c9a 200%);
  color: white;

  font-size: 10px;
  font-weight: 800;

  white-space: nowrap;

  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.12);
}

.point-box-badge i {
  color: #f59e0b;

  font-size: 9px;
}

/* 이벤트 제목 */

.point-box-title {
  position: absolute;

  top: 125px;
  left: 50%;

  transform: translateX(-50%);

  width: 90%;
  max-width: 90%;

  margin: 0;
  padding: 5px 12px;

  box-sizing: border-box;

  border-radius: 10px;

  text-align: center;

  font-size: 17px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.5px;

  color: #ffffff;

  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.18),
    rgba(255, 182, 215, 0.22)
  );

  text-shadow:
    0 1px 2px rgba(0, 0, 0, 0.75),
    0 2px 5px rgba(0, 0, 0, 0.45);

  backdrop-filter: blur(2px);
}

.point-box-title-reward {
  position: absolute;

  top: 155px;
  left: 50%;

  transform: translateX(-50%);

  width: 90%;
  max-width: 90%;

  margin: 0;
  padding: 5px 12px;

  box-sizing: border-box;

  border-radius: 10px;

  text-align: center;

  font-size: 13px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.5px;

  color: #ffffff;

  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.18),
    rgba(255, 182, 215, 0.22)
  );

  text-shadow:
    0 1px 2px rgba(0, 0, 0, 0.75),
    0 2px 5px rgba(0, 0, 0, 0.45);

  backdrop-filter: blur(2px);
}

/* 포인트 */

.point-reward {
  position: absolute;

  top: 155px;
  left: 50%;

  transform: translateX(-50%);

  display: flex;
  align-items: center;
  gap: 4px;

  color: #ffffff;

  white-space: nowrap;

  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.4);
}

.point-reward i {
  color: white;

  font-size: 14px;
}

.point-reward strong {
  color: white;

  font-size: 18px;
  font-weight: 800;
}

.point-reward span {
  font-size: 12px;
  font-weight: 600;
}

/* =========================
   장식
========================= */

.point-floating-icon {
  position: absolute;

  z-index: 3;

  top: 12px;
  right: 14px;

  display: flex;

  align-items: center;
  justify-content: center;

  width: 28px;
  height: 28px;

  border-radius: 50%;

  background: rgba(255, 255, 255, 0.18);

  color: #fff4a8;

  font-size: 12px;

  backdrop-filter: blur(3px);
}

/* =========================
   이벤트 참여 버튼
========================= */

.event-join-btn {
  position: relative;

  display: flex;

  align-items: center;
  justify-content: center;

  width: 88%;
  height: 48px;

  margin: 0 auto;

  padding: 0 44px 0 16px;

  border: none;

  border-radius: 14px;

  background: linear-gradient(135deg, #ffc107 0%, #ffb300 100%);

  color: #111111;

  font-family: inherit;

  font-size: 14px;
  font-weight: 800;

  cursor: pointer;

  box-sizing: border-box;

  box-shadow: 0 6px 14px rgba(245, 158, 11, 0.22);

  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease,
    filter 0.15s ease;

  -webkit-tap-highlight-color: transparent;
}

/* 왼쪽 아이콘 */

.event-btn-icon {
  display: flex;

  align-items: center;
  justify-content: center;

  width: 28px;
  height: 28px;

  margin-right: 7px;

  border-radius: 50%;

  background: rgba(255, 255, 255, 0.42);

  color: #7a4b00;

  font-size: 13px;

  flex-shrink: 0;
}

/* 텍스트 */

.event-btn-text {
  line-height: 1;

  white-space: nowrap;
}

/* 오른쪽 화살표 */

.event-btn-arrow {
  position: absolute;

  right: 14px;

  display: flex;

  align-items: center;
  justify-content: center;

  width: 24px;
  height: 24px;

  border-radius: 50%;

  background: rgba(255, 255, 255, 0.38);

  color: #7a4b00;

  font-size: 11px;
}

/* hover */

.event-join-btn:hover {
  filter: brightness(1.03);

  box-shadow: 0 8px 18px rgba(245, 158, 11, 0.28);
}

/* 클릭 */

.event-join-btn:active {
  transform: scale(0.98);

  box-shadow: 0 4px 9px rgba(245, 158, 11, 0.18);
}

/* =========================
   피드 내용
========================= */

.insta-content-wrap {
  padding-left: 0;
}

.feed-message {
  font-size: 13px;

  color: #111111;

  margin: 0;

  line-height: 1.4;

  margin-top: 5px;
}

.insta-message {
  padding-left: 0;
}

/* =========================
   작은 화면
========================= */

@media (max-width: 360px) {
  .point-box-card {
    width: 92%;
    height: 190px;
  }

  .event-join-btn {
    width: 92%;
  }

  .point-box-title {
    font-size: 15px;
  }

  .point-reward strong {
    font-size: 17px;
  }
}

/* =========================
   이벤트 참여 버튼
========================= */

.event-join-btn {
  display: flex;
  align-items: center;
  justify-content: center;

  position: relative;

  width: 88%;
  height: 48px;

  margin: 0 auto;

  padding: 0 42px 0 16px;

  border: none;
  border-radius: 14px;

  background: linear-gradient(135deg, #ff7eb3 0%, #ff5c9a 100%);

  color: #ffffff;

  font-family: inherit;
  font-size: 14px;
  font-weight: 700;

  cursor: pointer;

  box-sizing: border-box;

  box-shadow: 0 6px 14px rgba(255, 92, 154, 0.2);

  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease,
    filter 0.15s ease;

  -webkit-tap-highlight-color: transparent;
}

/* 아이콘 */

.event-btn-icon {
  display: flex;

  align-items: center;
  justify-content: center;

  margin-right: 7px;

  font-size: 14px;

  color: #ffffff;
}

/* 텍스트 */

.event-btn-text {
  line-height: 1;

  white-space: nowrap;
}

/* 심플한 화살표 */

.event-btn-arrow {
  position: absolute;

  right: 16px;

  display: flex;
  align-items: center;

  color: #ffffff;

  font-size: 20px;
  font-weight: 400;

  line-height: 1;
}

/* hover */

.event-join-btn:hover {
  filter: brightness(1.04);

  box-shadow: 0 8px 18px rgba(255, 92, 154, 0.25);
}

/* 클릭 */

.event-join-btn:active {
  transform: scale(0.98);

  box-shadow: 0 4px 9px rgba(255, 92, 154, 0.18);
}

@media (max-width: 360px) {
  .event-join-btn {
    width: 92%;
  }
}

.ai-btn-arrow {
  position: absolute;

  right: 15px;

  display: flex;
  align-items: center;
  justify-content: center;

  font-size: 12px;

  opacity: 0.85;
}
.point-box-title {
  max-width: 90%;

  margin: 0;
  padding: 5px 12px;

  border-radius: 10px;

  font-size: 17px;
  font-weight: 800;

  line-height: 1.35;
  letter-spacing: -0.5px;

  color: #ffffff;

  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.18),
    rgba(255, 182, 215, 0.22)
  );

  text-shadow:
    0 1px 2px rgba(0, 0, 0, 0.75),
    0 2px 5px rgba(0, 0, 0, 0.45);

  backdrop-filter: blur(2px);
}
</style>
