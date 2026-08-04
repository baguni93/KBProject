import { isAuthenticated } from '@/util/guards';

export default [
  {
    path: '/settlement/payment/:settlementId',
    name: 'settlement/payment/:settlementId',
    component: () => import('../pages/settlement/SettlementTransferPage.vue'),
    meta: {
      showBottomNav: false,
    },
  },
  {
    path: '/settlement',
    name: 'settlement/list',
    component: () => import('../pages/settlement/SettlementListPage.vue'),
    meta: {
      showBottomNav: false,
    },
  },
];
