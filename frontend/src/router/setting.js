const requireAccountUser = () => {
    const signupUserId =
        sessionStorage.getItem('signupUserId');
    const auth = localStorage.getItem('auth');

    if (!signupUserId && !auth) {
        return '/intro';
    }

    return true;
};

export default [
    {
        path: '/setting',
        name: 'setting-home',
        component: () =>
            import(
                '@/pages/setting/SettingHomePage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/profile',
        name: 'setting-profile',
        component: () =>
            import(
                '@/pages/setting/profile/ProfileEditPage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/accounts',
        name: 'account-list',
        component: () =>
            import(
                '@/pages/setting/account/AccountListPage.vue'
                ),
        beforeEnter: requireAccountUser,
        meta: {
            showBottomNav: false,
        },
    },
    {
        path: '/setting/cards',
        name: 'card-list',
        component: () =>
            import(
                '@/pages/setting/card/CardListPage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account/connect',
        name: 'account-bank-select',
        component: () =>
            import(
                '@/pages/setting/account/BankSelectPage.vue'
                ),
        beforeEnter: requireAccountUser,
        meta: {
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account/info',
        name: 'account-info',
        component: () =>
            import(
                '@/pages/setting/account/AccountInfoPage.vue'
                ),
        beforeEnter: requireAccountUser,
        meta: {
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account/verification',
        name: 'account-verification',
        component: () =>
            import(
                '@/pages/setting/account/AccountVerificationPage.vue'
                ),
        beforeEnter: requireAccountUser,
        meta: {
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account/complete',
        name: 'account-complete',
        component: () =>
            import(
                '@/pages/setting/account/AccountCompletePage.vue'
                ),
        beforeEnter: requireAccountUser,
        meta: {
            showBottomNav: false,
        },
    },
    {
        path: '/setting/notification',
        name: 'notification-setting',
        component: () =>
            import(
                '@/pages/setting/notification/NotificationSettingPage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account-management',
        name: 'account-management',
        component: () =>
            import(
                '@/pages/setting/accountManagement/AccountManagementPage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account-management/name',
        name: 'account-name-change',
        component: () =>
            import(
                '@/pages/setting/accountManagement/NameChangeGuidePage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account-management/name/verification',
        name: 'account-name-verification',
        component: () =>
            import(
                '@/pages/signup/VerificationCodePage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account-management/phone',
        name: 'account-phone-change',
        component: () =>
            import(
                '@/pages/setting/accountManagement/PhoneChangeGuidePage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account-management/pin',
        name: 'account-pin-current',
        component: () =>
            import(
                '@/pages/setting/accountManagement/PinChangePage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account-management/pin/new',
        name: 'account-pin-new',
        component: () =>
            import(
                '@/pages/setting/accountManagement/PinChangeNewPage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account-management/pin/confirm',
        name: 'account-pin-confirm',
        component: () =>
            import(
                '@/pages/setting/accountManagement/PinChangeConfirmPage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },

    {
        path: '/setting/account-management/withdraw',
        name: 'account-withdraw',
        component: () =>
            import(
                '@/pages/setting/accountManagement/WithdrawPage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account-management/logout',
        name: 'account-logout',
        component: () =>
            import(
                '@/pages/setting/accountManagement/LogoutPage.vue'
                ),
        meta: {
            requiresAuth: true,
            showBottomNav: false,
        },
    },
    {
        path: '/setting/account-management/complete',
        name: 'setting-complete',
        component: () =>
            import(
                '@/pages/setting/accountManagement/SettingCompletePage.vue'
                ),
        meta: {
            showBottomNav: false,
        },
    },
];