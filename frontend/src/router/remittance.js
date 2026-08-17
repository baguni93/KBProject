export default [
  {
    path: '/remittance',
    component: () => import('@/pages/remittance/RemittanceLayout.vue'),
    meta: {
      requiresAuth: true,
      showBottomNav: false,
    },
    children: [
      {
        path: '',
        redirect: '/remittance/account',
      },

      // 1. 계좌 송금 (Account)
      {
        path: 'account',
        name: 'remittance-account',
        alias: ['account/remit'],
        component: () => import('@/pages/remittance/account/AccountRemitPage.vue'),
      },
      {
        path: 'account/amount',
        name: 'remittance-account-amount',
        component: () => import('@/pages/remittance/account/AccountAmountPage.vue'),
      },
      {
        path: 'account/feed',
        name: 'remittance-account-feed',
        component: () => import('@/pages/remittance/account/AccountFeedPage.vue'),
      },
      {
        path: 'account/result',
        name: 'remittance-account-result',
        alias: ['account/confirm'],
        component: () => import('@/pages/remittance/account/AccountResultPage.vue'),
      },

      // 2. 친구 송금 (Friend)
      {
        path: 'friend',
        name: 'remittance-friend',
        alias: ['friend/select'],
        component: () => import('@/pages/remittance/friend/FriendSelectPage.vue'),
      },
      {
        path: 'friend/amount',
        name: 'remittance-friend-amount',
        component: () => import('@/pages/remittance/friend/FriendAmountPage.vue'),
      },
      {
        path: 'friend/feed',
        name: 'remittance-friend-feed',
        component: () => import('@/pages/remittance/friend/FriendFeedPage.vue'),
      },
      {
        path: 'friend/result',
        name: 'remittance-friend-result',
        alias: ['friend/confirm'],
        component: () => import('@/pages/remittance/friend/FriendResultPage.vue'),
      },

      // 3. N빵 정산 (Dutch)
      {
        path: 'dutch',
        name: 'remittance-dutch',
        alias: ['dutch/select'],
        component: () => import('@/pages/remittance/dutch/DutchSelectPage.vue'),
      },
      {
        path: 'dutch/transactions',
        name: 'remittance-dutch-transactions',
        component: () => import('@/pages/remittance/dutch/DutchTransactionsPage.vue'),
      },
      {
        path: 'dutch/amount',
        name: 'remittance-dutch-amount',
        component: () => import('@/pages/remittance/dutch/DutchAmountPage.vue'),
      },
      {
        path: 'dutch/summary',
        name: 'remittance-dutch-summary',
        alias: ['dutch/feed'],
        component: () => import('@/pages/remittance/dutch/DutchSummaryPage.vue'),
      },
      {
        path: 'dutch/result',
        name: 'remittance-dutch-result',
        alias: ['dutch/confirm'],
        component: () => import('@/pages/remittance/dutch/DutchResultPage.vue'),
      },
    ],
  },
];
