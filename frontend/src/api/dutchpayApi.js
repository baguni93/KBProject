import api from '@/api';

export default {
  // dutch-001: 더치페이 정산방 생성
  async createDutchpay(data) {
    const res = await api.post('/api/dutchpays', data);
    return res.data;
  },

  // dutch-002: 더치페이 정산 금액 분배 (1/N 또는 개별 분배 설정)
  async setDutchpaySplits(dutchpayId, data) {
    const res = await api.post(`/api/dutchpays/${dutchpayId}/splits`, data);
    return res.data;
  },
};
