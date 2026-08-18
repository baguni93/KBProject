import { defineStore } from 'pinia';
import { ref } from 'vue';
import feedApi from '@/api/feedApi';
import { useAuthStore } from './auth';
import commentApi from '@/api/commentApi';

export const useFeedStore = defineStore('feed', () => {
  const authStore = useAuthStore();

  // 공개 피드
  const publicFeeds = ref([]);
  // 친구 피드
  const friendFeeds = ref([]);
  // 내 피드
  const myFeeds = ref([]);
  //맴버 피드
  const memberFeeds = ref([]);

  const comments = ref([]);

  const feed = ref({});

  const isRefreshing = ref(false);

  // 피드 목록 스크롤 위치
  const savedScrollPosition = ref(0);

  const createRequestDTO = ({ feedType, visibility, content, targetId }) => {
    {
      const formData = new FormData();

      formData.append('userId', authStore.userId);
      formData.append('targetId', targetId);
      formData.append('feedType', feedType);
      formData.append('content', content);
      formData.append('visibility', visibility);

      return formData;
    }
  };

  const createFeed = async (formData) => {
    try {
      const data = await feedApi.createFeed(formData);
      console.log(data);
    } catch (e) {
      console.log(e);
    }
  };

  // 공개 피드 조회
  const getFeed = async (feedId) => {
    try {
      const data = await feedApi.getFeed(feedId);
      feed.value = data;

      return feed.value;
    } catch (e) {
      console.log(e);
    }
  };

  const deleteFeed = async (feedId) => {
    try {
      await feedApi.delete(feedId);

      publicFeeds.value = publicFeeds.value.filter(
        (feed) => feed.feedId !== feedId,
      );

      friendFeeds.value = friendFeeds.value.filter(
        (feed) => feed.feedId !== feedId,
      );

      myFeeds.value = myFeeds.value.filter((feed) => feed.feedId !== feedId);

      memberFeeds.value = memberFeeds.value.filter(
        (feed) => feed.feedId !== feedId,
      );
    } catch (e) {
      console.error('피드 삭제 실패:', e);
      throw e;
    }
  };

  // 공개 피드 조회
  const getList = async (params) => {
    try {
      const result = await feedApi.getList(params);

      if (params.page === 0) {
        publicFeeds.value = result;
      } else {
        publicFeeds.value.push(...result);
      }
      console.log(result);
      return result;
    } catch (e) {
      console.log(e);
      throw e;
    }
  };

  // 공개 피드 새로고침
  const refreshList = async (params) => {
    if (isRefreshing.value) {
      return;
    }

    isRefreshing.value = true;
    const startTime = Date.now();
    try {
      const result = await feedApi.getList(params);

      // API가 너무 빨리 끝나도 최소 800ms 동안 로딩 표시
      const elapsedTime = Date.now() - startTime;
      const remainingTime = Math.max(0, 800 - elapsedTime);

      if (remainingTime > 0) {
        await new Promise((resolve) => {
          setTimeout(resolve, remainingTime);
        });
      }

      const mergedFeeds = [...result, ...publicFeeds.value];

      // feedId 기준 중복 제거
      publicFeeds.value = Array.from(
        new Map(mergedFeeds.map((feed) => [feed.feedId, feed])).values(),
      );

      console.log('새로 추가된 피드:', result);

      return result;
    } catch (e) {
      console.log(e);
      throw e;
    } finally {
      isRefreshing.value = false;
    }
  };

  // 내 피드 조회
  const getMyList = async (params) => {
    try {
      const result = await feedApi.getMyList(params);

      if (params.page === 0) {
        myFeeds.value = result;
      } else {
        myFeeds.value.push(...result);
      }

      return result;
    } catch (e) {
      console.error('내 피드 조회 실패:', e);
      throw e;
    }
  };

  // 친구 피드 조회 (나중에)
  const getFriendList = async (params) => {
    try {
      friendFeeds.value = await feedApi.getFriendList(params);

      return friendFeeds.value;
    } catch (e) {
      console.log(e);
    }
  };

  // 맴버 피드 조회
  const getMemberList = async (params) => {
    try {
      const result = await feedApi.getMemberList(params);

      if (params.page === 0) {
        memberFeeds.value = result;
      } else {
        memberFeeds.value.push(...result);
      }

      return result;
    } catch (e) {
      console.error('멤버 피드 조회 실패:', e);
      throw e;
    }
  };

  // 좋아요 상태 변경
  const updateLike = (feeds, feedId, liked) => {
    const feed = feeds.find((feed) => feed.feedId === feedId);

    if (!feed) return;

    if (feed.liked === liked) return;

    if (liked) {
      feed.likeCount++;
    } else {
      feed.likeCount--;
    }

    feed.liked = liked;
  };

  // 좋아요 요청
  const toggleLike = async (params) => {
    const liked = await feedApi.toggleLike(params);

    updateLike(publicFeeds.value, params.feedId, liked);
    updateLike(friendFeeds.value, params.feedId, liked);
    updateLike(myFeeds.value, params.feedId, liked);
    updateLike(memberFeeds.value, params.feedId, liked);

    return liked;
  };

  const getComments = async (feedId) => {
    if (feedId == null) {
      return [];
    }

    try {
      const data = await commentApi.getList(feedId);

      comments.value = data ?? [];

      return comments.value;
    } catch (e) {
      console.error('댓글 조회 실패:', e);
      throw e;
    }
  };

  const findFeed = (feedId) => {
    return (
      publicFeeds.value.find((feed) => feed.feedId === feedId) ||
      friendFeeds.value.find((feed) => feed.feedId === feedId) ||
      myFeeds.value.find((feed) => feed.feedId === feedId) ||
      memberFeeds.value.find((feed) => feed.feedId === feedId)
    );
  };

  const createComment = async (params) => {
    try {
      const data = await commentApi.create(params);

      comments.value.unshift(data);

      const targetFeed = findFeed(params.feedId);

      if (targetFeed) {
        targetFeed.commentCount++;
      }

      return data;
    } catch (e) {
      console.error('댓글 등록 실패:', e);
      throw e;
    }
  };

  const deleteComment = async (commentId, feedId) => {
    try {
      await commentApi.delete(commentId);

      comments.value = comments.value.filter(
        (comment) => comment.commentId !== commentId,
      );

      const targetFeed = findFeed(feedId);

      if (targetFeed && targetFeed.commentCount > 0) {
        targetFeed.commentCount--;
      }
    } catch (e) {
      console.error('댓글 삭제 실패:', e);
      throw e;
    }
  };

  return {
    publicFeeds,
    friendFeeds,
    myFeeds,
    memberFeeds,
    comments,
    isRefreshing,

    savedScrollPosition,

    createRequestDTO,
    createFeed,
    deleteFeed,
    getFeed,
    getList,
    refreshList,
    getMyList,
    getFriendList,
    getMemberList,
    getComments,
    updateLike,
    toggleLike,
    createComment,
    deleteComment,
  };
});
