import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '@/pages/HomePage.vue';
import WalletPage from '@/pages/wallet/WalletPage.vue';
import RemittancePage from '@/pages/remittance/RemittancePage.vue';
import auth from './auth';

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
    ...auth,
  ],
});

export default router;
