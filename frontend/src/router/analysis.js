import AnalysisMainPage from '@/pages/analysis/AnalysisMainPage.vue';
import AnalysisClassificationPage from '@/pages/analysis/AnalysisClassificationPage.vue';
import AnalysisSubcategoryPage from '@/pages/analysis/AnalysisSubcategoryPage.vue';
import AnalysisResultPage from '@/pages/analysis/AnalysisResultPage.vue';

export default [
  {
    path: '/analysis',
    name: 'analysis-main',
    component: AnalysisMainPage,
  },
  {
    path: '/analysis/classification',
    name: 'analysis-classification',
    component: AnalysisClassificationPage,
  },
  {
    path: '/analysis/classification/:transactionId/subcategory',
    name: 'analysis-subcategory',
    component: AnalysisSubcategoryPage,
  },
  {
    path: '/analysis/result/:spendingAnalysisId',
    name: 'analysis-result',
    component: AnalysisResultPage,
  },
];
