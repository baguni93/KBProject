import api from './index';

const getBaseUrl = (userId) => `/api/users/${userId}/accounts`;

// 연결 계좌 목록 조회
export const getAccounts = async (userId) => {
    const { data } = await api.get(getBaseUrl(userId));
    return data;
};

// 계좌 인증번호 발급
export const requestAccountVerification = async (userId, requestData) => {
    const { data } = await api.post(`${getBaseUrl(userId)}/verification`, requestData);
    return data;
};

// 계좌 인증번호 확인
export const confirmAccountVerification = async (userId, confirmData) => {
    const { data } = await api.post(`${getBaseUrl(userId)}/verification/confirm`, confirmData);
    return data;
};

// 계좌 연결
export const connectAccount = async (userId, connectData) => {
    const { data } = await api.post(getBaseUrl(userId), connectData);
    return data;
};

// 대표계좌 설정
export const setPrimaryAccount = async (userId, linkedAccountId) => {
    const { data } = await api.patch(`${getBaseUrl(userId)}/${linkedAccountId}/primary`);
    return data;
};

// 계좌 연결 해제
export const disconnectAccount = async (userId, linkedAccountId) => {
    const { data } = await api.delete(`${getBaseUrl(userId)}/${linkedAccountId}`);
    return data;
};