import { createRouter, createWebHistory } from 'vue-router';
import WalletPage from '@/pages/wallet/WalletPage.vue';
import RemittancePage from '@/pages/remittance/RemittancePage.vue';
<<<<<<< Updated upstream
import TransactionListPage from '@/pages/transaction/TransactionListPage.vue';
import auth from './auth';
import feed from './feed';
import mypage from './mypage';
=======
>>>>>>> Stashed changes
import PointWalletPage from '@/pages/pointwallet/PointWalletPage.vue';
import RandomBoxPage from '@/pages/pointwallet/RandomBoxPage.vue';
import PointConversionPage from '@/pages/pointwallet/PointConversionPage.vue';
import PointTransactionPage from '@/pages/pointwallet/PointTransactionPage.vue';
import NicknamePage from '@/pages/signup/NicknamePage.vue';
import SignupCompletePage from '@/pages/signup/SignupCompletePage.vue';
import auth from './auth';
import signup from './signup';
import setting from './setting';
import { isAuthenticated } from '@/util/guards';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
<<<<<<< Updated upstream
      redirect: '/wallet',
=======
      name: 'home',
      component: HomePage,
      meta: { requiresAuth: true },
>>>>>>> Stashed changes
    },
    {
      path: '/wallet',
      name: 'wallet',
      component: WalletPage,
      meta: { requiresAuth: true },
    },
    {
      path: '/remittance',
      name: 'remittance',
      component: RemittancePage,
      meta: { requiresAuth: true },
    },
    {
      path: '/point-wallet',
      name: 'point-wallet',
      component: PointWalletPage,
      meta: { requiresAuth: true },
    },
    {
      path: '/point-wallet/random-box',
      name: 'point-random-box',
      component: RandomBoxPage,
      meta: { requiresAuth: true },
    },
    {
      path: '/point-wallet/conversion',
      name: 'point-conversion',
      component: PointConversionPage,
      meta: { requiresAuth: true },
    },
    {
      path: '/point-wallet/transactions',
      name: 'point-transactions',
      component: PointTransactionPage,
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
    {
      path: '/feed',
      name: 'feed-temp',
      component: {
        template: '<div></div>',
      },
      meta: { requiresAuth: true },
    },

    ...auth,
<<<<<<< Updated upstream
    ...feed,
    ...mypage,
=======
    ...signup,
    ...setting,
>>>>>>> Stashed changes
  ],
});

// 로그인 필수 화면 접근 확인
router.beforeEach((to) => {
  if (!to.matched.some((route) => route.meta.requiresAuth)) return true;

  return isAuthenticated(to);
});

export default router;