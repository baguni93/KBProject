import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '@/pages/HomePage.vue';
import WalletPage from '@/pages/wallet/WalletPage.vue';
import RemittancePage from '@/pages/remittance/RemittancePage.vue';
import auth from './auth';
import feed from './feed';
import mypage from './mypage';
import PointWalletPage from '@/pages/pointwallet/PointWalletPage.vue';
import RandomBoxPage from '@/pages/pointwallet/RandomBoxPage.vue';
import PointConversionPage from '@/pages/pointwallet/PointConversionPage.vue';
import PointTransactionPage from '@/pages/pointwallet/PointTransactionPage.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
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
    {
      path: '/point-wallet/random-box',
      name: 'point-random-box',
      component: RandomBoxPage,
    },
    {
      path: '/point-wallet/conversion',
      name: 'point-conversion',
      component: PointConversionPage,
    },
    {
      path: '/point-wallet/transactions',
      name: 'point-transactions',
      component: PointTransactionPage,
    },

    ...auth,
    ...feed,
    ...mypage,
  ],
});

export default router;
