import api from './index';

// 닉네임 중복 확인
export const checkNickname = async (nickname) => {
    const { data } = await api.get('/api/users/nickname/check', { params: { nickname } });
    return data;
};

// 회원가입
export const signup = async (signupData) => {
    const { data } = await api.post('/api/users', signupData);
    return data;
};

// PIN 재설정
export const resetPin = async (pinData) => {
    const { data } = await api.patch('/api/auth/pin/reset', pinData);
    return data;
};