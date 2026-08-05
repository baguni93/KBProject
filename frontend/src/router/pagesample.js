export default [
  {
    path: '/fullsample',
    name: 'fullsample',
    component: () => import('@/components/common/FullPageSample.vue'),
    meta: {
      showBottomNav: false,
    },
  },
  {
    path: '/appsample',
    name: 'appsample',
    component: () => import('@/components/common/AppPageSample.vue'),
    meta: {
      showBottomNav: true,
    },
  },
];
