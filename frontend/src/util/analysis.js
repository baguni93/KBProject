export const ANALYSIS_PERIODS = [
  { value: 1, label: '1개월' },
  { value: 3, label: '3개월' },
  { value: 12, label: '12개월' },
];

export const normalizeAnalysisPeriod = (value) => {
  const period = Number(value);
  return [1, 3, 12].includes(period) ? period : 1;
};

export const formatAnalysisNumber = (value) =>
  Number(value ?? 0).toLocaleString('ko-KR');

const normalizeTransactionText = (value) =>
  typeof value === 'string' && value.trim() ? value.trim() : '';

export const getAnalysisTransactionLabel = (transaction = {}) => {
  const transactionLabel = normalizeTransactionText(
    transaction.transactionLabel,
  );
  if (transactionLabel) return transactionLabel;

  const transactionType = normalizeTransactionText(
    transaction.transactionType,
  ).toUpperCase();
  const merchantName = normalizeTransactionText(transaction.merchantName);

  if (transactionType === 'PAYMENT') {
    return merchantName || '결제';
  }

  if (transactionType === 'TRANSFER') {
    const receiverName = normalizeTransactionText(transaction.receiverName);
    return receiverName ? `${receiverName}에게 송금` : merchantName || '송금';
  }

  if (transactionType === 'SETTLEMENT') {
    const settlementTitle = normalizeTransactionText(
      transaction.settlementTitle,
    );
    const memo = normalizeTransactionText(transaction.memo);
    return settlementTitle || memo || merchantName || '더치페이 정산';
  }

  return merchantName || '거래';
};

export const formatAnalysisDateTime = (value) => {
  if (!value) return '-';
  return String(value).replace('T', ' ');
};

// 일반 소비내역 화면에서는 초 단위까지 보여주지 않는다.
// 카테고리 분류/수정 화면은 기존 formatAnalysisDateTime()을 사용해 초 단위를 유지한다.
export const formatAnalysisDateTimeMinute = (value) => {
  if (!value) return '-';
  return String(value).replace('T', ' ').slice(0, 16);
};

export const formatAnalysisExecutionDate = (value, includeTime = true) => {
  if (!value) return '-';

  const normalized = String(value).replace('T', ' ');
  const [datePart, timePart = ''] = normalized.split(' ');
  const dateTokens = datePart.split('-');

  if (dateTokens.length !== 3) {
    return normalized;
  }

  const formattedDate = `${dateTokens[0]}.${dateTokens[1]}.${dateTokens[2]}`;
  if (!includeTime || !timePart) {
    return formattedDate;
  }

  return `${formattedDate} ${timePart.slice(0, 5)}`;
};

export const getAnalysisErrorMessage = (error, fallbackMessage) =>
  error?.response?.data?.message ?? error?.message ?? fallbackMessage;

export const isAnalysisResultNotFound = (error) =>
  error?.response?.status === 404 &&
  error?.response?.data?.code === 'AN007';

export const getCategoryIcon = (name = '') => {
  const icons = {
    식비: 'fa-solid fa-utensils',
    카페: 'fa-solid fa-mug-hot',
    생활: 'fa-solid fa-basket-shopping',
    온라인쇼핑: 'fa-solid fa-cart-shopping',

    // 뷰티·미용
    '뷰티/미용': 'fa-solid fa-wand-magic-sparkles',
    '뷰티·미용': 'fa-solid fa-wand-magic-sparkles',

    교통: 'fa-solid fa-bus',
    자동차: 'fa-solid fa-car',
    '주거/통신': 'fa-solid fa-house-signal',
    금융: 'fa-solid fa-building-columns',
    여행: 'fa-solid fa-plane',
    교육: 'fa-solid fa-book-open',
    반려동물: 'fa-solid fa-paw',
    병원: 'fa-solid fa-stethoscope',
    기타: 'fa-solid fa-ellipsis',

    산부인과: 'fa-solid fa-person-pregnant',
    안과: 'fa-solid fa-eye',
    내과: 'fa-solid fa-stethoscope',
    정형외과: 'fa-solid fa-bone',
    한의원: 'fa-solid fa-leaf',
    치과: 'fa-solid fa-tooth',
    소아과: 'fa-solid fa-child',
  };
  return icons[name] ?? 'fa-solid fa-tag';
};

/**
 * 카테고리 순위가 기간마다 달라져도 같은 카테고리는 같은 색으로 보이도록
 * 이름을 기준으로 고정 색상을 사용한다.
 */
export const ANALYSIS_CATEGORY_COLOR_MAP = {
  식비: '#FFB800',
  카페: '#FF8A65',
  생활: '#67C7B0',
  온라인쇼핑: '#F06A9B',
  '뷰티/미용': '#B28AE8',
  교통: '#5CA8E6',
  자동차: '#7E8EA8',
  '주거/통신': '#43B5D1',
  금융: '#6B7FD7',
  여행: '#42AF8A',
  교육: '#8BC75C',
  반려동물: '#E5A35B',
  병원: '#E57373',
  기타: '#A5A5A5',
};

export const ANALYSIS_CHART_COLORS = [
  '#FFB800',
  '#FF8A65',
  '#67C7B0',
  '#F06A9B',
  '#B28AE8',
  '#5CA8E6',
  '#7E8EA8',
  '#43B5D1',
  '#6B7FD7',
  '#42AF8A',
  '#8BC75C',
  '#E5A35B',
  '#E57373',
  '#A5A5A5',
];

export const getAnalysisCategoryColor = (categoryName, fallbackIndex = 0) =>
  ANALYSIS_CATEGORY_COLOR_MAP[categoryName] ??
  ANALYSIS_CHART_COLORS[fallbackIndex % ANALYSIS_CHART_COLORS.length];


const toDateOnly = (value) => {
  if (!value) return null;

  const normalized = String(value).replace('T', ' ');
  const [datePart] = normalized.split(' ');
  const [year, month, day] = datePart.split('-').map(Number);

  if (!year || !month || !day) {
    return null;
  }

  return new Date(year, month - 1, day);
};

const formatDateOnly = (date) => {
  if (!(date instanceof Date) || Number.isNaN(date.getTime())) {
    return '-';
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}.${month}.${day}`;
};

export const formatAnalysisPeriodRange = (value, period = 1) => {
  const endDate = toDateOnly(value);
  if (!endDate) return '-';

  const normalizedPeriod = Number(period) || 1;
  const startDate = new Date(endDate);
  startDate.setMonth(startDate.getMonth() - normalizedPeriod);

  return `${formatDateOnly(startDate)} ~ ${formatDateOnly(endDate)} (${normalizedPeriod}개월)`;
};
