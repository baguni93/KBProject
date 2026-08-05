import { createRouter, createWebHistory } from 'vue-router';
import feed from './feed';
import mypage from './mypage';
import settlement from './settlement';
import auth from './auth';
import signup from './signup';
import setting from './setting';
import pagesample from './pagesample';
import WalletPage from '@/pages/wallet/WalletPage.vue';
import RemittancePage from '@/pages/remittance/RemittancePage.vue';
import TransactionListPage from '@/pages/transaction/TransactionListPage.vue';
import analysis from './analysis';
import PointWalletPage from '@/pages/pointwallet/PointWalletPage.vue';
import RandomBoxPage from '@/pages/pointwallet/RandomBoxPage.vue';
import PointConversionPage from '@/pages/pointwallet/PointConversionPage.vue';
import PointTransactionPage from '@/pages/pointwallet/PointTransactionPage.vue';
import NotificationView from '@/pages/notification/NotificationView.vue';
import MemberDetailPage from '@/pages/member/MemberDetailPage.vue';
import NicknamePage from '@/pages/signup/NicknamePage.vue';
import SignupCompletePage from '@/pages/signup/SignupCompletePage.vue';
import HomePage from '@/pages/HomePage.vue';

import { isAuthenticated } from '@/util/guards';
import EventPage from '@/pages/event/EventPage.vue';
import EventListPage from '@/pages/event/EventListPage.vue';
import FinancePage from '@/pages/finance/FinancePage.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
      meta: { requiresAuth: true, showBottomNav: false },
    },
    {
      path: '/wallet',
      name: 'wallet',
      component: WalletPage,
    },

    {
      path: '/wallet',
      name: 'wallet',
      component: WalletPage,
      meta: { requiresAuth: true, showBottomNav: true },
    },

    {
      path: '/remittance',
      name: 'remittance',
      component: RemittancePage,
      meta: { requiresAuth: true, showBottomNav: true },
    },

    {
      path: '/point-wallet',
      name: 'point-wallet',
      component: PointWalletPage,
      meta: { showBottomNav: true },
    },
    {
      path: '/transactions',
      name: 'transaction-list',
      component: TransactionListPage,
      meta: { requiresAuth: true },
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
    ...feed,
    ...mypage,
    ...settlement,
    ...auth,
    ...signup,
    ...setting,
    ...analysis,
    ...pagesample,

    //bottom 이 필요없는 페이지
    {
      path: '/setting',
      name: 'setting',
      component: () => import('@/pages/setting/SettingPage.vue'),
      meta: {
        showBottomNav: false,
      },
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
      name: 'member/:userId',
      component: MemberDetailPage,
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
      path: '/finance',
      name: 'Finance',
      component: FinancePage,
    },
    {
      path: '/event',
      name: 'EventMain',
      component: EventPage,
    },
    {
      path: '/event/list',
      name: 'EventList',
      component: EventListPage,
      alias: '/event/list/joined',
    },

    ...auth,
  ],
});

// 로그인 필수 화면 접근 확인
router.beforeEach((to) => {
  if (!to.matched.some((route) => route.meta.requiresAuth)) return true;

  return isAuthenticated(to);
});

export default router;
