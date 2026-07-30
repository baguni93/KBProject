// 만약 src/api/index.js가 있다면 자동으로 index.js를 찾아서 import합니다.
import api from '@/api';
const BASE_URL = '/api/settlements';
export default {
  async getMyList(params) {
    console.log(`${BASE_URL}/me`);
    const { data } = await api.get(`${BASE_URL}/me`, { params });
    console.log('GET My Settlements LIST:', data);
    return data;
  },
};
