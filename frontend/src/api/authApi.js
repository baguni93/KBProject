import api from './index';

export default {
  // auth-001: 간편 PIN 6자리 인증 (POST /api/auth/pin/verify)
  verifyPin(userId, pinNumber) {
    return api.post('/api/auth/pin/verify', {
      userId,
      pinNumber,
    }).then(res => res.data);
  },
};
