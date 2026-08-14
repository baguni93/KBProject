<template>
  <div class="page">
    <!-- 유니티 UI 배경처럼 페이지 맨 뒤에 깔리는 이미지 -->
    <img
      class="dum"
      src="C:\KB_Fullstack\finalproject\KBProject\frontend\src\pages\feed\dum.PNG"
      alt="background"
    />

    <!-- 검색 -->
    <SearchBar />

    <InviteBanner />
    <!-- 피드 -->
    <main class="content-area">
      <FeedList />
    </main>
  </div>
</template>

<script setup>
import FeedList from '@/components/feed/FeedList.vue';
import SearchBar from '@/components/search/SearchBar.vue';
import InviteBanner from '@/components/common/InviteBanner.vue';
import { onMounted } from 'vue';
import { useFeedStore } from '@/stores/feed';
import { useAuthStore } from '@/stores/auth';
import HeaderButtons from '@/components/common/HeaderButtons.vue';

const feedStore = useFeedStore();
const authStore = useAuthStore();
const userId = authStore.userId;

//JWT 처리
onMounted(() => {
  const userId = authStore.userId || 1;
  feedStore.getList({
    userId,
  });
});
</script>

<style scoped>
/* 페이지 영역 */
.page {
  position: relative; /* 배경 이미지를 가두기 위한 기준점 */
  width: 100%;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  padding: 12px;
  background-color: white;
}

/* 작성하신 .dum 클래스 스타일 완성 */
.dum {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%); /* 정확히 정가운데로 이동 */
  width: 100%;
  height: 100%;
  object-fit: contain; /* 혹은 전체가 보이게 하고 싶다면 contain */
  object-position: center; /* 이미지의 중심을 맞춤 */
  opacity: 0.6;
  z-index: 1;
  pointer-events: none;
  -webkit-user-drag: none;
}

/* 배경 이미지(z-index: 0)보다 위로 오도록 자식 요소들에 z-index 부여 */
.page > *:not(.dum) {
  position: relative;
  z-index: 1;
}

:deep(.search-bar) {
  margin-bottom: 14px;
}

.content-area {
  flex: 1;
  padding: 13px 5px 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
