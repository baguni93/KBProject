import api from '@/api';

const BASE_URL = '/api/point-wallet';

export default {
  async getPoint() {
    const { data } = await api.get(`${BASE_URL}`);
    return data;
  },
};
