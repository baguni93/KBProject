import { defineStore } from 'pinia';
import { ref } from 'vue';
import feedApi from '@/api/feedApi';

export const useFeedStore = defineStore('feed', () => {
  // 공개 피드
  const publicFeeds = ref([]);
  // 친구 피드
  const friendFeeds = ref([]);
  // 내 피드
  const myFeeds = ref([]);
  //맴버 피드
  const memberFeeds = ref([]);

  const feed = ref({});

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
    } catch (e) {
      console.log(e);
    }
  };

  // 공개 피드 조회
  const getList = async (params) => {
    try {
      publicFeeds.value = await feedApi.getList(params);
    } catch (e) {
      console.log(e);
    }
  };

  // 내 피드 조회
  const getMyList = async (params) => {
    try {
      myFeeds.value = await feedApi.getMyList(params);

      return myFeeds.value;
    } catch (e) {
      console.log(e);
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
      memberFeeds.value = await feedApi.getMemberList(params);
      return memberFeeds.value;
    } catch (e) {
      console.log(e);
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

    return liked;
  };

  return {
    publicFeeds,
    friendFeeds,
    myFeeds,
    memberFeeds,

    deleteFeed,
    getFeed,
    getList,
    getMyList,
    getFriendList,
    getMemberList,

    updateLike,
    toggleLike,
  };
});
