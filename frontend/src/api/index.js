import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

const instance = axios.create({
  timeout: 1000,
});

const authStore = useAuthStore();

// 요청 인터셉터
//instance.interceptors.request.use(onSuccess, onError)
instance.interceptors.request.use(
  (config) => {
    const token = authStore.getToken();

    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
      console.log(config.headers.Authorization);
    }

    // config.headers : 요청 헤더
    return config;
  },
  (error) => {
    // 요청중 에러가난 경우
    return Promise.reject(error);
  },
);
// 응답 인터셉터
instance.interceptors.response.use(
  (response) => {
    if (response.status === 200) {
      return response;
    }

    if (response.status === 404) {
      return Promise.reject('404: 페이지 없음 ' + response.request);
    }

    return response;
  },

  async (error) => {
    if (error.response?.status === 401) {
      authStore.logout();

      router.push('/auth/login?error=login_required');

      return Promise.reject({ error: '로그인이 필요한 서비스입니다.' });
    }

    return Promise.reject(error);
  },
);

export default instance;
