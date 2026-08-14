<template>
  <div class="app-container">
    <!-- 1. 상단 검색바 & 스캔(그리드) 버튼 영역 -->
    <header class="header-section">
      <div class="search-bar" @click="handleSearchClick">
        <i class="fa-solid fa-magnifying-glass search-icon"></i>
        <span class="placeholder-text">사용자 검색</span>
      </div>

      <button
        class="icon-square-btn"
        type="button"
        aria-label="스캔 및 메뉴"
        @click="goToNotification"
      >
        <i class="fa-regular fa-bell"></i>
        <span
          v-if="notificationStore.hasUnread"
          class="notification-dot"
        ></span>
      </button>
    </header>

    <!-- 2. 프로모션 배너 영역 -->
    <section class="banner-section">
      <div class="banner-content">
        <h2 class="banner-title">
          Fast-forward payday.<br />
          Get paid up to two days<br />
          faster with Direct<br />
          Deposit.
        </h2>
        <button class="banner-btn" type="button">배너 액션</button>
      </div>
      <div class="banner-illustration">
        <div class="calendar-icon-box">📅</div>
      </div>
    </section>
    <p class="banner-subtext">
      Subject to bank/employer practices. Terms apply.
    </p>

    <!-- 3. 인스타그램 스타일 피드 리스트 영역 -->
    <main class="feed-section">
      <!-- 새로고침 로딩 -->
      <div v-if="feedStore.isRefreshing" class="refresh-loading-area">
        <div class="loading-spinner"></div>
      </div>

      <component
        v-for="feed in feedStore.publicFeeds"
        :key="feed.feedId"
        :is="feedComponents[feed.feedType]"
        :feed="feed"
      >
        <template #actions>
          <FeedActions
            :liked="feed.liked"
            :like-count="feed.likeCount"
            :comment-count="feed.commentCount"
            @comment="handleComment(feed.feedId)"
            @like="handleLike(feed.feedId)"
            @more="handleMore(feed.feedId)"
          />
        </template>
      </component>

      <!-- 무한 스크롤 감지 영역 -->
      <div ref="loadMoreTarget" class="load-more-area">
        <div v-if="isLoadingMore" class="loading-spinner"></div>
        <span v-else-if="!hasMore" class="end-message">
          모든 피드를 불러왔어요
        </span>
      </div>

      <CommentBottomSheet
        v-model="openComment"
        :comments="feedStore.comments"
        @create="handleCreateComment"
        @delete="handleDeleteComment"
      />
      <FeedBottomSheet
        v-model="openFeedMenu"
        :is-mine="isSelectedFeedMine"
        @edit="handleEdit"
        @delete="handleDelete"
        @report="handleReport"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue';
import PaymentFeed from './components/PaymentFeed.vue';
import EventFeed from './components/EventFeed.vue';
import AiAnalysisFeed from './components/AiAnalysisFeed.vue';
import CustomCardFeed from './components/CustomCardFeed.vue';
import TransferFeed from './components/TransferFeed.vue';
import SettlementFeed from './components/SettlementFeed.vue';
import CommentBottomSheet from '@/components/feed/CommentBottomSheet.vue';
import FeedBottomSheet from '@/components/feed/FeedBottomSheet.vue';
import FeedActions from './components/FeedActions.vue';

import { useNotificationStore } from '@/stores/notification';
const notificationStore = useNotificationStore();

import { useModalStore } from '@/stores/userModalStore.js';
const modalStore = useModalStore();

import { useRouter } from 'vue-router';
const router = useRouter();

import { useAuthStore } from '@/stores/auth';
const authStore = useAuthStore();
const userId = computed(() => authStore.userId);

import { useFeedStore } from '@/stores/feed';
const feedStore = useFeedStore();

// -- 알림
const goToNotification = () => {
  router.push('/notification');
};

//------------------------------페이지 네이션 --------------------------------------------//

const page = ref(0);
const size = 3;
const isLoadingMore = ref(false);
const hasMore = ref(true);

const loadMoreTarget = ref(null);

let observer = null;

//------------------------------페이지 네이션 --------------------------------------------//

const loadMoreFeeds = async () => {
  if (isLoadingMore.value || !hasMore.value) {
    return;
  }

  isLoadingMore.value = true;

  try {
    const nextPage = page.value + 1;
    // 로딩 UI 확인용
    await new Promise((resolve) => setTimeout(resolve, 800));
    const result = await feedStore.getList({
      page: nextPage,
      size: size,
    });

    page.value = nextPage;

    // 받아온 데이터가 size보다 적으면 마지막 페이지
    if (result.length < size) {
      hasMore.value = false;
    }
  } catch (e) {
    console.error('다음 피드 조회 실패', e);
  } finally {
    isLoadingMore.value = false;
  }
};

//----
const feedComponents = {
  CARD: CustomCardFeed,
  TRANSFER: TransferFeed,
  EVENT: EventFeed,
  PAYMENT: PaymentFeed,
  ANALYSIS: AiAnalysisFeed,
  SETTLEMENT: SettlementFeed,
};

const handleSearchClick = () => {
  router.push('/search');
};

const selectedFeedId = ref(null);
//--댓글 창
const openComment = ref(false);

const handleComment = async (feedId) => {
  selectedFeedId.value = feedId;

  await feedStore.getComments(feedId);

  openComment.value = true;
};

//--더보기 창
const openFeedMenu = ref(false);

const handleMore = (feedId) => {
  console.log(feedId);
  selectedFeedId.value = feedId;
  openFeedMenu.value = true;
};

// 댓글 액션

