import api from '@/api';

const BASE_URL = '/api/member';

export default {
  async searchMember({ keyword, userId }) {
    const { data } = await api.get(`${BASE_URL}/search`, {
      params: {
        keyword,
        userId,
      },
    });

    return data;
  },
};
