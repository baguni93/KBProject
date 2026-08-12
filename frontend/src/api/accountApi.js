import api from './index';

const BASE_URL = '/api/users/accounts';

// 연결 계좌 목록 조회
export const getAccounts = async () => {
  const { data } = await api.get(BASE_URL);
  return data;
};

// 계좌 인증번호 발급
export const requestAccountVerification = async (requestData) => {
  const { data } = await api.post(`${BASE_URL}/verification`, requestData, {
    headers: { 'Content-Type': 'application/json' },
  });

  return data;
};

// 계좌 인증번호 재발급
export const resendAccountVerification = async (verificationId) => {
  const { data } = await api.post(
    `${BASE_URL}/verification/${verificationId}/resend`,
  );
  return data;
};

// 계좌 인증번호 확인
export const confirmAccountVerification = async (confirmData) => {
  const { data } = await api.post(
    `${BASE_URL}/verification/confirm`,
    confirmData,
  );
  return data;
};

// 계좌 연결
export const connectAccount = async (connectData) => {
  const { data } = await api.post(BASE_URL, connectData);
  return data;
};

// 대표계좌 설정
export const setPrimaryAccount = async (linkedAccountId) => {
  const { data } = await api.patch(`${BASE_URL}/${linkedAccountId}/primary`);
  return data;
};

// 계좌 연결 해제
export const disconnectAccount = async (linkedAccountId) => {
  const { data } = await api.delete(`${BASE_URL}/${linkedAccountId}`);
  return data;
};

//회원의 은행에 맞는 계좌를 호출

export const getAccountByBankCode = async (userId, linkedAccountId) => {
  const { data } = await api.get(`/api/users/account/${linkedAccountId}`, {
    params: { userId }, // <-- 이렇게 객체로 감싸주어야 합니다!
  });
  return data;
};
