export const CARD_RECOMMENDATION_CARD_TYPES = [
  { value: 'CREDIT', label: '신용카드' },
  { value: 'CHECK', label: '체크카드' },
];

export const CARD_RECOMMENDATION_FEE_MODES = [
  {
    value: 'MAX_BENEFIT',
    label: '연회비 제외',
    description: '연회비를 제외한 예상 할인액으로 비교해요.',
  },
  {
    value: 'NET_BENEFIT',
    label: '연회비 포함',
    description: '예상 할인액에서 연회비를 뺀 금액으로 비교해요.',
  },
];

const CARD_IMAGE_MAP = {
  'my_wesh_card.png': '/images/cards/weish.png',
  'toktok_o_card.png': '/images/cards/toktok.png',
  'nori2_card.png': '/images/cards/nori2.png',
};

export const normalizeCardType = (value) =>
  value === 'CHECK' ? 'CHECK' : 'CREDIT';

export const normalizeFeeMode = (value) =>
  value === 'NET_BENEFIT' ? 'NET_BENEFIT' : 'MAX_BENEFIT';

export const formatCardAmount = (value) =>
  Number(value ?? 0).toLocaleString('ko-KR');

export const getCardImagePath = (cardImage) => {
  if (!cardImage) return null;

  const value = String(cardImage).trim();
  if (/^https?:\/\//i.test(value) || value.startsWith('/')) {
    return value;
  }

  return CARD_IMAGE_MAP[value] ?? null;
};

export const getCardRecommendationErrorMessage = (
  error,
  fallbackMessage,
) =>
  error?.response?.data?.message ??
  error?.originalError?.response?.data?.message ??
  error?.message ??
  error?.error ??
  fallbackMessage;

export const getCardTypeLabel = (cardType) =>
  cardType === 'CHECK' ? '체크카드' : '신용카드';

export const getFeeModeLabel = (feeMode) =>
  feeMode === 'NET_BENEFIT' ? '연회비 차감 기준' : '예상 할인액 기준';
