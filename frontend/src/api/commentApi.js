import api from '@/api';

const BASE_URL = '/api/comments';

export default {
  // 댓글 조회
  async getList(feedId) {
    const { data } = await api.get(`${BASE_URL}/${feedId}`);

    console.log(data);

    return data;
  },

  // 댓글 생성

  async create(params) {
    const { data } = await api.post(`${BASE_URL}`, {
      feedId: params.feedId,
      userId: params.userId,
      content: params.content,
    });

    console.log(data);

    return data;
  },

  // 댓글 수정

  async update(params) {
    const { data } = await api.patch(`${BASE_URL}`, {
      commentId: params.commentId,
      content: params.content,
    });

    console.log(data);

    return data;
  },

  // 댓글 삭제

  async delete(commentId) {
    const { data } = await api.delete(`${BASE_URL}/${commentId}`);

    console.log(data);

    return data;
  },
};
