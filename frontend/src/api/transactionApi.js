import api from './index';

export default {
  // transaction-001: 거래 내역 목록 조회
  getTransactions(userId = 1, type = '') {
    const params = { userId };
    if (type) params.type = type;
    return api.get('/api/transactions', { params }).then(res => res.data);
  },

  // transaction-002: 상세 영수증 조회
  getTransactionDetail(transactionId) {
    return api.get(`/api/transactions/${transactionId}`).then(res => res.data);
  },

  // 영수증 메모 업데이트
  updateMemo(transactionId, memo) {
    return api.put(`/api/transactions/${transactionId}/memo`, { memo }).then(res => res.data);
  },
};
