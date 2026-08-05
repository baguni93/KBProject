import api from '@/api';

const BASE_URL = '/api/friends';

export default {
  // 친구 목록 조회
  async getFriendList(userId) {
    const { data } = await api.get(BASE_URL, {
      params: {
        userId,
      },
    });

    return data;
  },

  // 친구 요청 목록 조회
  async getRequestList(userId) {
    const { data } = await api.get(`${BASE_URL}/requests`, {
      params: {
        userId,
      },
    });

    return data;
  },

  // 내가 보낸 친구 요청 목록 조회
  async getSendRequestList(userId) {
    const { data } = await api.get(`${BASE_URL}/sendRequests`, {
      params: {
        userId,
      },
    });

    return data;
  },

  // 친구요청
  async request(requesterId, receiverId) {
    const { data } = await api.post(`${BASE_URL}/request`, {
      requesterId,
      receiverId,
    });

    return data;
  },

  // 친구 요청 수락
  async accept(requestId) {
    const { data } = await api.patch(
      `${BASE_URL}/requests/${requestId}/accept`,
      null,
    );

    return data;
  },

  // 친구 요청 거절
  async reject(requestId) {
    const { data } = await api.patch(
      `${BASE_URL}/requests/${requestId}/reject`,
      null,
    );

    return data;
  },

  // 친구 요청 취소
  async cancel(requestId) {
    const { data } = await api.patch(
      `${BASE_URL}/requests/${requestId}/cancel`,
      null,
    );

    return data;
  },

  //친구 삭제
  async delete(userId, requestId) {
    const { data } = await api.delete(`${BASE_URL}/${requestId}`, {
      params: {
        userId,
      },
    });

    return data;
  },

  //친구 삭제
  async checkFriendStatus(userId, memberUserId) {
    const { data } = await api.get(
      `/api/friends/${memberUserId}/friendStatus`,
      {
        params: {
          userId: userId,
        },
      },
    );

    return data;
  },
};
