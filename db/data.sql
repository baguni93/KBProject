-- 3. 은행 테이블
INSERT INTO bank_tbl (
    bank_code,
    bank_name,
    bank_logo_name,
    use_yn
) VALUES
('004', 'KB국민은행', 'kb.png', 'Y'),
('088', '신한은행', 'shinhan.png', 'Y'),
('081', '하나은행', 'hana.png', 'Y'),
('020', '우리은행', 'woori.png', 'Y'),
('011', 'NH농협은행', 'nh.png', 'Y'),
('003', 'IBK기업은행', 'ibk.png', 'Y'),
('090', '카카오뱅크', 'kakaobank.png', 'Y'),
('092', '토스뱅크', 'tossbank.png', 'Y'),
('089', '케이뱅크', 'kbank.png', 'Y'),
('023', 'SC제일은행', 'sc.png', 'Y');

-- 4. 약관 정보 테이블
INSERT INTO agreement_tbl (
    agreement_type,
    agreement_name,
    agreement_content,
    required_yn,
    use_yn
) VALUES
(
    'SERVICE',
    '서비스 이용약관',
    '본 약관은 서비스 이용에 관한 기본적인 사항을 규정합니다. 회원은 서비스를 이용함으로써 본 약관에 동의한 것으로 간주됩니다.',
    'Y',
    'Y'
),
(
    'PRIVACY',
    '개인정보 처리방침',
    '회사는 관련 법령에 따라 회원의 개인정보를 안전하게 보호하며, 수집·이용 목적 범위 내에서만 개인정보를 처리합니다.',
    'Y',
    'Y'
);

-- 7.소비 카테고리 테이블 정의서
INSERT INTO spending_category_tbl (
    spending_category_id,
    category_name,
    parent_category_id
) VALUES
-- 대분류
(1, '식비', NULL),
(2, '카페', NULL),
(3, '생활', NULL),
(4, '온라인쇼핑', NULL),
(5, '뷰티/미용', NULL),
(6, '교통', NULL),
(7, '자동차', NULL),
(8, '주거/통신', NULL),
(9, '금융', NULL),
(10, '여행', NULL),
(11, '교육', NULL),
(12, '반려동물', NULL),
(13, '병원', NULL),

-- 병원 하위 카테고리
(14, '산부인과', 13),
(15, '안과', 13),
(16, '내과', 13),
(17, '정형외과', 13),
(18, '한의원', 13),
(19, '치과', 13),
(20, '소아과', 13);


-- 15. KB 카드 상품 설명 테이블 정의서

INSERT INTO kb_card_product_tbl (
    card_name,
    card_type,
    card_description,
    card_image,
    application,
    annual_fee
) VALUES
(
    'KB국민 My WE:SH 카드',
    'CREDIT',
    '나만을 위한 맞춤형 혜택을 제공하는 신용카드입니다. 음식, 배달, 커피 등 일상생활 영역에서 할인 혜택을 받을 수 있습니다.',
    'my_wesh_card.png',
    'my_wesh_apply.html',
    15000
),
(
    'KB국민 톡톡O 카드',
    'CREDIT',
    '온라인 쇼핑과 디지털 생활에 특화된 카드로 쇼핑몰, OTT 등 다양한 영역에서 할인 혜택을 제공합니다.',
    'toktok_o_card.png',
    'toktok_o_apply.html',
    12000
),
(
    'KB국민 노리2 체크카드',
    'CHECK',
    '대중교통, 카페, 편의점 등 생활 밀착 영역에서 할인 혜택을 제공하는 체크카드입니다.',
    'nori2_card.png',
    'nori2_apply.html',
    0
),
(
    'KB국민 직장인보너스체크카드',
    'CHECK',
    '직장인을 위한 생활 할인형 체크카드로 교통, 외식, 쇼핑 영역에서 혜택을 제공합니다.',
    'bonus_check_card.png',
    'bonus_check_apply.html',
    0
);

--  16. 카드 혜택 테이블
INSERT INTO card_benefit_tbl (
    card_product_id,
    spending_category_id,
    benefit_name,
    benefit_amount,
    benefit_rate,
    monthly_limit,
    minimum_spending_amount,
    benefit_description
) VALUES
(
    1,
    2,
    '카페 이용 할인',
    NULL,
    10.00,
    10000,
    300000,
    '스타벅스 등 카페 이용 시 10% 할인 혜택을 제공합니다.'
),
(
    1,
    1,
    '음식점 할인',
    1000,
    NULL,
    15000,
    300000,
    '음식점 결제 건당 1,000원 할인 혜택을 제공합니다.'
),
(
    2,
    4,
    '온라인 쇼핑 할인',
    NULL,
    10.00,
    20000,
    400000,
    '온라인 쇼핑몰 이용 시 10% 할인 혜택을 제공합니다.'
),
(
    2,
    13,
    '병원 이용 할인',
    NULL,
    5.00,
    10000,
    400000,
    '병원 및 의료 관련 업종 이용 시 5% 할인 혜택을 제공합니다.'
),
(
    3,
    6,
    '대중교통 할인',
    NULL,
    10.00,
    5000,
    200000,
    '버스와 지하철 이용 금액의 10% 할인 혜택을 제공합니다.'
),
(
    3,
    2,
    '카페 할인',
    500,
    NULL,
    5000,
    200000,
    '카페 이용 시 건당 500원 할인 혜택을 제공합니다.'
),
(
    4,
    7,
    '자동차 관련 할인',
    NULL,
    5.00,
    10000,
    300000,
    '주유 및 자동차 관련 가맹점 이용 시 5% 할인 혜택을 제공합니다.'
);

