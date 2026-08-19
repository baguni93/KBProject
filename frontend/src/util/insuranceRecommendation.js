export const INSURANCE_CATEGORY_OPTIONS = [
  { value: '', label: '전체' },
  { value: '건강·실비', label: '건강·실비' },
  { value: '운전자', label: '운전자' },
  { value: '치아', label: '치아' },
  { value: '펫', label: '펫' },
];

/*
 * DB에는 프로젝트용 파일명이 저장되어 있다.
 * 현재 저장소에는 보험 이미지 원본 파일이 없어서, 우선 KB손해보험 공식 상품 페이지의
 * 대표 이미지 URL과 연결한다. 추후 같은 파일명으로 public/images/insurance에 이미지를
 * 내려받으면 이 매핑만 로컬 경로로 교체하면 화면 코드는 수정할 필요가 없다.
 */
const INSURANCE_IMAGE_MAP = {
  'kb_51010_health_insurance.png':
    'https://www.kbinsure.co.kr/images/ins_prdt/kb_plus_helth/prdt_inform_visual_v2.jpg',
  'kb_medical_expense_insurance.png':
    'https://www.kbinsure.co.kr/images/ins_prdt/kid_helth/img_prod_mediCare01.png',
  'kb_easy_medical_expense_insurance.png':
    'https://www.kbinsure.co.kr/images/ins_prdt/oldage_nsxpn/img_prod_mediCare01.png',
  'kb_overseas_travel_insurance.png':
    'https://www.kbinsure.co.kr/images/ins_prdt/trvl_ls/img_oversea01.gif',

  // 공식 사이트에서는 유학/출장 장기체류 상품을 하나로 제공하므로 같은 대표 이미지를 사용한다.
  'kb_study_abroad_insurance.png':
    'https://www.kbinsure.co.kr/images/ins_prdt/trvl_ls/img_oversea01.gif',
  'kb_overseas_worker_insurance.png':
    'https://www.kbinsure.co.kr/images/ins_prdt/trvl_ls/img_oversea01.gif',

  'kb_auto_insurance.png':
    'https://www.kbinsure.co.kr/images/ins_prdt/ato_drivr/img_mgcar06.gif',
  'kb_driver_injury_insurance.png':
    'https://www.kbinsure.co.kr/images/ins_prdt/mgcar_drivrins_top_vis.jpg',
  'kb_dental_insurance.png':
    'https://www.kbinsure.co.kr/images/ins_prdt/sld_tooth_helth_ins/prdt_inform_visual2.jpg',
  'kb_pet_dog_insurance.png':
    'https://www.kbinsure.co.kr/images/ins_prdt/dog_ins/prdt_inform_visual.jpg',
  'kb_pet_cat_insurance.png':
    'https://www.kbinsure.co.kr/images/ins_prdt/cat_ins/prdt_inform_visual.jpg',
};

/*
 * 기존 DB의 application_url이 테스트용 html 파일명인 경우에도 실제 KB손해보험 페이지로
 * 이동할 수 있도록 상품 ID별 공식 URL을 보조 매핑한다.
 */
const INSURANCE_APPLICATION_URL_BY_NAME = {
  'KB 5.10.10 플러스 건강보험':
    'https://www.kbinsure.co.kr/CG302290101.ecs',
  'KB손보 실손의료비보장보험':
    'https://www.kbinsure.co.kr/CG302090101.ecs',
  'KB손보 간편가입 실손의료비보장보험':
    'https://www.kbinsure.co.kr/CG302250001.ec',
  '해외여행보험':
    'https://www.kbinsure.co.kr/CG308080001.ec',
  '해외장기체류(유학연수생)보험':
    'https://www.kbinsure.co.kr/CG308040001.ec',
  '해외장기체류(출장주재원)보험':
    'https://www.kbinsure.co.kr/CG308040001.ec',
  'KB자동차보험':
    'https://direct.kbinsure.co.kr/home/app/views/ws/car/individual/DS03_CAR_INDV_IS001M_static.html',
  'KB 플러스 운전자 상해보험':
    'https://www.kbinsure.co.kr/CG301040001.ec',
  'KB The 건강한 치아보험':
    'https://www.kbinsure.co.kr/CG302130101.ecs',
  'KB 금쪽같은 펫보험(강아지)':
    'https://www.kbinsure.co.kr/CG313010101.ecs',
  'KB 금쪽같은 펫보험(고양이)':
    'https://www.kbinsure.co.kr/CG313020101.ecs',
};

