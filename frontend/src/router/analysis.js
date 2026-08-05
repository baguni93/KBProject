import AnalysisEntryPage from '@/pages/analysis/AnalysisEntryPage.vue';
import AnalysisAgreementPage from '@/pages/analysis/AnalysisAgreementPage.vue';
import AnalysisMainPage from '@/pages/analysis/AnalysisMainPage.vue';
import AnalysisCheckPage from '@/pages/analysis/AnalysisCheckPage.vue';
import AnalysisClassificationPage from '@/pages/analysis/AnalysisClassificationPage.vue';
import AnalysisSubcategoryPage from '@/pages/analysis/AnalysisSubcategoryPage.vue';
import AnalysisResultPage from '@/pages/analysis/AnalysisResultPage.vue';
import AnalysisCategoryEditPage from '@/pages/analysis/AnalysisCategoryEditPage.vue';
import analysisAgreementApi from '@/api/analysisAgreementApi';

const requireAnalysisAgreement = async () => {
  try {
    const status = await analysisAgreementApi.getStatus();
    return status.agreed ? true : { name: 'analysis-agreement' };
  } catch (error) {
    return { name: 'analysis-agreement' };
  }
};
// beforeEnter: 라우터 진입 전 검사용도.
export default [
  {
    path: '/analysis',
    name: 'analysis-entry',
    component: AnalysisEntryPage,
  },
  {
    path: '/analysis/agreement',
    name: 'analysis-agreement',
    component: AnalysisAgreementPage,
  },
  {
    path: '/analysis/main',
    name: 'analysis-main',
    component: AnalysisMainPage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    path: '/analysis/check',
    name: 'analysis-check',
    component: AnalysisCheckPage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    path: '/analysis/classification',
    name: 'analysis-classification',
    component: AnalysisClassificationPage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    path: '/analysis/classification/:transactionId/subcategory',
    name: 'analysis-subcategory',
    component: AnalysisSubcategoryPage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    path: '/analysis/transactions/:transactionId/category',
    name: 'analysis-category-edit',
    component: AnalysisCategoryEditPage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    path: '/analysis/result/:spendingAnalysisId',
    name: 'analysis-result',
    component: AnalysisResultPage,
    beforeEnter: requireAnalysisAgreement,
  },
];
