import api from '@/api';

const ANALYSIS_AGREEMENT_URL = '/api/analysis-agreements';

export default {
  async getStatus() {
    const { data } = await api.get(`${ANALYSIS_AGREEMENT_URL}/status`);
    return data;
  },

  async getAgreements() {
    const { data } = await api.get(ANALYSIS_AGREEMENT_URL);
    return data;
  },

  async saveAgreements(agreements) {
    const { data } = await api.post(ANALYSIS_AGREEMENT_URL, { agreements });
    return data;
  },
};
