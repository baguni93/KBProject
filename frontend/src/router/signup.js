export default [
  {
    path: '/intro',
    name: 'intro',
    component: () => import('@/pages/intro/IntroPage.vue'),
  },
  {
    path: '/signup/agreement',
    name: 'agreement',
    component: () => import('@/pages/signup/AgreementPage.vue'),
  },
  {
    path: '/signup/agreement/:agreementType',
    name: 'agreement-detail',
    component: () => import('@/pages/signup/AgreementDetailPage.vue'),
  },
  {
    path: '/signup/check',
    name: 'signup-check',
    component: () => import('@/pages/signup/MembershipCheckPage.vue'),
  },
  {
    path: '/signup/verification',
    name: 'signup-verification',
    component: () => import('@/pages/signup/VerificationCodePage.vue'),
  },
  {
    path: '/signup/existing-member',
    name: 'existing-member',
    component: () => import('@/pages/signup/ExistingMemberPage.vue'),
  },
  {
    path: '/signup/new-member',
    name: 'new-member',
    component: () => import('@/pages/signup/NewMemberPage.vue'),
  },
  {
    path: '/signup/pin',
    name: 'signup-pin',
    component: () => import('@/pages/signup/PinPage.vue'),
  },
  {
    path: '/signup/pin-confirm',
    name: 'signup-pin-confirm',
    component: () => import('@/pages/signup/PinConfirmPage.vue'),
  },
  {
    path: '/auth/pin-login',
    name: 'pin-login',
    component: () => import('@/pages/auth/PinLoginPage.vue'),
    beforeEnter: () => {
      const phoneNumber = sessionStorage.getItem('pinLoginPhoneNumber');

      if (!phoneNumber) return '/intro';

      return true;
    },
  },
  {
    path: '/auth/pin-reset',
    name: 'pin-reset',
    component: () => import('@/pages/auth/PinResetPage.vue'),
    beforeEnter: () => {
      const phoneNumber = sessionStorage.getItem('pinResetPhoneNumber');

      if (!phoneNumber) return '/intro';

      return true;
    },
  },
  {
    path: '/auth/pin-reset-confirm',
    name: 'pin-reset-confirm',
    component: () => import('@/pages/auth/PinResetConfirmPage.vue'),
    beforeEnter: () => {
      const phoneNumber = sessionStorage.getItem('pinResetPhoneNumber');
      const newPinPassword = sessionStorage.getItem('pinResetNewPin');

      if (!phoneNumber || !newPinPassword) return '/auth/pin-reset';

      return true;
    },
  },
  {
    path: '/auth/pin-reset-complete',
    name: 'pin-reset-complete',
    component: () => import('@/pages/auth/PinResetCompletePage.vue'),
    beforeEnter: () => {
      const completed = sessionStorage.getItem('pinResetCompleted');

      if (completed !== 'true') return '/intro';

      return true;
    },
  },
];
