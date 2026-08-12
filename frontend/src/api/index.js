import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

const instance = axios.create({ timeout: 10000 });

// 요청 인터셉터
instance.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    const token = authStore.getToken();

    if (token) config.headers.Authorization = `Bearer ${token}`;

    return config;
  },
  (error) => Promise.reject(error),
);

// 응답 인터셉터
instance.interceptors.response.use(
  (response) => {
    if (response.status === 200) return response;

    return response;
  },

  async (error) => {
    const authStore = useAuthStore();
    const originalRequest = error.config;
    const refreshToken = authStore.getRefreshToken();

    // 404 응답 처리
    if (error.response?.status === 404) {
      return Promise.reject({
        // 💡 서버가 보낸 진짜 데이터(CustomException의 JSON)가 있다면 그것을 우선 사용하고, 없으면 기본 문구 사용
        error:
          error.response?.data?.message || '요청한 페이지를 찾을 수 없습니다.',
        code: error.response?.data?.code, // 에러 코드도 필요하면 함께 보존
        originalError: error,
        response: error.response, // response 객체 통째로 보존
      });
    }

    // Access Token 만료 시 토큰 재발급
    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      refreshToken &&
      originalRequest.url !== '/api/refresh'
    ) {
      originalRequest._retry = true;

      try {
        const { data } = await axios.post('/api/refresh', { refreshToken });

        authStore.updateTokens(data);

        originalRequest.headers = originalRequest.headers || {};
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;

        return instance(originalRequest);
      } catch (refreshError) {
        authStore.clearAuth();

        await router.replace({
          path: '/intro',
          query: { error: 'login_required' },
        });

        return Promise.reject(refreshError);
      }
    }

    // Refresh Token이 없는 상태에서 401 발생
    if (error.response?.status === 401) {
      authStore.clearAuth();

      await router.replace({
        path: '/intro',
        query: { error: 'login_required' },
      });

      return Promise.reject({
        error: '로그인이 필요한 서비스입니다.',
        originalError: error,
      });
    }

    return Promise.reject(error);
  },
);

export default instance;
