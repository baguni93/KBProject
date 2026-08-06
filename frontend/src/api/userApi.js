import api from './index';

// 회원 기본정보 조회
export const getUserInfo = async (userId) => {
    const { data } = await api.get(`/api/users/${userId}`);

    return data;
};

// 닉네임 중복 확인
export const checkNickname = async (nickname) => {
    const { data } = await api.get('/api/users/nickname/check', {
        params: { nickname },
    });

    return data;
};

// 회원가입
export const signup = async (signupData) => {
    const { data } = await api.post('/api/users', signupData);

    return data;
};

// 현재 PIN 확인
export const verifyPin = async (userId, pinPassword) => {
    const { data } = await api.post(`/api/users/${userId}/pin/verify`, {
        pinPassword,
    });

    return data;
};

// PIN 변경
export const changePin = async (userId, pinData) => {
    const { data } = await api.patch(`/api/users/${userId}/pin`, pinData);

    return data;
};

// PIN 재설정
export const resetPin = async (pinData) => {
    const { data } = await api.patch('/api/auth/pin/reset', pinData);

    return data;
};

// 회원 이름 변경
export const changeUserName = async (userId, changeData) => {
    const { data } = await api.patch(`/api/users/${userId}/name`, changeData);

    return data;
};

// 회원 휴대폰번호 변경
export const changePhoneNumber = async (userId, changeData) => {
    const { data } = await api.patch(`/api/users/${userId}/phone`, changeData);

    return data;
};

// 회원탈퇴
export const withdrawUser = async (userId, withdrawalData) => {
    const { data } = await api.delete(`/api/users/${userId}`, {
        data: withdrawalData,
    });

    return data;
};