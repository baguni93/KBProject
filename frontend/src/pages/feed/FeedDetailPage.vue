<template>
  <div class="notification-page">
    <PageHeader
      title="피드 상세보기"
      :custom-back="true"
      @back="goBackToFeed"
    />

    <main class="feed-detail">
      <!-- =========================
           피드
           ========================= -->
      <section v-if="feed" class="feed-section">
        <component :is="feedComponents[feed.feedType]" :feed="feed">
          <template #actions>
            <FeedActions
              :liked="feed.liked"
              :like-count="feed.likeCount"
              :comment-count="feed.commentCount"
              @comment="handleComment"
              @like="handleLike"
              @more="handleMore"
            />
          </template>
        </component>
      </section>
    </main>

    <!-- =========================
         댓글 Bottom Sheet
         ========================= -->
    <CommentBottomSheet
      v-model="openComment"
      :comments="feedStore.comments"
      @create="handleCreateComment"
      @delete="handleDeleteComment"
    />

    <!-- =========================
         피드 더보기 Bottom Sheet
         ========================= -->
    <FeedBottomSheet
      v-model="openFeedMenu"
      :is-mine="isSelectedFeedMine"
      @edit="handleEdit"
      @delete="handleDelete"
      @report="handleReport"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useFeedStore } from '@/stores/feed';
import { useAuthStore } from '@/stores/auth';
import { useModalStore } from '@/stores/userModalStore.js';

// =========================
// 피드 컴포넌트
// =========================
import PaymentFeed from './components/PaymentFeed.vue';
import EventFeed from './components/EventFeed.vue';
import AiAnalysisFeed from './components/AiAnalysisFeed.vue';
import CustomCardFeed from './components/CustomCardFeed.vue';
import TransferFeed from './components/TransferFeed.vue';
import SettlementFeed from './components/SettlementFeed.vue';

// =========================
// 공통 컴포넌트
// =========================
import CommentBottomSheet from '@/components/feed/CommentBottomSheet.vue';
import FeedBottomSheet from '@/components/feed/FeedBottomSheet.vue';
import FeedActions from './components/FeedActions.vue';
import PageHeader from '@/components/common/PageHeader.vue';

// =========================
// Router / Store
// =========================
const route = useRoute();
const router = useRouter();

const feedStore = useFeedStore();
const authStore = useAuthStore();
const modalStore = useModalStore();

const userId = authStore.userId;

// =========================
// 상태
// =========================
const feed = ref(null);

const selectedFeedId = ref(null);

const openComment = ref(false);
const openFeedMenu = ref(false);

// =========================
// 피드 타입별 컴포넌트
// =========================
const feedComponents = {
  CARD: CustomCardFeed,
  TRANSFER: TransferFeed,
  EVENT: EventFeed,
  PAYMENT: PaymentFeed,
  ANALYSIS: AiAnalysisFeed,
  SETTLEMENT: SettlementFeed,
};

// =========================
// 댓글
// =========================
const handleComment = async () => {
  if (!feed.value) {
    return;
  }

  selectedFeedId.value = feed.value.feedId;

  try {
    await feedStore.getComments(feed.value.feedId);

    openComment.value = true;
  } catch (e) {
    console.error('댓글 조회 실패:', e);
  }
};

// =========================
// 댓글 생성
// =========================
const handleCreateComment = async (content) => {
  if (!selectedFeedId.value) {
    return;
  }

  try {
    await feedStore.createComment({
      feedId: selectedFeedId.value,
      userId,
      content,
    });

    // 댓글 수 즉시 반영
    if (feed.value) {
      feed.value.commentCount = Number(feed.value.commentCount ?? 0) + 1;
    }
  } catch (e) {
    console.error('댓글 등록 실패:', e);
  }
};

// =========================
// 댓글 삭제
// =========================
const handleDeleteComment = async (commentId) => {
  const isConfirm = await modalStore.showConfirm('댓글을 삭제하시겠습니까?');

  if (!isConfirm) {
    return;
  }

  try {
    await feedStore.deleteComment(commentId, selectedFeedId.value);

    // 댓글 수 즉시 반영
    if (feed.value && Number(feed.value.commentCount) > 0) {
      feed.value.commentCount--;
    }
  } catch (e) {
    console.error('댓글 삭제 실패:', e);
  }
};

// =========================
// 좋아요
// =========================
const handleLike = async () => {
  if (!feed.value) {
    return;
  }

  try {
    await feedStore.toggleLike({
      feedId: feed.value.feedId,
      userId,
    });
    /*
     * toggleLike()에서 store의 피드 객체를
     * 직접 수정하지 않는 구조라면
     * 아래처럼 상세 피드도 직접 갱신해야 함.
     *
     * 현재 store 구현에 따라 필요하면 활성화.
     */

    feed.value.liked = !feed.value.liked;
    feed.value.likeCount += feed.value.liked ? 1 : -1;
  } catch (e) {
    console.error('좋아요 처리 실패:', e);
  }
};

// =========================
// 더보기
// =========================
const handleMore = () => {
  if (!feed.value) {
    return;
  }

  selectedFeedId.value = feed.value.feedId;

  openFeedMenu.value = true;
};

// =========================
// 내가 작성한 피드인지
// =========================
const isSelectedFeedMine = computed(() => {
  if (!feed.value) {
    return false;
  }

  return feed.value.userId === userId;
});

// =========================
// 피드 수정
// =========================
const handleEdit = () => {
  if (!selectedFeedId.value) {
    return;
  }

  router.push(`/feed/edit/${selectedFeedId.value}`);
};

// =========================
// 피드 삭제
// =========================
const handleDelete = async () => {
  if (!selectedFeedId.value) {
    return;
  }

  const isConfirm = await modalStore.showConfirm('피드를 삭제하시겠습니까?');

  if (!isConfirm) {
    return;
  }

  try {
    await feedStore.deleteFeed(selectedFeedId.value);

    // 삭제 후 이전 페이지로 이동
    router.back();
  } catch (e) {
    console.error('피드 삭제 실패:', e);
  }
};

// =========================
// 피드 신고
// =========================
const handleReport = async () => {
  await modalStore.showAlert('신고가 완료되었습니다.', '신고하기');

  console.log('신고할 피드:', selectedFeedId.value);
};

const goBackToFeed = () => {
  // router.replace({
  //   path: '/feed/public',
  //   query: {
  //     feedId: feed.value.feedId,
  //   },
  // });

  router.push('/feed');
};

// =========================
// 피드 조회
// =========================
onMounted(async () => {
  try {
    const feedId = route.params.feedId;

    feed.value = await feedStore.getFeed(feedId);

    // 피드가 존재하지 않는 경우
    if (!feed.value) {
      await modalStore.showAlert('삭제되었거나 존재하지 않는 피드입니다.');

      router.back();
      return;
    }

    selectedFeedId.value = feedId;

    console.log('피드 상세:', feed.value);
  } catch (e) {
    console.error('피드 상세 조회 실패:', e);

    await modalStore.showAlert('삭제되었거나 존재하지 않는 피드입니다.');

    router.back();
  }
});
</script>

<style scoped>
/* =========================
   전체 페이지
   ========================= */

.notification-page {
  width: 100%;
  min-height: 100%;

  padding: 0 20px 30px;

  background: #ffffff;

  box-sizing: border-box;
}

/* =========================
   피드 상세
   ========================= */

.feed-detail {
  display: flex;
  flex-direction: column;

  width: 100%;
}

/* =========================
   피드
   ========================= */

.feed-section {
  width: 100%;
}
</style>
