import api from '@/api';

const BASE_URL = '/api/customcard';

export default {
  // 1. 약관 목록 조회
  async getAgreements() {
    const { data } = await api.get(`${BASE_URL}/agreements/list`);
    return data;
  },

  // 2. 카드 발급 약관 동의 저장
  async setAgreementAgree(userId) {
    const { data } = await api.post(`${BASE_URL}/agreements`, null, {
      params: { userId },
    });
    return data;
  },

  // 3. 약관 동의 여부 확인
  async checkAgreementAgree(userId) {
    const { data } = await api.get(`${BASE_URL}/agreements`, {
      params: { userId }, // <-- 이렇게 객체로 감싸주어야 합니다!
    });
    return data;
  },

  // 4. 계좌 상태 및 KB ON MY WAY 발급 이력 확인
  async checkCanIssue(checkCanIssueDTO) {
    const { data } = await api.post(
      `${BASE_URL}/agreements/checkCanIssue`,
      checkCanIssueDTO,
    );
    return data;
  },

  // 4. 계좌 상태 및 KB ON MY WAY 발급 이력 확인
  async save(customCardSaveDTO) {
    const { data } = await api.post(`${BASE_URL}/save`, customCardSaveDTO);
    return data;
  },
};
