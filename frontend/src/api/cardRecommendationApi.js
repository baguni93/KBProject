import api from '@/api';

const ANALYSIS_URL = '/api/spending-analyses';
const RECOMMENDATION_URL = '/api/card-recommendations';

export default {
  async createOrReuse(spendingAnalysisId) {
    const { data } = await api.post(
      `${ANALYSIS_URL}/${spendingAnalysisId}/card-recommendations`,
      null,
      { timeout: 120000 },
    );
    return data;
  },

  async getRecommendations(
    spendingAnalysisId,
    cardType = 'CREDIT',
    feeMode = 'MAX_BENEFIT',
  ) {
    const { data } = await api.get(
      `${ANALYSIS_URL}/${spendingAnalysisId}/card-recommendations`,
      { params: { cardType, feeMode } },
    );
    return data;
  },

  async getRecommendationDetail(
    cardRecommendationId,
    feeMode = 'MAX_BENEFIT',
  ) {
    const { data } = await api.get(
      `${RECOMMENDATION_URL}/${cardRecommendationId}`,
      { params: { feeMode } },
    );
    return data;
  },
};
