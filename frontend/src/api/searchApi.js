import api from '@/api';

const BASE_URL = '/api/member/search';

export default {
  async searchMember({ keyword, userId }) {
    const { data } = await api.get(`${BASE_URL}`, {
      params: {
        keyword,
        userId,
      },
    });

    return data;
  },
};
