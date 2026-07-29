import api from '@/api';

const BASE_URL = '/api/wallets';

export default {
  // 회원 ID로 지갑 정보/잔액 조회
  async getWalletByUserId(userId) {
    const { data } = await api.get(`${BASE_URL}/user/${userId}`);
    return data;
  },

  // wallet-001: 1회용 QR 결제 토큰 발급
  async getQrToken(userId = 1) {
    const { data } = await api.get(`${BASE_URL}/me/qr-token?userId=${userId}`);
    return data;
  },

  // wallet-002: 1회용 바코드 결제 토큰 발급
  async getBarcodeToken(userId = 1) {
    const { data } = await api.get(`${BASE_URL}/me/barcode-token?userId=${userId}`);
    return data;
  },

  // charge-001: 지갑 머니 수동 충전 신청
  async chargeWallet(chargeData) {
    const { data } = await api.post(`${BASE_URL}/charges`, chargeData);
    return data;
  },

  // charge-002: 지갑 머니 충전 내역 상세 조회
  async getChargeDetails(chargeId) {
    const { data } = await api.get(`${BASE_URL}/charges/${chargeId}`);
    return data;
  },

  // autocharge-001: 부족금 자동 충전 처리
  async autoCharge(chargeData) {
    const { data } = await api.post(`${BASE_URL}/auto-charge`, chargeData);
    return data;
  },

  // POS / 가맹점 결제 승인 요청 (토큰 소멸 & 잔액 차감)
  async confirmPayment(paymentData) {
    const { data } = await api.post('/api/payments/confirm', paymentData);
    return data;
  },
};
