<template>
  <article class="feed-item insta-feed-item">
    <!-- 정산 헤더 -->
    <div class="feed-header">
      <div class="dual-avatar-container" @click="handleMemberPage">
        <!-- 요청자 -->
        <div class="avatar sender">
          <img
            v-if="feed.settlement?.sender?.profileImageName"
            :src="`/api/feeds/profile/${feed.settlement.sender.profileImageName}`"
            :alt="feed.settlement.sender.nickname"
          />

          <i v-else class="fa-solid fa-user"></i>
        </div>

        <!-- 정산 멤버 -->
        <div
          v-for="(member, index) in visibleMembers"
          :key="member.settlementMemberId"
          class="avatar settlement-member"
          :class="`member-${index + 1}`"
        >
          <img
            v-if="member.receiver?.profileImageName"
            :src="`/api/feeds/profile/${member.receiver.profileImageName}`"
            :alt="member.receiver.nickname"
          />

          <i v-else class="fa-solid fa-user"></i>
        </div>

        <!-- 4명 초과 -->
        <div
          v-if="feed.settlement?.members?.length > 4"
          class="avatar extra-member"
        >
          +{{ feed.settlement.members.length - 4 }}
        </div>
      </div>

      <div class="feed-info">
        <p class="feed-user-text">
          <strong>{{ feed.settlement?.sender?.nickname }}</strong>
        </p>

        <span class="feed-time">
          {{ formatRelativeDate(feed.createdAt) }}
          <i :class="icon"></i>
          {{ text }}
        </span>
      </div>

      <!-- 이미지가 있을 때만 토글 표시 -->
      <button
        v-if="feed.images?.length"
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
      <div v-if="showImages && feed.images?.length" class="image-wrapper">
        <FeedImageSlider :images="feed.images" />
      </div>
    </Transition>

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
import { computed, ref } from 'vue';
import FeedTags from './FeedTags.vue';
import FeedImageSlider from '@/components/feed/FeedImageSlider.vue';
import { formatRelativeDate } from '@/util/data';

import { useRouter } from 'vue-router';
const router = useRouter();

import { useAuthStore } from '@/stores/auth.js';
const authStore = useAuthStore();
const myUserId = authStore.userId;

import config from '@/config/feed';

const visibilityMap = config.visibilityMap;

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

/* 이미지 표시 여부 */
const showImages = ref(true);

/* 공개 범위 아이콘 */
const icon = computed(() => {
  return visibilityMap[props.feed.visibility]?.icon ?? '';
});

/* 공개 범위 텍스트 */
const text = computed(() => {
  return visibilityMap[props.feed.visibility]?.text ?? '';
});

/* 최대 4명까지만 프로필 표시 */
const visibleMembers = computed(() => {
  return props.feed.settlement?.members?.slice(0, 4) ?? [];
});

const handleMemberPage = () => {
  if (props.feed.userId === myUserId) {
    router.push('/mypage');
    return;
  }
  router.push(`/member/${props.feed.userId}`);
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

  // 카테고리
  if (typeConfig.CATEGORY && props.feed.transaction?.category?.categoryName) {
    result.push({
      icon: typeConfig.CATEGORY.icon,
      value: props.feed.transaction?.category?.categoryName,
    });
  }

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
   정산 헤더
========================= */

.feed-header {
  display: flex;
  align-items: center;

  gap: 10px;

  margin-bottom: 12px;
}

/* =========================
   프로필 전체 영역
========================= */

.dual-avatar-container {
  display: flex;

  position: relative;

  width: 70px;
  height: 40px;

  flex-shrink: 0;
}

/* =========================
   모든 프로필
========================= */

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;

  overflow: hidden;

  border-radius: 50%;
  border: 2px solid #ffffff;

  box-sizing: border-box;

  color: #94a3b8;

  background: #cbd5e1;

  position: absolute;
}

/* 프로필 이미지 */

.avatar img {
  width: 100%;
  height: 100%;

  object-fit: cover;

  display: block;
}

/* =========================
   요청자
========================= */

.avatar.sender {
  left: 0;
  top: 0;

  width: 36px;
  height: 36px;

  z-index: 5;

  background: #cbd5e1;
}

/* =========================
   정산 멤버
========================= */

.avatar.member-1 {
  left: 20px;
  top: 4px;

  width: 32px;
  height: 32px;

  z-index: 4;

  background: #94a3b8;
}

.avatar.member-2 {
  left: 36px;
  top: 4px;

  width: 32px;
  height: 32px;

  z-index: 3;

  background: #64748b;
}

.avatar.member-3 {
  left: 52px;
  top: 4px;

  width: 32px;
  height: 32px;

  z-index: 2;

  background: #475569;
}

.avatar.member-4 {
  left: 68px;
  top: 4px;

  width: 32px;
  height: 32px;

  z-index: 1;

  background: #334155;
}

/* =========================
   5명 이상
========================= */

.avatar.extra-member {
  left: 68px;
  top: 4px;

  width: 32px;
  height: 32px;

  background: #0085ff;

  color: #ffffff;

  font-size: 11px;
  font-weight: 700;

  z-index: 6;
}

/* =========================
   헤더 정보
========================= */

.feed-info {
  min-width: 0;

  flex: 1;
}

.feed-user-text {
  margin: 0;

  font-size: 14px;

  color: #111827;

  line-height: 1.4;
}

.feed-user-text strong {
  font-weight: 700;
}

.feed-time {
  display: flex;
  align-items: center;

  gap: 5px;

  margin-top: 3px;

  font-size: 10px;

  color: #94a3b8;
}

/* =========================
   이미지 토글
========================= */

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

/* 토글 동그라미 */

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

/* 클릭 시 살짝 축소 */

.image-toggle-btn:active .toggle-knob {
  transform: scale(0.9);
}

.image-toggle-btn.active:active .toggle-knob {
  transform: translateX(16px) scale(0.9);
}

/* =========================
   이미지 영역
========================= */

.image-wrapper {
  width: 100%;

  overflow: hidden;

  border-radius: 12px;

  margin-bottom: 12px;
}

/* =========================
   이미지 접기 / 펼치기
========================= */

.image-collapse-enter-active,
.image-collapse-leave-active {
  transition:
    max-height 0.35s ease,
    opacity 0.25s ease,
    margin 0.35s ease;
}

/* 닫히는 상태 */

.image-collapse-enter-from,
.image-collapse-leave-to {
  max-height: 0;

  opacity: 0;

  margin-top: 0;
  margin-bottom: 0;
}

/* 열려있는 상태 */

.image-collapse-enter-to,
.image-collapse-leave-from {
  max-height: 1000px;

  opacity: 1;

  margin-bottom: 12px;
}

/* =========================
   이미지
========================= */

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

/* =========================
   내용
========================= */

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
