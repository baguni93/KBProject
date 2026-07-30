// 만약 src/api/index.js가 있다면 자동으로 index.js를 찾아서 import합니다.
import api from '@/api';
const BASE_URL = '/api/feeds';
export default {
  async getList(params) {
    const { data } = await api.get(BASE_URL, { params });
    console.log('GET LIST:', data);
    return data;
  },

  async getMyList(params) {
    console.log(`${BASE_URL}/me`);
    const { data } = await api.get(`${BASE_URL}/me`, { params });
    console.log('GET MY LIST:', data);
    return data;
  },
};
