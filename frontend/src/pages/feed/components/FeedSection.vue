<template>
  <main class="feed-section">
    <!-- 새로고침 로딩 -->

    <div
      v-if="type === 'public' && feedStore.isRefreshing"
      class="refresh-loading-area"
    >
      <div class="loading-spinner"></div>
    </div>

    <component
      v-for="feed in feeds"
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

    <!-- 무한 스크롤 -->
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
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue';

// 피드 컴포넌트

import PaymentFeed from './PaymentFeed.vue';
import EventFeed from './EventFeed.vue';
import AiAnalysisFeed from './AiAnalysisFeed.vue';
import CustomCardFeed from './CustomCardFeed.vue';
import TransferFeed from './TransferFeed.vue';
import SettlementFeed from './SettlementFeed.vue';

// 피드 공통 컴포넌트
import CommentBottomSheet from '@/components/feed/CommentBottomSheet.vue';
import FeedBottomSheet from '@/components/feed/FeedBottomSheet.vue';
import FeedActions from './FeedActions.vue';
// Store
import { useFeedStore } from '@/stores/feed';
import { useAuthStore } from '@/stores/auth';
import { useModalStore } from '@/stores/userModalStore.js';

// Router
import { useRouter } from 'vue-router';

const router = useRouter();

const props = defineProps({
  type: {
    type: String,
    default: 'public',
  },

  memberUserId: {
    type: Number,
    default: null,
  },
});

const feeds = computed(() => {
  switch (props.type) {
    case 'my':
      return feedStore.myFeeds;

    case 'friend':
      return feedStore.friendFeeds;

    case 'member':
      return feedStore.memberFeeds;

    case 'public':
    default:
      return feedStore.publicFeeds;
  }
});

const feedStore = useFeedStore();
const authStore = useAuthStore();
const modalStore = useModalStore();

const userId = computed(() => authStore.userId);

// =====================================================
// 페이지네이션
// =====================================================

const page = ref(0);
const size = 3;

const isLoadingMore = ref(false);
const hasMore = ref(true);

const loadMoreTarget = ref(null);

let observer = null;

// =====================================================
// 피드 타입별 컴포넌트
// =====================================================

const feedComponents = {
  CARD: CustomCardFeed,
  TRANSFER: TransferFeed,
  EVENT: EventFeed,
  PAYMENT: PaymentFeed,
  ANALYSIS: AiAnalysisFeed,
  SETTLEMENT: SettlementFeed,
};

// =====================================================
// 무한 스크롤
// =====================================================

const loadFeeds = async (page) => {
  switch (props.type) {
    case 'my':
      return await feedStore.getMyList({
        page,
        size,
      });

    case 'friend':
      return await feedStore.getFriendList({
        page,
        size,
      });

    case 'member':
      return await feedStore.getMemberList({
        memberUserId: props.memberUserId,
        page,
        size,
      });

    case 'public':
    default:
      return await feedStore.getList({
        page,
        size,
      });
  }
};

const loadMoreFeeds = async () => {
  console.log(' loadMoreFeeds 진입', {
    isLoadingMore: isLoadingMore.value,
    hasMore: hasMore.value,
    page: page.value,
  });
  if (isLoadingMore.value || !hasMore.value) {
    console.log(' 다음 페이지 요청 막힘', {
      isLoadingMore: isLoadingMore.value,
      hasMore: hasMore.value,
    });
    return;
  }

  isLoadingMore.value = true;

  try {
    const nextPage = page.value + 1;
    // 로딩 UI 확인용
    await new Promise((resolve) => setTimeout(resolve, 1000));

    console.log('🔥 다음 페이지 요청:', {
      type: props.type,
      memberUserId: props.memberUserId,
      page: nextPage,
      size,
    });

    const result = await loadFeeds(nextPage);

    page.value = nextPage;

    if (result.length < size) {
      hasMore.value = false;
    }
  } catch (e) {
    console.error('다음 피드 조회 실패:', e);
  } finally {
    isLoadingMore.value = false;
  }
};

// =====================================================
// 댓글
// =====================================================

const selectedFeedId = ref(null);

const openComment = ref(false);

const handleComment = async (feedId) => {
  selectedFeedId.value = feedId;

  await feedStore.getComments(feedId);

  openComment.value = true;
};

