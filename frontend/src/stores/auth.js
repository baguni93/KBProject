import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import axios from 'axios';
import { connectStomp, disconnectStomp } from '@/websocket';

const initState = {
  userId: null,
  userName: '',
  tokenType: '',
  accessToken: '',
  refreshToken: '',
  accessTokenExpiresIn: 0,
};

export const useAuthStore = defineStore('auth', () => {
  const state = ref({ ...initState });

  // 로그인 여부
  const isLogin = computed(
    () => !!state.value.accessToken && !!state.value.userId,
  );

  // 로그인 회원번호
  const userId = computed(() => state.value.userId);

  // 로그인 사용자 이름
  const userName = computed(() => state.value.userName);

  // 로그인 사용자 정보
  const user = computed(() => ({
    userId: state.value.userId,
    userName: state.value.userName,
  }));

  // JWT 정보 해석
  const decodeToken = (token) => {
    if (!token) return null;

    try {
      const payload = token.split('.')[1];
      const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/');
      const paddedPayload = normalizedPayload.padEnd(
        Math.ceil(normalizedPayload.length / 4) * 4,
        '=',
      );
      const decodedPayload = decodeURIComponent(
        atob(paddedPayload)
          .split('')
          .map(
            (character) =>
              `%${character.charCodeAt(0).toString(16).padStart(2, '0')}`,
          )
          .join(''),
      );

      return JSON.parse(decodedPayload);
    } catch (error) {
      console.error('JWT 정보를 확인할 수 없습니다.', error);
      return null;
    }
  };

  // 로그인 정보 저장
  const setAuth = (tokenData) => {
    const payload = decodeToken(tokenData.accessToken);
    const tokenUserId = Number(payload?.userId || tokenData.userId);

    state.value = {
      userId: tokenUserId || null,
      userName: tokenData.userName || state.value.userName || '',
      tokenType: tokenData.tokenType || 'Bearer',
      accessToken: tokenData.accessToken || '',
      refreshToken: tokenData.refreshToken || '',
      accessTokenExpiresIn: tokenData.accessTokenExpiresIn || 0,
    };

    localStorage.setItem('auth', JSON.stringify(state.value));
  };

  // 로그인 사용자 이름 저장
  const setUserName = (name) => {
    state.value.userName = name || '';
    localStorage.setItem('auth', JSON.stringify(state.value));
  };

  // PIN 로그인
  const login = async (loginData) => {
    const { data } = await axios.post('/api/login', loginData);

    setAuth(data);

    const payload = decodeToken(data.accessToken);
    const tokenUserId = Number(payload?.userId || data.userId);
    connectStomp(data.accessToken);

    return data;
  };

  // 재발급된 토큰 저장
  const updateTokens = (tokenData) => {
    setAuth({
      ...tokenData,
      userId: tokenData.userId || state.value.userId,
      userName: tokenData.userName || state.value.userName,
    });

    disconnectStomp();
    connectStomp(state.value.accessToken);
  };

  // 로그인 정보 삭제
  const clearAuth = () => {
    localStorage.removeItem('auth');
    state.value = { ...initState };
  };

  // 로그아웃
  const logout = async () => {
    const savedRefreshToken = state.value.refreshToken;

    try {
      if (savedRefreshToken)
        await axios.post('/api/logout', { refreshToken: savedRefreshToken });

      disconnectStomp();
    } catch (error) {
      console.error('로그아웃 요청에 실패했습니다.', error);
    } finally {
      clearAuth();
    }
  };

  // Access Token 반환
  const getToken = () => state.value.accessToken;

  // Refresh Token 반환
  const getRefreshToken = () => state.value.refreshToken;

  // 저장된 로그인 정보 불러오기
  const load = () => {
    const savedAuth = localStorage.getItem('auth');

    if (!savedAuth) return;

    try {
      const parsedAuth = JSON.parse(savedAuth);
      const payload = decodeToken(parsedAuth.accessToken);
      const tokenUserId = Number(payload?.userId || parsedAuth.userId);

      if (!parsedAuth.accessToken || !parsedAuth.refreshToken || !tokenUserId) {
        clearAuth();
        return;
      }

      state.value = {
        ...initState,
        ...parsedAuth,
        userId: tokenUserId,
        userName: parsedAuth.userName || '',
      };

      connectStomp(parsedAuth.accessToken);
    } catch (error) {
      console.error('저장된 로그인 정보를 불러오지 못했습니다.', error);
      clearAuth();
    }
  };

  load();

  return {
    state,
    user,
    userId,
    userName,
    isLogin,
    login,
    logout,
    clearAuth,
    setAuth,
    setUserName,
    updateTokens,
    getToken,
    getRefreshToken,
  };
});
