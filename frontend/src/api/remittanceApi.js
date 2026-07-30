import api from '@/api';

const BASE_URL = '/api/remittances';

export default {
  // remit-bank-001: 최근 송금 계좌 3건 및 은행 목록 조회
  async getBankRemittanceInfo(userId = 1) {
    const { data } = await api.get(`${BASE_URL}/banks`, { params: { userId } });
    return data;
  },

  // remit-003: 송금 실행 (지갑 또는 계좌 송금)
  async sendMoney(remittanceData) {
    const { data } = await api.post(BASE_URL, remittanceData);
    console.log('REMITTANCE POST RESULT:', data);
    return data;
  },
};
