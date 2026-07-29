import api from '@/api';

const BASE_URL = '/api/remittances';

export default {
  // 송금 실행 (지갑 또는 계좌 송금)
  async sendMoney(remittanceData) {
    const { data } = await api.post(BASE_URL, remittanceData);
    console.log('REMITTANCE POST RESULT:', data);
    return data;
  },
};
