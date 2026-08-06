import api from '@/api';

const SPENDING_ANALYSIS_URL = '/api/spending-analyses';

export default {
  async getAvailability(period = 1) {
    const { data } = await api.get(`${SPENDING_ANALYSIS_URL}/availability`, {
      params: { period },
    });
    return data;
  },

  async getTransactions(period = 1) {
    const { data } = await api.get(`${SPENDING_ANALYSIS_URL}/transactions`, {
      params: { period },
    });
    return data;
  },

  async getTransaction(transactionId) {
    const { data } = await api.get(
      `${SPENDING_ANALYSIS_URL}/transactions/${transactionId}`,
    );
    return data;
  },

  async getAnalysisResultTransactions(spendingAnalysisId) {
    const { data } = await api.get(
      `${SPENDING_ANALYSIS_URL}/${spendingAnalysisId}/transactions`,
    );
    return data;
  },

  async getUnclassifiedTransactions(period = 1) {
    const { data } = await api.get(
      `${SPENDING_ANALYSIS_URL}/unclassified-transactions`,
      { params: { period } },
    );
    return data;
  },

  async getCategories() {
    const { data } = await api.get(`${SPENDING_ANALYSIS_URL}/categories`);
    return data;
  },

  async classifyTransaction(transactionId, spendingCategoryId) {
    const { data } = await api.patch(
      `${SPENDING_ANALYSIS_URL}/transactions/${transactionId}/category`,
      { spendingCategoryId },
    );
    return data;
  },

  async executeAnalysis(period) {
    // 소비 분석은 OpenAI 문구 생성까지 포함하므로
    // 공통 Axios 제한(10초)보다 오래 걸릴 수 있습니다.
    const { data } = await api.post(
      SPENDING_ANALYSIS_URL,
      { period },
      { timeout: 120000 },
    );
    return data;
  },

  async startAsyncAnalysis(period) {
    const { data } = await api.post(
      `${SPENDING_ANALYSIS_URL}/async`,
      { period },
    );
    return data;
  },

  async getAnalysisStatus(period = 1) {
    const { data } = await api.get(`${SPENDING_ANALYSIS_URL}/status`, {
      params: { period },
    });
    return data;
  },

  async getAnalysisDetail(spendingAnalysisId) {
    const { data } = await api.get(
      `${SPENDING_ANALYSIS_URL}/${spendingAnalysisId}`,
    );
    return data;
  },

  async getLatestAnalysisDetail(period = 1) {
    const { data } = await api.get(`${SPENDING_ANALYSIS_URL}/latest`, {
      params: { period },
    });
    return data;
  },
};
