const requireAccountUser = () => {
    const signupUserId = sessionStorage.getItem('signupUserId');
    const auth = localStorage.getItem('auth');

    if (!signupUserId && !auth) return '/intro';

    return true;
};

export default [
    {
        path: '/setting/accounts',
        name: 'account-list',
        component: () => import('@/pages/setting/account/AccountListPage.vue'),
        beforeEnter: requireAccountUser,
    },
    {
        path: '/setting/account/connect',
        name: 'account-bank-select',
        component: () => import('@/pages/setting/account/BankSelectPage.vue'),
        beforeEnter: requireAccountUser,
    },
    {
        path: '/setting/account/info',
        name: 'account-info',
        component: () => import('@/pages/setting/account/AccountInfoPage.vue'),
        beforeEnter: requireAccountUser,
    },
    {
        path: '/setting/account/verification',
        name: 'account-verification',
        component: () => import('@/pages/setting/account/AccountVerificationPage.vue'),
        beforeEnter: requireAccountUser,
    },
    {
        path: '/setting/account/complete',
        name: 'account-complete',
        component: () => import('@/pages/setting/account/AccountCompletePage.vue'),
        beforeEnter: requireAccountUser,
    },
];