const handleCreateComment = async (content) => {
  if (!selectedFeedId.value) return;

  try {
    await feedStore.createComment({
      feedId: selectedFeedId.value,
      userId: userId.value,
      content,
    });
  } catch (e) {
    console.error('댓글 등록 실패', e);
  }
};

const handleDeleteComment = async (commentId) => {
  const isConfirm = await modalStore.showConfirm('댓글을 삭제하시겠습니까?');

  if (!isConfirm) return;

  try {
    await feedStore.deleteComment(commentId, selectedFeedId.value);
  } catch (e) {
    console.error('댓글 삭제 실패', e);
  }
};

//-- 좋아요

const handleLike = async (feedId) => {
  try {
    await feedStore.toggleLike({
      feedId: feedId,
      userId: userId.value,
    });
  } catch (e) {
    console.log(e);
  }
};

//-- 더보기
const handleEdit = () => {
  console.log('수정할 피드:', selectedFeedId.value);
  router.push(`/feed/edit/${selectedFeedId.value}`);
};

const handleDelete = async () => {
  const isConfirm = await modalStore.showConfirm('피드를 삭제하시겠습니까?');

  if (!isConfirm) {
    return;
  }

  try {
    await feedStore.deleteFeed(selectedFeedId.value);
  } catch (e) {
    console.log(e);
  }
};

// 신고하기
const isSelectedFeedMine = computed(() => {
  const feed = feedStore.publicFeeds.find(
    (feed) => feed.feedId === selectedFeedId.value,
  );

  return feed?.userId === userId.value;
});

const handleReport = async () => {
  await modalStore.showAlert('신고가 완료되었습니다.', '신고하기');
  console.log('신고할 피드:', selectedFeedId.value);
};

const initFeed = async () => {
  page.value = 0;
  hasMore.value = true;

  await feedStore.getList({
    page: page.value,
    size: size,
  });
};

defineExpose({
  refreshFeed: initFeed,
});

// 위로 당겻을때

let wasScrolled = false;

const handleScroll = () => {
  const content = document.querySelector('.app-content');

  if (!content || isInitialLoad.value) {
    return;
  }

  // 한 번이라도 아래로 스크롤했다는 기록
  if (content.scrollTop > 20) {
    wasScrolled = true;
  }

  // 아래로 갔다가 다시 최상단에 도착
  if (content.scrollTop <= 0 && wasScrolled) {
    wasScrolled = false;

    feedStore.refreshList({
      page: 0,
      size: 3,
    });
  }
};

//이지가 처음 로드될 때 scrollTop이 0이니까 새로고침 API가 한 번 더 호출될 수 있다 방지
const isInitialLoad = ref(true);

onMounted(async () => {
  await initFeed();
  isInitialLoad.value = false;

  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) {
        loadMoreFeeds();
      }
    },
    {
      root: document.querySelector('.app-content'),
      rootMargin: '100px',
      threshold: 0,
    },
  );

  if (loadMoreTarget.value) {
    observer.observe(loadMoreTarget.value);
  }

  const content = document.querySelector('.app-content');

  if (content) {
    content.addEventListener('scroll', handleScroll);
  }
});

onBeforeUnmount(() => {
  observer?.disconnect();

  const content = document.querySelector('.app-content');

  if (content) {
    content.removeEventListener('scroll', handleScroll);
  }
});
</script>

<style scoped>
/* 전체 화면 컨테이너 */
.app-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  background: var(--color-bg-screen);
  min-height: 100vh;
  box-sizing: border-box;
  padding: 16px 16px 20px 16px;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 1. 상단 검색바 & 우측 버튼 (Grid로 위치 고정) */
.header-section {
  display: grid;
  grid-template-columns: 1fr 48px;
  align-items: center;
  gap: 10px;
  width: 100%;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 48px;
  padding: 0 16px;
  background: #e2edf6;
  border-radius: 24px;
  cursor: pointer;
  box-sizing: border-box;
}

.search-icon {
  color: #555;
  font-size: 16px;
  flex-shrink: 0;
}

.placeholder-text {
  color: #667085;
  font-size: 15px;
  font-weight: 500;
}

.icon-square-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: #e2edf6;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  box-sizing: border-box;
  flex-shrink: 0;
}

.icon-square-btn i {
  color: #333;
  font-size: 18px;
}

/* 2. 프로모션 배너 */
.banner-section {
  background: #ffffff;
  border-radius: 20px;
  padding: 24px 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
}

.banner-title {
  font-size: 18px;
  font-weight: 700;
  color: #111;
  line-height: 1.35;
  margin: 0 0 16px 0;
}

.banner-btn {
  background: #0085ff;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 10px 18px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.banner-illustration {
  width: 80px;
  height: 80px;
  background: #e0f2fe;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.banner-subtext {
  font-size: 11px;
  color: #8892a0;
  margin: 8px 4px 24px 4px;
}

/* 3. 인스타그램 스타일 피드 섹션 */
.feed-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 12px;
}

.load-more-area {
  display: flex;
  justify-content: center;
  align-items: center;

  width: 100%;
  height: 50px;
}

.loading-spinner {
  width: 22px;
  height: 22px;

  border: 2px solid #e5e7eb;
  border-top-color: #555;

  border-radius: 50%;

  animation: feed-spinner 0.7s linear infinite;
}
.refresh-loading-area {
  display: flex;
  justify-content: center;
  align-items: center;

  width: 100%;
  height: 40px;
}

@keyframes feed-spinner {
  to {
    transform: rotate(360deg);
  }
}

.load-more-area {
  display: flex;
  justify-content: center;
  align-items: center;

  width: 100%;
  min-height: 50px;
  padding: 10px 0;
}

.end-message {
  font-size: 13px;
  color: #9ca3af;
}
</style>
