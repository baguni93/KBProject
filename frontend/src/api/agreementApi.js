import api from '@/api';

const BASE_URL = '/api/agreements';

// AGR-001 약관 목록 조회
export default {
    async getAgreements() {
        const { data } = await api.get(BASE_URL);
        return data.data;
    },

    // AGR-002 약관 상세 조회
    async getAgreementDetail(agreementType) {
        const { data } = await api.get(`${BASE_URL}/${agreementType}`);
        return data.data;
    },

    // AGR-003 회원 약관 동의 저장
    async saveAgreementConsents(consents) {
        const { data } = await api.post('/api/users/agreements', consents);
        return data;
    },
};