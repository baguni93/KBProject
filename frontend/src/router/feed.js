import { isAuthenticated } from '@/util/guards';

export default [
  {
    path: '/feed',
    name: 'feed',
    component: () => import('../pages/feed/FeedMainPage.vue'),
    meta: {
      showBottomNav: true,
    },
  },
  {
    path: '/feed/edit/:feedId',
    name: 'feed/edit/:feedId',
    component: () => import('@/pages/feed/FeedEditPage.vue'),
  },
  {
    path: '/feed/detail/:feedId',
    name: 'feed/detail/:feedId',
    component: () => import('@/pages/feed/FeedDetailPage.vue'),
  },
  //   {
  //     path: '/feed/detail/:no',
  //     name: 'feed/detail',
  //     component: () => import('../pages/board/BoardDetailPage.vue'),
  //     beforeEnter: isAuthenticated,
  //   },
  //   {
  //     path: '/feed/create',
  //     name: 'feed/create',
  //     component: () => import('../pages/board/BoardCreatePage.vue'),
  //     beforeEnter: isAuthenticated,
  //   },
  //   {
  //     path: '/feed/update/:no',
  //     name: 'feed/update',
  //     component: () => import('../pages/board/BoardUpdatePage.vue'),
  //     beforeEnter: isAuthenticated,
  //   },
];