-- 18.KB 보험 상품 테이블
INSERT INTO kb_insurance_product_tbl (
    insurance_name,
    insurance_category,
    insurance_description,
    monthly_premium,
    insurance_image,
    application_url
) VALUES
(
    'KB손해보험 다이렉트 자동차보험',
    '자동차',
    '자동차 사고 발생 시 대인, 대물, 자기신체손해 등을 보장하는 온라인 전용 자동차보험입니다.',
    50000,
    'kb_auto_insurance.png',
    'kb_auto_apply.html'
),
(
    'KB손해보험 건강보험',
    '건강',
    '질병 및 상해로 인한 의료비 부담을 대비할 수 있는 종합 건강보험 상품입니다.',
    30000,
    'kb_health_insurance.png',
    'kb_health_apply.html'
),
(
    'KB손해보험 실손의료비보험',
    '실손',
    '병원 진료 및 치료 과정에서 발생하는 의료비를 보장하는 실손형 보험 상품입니다.',
    15000,
    'kb_silson_insurance.png',
    'kb_silson_apply.html'
),
(
    'KB손해보험 운전자보험',
    '운전자',
    '자동차 사고 발생 시 운전자에게 필요한 법률 비용 및 사고 관련 위험을 보장합니다.',
    12000,
    'kb_driver_insurance.png',
    'kb_driver_apply.html'
),
(
    'KB손해보험 여행자보험',
    '여행',
    '국내외 여행 중 발생할 수 있는 사고, 질병, 휴대품 손해 등을 보장합니다.',
    8000,
    'kb_travel_insurance.png',
    'kb_travel_apply.html'
);

-- 19.KB 보험 보장 항목 테이블

INSERT INTO kb_insurance_coverage_tbl (
    insurance_product_id,
    coverage_name,
    coverage_amount,
    coverage_description,
    coverage_limit
) VALUES
-- 자동차보험 (insurance_product_id = 1)
(
    1,
    '대인배상',
    100000000,
    '자동차 사고로 타인의 신체 피해 발생 시 손해를 보장합니다.',
    '사고당'
),
(
    1,
    '대물배상',
    200000000,
    '자동차 사고로 타인의 차량 및 재산 피해 발생 시 보장합니다.',
    '사고당'
),
(
    1,
    '자기신체사고',
    50000000,
    '자동차 사고로 본인에게 발생한 상해를 보장합니다.',
    '사고당'
),

-- 건강보험 (insurance_product_id = 2)
(
    2,
    '암 진단비',
    30000000,
    '암 진단 확정 시 진단비를 지급합니다.',
    '1회'
),
(
    2,
    '질병 입원비',
    50000,
    '질병으로 입원 치료 시 입원 일당을 지급합니다.',
    '입원일 기준'
),
(
    2,
    '수술비',
    1000000,
    '질병 및 상해 수술 발생 시 수술비를 지급합니다.',
    '수술별'
),

-- 실손의료비보험 (insurance_product_id = 3)
(
    3,
    '입원 의료비',
    50000000,
    '입원 치료 과정에서 발생한 의료비를 보장합니다.',
    '연간 한도'
),
(
    3,
    '통원 의료비',
    30000000,
    '외래 진료 및 통원 치료 비용을 보장합니다.',
    '연간 한도'
),

-- 운전자보험 (insurance_product_id = 4)
(
    4,
    '교통사고 처리 지원금',
    50000000,
    '교통사고 발생 시 형사 합의 관련 비용을 지원합니다.',
    '사고당'
),
(
    4,
    '벌금 보장',
    20000000,
    '자동차 사고 관련 벌금 발생 시 보장합니다.',
    '사고당'
),

-- 여행자보험 (insurance_product_id = 5)
(
    5,
    '여행 중 상해 의료비',
    10000000,
    '여행 중 발생한 상해 치료 비용을 보장합니다.',
    '사고당'
),
(
    5,
    '휴대품 손해',
    1000000,
    '여행 중 휴대품 분실 및 파손 발생 시 보장합니다.',
    '사고당'
);