const handleCreateComment = async (content) => {
  if (!selectedFeedId.value) {
    return;
  }

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

  if (!isConfirm) {
    return;
  }

  try {
    await feedStore.deleteComment(commentId, selectedFeedId.value);
  } catch (e) {
    console.error('댓글 삭제 실패', e);
  }
};

// =====================================================
// 좋아요
// =====================================================

const handleLike = async (feedId) => {
  try {
    await feedStore.toggleLike({
      feedId,
      userId: userId.value,
    });
  } catch (e) {
    console.error('좋아요 처리 실패', e);
  }
};

// =====================================================
// 더보기
// =====================================================

const openFeedMenu = ref(false);

const handleMore = (feedId) => {
  selectedFeedId.value = feedId;
  openFeedMenu.value = true;
};

// =====================================================
// 피드 수정
// =====================================================

const handleEdit = () => {
  router.push(`/feed/edit/${selectedFeedId.value}`);
};

// =====================================================
// 피드 삭제
// =====================================================

const handleDelete = async () => {
  const isConfirm = await modalStore.showConfirm('피드를 삭제하시겠습니까?');

  if (!isConfirm) {
    return;
  }

  try {
    await feedStore.deleteFeed(selectedFeedId.value);
  } catch (e) {
    console.error('피드 삭제 실패', e);
  }
};

// =====================================================
// 피드 신고
// =====================================================

const isSelectedFeedMine = computed(() => {
  const feed = feeds.value.find((feed) => feed.feedId === selectedFeedId.value);

  return feed?.userId === userId.value;
});

const handleReport = async () => {
  await modalStore.showAlert('신고가 완료되었습니다.', '신고하기');

  console.log('신고할 피드:', selectedFeedId.value);
};

// =====================================================
// 피드 초기화 / 새로고침
// =====================================================

const initFeed = async () => {
  page.value = 0;
  hasMore.value = true;

  const result = await loadFeeds(0);

  console.log(result);
  if (result.length < size) {
    hasMore.value = false;
  }
};

// 부모에서 refreshFeed() 호출 가능
defineExpose({
  refreshFeed: initFeed,
});

// =====================================================
// Pull To Refresh
// =====================================================

let wasScrolled = false;

const isInitialLoad = ref(true);

const handleScroll = () => {
  if (props.type !== 'public') {
    return;
  }

  const content = document.querySelector('.app-content');

  if (!content || isInitialLoad.value) {
    return;
  }

  if (content.scrollTop > 20) {
    wasScrolled = true;
  }

  if (content.scrollTop <= 0 && wasScrolled) {
    wasScrolled = false;

    feedStore.refreshList({
      page: 0,
      size,
    });
  }
};

// =====================================================
// Mounted
// =====================================================

onMounted(async () => {
  await initFeed();

  isInitialLoad.value = false;

  // 무한 스크롤 Observer
  observer = new IntersectionObserver(
    (entries) => {
      console.log('observer:', entries[0].isIntersecting);
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

  // Pull To Refresh
  const content = document.querySelector('.app-content');

  if (content) {
    content.addEventListener('scroll', handleScroll);
  }
});

// =====================================================
// Unmounted
// =====================================================

onBeforeUnmount(() => {
  observer?.disconnect();

  const content = document.querySelector('.app-content');

  if (content) {
    content.removeEventListener('scroll', handleScroll);
  }
});
</script>

<style scoped>
/* 피드 전체 */
.feed-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

/* 새로고침 로딩 */
.refresh-loading-area {
  display: flex;
  justify-content: center;
  align-items: center;

  width: 100%;
  height: 40px;
}

/* 무한 스크롤 로딩 영역 */
.load-more-area {
  display: flex;
  justify-content: center;
  align-items: center;

  width: 100%;
  min-height: 50px;
  padding: 10px 0;
}

/* 로딩 스피너 */
.loading-spinner {
  width: 22px;
  height: 22px;

  border: 2px solid #e5e7eb;
  border-top-color: #555;

  border-radius: 50%;

  animation: feed-spinner 0.7s linear infinite;
}

/* 모든 피드를 불러온 경우 */
.end-message {
  font-size: 13px;
  color: #9ca3af;
}

@keyframes feed-spinner {
  to {
    transform: rotate(360deg);
  }
}
</style>
