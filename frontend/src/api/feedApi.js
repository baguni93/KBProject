import api from '@/api';

const BASE_URL = '/api/feeds';
const headers = { 'Content-Type': 'multipart/form-data' };

export default {
  async createFeed(formData) {
    const { data } = await api.post(`${BASE_URL}`, formData, { headers });

    return data;
  },

  //  피드 조회
  async getFeed(feedId) {
    const { data } = await api.get(`${BASE_URL}/${feedId}`);

    return data;
  },

  // 전체 피드 조회
  async getList(params) {
    const { data } = await api.get(BASE_URL, {
      params,
    });
    return data;
  },

  // 내 피드 조회
  async getMyList(params) {
    const { data } = await api.get(`${BASE_URL}/me`, {
      params,
    });

    return data;
  },

  // 회원 피드 조회
  async getMemberList(params) {
    const { memberUserId, ...queryParams } = params;

    const { data } = await api.get(`${BASE_URL}/member/${memberUserId}`, {
      params: queryParams,
    });

    return data;
  },

  async updateFeed(formData) {
    const { data } = await api.put(BASE_URL, formData, {
      headers,
    });

    return data;
  },

  async delete(feedId) {
    const { data } = await api.patch(`${BASE_URL}/${feedId}`, null);
    return data;
  },

  // 피드 좋아요 토글
  async toggleLike(params) {
    const { data } = await api.post(`/api/like/${params.feedId}`, null, {
      params: {
        userId: params.userId,
      },
    });

    return data;
  },
};