/*
 * seed의 상품 ID도 현재 1~11로 고정되어 있어 이름이 바뀐 경우를 대비한 보조 매핑만 둔다.
 * 기본 매칭 기준은 상품명이므로 DB 재적재 순서가 달라져도 영향이 적다.
 */
const INSURANCE_APPLICATION_URL_BY_ID = {
  1: 'https://www.kbinsure.co.kr/CG302290101.ecs',
  2: 'https://www.kbinsure.co.kr/CG302090101.ecs',
  3: 'https://www.kbinsure.co.kr/CG302250001.ec',
  4: 'https://www.kbinsure.co.kr/CG308080001.ec',
  5: 'https://www.kbinsure.co.kr/CG308040001.ec',
  6: 'https://www.kbinsure.co.kr/CG308040001.ec',
  7: 'https://direct.kbinsure.co.kr/home/app/views/ws/car/individual/DS03_CAR_INDV_IS001M_static.html',
  8: 'https://www.kbinsure.co.kr/CG301040001.ec',
  9: 'https://www.kbinsure.co.kr/CG302130101.ecs',
  10: 'https://www.kbinsure.co.kr/CG313010101.ecs',
  11: 'https://www.kbinsure.co.kr/CG313020101.ecs',
};

export const formatInsuranceAmount = (value) =>
  Number(value ?? 0).toLocaleString('ko-KR');

export const getInsuranceImageUrl = (insuranceImage) => {
  if (!insuranceImage) return null;

  const value = String(insuranceImage).trim();
  if (/^https?:\/\//i.test(value) || value.startsWith('/')) {
    return value;
  }

  return INSURANCE_IMAGE_MAP[value]
    ?? `/api/insurance-products/images/${encodeURIComponent(value)}`;
};

export const getInsuranceApplicationUrl = (insurance) => {
  const rawUrl = String(insurance?.applicationUrl ?? '').trim();

  // DB가 실제 URL로 교체된 뒤에는 DB 값을 최우선으로 사용한다.
  if (/^https?:\/\//i.test(rawUrl)) {
    return rawUrl;
  }

  const insuranceName = String(insurance?.insuranceName ?? '').trim();
  if (INSURANCE_APPLICATION_URL_BY_NAME[insuranceName]) {
    return INSURANCE_APPLICATION_URL_BY_NAME[insuranceName];
  }

  const productId = Number(insurance?.insuranceProductId);
  return INSURANCE_APPLICATION_URL_BY_ID[productId] ?? null;
};

export const getInsuranceCategoryIcon = (category) => {
  switch (category) {
    case '건강·실비':
      return 'fa-solid fa-heart-pulse';
    case '여행자':
      return 'fa-solid fa-plane-departure';
    case '운전자':
      return 'fa-solid fa-car-side';
    case '치아':
      return 'fa-solid fa-tooth';
    case '펫':
      return 'fa-solid fa-paw';
    default:
      return 'fa-solid fa-shield-heart';
  }
};

export const getInsuranceRecommendationErrorMessage = (
  error,
  fallbackMessage,
) =>
  error?.response?.data?.message ??
  error?.originalError?.response?.data?.message ??
  error?.message ??
  error?.error ??
  fallbackMessage;

export const formatInsuranceDateTime = (value) => {
  if (!value) return '-';

  const normalized = String(value).replace('T', ' ');
  const [datePart, timePart = ''] = normalized.split(' ');
  const [year, month, day] = datePart.split('-');
  const shortTime = timePart.slice(0, 5);

  if (!year || !month || !day) return normalized;
  return shortTime
    ? `${year}.${month}.${day} ${shortTime}`
    : `${year}.${month}.${day}`;
};

export const formatInsuranceAnalysisRange = (startDate, endDate) => {
  const start = formatInsuranceDateTime(startDate).split(' ')[0];
  const end = formatInsuranceDateTime(endDate).split(' ')[0];

  if (start === '-' || end === '-') return '-';
  return `${start} ~ ${end}`;
};
