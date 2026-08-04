export const transactionTypeOptions = [
    { value: 'ALL', label: '전체' },
    { value: 'EARN', label: '적립' },
    { value: 'USE', label: '사용' },
];

const transactionTypeLabels = {
    EARN: '적립',
    USE: '사용',
};

const reasonTypeLabels = {
    ATTENDANCE: '출석 체크',
    RANDOM_BOX: '랜덤박스',
    CONVERSION: '전자지갑 전환',
    EVENT: '이벤트',
};

export const formatNumber = (value) => Number(value ?? 0).toLocaleString('ko-KR');

export const getTransactionTypeLabel = (type) => transactionTypeLabels[type] ?? type ?? '-';

export const getReasonTypeLabel = (reason) => reasonTypeLabels[reason] ?? reason ?? '-';

export const getPointSign = (type) => {
    if (type === 'EARN') return '+';
    if (type === 'USE') return '-';
    return '';
};

export const getApiErrorMessage = (error, fallback = '요청 처리 중 오류가 발생했습니다.') => {
    return error?.response?.data?.message
        ?? error?.response?.data?.error
        ?? error?.message
        ?? fallback;
};
