import { isAuthenticated } from '@/util/guards';
import CardCreatePage from '@/pages/card/CardCreatePage.vue';
import CardCompletePage from '@/pages/card/CardCompletePage.vue';
import CardCreateAgreementPage from '@/pages/card/CardCreateAgreementPage.vue';
import CardCreateIntro from '@/pages/card/CardCreateIntro.vue';
import CardAgreementDetailPage from '@/pages/card/CardAgreementDetailPage.vue';
import CardCheckCanIssuePage from '@/pages/card/CardCheckCanIssuePage.vue';
import CardSelectBenefit from '@/pages/card/CardSelectBenefit.vue';
import CardIssueVerificationPage from '@/pages/card/CardIssueVerification.page.vue';
export default [
  {
    path: '/card/create/intro',
    name: 'card/create/intro',
    component: CardCreateIntro,
    meta: {
      showBottomNav: false,
    },
  },
  {
    path: '/card/create/agreement',
    name: 'card/create/agreement',
    component: CardCreateAgreementPage,
    meta: {
      showBottomNav: false,
    },
  },
  {
    path: '/card/detail/:agreementId',
    name: 'card/detail/:agreementId',
    component: CardAgreementDetailPage,
  },
  {
    path: '/card/create',
    name: 'card/create',
    component: CardCreatePage,
    meta: {
      showBottomNav: false,
    },
  },
  {
    path: '/card/complete',
    name: 'card/complete',
    component: CardCompletePage,
    meta: {
      showBottomNav: false,
    },
  },
  {
    path: '/card/select/beneift',
    name: 'card/select/beneift',
    component: CardSelectBenefit,
    meta: {
      showBottomNav: false,
    },
  },
  {
    path: '/card/check/issue',
    name: 'card/check/issue',
    component: CardCheckCanIssuePage,
    meta: {
      showBottomNav: false,
    },
  },
  {
    path: '/card/check/issue/verification',
    name: 'card/check/issue/verification',
    component: CardIssueVerificationPage,
    meta: {
      showBottomNav: false,
    },
  },
];
