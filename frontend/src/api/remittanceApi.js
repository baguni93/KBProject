import api from '@/api';

const BASE_URL = '/api/remittances';

export default {
  // remit-bank-001: 최근 송금 계좌 3건 및 은행 목록 조회
  async getBankRemittanceInfo(userId = 1) {
    const { data } = await api.get(`${BASE_URL}/banks`, { params: { userId } });
    return data;
  },

  // remit-friend-001: 송금 대상 친구 목록 조회
  async getFriends(userId = 1) {
    const { data } = await api.get('/api/friends', { params: { userId } });
    return data;
  },

  // remit-003: 송금 실행 (백엔드 @ModelAttribute 바인딩을 위한 FormData 전송)
  async sendMoney(remittanceData) {
    const formData = new FormData();
    formData.append('walletId', remittanceData.walletId || 1);
    formData.append('receiverId', remittanceData.receiverId || 2);
    formData.append('amount', remittanceData.amount || 0);
    formData.append('memo', remittanceData.memo || '송금 완료');
    formData.append('content', remittanceData.content || remittanceData.memo || '송금 완료');
    formData.append('receiverType', remittanceData.receiverType || 'WALLET');
    formData.append('bankCode', remittanceData.bankCode || '004');
    formData.append('accountNumber', remittanceData.accountNumber || '');
    formData.append('visibility', remittanceData.visibility || 'PUBLIC');

    if (remittanceData.file) {
      formData.append('files', remittanceData.file);
    }

    const { data } = await api.post(BASE_URL, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    console.log('REMITTANCE POST SUCCESS RESULT:', data);
    return data;
  },

  // 팀원 작성 백엔드 API: 더치페이 정산 생성 (POST /api/settlements)
  async createSettlement(settlementData) {
    const { data } = await api.post('/api/settlements', settlementData);
    console.log('SETTLEMENT CREATE RESULT:', data);
    return data;
  },

  // 팀원 작성 백엔드 API: 더치페이 정산 취소/해체 (PATCH /api/settlements/{id}/cancel)
  async cancelSettlement(settlementId, userId = 1) {
    const { data } = await api.patch(`/api/settlements/${settlementId}/cancel`, null, { params: { userId } });
    return data;
  }
};
