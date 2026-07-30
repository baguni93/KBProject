import { isAuthenticated } from '@/util/guards';

export default [
  {
    path: '/mypage',
    component: () => import('../pages/mypage/MyPage.vue'),
    // children: [
    //   {
    //     path: '',
    //     redirect: { name: 'myfeed' },
    //   },

    //   {
    //     path: 'myfeed',
    //     name: 'myfeed',
    //     component: () => import('../pages/mypage/MyFeed.vue'),
    //   },

    //   {
    //     path: 'mywallet',
    //     name: 'mywallet',
    //     component: () => import('../pages/mypage/MyWallet.vue'),
    //   },
    // ],
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
