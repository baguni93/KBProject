import { isAuthenticated } from '@/util/guards';

export default [
  {
    path: '/mypage',
    name: 'mypage',
    component: () => import('../pages/mypage/MyPage.vue'),
    meta: {
      showBottomNav: true,
    },
  },
  {
    path: '/friends',
    name: 'friends',
    component: () => import('@/pages/friend/FriendPage.vue'),
  },
];
