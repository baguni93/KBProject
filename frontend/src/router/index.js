import { createRouter, createWebHistory } from 'vue-router';
import feed from './feed';
import mypage from './mypage';
import settlement from './settlement';
import WalletPage from '@/pages/wallet/WalletPage.vue';
import RemittancePage from '@/pages/remittance/RemittancePage.vue';
import PointWalletPage from '@/pages/pointwallet/PointWalletPage.vue';
import RandomBoxPage from '@/pages/pointwallet/RandomBoxPage.vue';
import PointConversionPage from '@/pages/pointwallet/PointConversionPage.vue';
import PointTransactionPage from '@/pages/pointwallet/PointTransactionPage.vue';
import NotificationView from '@/pages/notification/NotificationView.vue';
import MemberDetailPage from '@/pages/member/MemberDetailPage.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: WalletPage,
    },

    {
      path: '/wallet',
      name: 'wallet',
      component: WalletPage,
    },

    {
      path: '/remittance',
      name: 'remittance',
      component: RemittancePage,
    },

    {
      path: '/point-wallet',
      name: 'point-wallet',
      component: PointWalletPage,
    },
    ...feed,
    ...mypage,
    ...settlement,

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
      meta: {
        showBottomNav: false,
      },
    },
    {
      path: '/member/:userId',
      name: 'member/:userId',
      component: MemberDetailPage,
      meta: {
        showBottomNav: false,
      },
    },
    {
      path: '/point-wallet/random-box',
      name: 'point-random-box',
      component: RandomBoxPage,
      meta: {
        showBottomNav: false,
      },
    },
    {
      path: '/point-wallet/conversion',
      name: 'point-conversion',
      component: PointConversionPage,
      meta: {
        showBottomNav: false,
      },
    },
    {
      path: '/point-wallet/transactions',
      name: 'point-transactions',
      component: PointTransactionPage,
      meta: {
        showBottomNav: false,
      },
    },
  ],
});

export default router;
