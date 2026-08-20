import api from '@/api';

const BASE_URL = '/api/remittances';

export default {
  // remit-bank-001: 최근 송금 계좌 3건 및 은행 목록 조회
  async getBankRemittanceInfo(userId = 1) {
    const { data } = await api.get(`${BASE_URL}/banks`, { params: { userId } });
    return data;
  },

  // remit-bank-002: 계좌 예금주 실명 검증 (POST /api/remittances/bank-accounts/verify)
  async verifyBankAccount(bankCode, accountNumber) {
    const { data } = await api.post(`${BASE_URL}/bank-accounts/verify`, { bankCode, accountNumber });
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
    const uId = remittanceData.userId || remittanceData.walletId;
    if (uId) {
      formData.append('userId', uId);
      formData.append('walletId', uId);
    }
    if (remittanceData.receiverId) formData.append('receiverId', remittanceData.receiverId);
    if (remittanceData.settlementId) formData.append('settlementId', remittanceData.settlementId);
    formData.append('amount', remittanceData.amount || 0);
    formData.append('spendingCategoryId', remittanceData.spendingCategoryId || 1);
    formData.append('memo', remittanceData.memo || '송금 완료');
    formData.append('content', remittanceData.content || remittanceData.memo || '송금 완료');
    formData.append('receiverType', remittanceData.receiverType || 'WALLET');
    if (remittanceData.receiverName) formData.append('receiverName', remittanceData.receiverName);
    if (remittanceData.merchantName) formData.append('merchantName', remittanceData.merchantName);
    formData.append('bankCode', remittanceData.bankCode || '004');
    formData.append('accountNumber', remittanceData.accountNumber || '');
    formData.append('visibility', remittanceData.visibility || 'PUBLIC');

    if (remittanceData.files && remittanceData.files.length) {
      remittanceData.files.forEach((f) => formData.append('files', f));
    } else if (remittanceData.file) {
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
  },

  // 송금 완료 후 피드 자동 생성을 위한 영수증 피드 API 연동
  async createReceiptFeed(feedData) {
    try {
      const { data } = await api.post('/api/feeds/receipt', feedData);
      console.log('RECEIPT FEED AUTO CREATE RESULT:', data);
      return data;
    } catch (e) {
      console.warn('영수증 피드 생성 실패:', e);
      return null;
    }
  }
};
