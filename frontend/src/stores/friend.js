import { defineStore } from 'pinia';
import { ref } from 'vue';
import friendApi from '@/api/friend.js';

export const useFriendStore = defineStore('friend', () => {
  // 내 친구 목록
  const friendList = ref([]);

  // 받은 친구 요청 목록
  const requestList = ref([]);

  // 내가 보낸 친구 요청 목록
  const sendRequestList = ref([]);

  const friendStatusVersion = ref(0);
  // ================================
  // 친구 목록 조회
  // ================================

  const getFriendList = async (userId) => {
    try {
      friendList.value = await friendApi.getFriendList(userId);
      console.log('친구 리스트:', friendList.value);
    } catch (e) {
      console.error('친구 리스트 조회 실패:', e);
      throw e;
    }
  };

  // ================================
  // 받은 친구 요청 목록
  // ================================

  const getRequestList = async (userId) => {
    try {
      requestList.value = await friendApi.getRequestList(userId);
      console.log('받은 친구 요청 리스트:', requestList.value);
    } catch (e) {
      console.error('친구 요청 리스트 조회 실패:', e);
      throw e;
    }
  };

  // ================================
  // 보낸 친구 요청 목록
  // ================================

  const getSendRequestList = async (userId) => {
    try {
      sendRequestList.value = await friendApi.getSendRequestList(userId);
      console.log('보낸 친구 요청 리스트:', sendRequestList.value);
    } catch (e) {
      console.error('보낸 친구 요청 리스트 조회 실패:', e);
      throw e;
    }
  };

  // ================================
  // 친구 요청
  // ================================

  const request = async (requesterId, receiverId) => {
    try {
      const data = await friendApi.request(requesterId, receiverId);

      console.log('친구 요청:', data);

      // 내가 보낸 요청 목록 갱신
      await getSendRequestList(requesterId);

      return data;
    } catch (e) {
      console.error('친구 요청 실패:', e);
      throw e;
    }
  };

  // ================================
  // 친구 요청 수락
  // ================================

  const accept = async (userId, requestId) => {
    try {
      const data = await friendApi.accept(requestId);

      console.log('친구 요청 수락:', data);

      await getFriendList(userId);
      await getRequestList(userId);

      return data;
    } catch (e) {
      console.error('친구 요청 수락 실패:', e);
      throw e;
    }
  };

  // ================================
  // 친구 요청 거절
  // ================================

  const reject = async (userId, requestId) => {
    try {
      const data = await friendApi.reject(requestId);

      console.log('친구 요청 거절:', data);

      await getRequestList(userId);

      return data;
    } catch (e) {
      console.error('친구 요청 거절 실패:', e);
      throw e;
    }
  };

  // ================================
  // 친구 요청 취소
  // ================================

  const cancel = async (userId, requestId) => {
    try {
      const data = await friendApi.cancel(requestId);

      console.log('친구 요청 취소:', data);

      await getSendRequestList(userId);

      return data;
    } catch (e) {
      console.error('친구 요청 취소 실패:', e);
      throw e;
    }
  };

  // ================================
  // 친구 삭제
  // ================================

  const deleteFriend = async (userId, friendUserId) => {
    try {
      const data = await friendApi.delete(userId, friendUserId);

      console.log('친구 삭제:', data);

      await getFriendList(userId);

      return data;
    } catch (e) {
      console.error('친구 삭제 실패:', e);
      throw e;
    }
  };

  // ================================
  // 친구 상태 조회
  // ================================

  const checkFriendStatus = async (userId, memberUserId) => {
    try {
      const data = await friendApi.checkFriendStatus(userId, memberUserId);

      console.log('친구 상태:', data);

      return data;
    } catch (e) {
      console.error('친구 상태 조회 실패:', e);
      throw e;
    }
  };

  const handleFriendEvent = async (event) => {
    console.log('친구 이벤트 처리:', event);

    friendStatusVersion.value++;

    switch (event.type) {
      case 'REQUEST':
        await getRequestList(event.receiverId);
        break;

      case 'ACCEPT':
        await getFriendList(event.receiverId);
        await getSendRequestList(event.receiverId);
        break;

      case 'REJECT':
        await getSendRequestList(event.receiverId);
        break;

      case 'CANCEL':
        await getRequestList(event.receiverId);
        break;

      case 'DELETE':
        await getFriendList(event.receiverId);
        break;
    }
  };

  return {
    friendList,
    requestList,
    sendRequestList,
    friendStatusVersion,

    getFriendList,
    getRequestList,
    getSendRequestList,
    checkFriendStatus,

    request,
    accept,
    reject,
    cancel,
    deleteFriend,

    handleFriendEvent,
  };
});
