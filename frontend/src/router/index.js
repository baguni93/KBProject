import { createRouter, createWebHistory } from 'vue-router';
import feed from './feed';
import mypage from './mypage';
import settlement from './settlement';
import auth from './auth';
import signup from './signup';
import setting from './setting';
import pagesample from './pagesample';
import analysis from './analysis';
import customcard from './customcard';

import WalletPage from '@/pages/wallet/WalletPage.vue';
import RemittancePage from '@/pages/remittance/RemittancePage.vue';
import TransactionListPage from '@/pages/transaction/TransactionListPage.vue';
import PointWalletPage from '@/pages/pointwallet/PointWalletPage.vue';
import RandomBoxPage from '@/pages/pointwallet/RandomBoxPage.vue';
import PointConversionPage from '@/pages/pointwallet/PointConversionPage.vue';
import PointTransactionPage from '@/pages/pointwallet/PointTransactionPage.vue';
import NotificationView from '@/pages/notification/NotificationView.vue';
import MemberDetailPage from '@/pages/member/MemberDetailPage.vue';
import NicknamePage from '@/pages/signup/NicknamePage.vue';
import SignupCompletePage from '@/pages/signup/SignupCompletePage.vue';
import EventPage from '@/pages/event/EventPage.vue';
import EventListPage from '@/pages/event/EventListPage.vue';
import FinancePage from '@/pages/finance/FinancePage.vue';

import { isAuthenticated } from '@/util/guards';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/wallet',
    },
    {
      path: '/wallet',
      name: 'wallet',
      component: WalletPage,
      meta: {
        requiresAuth: true,
        showBottomNav: true,
      },
    },
    {
      path: '/wallet/card/add',
      name: 'card-add',
      component: () => import('@/pages/wallet/CardAddPage.vue'),
      meta: {
        requiresAuth: true,
        showBottomNav: false,
      },
    },
    {
      path: '/remittance',
      name: 'remittance',
      component: RemittancePage,
      meta: {
        requiresAuth: true,
        showBottomNav: true,
      },
    },
    {
      path: '/transactions',
      name: 'transaction-list',
      component: TransactionListPage,
      meta: { requiresAuth: true, showBottomNav: true },
    },
    {
      path: '/wallet/card/add',
      name: 'card-add',
      component: () => import('@/pages/wallet/CardAddPage.vue'),
      meta: { requiresAuth: true, showBottomNav: false },
    },
    {
      path: '/signup/nickname',
      name: 'signup-nickname',
      component: NicknamePage,
    },
    {
      path: '/signup/complete',
      name: 'signup-complete',
      component: SignupCompletePage,
    },
    {
      path: '/finance',
      name: 'Finance',
      component: FinancePage,
      meta: {
        showBottomNav: true,
      },
    },
    {
      path: '/event',
      name: 'EventMain',
      component: EventPage,
      meta: {
        showBottomNav: true,
      },
    },
    {
      path: '/event/list',
      name: 'EventList',
      component: EventListPage,
      alias: '/event/list/joined',
      meta: {
        showBottomNav: true,
      },
    },
    ...feed,
    ...mypage,
    ...settlement,
    ...signup,
    ...setting,
    ...analysis,
    ...pagesample,
    ...customcard,

    //bottom 이 필요없는 페이지

    {
      path: '/setting',
      name: 'setting',
      component: () => import('@/pages/setting/SettingHomePage.vue'),
      meta: {
        requiresAuth: true,
        showBottomNav: true,
      },
    },
    {
      path: '/point-wallet',
      name: 'point-wallet',
      component: PointWalletPage,
      meta: {
        showBottomNav: true,
      },
    },
    {
      path: '/point-wallet/random-box',
      name: 'point-random-box',
      component: RandomBoxPage,
      meta: {
        showBottomNav: true,
      },
    },
    {
      path: '/point-wallet/conversion',
      name: 'point-conversion',
      component: PointConversionPage,
      meta: {
        showBottomNav: true,
      },
    },
    {
      path: '/point-wallet/transactions',
      name: 'point-transactions',
      component: PointTransactionPage,
      meta: {
        showBottomNav: true,
      },
    },
    {
      path: '/signup/nickname',
      name: 'signup-nickname',
      component: NicknamePage,
    },
    {
      path: '/signup/complete',
      name: 'signup-complete',
      component: SignupCompletePage,
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('@/pages/search/SearchPage.vue'),
      meta: {
        showBottomNav: false,
      },
    },
    {
      path: '/notification',
      name: 'notification',
      component: NotificationView,
    },
    {
      path: '/member/:userId',
      name: 'member-detail',
      component: MemberDetailPage,
    },
  ],
});

// 로그인 상태에 따른 화면 접근 제어
router.beforeEach((to) => {
  const authData = localStorage.getItem('auth');
  let loggedIn = false;

  if (authData) {
    try {
      const parsed = JSON.parse(authData);
      loggedIn = !!parsed.accessToken;
    } catch (e) {
      localStorage.removeItem('auth');
    }
  }

  // 로그인 상태에서는 로그인/회원가입 화면 접근 차단
  // const guestOnlyPaths = [
  //   '/intro',
  //   '/auth/pin-login',
  //   '/signup/agreement',
  //   '/signup/existing-member',
  //   '/signup/new-member',
  //   '/signup/pin',
  //   '/signup/pin-confirm',
  //   '/signup/nickname',
  //   '/signup/complete',
  // ];
  //
  // const isGuestOnly = guestOnlyPaths.includes(to.path);
  //
  // if (loggedIn && isGuestOnly) {
  //   return '/wallet';
  // }

  // 로그인이 필요한 화면
  if (to.matched.some((route) => route.meta.requiresAuth)) {
    return isAuthenticated(to);
  }

  return true;
});

export default router;
