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

export const formatAnalysisDateTime = (value) => {
  if (!value) return '-';
  return String(value).replace('T', ' ');
};

export const getAnalysisErrorMessage = (error, fallbackMessage) =>
  error?.response?.data?.message ?? error?.message ?? fallbackMessage;

export const isAnalysisResultNotFound = (error) =>
  error?.response?.status === 404 &&
  error?.response?.data?.code === 'AN007';
