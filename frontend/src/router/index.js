import { createRouter, createWebHistory } from 'vue-router';
import WalletPage from '@/pages/wallet/WalletPage.vue';
import RemittancePage from '@/pages/remittance/RemittancePage.vue';
import TransactionListPage from '@/pages/transaction/TransactionListPage.vue';
import auth from './auth';
import feed from './feed';
import mypage from './mypage';
import analysis from './analysis';
import PointWalletPage from '@/pages/pointwallet/PointWalletPage.vue';
import RandomBoxPage from '@/pages/pointwallet/RandomBoxPage.vue';
import PointConversionPage from '@/pages/pointwallet/PointConversionPage.vue';
import PointTransactionPage from '@/pages/pointwallet/PointTransactionPage.vue';

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
    ...analysis,
  ],
});

export default router;
