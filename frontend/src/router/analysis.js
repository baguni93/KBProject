import AnalysisEntryPage from '@/pages/analysis/AnalysisEntryPage.vue';
import AnalysisAgreementPage from '@/pages/analysis/AnalysisAgreementPage.vue';
import AnalysisMainPage from '@/pages/analysis/AnalysisMainPage.vue';
import AnalysisCheckPage from '@/pages/analysis/AnalysisCheckPage.vue';
import AnalysisClassificationPage from '@/pages/analysis/AnalysisClassificationPage.vue';
import AnalysisSubcategoryPage from '@/pages/analysis/AnalysisSubcategoryPage.vue';
import AnalysisResultPage from '@/pages/analysis/AnalysisResultPage.vue';
import AnalysisCategorySummaryPage from '@/pages/analysis/AnalysisCategorySummaryPage.vue';
import AnalysisTransactionListPage from '@/pages/analysis/AnalysisTransactionListPage.vue';
import AnalysisCategoryEditPage from '@/pages/analysis/AnalysisCategoryEditPage.vue';
import RecommendationPeriodGuidePage from '@/pages/analysis/RecommendationPeriodGuidePage.vue';
import CardRecommendationPage from '@/pages/cardRecommendation/CardRecommendationPage.vue';
import CardRecommendationDetailPage from '@/pages/cardRecommendation/CardRecommendationDetailPage.vue';
import analysisAgreementApi from '@/api/analysisAgreementApi';
import InsuranceRecommendationPage from '@/pages/insuranceRecommendation/InsuranceRecommendationPage.vue';
import InsuranceProductListPage from '@/pages/insuranceRecommendation/InsuranceProductListPage.vue';
import InsuranceProductDetailPage from '@/pages/insuranceRecommendation/InsuranceProductDetailPage.vue';
import AnalysisAgreementDetailPage from '@/pages/analysis/AnalysisAgreementDetailPage.vue';

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
    path: '/analysis/agreement/:agreementId',
    name: 'analysis-agreement-detail',
    component: AnalysisAgreementDetailPage,
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
  {
    path: '/analysis/result/:spendingAnalysisId/categories',
    name: 'analysis-category-summary',
    component: AnalysisCategorySummaryPage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    path: '/analysis/transactions',
    name: 'analysis-transactions',
    component: AnalysisTransactionListPage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    path: '/analysis/recommendation-guide',
    name: 'analysis-recommendation-guide',
    component: RecommendationPeriodGuidePage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    path: '/analysis/result/:spendingAnalysisId/card-recommendations',
    name: 'card-recommendation',
    component: CardRecommendationPage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    path: '/card-recommendations/:cardRecommendationId',
    name: 'card-recommendation-detail',
    component: CardRecommendationDetailPage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    // 12개월 소비분석 결과에서 진입하는 보험 추천 메인 화면
    path: '/analysis/result/:spendingAnalysisId/insurance-recommendations',
    name: 'insurance-recommendation',
    component: InsuranceRecommendationPage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    path: '/analysis/insurance-products',
    name: 'insurance-product-list',
    component: InsuranceProductListPage,
    beforeEnter: requireAnalysisAgreement,
  },
  {
    path: '/insurance-products/:insuranceProductId',
    name: 'insurance-product-detail',
    component: InsuranceProductDetailPage,
    beforeEnter: requireAnalysisAgreement,
  },
];
