import { useAuthStore } from '@/stores/auth';

export const isAuthenticated = (to) => {
  const authStore = useAuthStore();

  if (!authStore.isLogin) {
    // console.log('로그인 필요.....');
    return {
      path: '/intro',
      query: { next: to.fullPath },
    };
  }
  // console.log('로그인 인증 완료');

  return true;
};