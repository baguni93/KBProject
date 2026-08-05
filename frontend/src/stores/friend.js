import { defineStore } from 'pinia';
import { ref } from 'vue';
import friendApi from '@/api/friend.js';

export const useFriendStore = defineStore('friend', () => {
  // 내 친구 목록
  const friendList = ref([]);

  // 친구 요청 목록
  const requestList = ref([]);

  // 내가 친구 요청한 목록
  const sendRequestList = ref([]);

  // 친구 목록 조회
  const getFriendList = async (userId) => {
    try {
      friendList.value = await friendApi.getFriendList(userId);
      console.log('친구 리스트 :', friendList.value);
    } catch (e) {
      console.log(e);
    }
  };

  // 친구 요청 목록 조회
  const getRequestList = async (userId) => {
    try {
      requestList.value = await friendApi.getRequestList(userId);
      console.log('친구 요청 리스트 :', requestList.value);
    } catch (e) {
      console.log(e);
    }
  };

  // 내가 보낸 친구 요청 목록 조회
  const getSendRequestList = async (userId) => {
    try {
      sendRequestList.value = await friendApi.getSendRequestList(userId);
      console.log(' 보낸 친구 요청 리스트 :', sendRequestList.value);
    } catch (e) {
      console.log(e);
    }
  };

  // 친구 요청
  const request = async (requesterId, receiverId) => {
    try {
      const data = await friendApi.request(requesterId, receiverId);
      console.log('친구 요청:', data);
      await getRequestList(userId);
    } catch (e) {
      console.log(e);
    }
  };

  // 친구 요청 수락
  const accept = async (userId, requestId) => {
    try {
      const data = await friendApi.accept(requestId);
      console.log('친구 요청 수락 :', data);
      await getFriendList(userId);
      await getRequestList(userId);
    } catch (e) {
      console.log(e);
    }
  };

  // 친구 요청 거절
  const reject = async (userId, requestId) => {
    try {
      const data = await friendApi.reject(requestId);
      console.log('친구 요청 거절 :', data);
      await getRequestList(userId);
    } catch (e) {
      console.log(e);
    }
  };

  // 친구 요청 취소
  const cancel = async (userId, requestId) => {
    try {
      const data = await friendApi.cancel(requestId);
      console.log('친구 요청 취소 :', data);
      await getSendRequestList(userId);
    } catch (e) {
      console.log(e);
    }
  };

  // 친구 삭제
  const deleteFriend = async (userId, friendUserId) => {
    try {
      const data = await friendApi.delete(userId, friendUserId);
      console.log('친구 삭제 :', data);
      await getFriendList(userId);
    } catch (e) {
      console.log(e);
    }
  };

  // 친구 상태 조회
  const checkFriendStatus = async (userId, memberUserId) => {
    const data = await friendApi.checkFriendStatus(userId, memberUserId);
    console.log('친구 상태 :', data);

    return data;
  };

  return {
    friendList,
    requestList,
    sendRequestList,

    getFriendList,
    getRequestList,
    getSendRequestList,
    checkFriendStatus,
    request,
    accept,
    reject,
    cancel,
    deleteFriend,
  };
});
