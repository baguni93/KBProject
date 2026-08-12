import api from "./index";

// PAY-001 대표 카드 정보 조회
export const getPrimaryCard = async (userId) => {
  const { data } = await api.get("/api/payments/cards/primary", {
    params: { userId },
  });

  return data;
};

// PAY-002 카드 등록 상태 및 가이드 조회
export const getCardStatus = async (userId) => {
  const { data } = await api.get("/api/payments/cards/status", {
    params: { userId },
  });

  return data;
};

// CARD-001 신용/체크카드 정보 등록
export const registerCard = async (cardData) => {
  const { data } = await api.post("/api/cards", cardData, {
    headers: { 'Content-Type': 'application/json' }
  });

  return data;
};

// CARD-002 카드 결제 약관 동의
export const saveCardAgreements = async (agreementData) => {
  const { data } = await api.post("/api/cards/agreements", agreementData);

  return data;
};

// CARD-003 카드 등록 후 대표 카드 지정
export const setRegisteredPrimaryCard = async (cardId, userId) => {
  const { data } = await api.patch(`/api/cards/${cardId}/primary`, null, {
    params: { userId },
  });

  return data;
};

// setting-card-001 연결 카드 목록 조회
export const getCards = async () => {
  const { data } = await api.get("/api/users/cards");

  return data;
};

// setting-card-002 연결 카드 대표카드 설정
export const setPrimaryCard = async (linkedCardId) => {
  const { data } = await api.patch(`/api/users/cards/${linkedCardId}/represent`);

  return data;
};


// setting-card-003 카드 연결 해제
export const disconnectCard = async (linkedCardId) => {
  const { data } = await api.delete(`/api/users/cards/${linkedCardId}`);

  return data;
};

// 카드 결제 1단계: 결제 대기(PENDING) 레코드 생성
export const requestCardPayment = async (linkedCardId = 1) => {
  const { data } = await api.post("/api/cards/payments/request", { linkedCardId });
  return data;
};

// 카드 결제 2~3단계: 결제 승인 요청 (테스트용)
export const approveCardPayment = async (approveData) => {
  const { data } = await api.post("/api/cards/payments/approve", approveData);
  return data;
};

// 카드 결제 상태 단건 조회
export const getCardTransactionStatus = async (cardTransactionId) => {
  const { data } = await api.get(`/api/cards/payments/transactions/${cardTransactionId}`);
  return data;
};

// 카드 결제 취소/만료 (FAILED 처리)
export const cancelCardPayment = async (cardTransactionId) => {
  const { data } = await api.post("/api/cards/payments/cancel", { cardTransactionId });
  return data;
};