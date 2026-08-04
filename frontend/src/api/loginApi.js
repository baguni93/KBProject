import api from '@/api';

const BASE_URL = '/api/login';

export default {
    // 인증번호 발급
    async sendPhoneAuthCode(formData) {
        const { data } = await api.post(`${BASE_URL}/phone/send`, formData);
        return data;
    },

    // 인증번호 재발급
    async resendPhoneAuthCode(formData) {
        const { data } = await api.post(`${BASE_URL}/phone/resend`, formData);
        return data;
    },

    // 인증번호 확인
    async verifyPhoneAuthCode(formData) {
        const { data } = await api.post(`${BASE_URL}/phone/verify`, formData);
        return data;
    },

    // 가입 여부 확인
    async checkSignupStatus(formData) {
        const { data } = await api.post(`${BASE_URL}/signup/check`, formData);
        return data;
    },

    // PIN 로그인
    async login(formData) {
        const { data } = await api.post(BASE_URL, formData);
        return data;
    },
};