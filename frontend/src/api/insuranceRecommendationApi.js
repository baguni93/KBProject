import api from '@/api';

const ANALYSIS_URL = '/api/spending-analyses';
const PRODUCT_URL = '/api/insurance-products';

export default {
  // 개발/API 테스트용 동기 생성. 프론트 추천 화면에서는 async 방식을 우선 사용한다.
  async createOrReuse(spendingAnalysisId) {
    const { data } = await api.post(
      `${ANALYSIS_URL}/${spendingAnalysisId}/insurance-recommendations`,
      null,
      { timeout: 120000 },
    );
    return data;
  },

  // 보험 추천 비동기 작업 시작
  async startAsync(spendingAnalysisId) {
    const { data } = await api.post(
      `${ANALYSIS_URL}/${spendingAnalysisId}/insurance-recommendations/async`,
    );
    return data;
  },

  // 비동기 작업 상태 polling
  async getStatus(spendingAnalysisId) {
    const { data } = await api.get(
      `${ANALYSIS_URL}/${spendingAnalysisId}/insurance-recommendations/status`,
    );
    return data;
  },

  // 저장된 맞춤 보험 추천 목록
  async getRecommendations(spendingAnalysisId) {
    const { data } = await api.get(
      `${ANALYSIS_URL}/${spendingAnalysisId}/insurance-recommendations`,
    );
    return data;
  },

  // 추천 보험 한 건 상세: 상품정보 + 보장내용 + 실제 추천근거 거래
  async getRecommendationDetail(insuranceRecommendationId) {
    const { data } = await api.get(
      `/api/insurance-recommendations/${insuranceRecommendationId}`,
    );
    return data;
  },

  // 추천에 사용된 실제 거래 카테고리 집계
  async getEvidence(spendingAnalysisId) {
    const { data } = await api.get(
      `${ANALYSIS_URL}/${spendingAnalysisId}/insurance-recommendations/evidence`,
    );
    return data;
  },

  // 전체 보험상품. category가 없으면 전체 조회
  async getProducts(category = '') {
    const params = category ? { category } : undefined;
    const { data } = await api.get(PRODUCT_URL, { params });
    return data;
  },

  // 추천 여부와 무관한 보험상품 자체 상세
  async getProductDetail(insuranceProductId) {
    const { data } = await api.get(
      `${PRODUCT_URL}/${insuranceProductId}`,
    );
    return data;
  },
};
