import axios from 'axios';

const api = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/json',
  },
});

export default {
  // pay-001: 대표 카드 정보 조회
  getPrimaryCard(userId = 1) {
    return api.get(`/api/payments/cards/primary?userId=${userId}`).then(res => res.data);
  },

  // pay-002: 카드 등록 상태 및 가이드 조회
  getCardStatus(userId = 1) {
    return api.get(`/api/payments/cards/status?userId=${userId}`).then(res => res.data);
  },

  // card-001: 신용/체크 카드 정보 등록
  registerCard(cardData) {
    return api.post('/api/cards', cardData).then(res => res.data);
  },

  // card-002: 카드 결제 약관 동의
  saveAgreements(agreementData) {
    return api.post('/api/cards/agreements', agreementData).then(res => res.data);
  },

  // card-003: 대표 카드 지정 완료
  setPrimaryCard(cardId, userId = 1) {
    return api.patch(`/api/cards/${cardId}/primary?userId=${userId}`).then(res => res.data);
  },
};
