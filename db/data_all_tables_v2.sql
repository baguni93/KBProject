-- =====================================================================
-- KBProject 전체 더미 데이터 (init(3).sql 기준)
-- 생성 기준:
--   * 부모/기준/1:1 성격 테이블: 약 3건
--   * 자식/이력/M 성격 테이블: 약 6건
--   * 총 55개 테이블 모두 INSERT 포함
-- 주의:
--   * 이 스크립트는 기존 데이터를 TRUNCATE 합니다.
--   * 반드시 init(3).sql 실행 후 사용하세요.
--   * tbl_member 테스트 계정: test1 / test2 / test3
--   * 모든 로그인 비밀번호: 1234 (BCrypt 해시 저장)
--   * init(3).sql의 point_transaction_tbl 제약 때문에 reason_type은 EVENT만 사용합니다.
-- =====================================================================

USE kbproject;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE linked_card_tbl;
TRUNCATE TABLE card_company_tbl;
TRUNCATE TABLE event_challenge_user_tbl;
TRUNCATE TABLE event_challenge_tbl;
TRUNCATE TABLE event_reward_receive_tbl;
TRUNCATE TABLE event_participation_tbl;
TRUNCATE TABLE event_reward_tbl;
TRUNCATE TABLE event_tbl;
TRUNCATE TABLE card_application_history_tbl;
TRUNCATE TABLE custom_image_tbl;
TRUNCATE TABLE file_image_tbl;
TRUNCATE TABLE card_asset_tbl;
TRUNCATE TABLE feed_comment_tbl;
TRUNCATE TABLE feed_like_tbl;
TRUNCATE TABLE feed_image_tbl;
TRUNCATE TABLE feed_tbl;
TRUNCATE TABLE receipt_memo_tbl;
TRUNCATE TABLE payment_token_tbl;
TRUNCATE TABLE registered_card_tbl;
TRUNCATE TABLE card_tbl;
TRUNCATE TABLE wallet_transaction_tbl;
TRUNCATE TABLE account_transaction_tbl;
TRUNCATE TABLE account_dummy_tbl;
TRUNCATE TABLE financial_transaction_tbl;
TRUNCATE TABLE notification_tbl;
TRUNCATE TABLE settlement_member_tbl;
TRUNCATE TABLE settlement_tbl;
TRUNCATE TABLE friend_tbl;
TRUNCATE TABLE friend_request_tbl;
TRUNCATE TABLE kb_insurance_recommendation_tbl;
TRUNCATE TABLE kb_insurance_coverage_tbl;
TRUNCATE TABLE kb_insurance_product_tbl;
TRUNCATE TABLE card_recommendation_tbl;
TRUNCATE TABLE card_benefit_tbl;
TRUNCATE TABLE kb_card_product_tbl;
TRUNCATE TABLE spending_analysis_category_tbl;
TRUNCATE TABLE spending_analysis_tbl;
TRUNCATE TABLE point_conversion_history_tbl;
TRUNCATE TABLE attendance_tbl;
TRUNCATE TABLE user_random_box_tbl;
TRUNCATE TABLE point_transaction_tbl;
TRUNCATE TABLE point_wallet_tbl;
TRUNCATE TABLE spending_category_tbl;
TRUNCATE TABLE refresh_token_tbl;
TRUNCATE TABLE notification_setting_tbl;
TRUNCATE TABLE profile_tbl;
TRUNCATE TABLE verification_tbl;
TRUNCATE TABLE wallet_tbl;
TRUNCATE TABLE user_agreement_tbl;
TRUNCATE TABLE agreement_tbl;
TRUNCATE TABLE linked_account_tbl;
TRUNCATE TABLE bank_tbl;
TRUNCATE TABLE user_tbl;
TRUNCATE TABLE tbl_member_auth;
TRUNCATE TABLE tbl_member;
SET FOREIGN_KEY_CHECKS = 1;

START TRANSACTION;

-- ---------------------------------------------------------------------
-- Spring Security 임시 회원: 3건
-- ---------------------------------------------------------------------
INSERT INTO tbl_member (
    username,
    password,
    email,
    reg_date,
    update_date
) VALUES
('test1', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'test1@kbproject.local', '2026-07-01 09:00:00', '2026-07-01 09:00:00'),
('test2', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'test2@kbproject.local', '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
('test3', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'test3@kbproject.local', '2026-07-03 09:00:00', '2026-07-03 09:00:00');

-- ---------------------------------------------------------------------
-- 회원 권한: 6건
-- ---------------------------------------------------------------------
INSERT INTO tbl_member_auth (
    username,
    auth
) VALUES
('test1', 'ROLE_USER'),
('test1', 'ROLE_ADMIN'),
('test2', 'ROLE_USER'),
('test2', 'ROLE_MANAGER'),
('test3', 'ROLE_USER'),
('test3', 'ROLE_TESTER');

-- ---------------------------------------------------------------------
-- 서비스 회원: 3건
-- ---------------------------------------------------------------------
INSERT INTO user_tbl (
    user_id,
    user_name,
    birth_date,
    phone_number,
    pin_password,
    user_status,
    created_at,
    updated_at,
    withdrawn_at,
    last_login_at
) VALUES
(1, '테스트회원1', '20000115', '01011112222', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'ACTIVE', '2026-07-01 09:00:00', '2026-07-24 08:30:00', NULL, '2026-07-24 08:30:00'),
(2, '테스트회원2', '19990321', '01022223333', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'ACTIVE', '2026-07-02 10:00:00', '2026-07-23 19:10:00', NULL, '2026-07-23 19:10:00'),
(3, '테스트회원3', '20010509', '01033334444', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'ACTIVE', '2026-07-03 11:00:00', '2026-07-22 14:20:00', NULL, '2026-07-22 14:20:00');

-- ---------------------------------------------------------------------
-- 은행: 3건
-- ---------------------------------------------------------------------
INSERT INTO bank_tbl (
    bank_code,
    bank_name,
    bank_logo_name,
    use_yn
) VALUES
('004', 'KB국민은행', 'kb.png', 'Y'),
('088', '신한은행', 'shinhan.png', 'Y'),
('081', '하나은행', 'hana.png', 'Y');

-- ---------------------------------------------------------------------
-- 연결 계좌: 6건
-- ---------------------------------------------------------------------
INSERT INTO linked_account_tbl (
    linked_account_id,
    user_id,
    bank_code,
    account_number,
    account_holder,
    primary_yn,
    connection_status
) VALUES
(1, 1, '004', '110-111-111111', '테스트회원1', 'Y', 'CONNECTED'),
(2, 1, '088', '110-111-222222', '테스트회원1', 'N', 'CONNECTED'),
(3, 2, '088', '110-222-111111', '테스트회원2', 'Y', 'CONNECTED'),
(4, 2, '081', '110-222-222222', '테스트회원2', 'N', 'CONNECTED'),
(5, 3, '081', '110-333-111111', '테스트회원3', 'Y', 'CONNECTED'),
(6, 3, '004', '110-333-222222', '테스트회원3', 'N', 'CONNECTED');

-- ---------------------------------------------------------------------
-- 약관: 3건
-- ---------------------------------------------------------------------
INSERT INTO agreement_tbl (
    agreement_id,
    agreement_type,
    agreement_name,
    agreement_content,
    required_yn,
    use_yn
) VALUES
(1, 'SERVICE', '서비스 이용약관', '서비스 이용에 필요한 기본 약관입니다.', 'Y', 'Y'),
(2, 'PRIVACY', '개인정보 처리방침', '개인정보 수집 및 이용에 관한 약관입니다.', 'Y', 'Y'),
(3, 'MARKETING', '마케팅 정보 수신 동의', '이벤트와 혜택 정보 수신을 위한 선택 약관입니다.', 'N', 'Y');

-- ---------------------------------------------------------------------
-- 회원 약관 동의: 6건
-- ---------------------------------------------------------------------
INSERT INTO user_agreement_tbl (
    user_agreement_id,
    user_id,
    agreement_id,
    agreed_yn,
    agreed_at
) VALUES
(1, 1, 1, 'Y', '2026-07-01 09:05:00'),
(2, 1, 2, 'Y', '2026-07-01 09:05:10'),
(3, 2, 1, 'Y', '2026-07-02 10:05:00'),
(4, 2, 2, 'Y', '2026-07-02 10:05:10'),
(5, 3, 1, 'Y', '2026-07-03 11:05:00'),
(6, 3, 3, 'N', '2026-07-03 11:05:10');

-- ---------------------------------------------------------------------
-- 전자지갑: 3건
-- ---------------------------------------------------------------------
INSERT INTO wallet_tbl (
    wallet_id,
    user_id,
    balance,
    wallet_status
) VALUES
(1, 1, 107000, 'ACTIVE'),
(2, 2, 185000, 'ACTIVE'),
(3, 3, 145000, 'ACTIVE');

-- ---------------------------------------------------------------------
-- 휴대폰 인증: 6건
-- ---------------------------------------------------------------------
INSERT INTO verification_tbl (
    verification_id,
    user_id,
    user_name,
    birth_date,
    carrier_code,
    phone_number,
    verification_code,
    verification_purpose,
    requested_at,
    verified_yn,
    fail_count
) VALUES
(1, 1, '테스트회원1', '20000115', 'SKT', '01011112222', 'ENC-111111', 'SIGN_UP', '2026-07-01 08:55:00', 'Y', 0),
(2, 2, '테스트회원2', '19990321', 'KT', '01022223333', 'ENC-222222', 'SIGN_UP', '2026-07-02 09:55:00', 'Y', 0),
(3, 3, '테스트회원3', '20010509', 'LGU', '01033334444', 'ENC-333333', 'SIGN_UP', '2026-07-03 10:55:00', 'Y', 0),
(4, 1, '테스트회원1', '20000115', 'SKT', '01011112222', 'ENC-444444', 'PIN_RESET', '2026-07-20 10:00:00', 'Y', 1),
(5, 2, '테스트회원2', '19990321', 'KT_MVNO', '01022223333', 'ENC-555555', 'PIN_RESET', '2026-07-21 11:00:00', 'N', 2),
(6, NULL, '가입대기회원', '20021212', 'LGU_MVNO', '01099998888', 'ENC-666666', 'SIGN_UP', '2026-07-24 09:00:00', 'N', 0);

-- ---------------------------------------------------------------------
-- 프로필: 3건
-- ---------------------------------------------------------------------
INSERT INTO profile_tbl (
    profile_id,
    user_id,
    nickname,
    introduction,
    original_name,
    stored_name,
    created_at,
    updated_at
) VALUES
(1, 1, '노랑지갑', '포인트를 모으는 중입니다.', 'profile1.png', 'profile_1.png', '2026-07-01 09:10:00', '2026-07-20 12:00:00'),
(2, 2, '절약왕', '합리적인 소비를 좋아합니다.', 'profile2.png', 'profile_2.png', '2026-07-02 10:10:00', '2026-07-21 12:00:00'),
(3, 3, '여행저축러', '여행 자금을 모으는 중입니다.', 'profile3.png', 'profile_3.png', '2026-07-03 11:10:00', '2026-07-22 12:00:00');

-- ---------------------------------------------------------------------
-- 알림 설정: 3건
-- ---------------------------------------------------------------------
INSERT INTO notification_setting_tbl (
    notification_setting_id,
    user_id,
    finance_notification_yn,
    reward_notification_yn,
    event_benefit_notification_yn,
    updated_at
) VALUES
(1, 1, 'Y', 'Y', 'Y', '2026-07-20 12:00:00'),
(2, 2, 'Y', 'N', 'N', '2026-07-21 12:00:00'),
(3, 3, 'N', 'Y', 'Y', '2026-07-22 12:00:00');

-- ---------------------------------------------------------------------
-- 리프레시 토큰: 6건
-- ---------------------------------------------------------------------
INSERT INTO refresh_token_tbl (
    refresh_token_id,
    user_id,
    refresh_token,
    issued_at,
    expires_at
) VALUES
(1, 1, 'refresh-token-user1-a', '2026-07-23 08:00:00', '2026-08-23 08:00:00'),
(2, 1, 'refresh-token-user1-b', '2026-07-24 08:00:00', '2026-08-24 08:00:00'),
(3, 2, 'refresh-token-user2-a', '2026-07-23 09:00:00', '2026-08-23 09:00:00'),
(4, 2, 'refresh-token-user2-b', '2026-07-24 09:00:00', '2026-08-24 09:00:00'),
(5, 3, 'refresh-token-user3-a', '2026-07-23 10:00:00', '2026-08-23 10:00:00'),
(6, 3, 'refresh-token-user3-b', '2026-07-24 10:00:00', '2026-08-24 10:00:00');

-- ---------------------------------------------------------------------
-- 소비 카테고리: 6건
-- ---------------------------------------------------------------------
INSERT INTO spending_category_tbl (
    spending_category_id,
    category_name,
    parent_category_id
) VALUES
(1, '식비', NULL),
(2, '카페', NULL),
(3, '교통', NULL),
(4, '외식', 1),
(5, '배달', 1),
(6, '대중교통', 3);

-- ---------------------------------------------------------------------
-- 포인트 지갑: 3건
-- ---------------------------------------------------------------------
INSERT INTO point_wallet_tbl (
    point_wallet_id,
    user_id,
    point_balance,
    updated_at
) VALUES
(1, 1, 4000, '2026-07-24 09:10:00'),
(2, 2, 5000, '2026-07-24 09:20:00'),
(3, 3, 7500, '2026-07-24 09:30:00');

-- ---------------------------------------------------------------------
-- 포인트 거래내역: 6건
-- ---------------------------------------------------------------------
INSERT INTO point_transaction_tbl (
    point_transaction_id,
    point_wallet_id,
    transaction_type,
    point_amount,
    reason_type,
    created_at
) VALUES
(1, 1, 'EARN', 5000, 'EVENT', '2026-07-20 09:00:00'),
(2, 1, 'USE', 1000, 'EVENT', '2026-07-24 09:10:00'),
(3, 2, 'EARN', 7000, 'EVENT', '2026-07-21 09:00:00'),
(4, 2, 'USE', 2000, 'EVENT', '2026-07-24 09:20:00'),
(5, 3, 'EARN', 10000, 'EVENT', '2026-07-22 09:00:00'),
(6, 3, 'USE', 2500, 'EVENT', '2026-07-24 09:30:00');

-- ---------------------------------------------------------------------
-- 사용자 랜덤박스: 6건
-- ---------------------------------------------------------------------
INSERT INTO user_random_box_tbl (
    user_random_box_id,
    user_id,
    issue_reason,
    box_status,
    reward_point,
    issued_at,
    opened_at
) VALUES
(1, 1, 'ATTENDANCE_EVENT', 'OPENED', 500, '2026-07-18 09:00:00', '2026-07-18 09:05:00'),
(2, 1, 'MISSION_EVENT', 'UNOPENED', NULL, '2026-07-24 09:00:00', NULL),
(3, 2, 'ATTENDANCE_EVENT', 'OPENED', 1000, '2026-07-19 09:00:00', '2026-07-19 09:05:00'),
(4, 2, 'MISSION_EVENT', 'UNOPENED', NULL, '2026-07-24 09:10:00', NULL),
(5, 3, 'ATTENDANCE_EVENT', 'OPENED', 300, '2026-07-20 09:00:00', '2026-07-20 09:05:00'),
(6, 3, 'MISSION_EVENT', 'UNOPENED', NULL, '2026-07-24 09:20:00', NULL);

-- ---------------------------------------------------------------------
-- 출석 내역: 6건
-- ---------------------------------------------------------------------
INSERT INTO attendance_tbl (
    attendance_id,
    user_id,
    attendance_date
) VALUES
(1, 1, '2026-07-22'),
(2, 1, '2026-07-23'),
(3, 2, '2026-07-22'),
(4, 2, '2026-07-24'),
(5, 3, '2026-07-23'),
(6, 3, '2026-07-24');

-- ---------------------------------------------------------------------
-- 포인트 전환 내역: 6건
-- ---------------------------------------------------------------------
INSERT INTO point_conversion_history_tbl (
    point_conversion_id,
    user_id,
    point_wallet_id,
    linked_account_id,
    converted_point,
    converted_at
) VALUES
(1, 1, 1, 1, 500, '2026-07-18 12:00:00'),
(2, 1, 1, 2, 1000, '2026-07-19 12:00:00'),
(3, 2, 2, 3, 500, '2026-07-20 12:00:00'),
(4, 2, 2, 4, 1000, '2026-07-21 12:00:00'),
(5, 3, 3, 5, 500, '2026-07-22 12:00:00'),
(6, 3, 3, 6, 1000, '2026-07-23 12:00:00');

-- ---------------------------------------------------------------------
-- 소비 분석: 6건
-- ---------------------------------------------------------------------
INSERT INTO spending_analysis_tbl (
    spending_analysis_id,
    user_id,
    analysis_period,
    representative_category_id,
    ai_title,
    ai_analysis_summary,
    created_at
) VALUES
(1, 1, 1, 1, '한 달 외식 탐험가', '최근 한 달 동안 식비 지출 비중이 가장 높습니다.', '2026-07-01 00:00:00'),
(2, 1, 3, 2, '카페 단골 손님', '최근 세 달 동안 카페 이용이 꾸준히 증가했습니다.', '2026-07-02 00:00:00'),
(3, 2, 1, 3, '대중교통 마스터', '교통비 비중이 높고 이동이 잦은 소비 패턴입니다.', '2026-07-03 00:00:00'),
(4, 2, 12, 1, '알뜰 식비 관리자', '연간 식비가 안정적으로 관리되고 있습니다.', '2026-07-04 00:00:00'),
(5, 3, 3, 2, '커피와 함께하는 사람', '카페와 간식 관련 결제가 많은 편입니다.', '2026-07-05 00:00:00'),
(6, 3, 12, 3, '움직이는 저축러', '교통 지출과 저축이 균형을 이루고 있습니다.', '2026-07-06 00:00:00');

-- ---------------------------------------------------------------------
-- 카테고리별 분석: 6건
-- ---------------------------------------------------------------------
INSERT INTO spending_analysis_category_tbl (
    analysis_category_id,
    spending_analysis_id,
    spending_category_id,
    spending_amount,
    spending_ratio,
    transaction_count,
    created_at
) VALUES
(1, 1, 1, 180000, 60.00, 12, '2026-07-01 00:05:00'),
(2, 1, 2, 120000, 40.00, 8, '2026-07-01 00:05:00'),
(3, 2, 2, 260000, 65.00, 20, '2026-07-02 00:05:00'),
(4, 2, 3, 140000, 35.00, 14, '2026-07-02 00:05:00'),
(5, 3, 3, 90000, 75.00, 30, '2026-07-03 00:05:00'),
(6, 3, 1, 30000, 25.00, 5, '2026-07-03 00:05:00');

-- ---------------------------------------------------------------------
-- KB 카드 상품: 3건
-- ---------------------------------------------------------------------
INSERT INTO kb_card_product_tbl (
    card_product_id,
    card_name,
    card_type,
    card_description,
    card_image,
    application,
    annual_fee,
    created_at
) VALUES
(1, 'KB국민 My WE:SH 카드', 'CREDIT', '음식과 카페 생활 영역에 특화된 카드입니다.', 'my_wesh.png', 'my_wesh_apply.html', 15000, '2026-07-01 09:00:00'),
(2, 'KB국민 톡톡O 카드', 'CREDIT', '온라인과 생활 할인에 특화된 카드입니다.', 'toktok_o.png', 'toktok_o_apply.html', 12000, '2026-07-01 09:10:00'),
(3, 'KB국민 노리2 체크카드', 'CHECK', '교통과 카페 할인을 제공하는 체크카드입니다.', 'nori2.png', 'nori2_apply.html', 0, '2026-07-01 09:20:00');

-- ---------------------------------------------------------------------
-- 카드 혜택: 6건
-- ---------------------------------------------------------------------
INSERT INTO card_benefit_tbl (
    card_benefit_id,
    card_product_id,
    spending_category_id,
    benefit_name,
    benefit_amount,
    benefit_rate,
    monthly_limit,
    minimum_spending_amount,
    benefit_description,
    created_at
) VALUES
(1, 1, 1, '음식점 할인', 1000, NULL, 15000, 300000, '음식점 결제 건당 1,000원 할인', '2026-07-01 10:00:00'),
(2, 1, 2, '카페 할인', NULL, 10.00, 10000, 300000, '카페 이용 금액의 10% 할인', '2026-07-01 10:10:00'),
(3, 2, 1, '배달 할인', NULL, 8.00, 12000, 250000, '배달 결제 금액의 8% 할인', '2026-07-01 10:20:00'),
(4, 2, 3, '교통 할인', 500, NULL, 8000, 250000, '교통 결제 건당 500원 할인', '2026-07-01 10:30:00'),
(5, 3, 2, '카페 캐시백', 300, NULL, 5000, 100000, '카페 결제 건당 300원 캐시백', '2026-07-01 10:40:00'),
(6, 3, 3, '대중교통 할인', NULL, 10.00, 5000, 100000, '대중교통 이용 금액의 10% 할인', '2026-07-01 10:50:00');

-- ---------------------------------------------------------------------
-- 카드 추천: 6건
-- ---------------------------------------------------------------------
INSERT INTO card_recommendation_tbl (
    card_recommendation_id,
    spending_analysis_id,
    card_product_id,
    recommendation_rank,
    expected_benefit_amount,
    created_at
) VALUES
(1, 1, 1, 1, 12000, '2026-07-01 01:00:00'),
(2, 1, 2, 2, 8000, '2026-07-01 01:00:00'),
(3, 2, 1, 1, 15000, '2026-07-02 01:00:00'),
(4, 3, 3, 1, 7000, '2026-07-03 01:00:00'),
(5, 4, 2, 1, 9000, '2026-07-04 01:00:00'),
(6, 5, 3, 1, 6000, '2026-07-05 01:00:00');

-- ---------------------------------------------------------------------
-- KB 보험 상품: 3건
-- ---------------------------------------------------------------------
INSERT INTO kb_insurance_product_tbl (
    insurance_product_id,
    insurance_name,
    insurance_category,
    insurance_description,
    monthly_premium,
    insurance_image,
    application_url,
    created_at
) VALUES
(1, 'KB 5.10.10 플러스 건강보험', '건강', '질병과 상해 위험을 보장하는 건강보험입니다.', 30000, 'health.png', 'health_apply.html', '2026-07-01 09:00:00'),
(2, 'KB손보 실손의료비보장보험', '실손', '실제 발생한 의료비를 보장하는 상품입니다.', 15000, 'medical.png', 'medical_apply.html', '2026-07-01 09:10:00'),
(3, 'KB손해보험 여행자보험', '여행자', '여행 중 발생할 수 있는 사고를 보장합니다.', 8000, 'travel.png', 'travel_apply.html', '2026-07-01 09:20:00');

-- ---------------------------------------------------------------------
-- 보험 보장 항목: 6건
-- ---------------------------------------------------------------------
INSERT INTO kb_insurance_coverage_tbl (
    insurance_coverage_id,
    insurance_product_id,
    coverage_name,
    coverage_amount,
    coverage_description,
    coverage_limit,
    created_at
) VALUES
(1, 1, '암 진단비', 30000000, '약관상 암 진단 확정 시 보장', '최초 1회', '2026-07-01 10:00:00'),
(2, 1, '입원비', 100000, '질병 입원 시 일당 보장', '연 120일', '2026-07-01 10:10:00'),
(3, 2, '입원의료비', 50000000, '입원 치료 시 실제 부담 의료비 보장', '연간 한도', '2026-07-01 10:20:00'),
(4, 2, '통원의료비', 300000, '통원 치료 시 실제 부담 의료비 보장', '1일 한도', '2026-07-01 10:30:00'),
(5, 3, '해외상해의료비', 20000000, '해외여행 중 상해 치료비 보장', '여행 기간', '2026-07-01 10:40:00'),
(6, 3, '휴대품 손해', 1000000, '휴대품 도난 및 파손 손해 보장', '품목별 한도', '2026-07-01 10:50:00');

-- ---------------------------------------------------------------------
-- 보험 추천: 6건
-- ---------------------------------------------------------------------
INSERT INTO kb_insurance_recommendation_tbl (
    insurance_recommendation_id,
    spending_analysis_id,
    insurance_product_id,
    recommendation_reason,
    created_at
) VALUES
(1, 1, 1, '의료와 생활 소비를 함께 고려해 건강보험을 추천합니다.', '2026-07-01 02:00:00'),
(2, 1, 2, '병원비 부담을 줄이기 위해 실손보험을 추천합니다.', '2026-07-01 02:10:00'),
(3, 2, 2, '정기적인 의료비 지출 가능성에 대비할 수 있습니다.', '2026-07-02 02:00:00'),
(4, 3, 3, '이동이 잦은 소비 패턴을 고려해 여행자보험을 추천합니다.', '2026-07-03 02:00:00'),
(5, 4, 1, '장기적인 건강 관리에 적합한 상품입니다.', '2026-07-04 02:00:00'),
(6, 5, 3, '여가와 외출 소비가 많아 여행 보장이 유용합니다.', '2026-07-05 02:00:00');

-- ---------------------------------------------------------------------
-- 친구 요청: 6건
-- ---------------------------------------------------------------------
INSERT INTO friend_request_tbl (
    request_id,
    requester_id,
    receiver_id,
    status,
    created_at,
    updated_at
) VALUES
(1, 1, 2, 'ACCEPT', '2026-07-10 10:00:00', '2026-07-10 10:05:00'),
(2, 1, 3, 'REQUEST', '2026-07-11 10:00:00', '2026-07-11 10:00:00'),
(3, 2, 1, 'ACCEPT', '2026-07-12 10:00:00', '2026-07-12 10:05:00'),
(4, 2, 3, 'REJECT', '2026-07-13 10:00:00', '2026-07-13 10:05:00'),
(5, 3, 1, 'CANCEL', '2026-07-14 10:00:00', '2026-07-14 10:05:00'),
(6, 3, 2, 'REQUEST', '2026-07-15 10:00:00', '2026-07-15 10:00:00');

-- ---------------------------------------------------------------------
-- 친구: 6건
-- ---------------------------------------------------------------------
INSERT INTO friend_tbl (
    friend_id,
    user_id,
    friend_user_id,
    created_at
) VALUES
(1, 1, 2, '2026-07-10 10:05:00'),
(2, 2, 1, '2026-07-10 10:05:00'),
(3, 1, 3, '2026-07-16 10:05:00'),
(4, 3, 1, '2026-07-16 10:05:00'),
(5, 2, 3, '2026-07-17 10:05:00'),
(6, 3, 2, '2026-07-17 10:05:00');

-- ---------------------------------------------------------------------
-- 정산: 3건
-- ---------------------------------------------------------------------
INSERT INTO settlement_tbl (
    settlement_id,
    requester_id,
    title,
    total_amount,
    status,
    created_at,
    settlement_type,
    spending_category_id,
    completed_at
) VALUES
(1, 1, '저녁 식사 정산', 30000, 'REQUEST', '2026-07-20 19:00:00', 'EQUAL', 1, NULL),
(2, 2, '카페 모임 정산', 24000, 'COMPLETE', '2026-07-21 15:00:00', 'EQUAL', 2, '2026-07-21 18:00:00'),
(3, 3, '택시비 정산', 18000, 'CANCEL', '2026-07-22 23:00:00', 'UNEQUAL', 3, NULL);

-- ---------------------------------------------------------------------
-- 정산 참여자: 6건
-- ---------------------------------------------------------------------
INSERT INTO settlement_member_tbl (
    settlement_member_id,
    settlement_id,
    user_id,
    amount,
    status,
    created_at,
    last_reminder_date,
    completed_at
) VALUES
(1, 1, 2, 15000, 'REQUEST', '2026-07-20 19:01:00', '2026-07-21 09:00:00', NULL),
(2, 1, 3, 15000, 'REQUEST', '2026-07-20 19:01:00', '2026-07-21 09:00:00', NULL),
(3, 2, 1, 12000, 'COMPLETE', '2026-07-21 15:01:00', '2026-07-21 16:00:00', '2026-07-21 17:30:00'),
(4, 2, 3, 12000, 'COMPLETE', '2026-07-21 15:01:00', '2026-07-21 16:00:00', '2026-07-21 18:00:00'),
(5, 3, 1, 8000, 'CANCEL', '2026-07-22 23:01:00', '2026-07-23 09:00:00', NULL),
(6, 3, 2, 10000, 'CANCEL', '2026-07-22 23:01:00', '2026-07-23 09:00:00', NULL);

-- ---------------------------------------------------------------------
-- 알림: 6건
-- ---------------------------------------------------------------------
INSERT INTO notification_tbl (
    notification_id,
    receiver_id,
    sender_id,
    notification_type,
    target_id,
    is_read,
    created_at
) VALUES
(1, 2, 1, 'FRIEND_REQUEST', 1, 'Y', '2026-07-10 10:00:00'),
(2, 3, 1, 'FRIEND_REQUEST', 2, 'N', '2026-07-11 10:00:00'),
(3, 2, 1, 'SETTLEMENT', 1, 'N', '2026-07-20 19:01:00'),
(4, 3, 2, 'COMMENT', 1, 'Y', '2026-07-21 20:00:00'),
(5, 1, 3, 'LIKE', 2, 'N', '2026-07-22 20:00:00'),
(6, 1, 2, 'SETTLEMENT', 2, 'Y', '2026-07-23 20:00:00');

-- ---------------------------------------------------------------------
-- 통합 금융 거래: 6건
-- ---------------------------------------------------------------------
INSERT INTO financial_transaction_tbl (
    transaction_id,
    parent_transaction_id,
    user_id,
    receive_id,
    transaction_type,
    source_type,
    target_type,
    transaction_status,
    amount,
    spending_category_id,
    created_at
) VALUES
(1, NULL, 1, NULL, 'CHARGE', 'ACCOUNT', 'WALLET', 'SUCCESS', 10000, NULL, '2026-07-20 09:00:00'),
(2, NULL, 1, 2, 'TRANSFER', 'WALLET', 'ACCOUNT', 'SUCCESS', 8000, NULL, '2026-07-20 10:00:00'),
(3, NULL, 2, NULL, 'CHARGE', 'ACCOUNT', 'WALLET', 'SUCCESS', 12000, NULL, '2026-07-21 09:00:00'),
(4, NULL, 2, 1, 'SETTLEMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 15000, 1, '2026-07-21 18:00:00'),
(5, NULL, 3, 1, 'TRANSFER', 'ACCOUNT', 'WALLET', 'SUCCESS', 20000, NULL, '2026-07-22 11:00:00'),
(6, NULL, 3, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 5000, 3, '2026-07-23 08:00:00');

-- ---------------------------------------------------------------------
-- 계좌 더미: 6건
-- ---------------------------------------------------------------------
INSERT INTO account_dummy_tbl (
    account_id,
    user_id,
    bank_code,
    account_number,
    owner_name,
    balance,
    account_password,
    created_at
) VALUES
(1, 1, '004', '111-001-000001', '테스트회원1', 510000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', '2026-07-01 09:20:00'),
(2, 1, '088', '111-001-000002', '테스트회원1', 250000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', '2026-07-01 09:30:00'),
(3, 2, '088', '222-002-000001', '테스트회원2', 305000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', '2026-07-02 10:20:00'),
(4, 2, '081', '222-002-000002', '테스트회원2', 180000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', '2026-07-02 10:30:00'),
(5, 3, '081', '333-003-000001', '테스트회원3', 680000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', '2026-07-03 11:20:00'),
(6, 3, '004', '333-003-000002', '테스트회원3', 205000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', '2026-07-03 11:30:00');

-- ---------------------------------------------------------------------
-- 계좌 거래 상세: 6건
-- ---------------------------------------------------------------------
INSERT INTO account_transaction_tbl (
    account_transaction_id,
    transaction_id,
    user_id,
    direction,
    account_id,
    balance_before,
    balance_after
) VALUES
(1, 1, 1, 'DEBIT', 1, 500000, 490000),
(2, 2, 2, 'CREDIT', 3, 297000, 305000),
(3, 3, 2, 'DEBIT', 3, 309000, 297000),
(4, 4, 1, 'CREDIT', 1, 495000, 510000),
(5, 5, 3, 'DEBIT', 5, 700000, 680000),
(6, 6, 3, 'CREDIT', 6, 200000, 205000);

-- ---------------------------------------------------------------------
-- 지갑 거래 상세: 6건
-- ---------------------------------------------------------------------
INSERT INTO wallet_transaction_tbl (
    wallet_transaction_id,
    transaction_id,
    user_id,
    direction,
    wallet_id,
    balance_before,
    balance_after
) VALUES
(1, 1, 1, 'CREDIT', 1, 90000, 100000),
(2, 2, 1, 'DEBIT', 1, 100000, 92000),
(3, 3, 2, 'CREDIT', 2, 188000, 200000),
(4, 4, 2, 'DEBIT', 2, 200000, 185000),
(5, 5, 1, 'CREDIT', 1, 92000, 112000),
(6, 6, 3, 'DEBIT', 3, 150000, 145000);

-- ---------------------------------------------------------------------
-- 카드 원장: 3건
-- ---------------------------------------------------------------------
INSERT INTO card_tbl (
    card_code,
    account_id,
    card_img_file_name,
    card_num,
    expiry_date,
    cvv
) VALUES
('KB-CARD-001', 1, 'card_001.png', 'ENC-CARD-1111', '12/30', 'ENC-111'),
('KB-CARD-002', 3, 'card_002.png', 'ENC-CARD-2222', '11/30', 'ENC-222'),
('KB-CARD-003', 5, 'card_003.png', 'ENC-CARD-3333', '10/30', 'ENC-333');

-- ---------------------------------------------------------------------
-- 등록 실물카드: 6건
-- ---------------------------------------------------------------------
INSERT INTO registered_card_tbl (
    card_id,
    account_id,
    user_id,
    card_num,
    expiry_date,
    cvv,
    card_password,
    represent_yn,
    created_at,
    delete_yn
) VALUES
(1, 1, 1, 'ENC-REG-1111', '12/30', 'ENC-111', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'Y', '2026-07-01 09:40:00', 'N'),
(2, 2, 1, 'ENC-REG-1112', '09/30', 'ENC-112', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'N', '2026-07-01 09:50:00', 'N'),
(3, 3, 2, 'ENC-REG-2221', '11/30', 'ENC-221', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'Y', '2026-07-02 10:40:00', 'N'),
(4, 4, 2, 'ENC-REG-2222', '08/30', 'ENC-222', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'N', '2026-07-02 10:50:00', 'N'),
(5, 5, 3, 'ENC-REG-3331', '10/30', 'ENC-331', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'Y', '2026-07-03 11:40:00', 'N'),
(6, 6, 3, 'ENC-REG-3332', '07/30', 'ENC-332', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'N', '2026-07-03 11:50:00', 'N');

-- ---------------------------------------------------------------------
-- 결제 일회성 토큰: 6건
-- ---------------------------------------------------------------------
INSERT INTO payment_token_tbl (
    token_value,
    user_id,
    card_id,
    expired_at,
    used_yn
) VALUES
('pay-token-001', 1, 1, '2026-07-24 10:10:00', 'Y'),
('pay-token-002', 1, 2, '2026-07-24 11:10:00', 'N'),
('pay-token-003', 2, 3, '2026-07-24 12:10:00', 'Y'),
('pay-token-004', 2, 4, '2026-07-24 13:10:00', 'N'),
('pay-token-005', 3, 5, '2026-07-24 14:10:00', 'Y'),
('pay-token-006', 3, 6, '2026-07-24 15:10:00', 'N');

-- ---------------------------------------------------------------------
-- 영수증 메모: 6건
-- ---------------------------------------------------------------------
INSERT INTO receipt_memo_tbl (
    memo_id,
    transaction_id,
    memo_content,
    created_at,
    updated_at
) VALUES
(1, 1, '포인트 지갑 충전', '2026-07-20 09:01:00', '2026-07-20 09:01:00'),
(2, 2, '친구에게 송금', '2026-07-20 10:01:00', '2026-07-20 10:01:00'),
(3, 3, '월급 계좌 충전', '2026-07-21 09:01:00', '2026-07-21 09:01:00'),
(4, 4, '저녁 식사 정산', '2026-07-21 18:01:00', '2026-07-21 18:01:00'),
(5, 5, '여행 경비 송금', '2026-07-22 11:01:00', '2026-07-22 11:01:00'),
(6, 6, '교통비 결제', '2026-07-23 08:01:00', '2026-07-23 08:01:00');

-- ---------------------------------------------------------------------
-- 피드: 6건
-- ---------------------------------------------------------------------
INSERT INTO feed_tbl (
    feed_id,
    user_id,
    transaction_id,
    feed_type,
    content,
    visibility,
    created_at,
    updated_at
) VALUES
(1, 1, 1, 'SHARE', '지갑 충전 완료', 'PUBLIC', '2026-07-20 09:05:00', '2026-07-20 09:05:00'),
(2, 1, 2, 'TRANSFER', '친구에게 송금', 'FRIEND', '2026-07-20 10:05:00', '2026-07-20 10:05:00'),
(3, 2, 3, 'SHARE', '지갑 충전 완료', 'PRIVATE', '2026-07-21 09:05:00', '2026-07-21 09:05:00'),
(4, 2, 4, 'SETTLEMENT', '정산 완료', 'FRIEND', '2026-07-21 18:05:00', '2026-07-21 18:05:00'),
(5, 3, 5, 'TRANSFER', '여행비 송금', 'PUBLIC', '2026-07-22 11:05:00', '2026-07-22 11:05:00'),
(6, 3, 6, 'PAYMENT', '교통비 결제', 'PRIVATE', '2026-07-23 08:05:00', '2026-07-23 08:05:00');

-- ---------------------------------------------------------------------
-- 피드 이미지: 6건
-- ---------------------------------------------------------------------
INSERT INTO feed_image_tbl (
    image_id,
    feed_id,
    image_name
) VALUES
(1, 1, 'feed_1_1.png'),
(2, 2, 'feed_2_1.png'),
(3, 3, 'feed_3_1.png'),
(4, 4, 'feed_4_1.png'),
(5, 5, 'feed_5_1.png'),
(6, 6, 'feed_6_1.png');

-- ---------------------------------------------------------------------
-- 피드 좋아요: 6건
-- ---------------------------------------------------------------------
INSERT INTO feed_like_tbl (
    like_id,
    feed_id,
    user_id,
    created_at
) VALUES
(1, 1, 2, '2026-07-20 09:10:00'),
(2, 1, 3, '2026-07-20 09:11:00'),
(3, 2, 2, '2026-07-20 10:10:00'),
(4, 4, 1, '2026-07-21 18:10:00'),
(5, 5, 1, '2026-07-22 11:10:00'),
(6, 5, 2, '2026-07-22 11:11:00');

-- ---------------------------------------------------------------------
-- 피드 댓글: 6건
-- ---------------------------------------------------------------------
INSERT INTO feed_comment_tbl (
    comment_id,
    feed_id,
    user_id,
    content,
    created_at,
    updated_at
) VALUES
(1, 1, 2, '충전 축하해요', '2026-07-20 09:15:00', '2026-07-20 09:15:00'),
(2, 2, 2, '송금 확인했어요', '2026-07-20 10:15:00', '2026-07-20 10:15:00'),
(3, 3, 1, '좋은 습관이에요', '2026-07-21 09:15:00', '2026-07-21 09:15:00'),
(4, 4, 3, '정산 고마워요', '2026-07-21 18:15:00', '2026-07-21 18:15:00'),
(5, 5, 1, '즐거운 여행!', '2026-07-22 11:15:00', '2026-07-22 11:15:00'),
(6, 6, 2, '교통비 절약!', '2026-07-23 08:15:00', '2026-07-23 08:15:00');

-- ---------------------------------------------------------------------
-- 커스텀 카드 에셋: 3건
-- ---------------------------------------------------------------------
INSERT INTO card_asset_tbl (
    asset_id,
    asset_type,
    asset_name,
    src_url,
    background_color,
    font_type,
    use_yn
) VALUES
(1, 'BACKGROUND_SOLID', 'KB 옐로우', '/assets/yellow.png', '#FFCC00', 'PRETENDARD', 'Y'),
(2, 'BACKGROUND_GRADIENT', '선셋 그라데이션', '/assets/sunset.png', '#FF9966', 'PRETENDARD', 'Y'),
(3, 'STICKER', '별 스티커', '/assets/star.png', NULL, NULL, 'Y');

-- ---------------------------------------------------------------------
-- 업로드 파일: 6건
-- ---------------------------------------------------------------------
INSERT INTO file_image_tbl (
    file_id,
    user_id,
    file_name,
    file_size,
    created_at
) VALUES
(1, 1, 'upload_user1_1.png', 120000, '2026-07-20 14:00:00'),
(2, 1, 'upload_user1_2.png', 130000, '2026-07-20 14:10:00'),
(3, 2, 'upload_user2_1.png', 140000, '2026-07-21 14:00:00'),
(4, 2, 'upload_user2_2.png', 150000, '2026-07-21 14:10:00'),
(5, 3, 'upload_user3_1.png', 160000, '2026-07-22 14:00:00'),
(6, 3, 'upload_user3_2.png', 170000, '2026-07-22 14:10:00');

-- ---------------------------------------------------------------------
-- 커스텀 카드 이미지: 6건
-- ---------------------------------------------------------------------
INSERT INTO custom_image_tbl (
    custom_image_id,
    user_id,
    asset_id,
    file_id,
    custom_image_path,
    custom_image_name,
    custom_image_size,
    created_at
) VALUES
(1, 1, 1, 1, '/custom/user1/', 'custom_1.png', 220000, '2026-07-20 15:00:00'),
(2, 1, 2, 2, '/custom/user1/', 'custom_2.png', 230000, '2026-07-20 15:10:00'),
(3, 2, 2, 3, '/custom/user2/', 'custom_3.png', 240000, '2026-07-21 15:00:00'),
(4, 2, 3, 4, '/custom/user2/', 'custom_4.png', 250000, '2026-07-21 15:10:00'),
(5, 3, 1, 5, '/custom/user3/', 'custom_5.png', 260000, '2026-07-22 15:00:00'),
(6, 3, 3, 6, '/custom/user3/', 'custom_6.png', 270000, '2026-07-22 15:10:00');

-- ---------------------------------------------------------------------
-- 카드 신청 이력: 6건
-- ---------------------------------------------------------------------
INSERT INTO card_application_history_tbl (
    apply_id,
    user_id,
    custom_image_id,
    card_code,
    card_name,
    card_status,
    created_at,
    updated_at
) VALUES
(1, 1, 1, 'KB-CARD-001', '나만의 옐로우카드', 'REQUEST', '2026-07-20 16:00:00', '2026-07-20 16:00:00'),
(2, 1, 2, 'KB-CARD-001', '선셋 카드', 'ISSUED', '2026-07-20 16:10:00', '2026-07-22 10:00:00'),
(3, 2, 3, 'KB-CARD-002', '노을 카드', 'REQUEST', '2026-07-21 16:00:00', '2026-07-21 16:00:00'),
(4, 2, 4, 'KB-CARD-002', '별빛 카드', 'CANCELLED', '2026-07-21 16:10:00', '2026-07-22 11:00:00'),
(5, 3, 5, 'KB-CARD-003', '노랑 여행카드', 'ISSUED', '2026-07-22 16:00:00', '2026-07-24 09:00:00'),
(6, 3, 6, 'KB-CARD-003', '별 여행카드', 'REQUEST', '2026-07-22 16:10:00', '2026-07-22 16:10:00');

-- ---------------------------------------------------------------------
-- 이벤트: 3건
-- ---------------------------------------------------------------------
INSERT INTO event_tbl (
    event_id,
    event_name,
    event_desc,
    event_type,
    event_status,
    event_img_name,
    event_target,
    event_level,
    start_at,
    end_at,
    created_at
) VALUES
(1, '7월 출석 이벤트', '매일 출석하고 포인트를 받으세요.', 'ATTENDANCE', 'OPEN', 'attendance_july.png', 20, 3, '2026-07-01 00:00:00', '2026-07-31 23:59:59', '2026-06-25 09:00:00'),
(2, '주말 출석 이벤트', '주말 출석 시 추가 포인트를 제공합니다.', 'ATTENDANCE', 'OPEN', 'attendance_weekend.png', 8, 2, '2026-07-01 00:00:00', '2026-07-31 23:59:59', '2026-06-25 09:10:00'),
(3, '6월 출석 이벤트', '지난달 출석 이벤트입니다.', 'ATTENDANCE', 'CLOSE', 'attendance_june.png', 20, 3, '2026-06-01 00:00:00', '2026-06-30 23:59:59', '2026-05-25 09:00:00');

-- ---------------------------------------------------------------------
-- 이벤트 보상: 6건
-- ---------------------------------------------------------------------
INSERT INTO event_reward_tbl (
    reward_id,
    event_id,
    event_level,
    reward_point,
    reward_exe,
    req_count,
    use_yn
) VALUES
(1, 1, 1, 100, 10, 5, 'Y'),
(2, 1, 2, 300, 30, 10, 'Y'),
(3, 2, 1, 200, 20, 2, 'Y'),
(4, 2, 2, 500, 50, 6, 'Y'),
(5, 3, 1, 100, 10, 5, 'N'),
(6, 3, 2, 300, 30, 10, 'N');

-- ---------------------------------------------------------------------
-- 이벤트 참여: 6건
-- ---------------------------------------------------------------------
INSERT INTO event_participation_tbl (
    participation_id,
    event_id,
    user_id,
    participated_at
) VALUES
(1, 1, 1, '2026-07-01 08:00:00'),
(2, 1, 2, '2026-07-01 08:10:00'),
(3, 1, 3, '2026-07-01 08:20:00'),
(4, 2, 1, '2026-07-05 08:00:00'),
(5, 2, 2, '2026-07-05 08:10:00'),
(6, 3, 3, '2026-06-01 08:20:00');

-- ---------------------------------------------------------------------
-- 이벤트 보상 수령: 6건
-- ---------------------------------------------------------------------
INSERT INTO event_reward_receive_tbl (
    recv_id,
    event_id,
    reward_id,
    user_id,
    received_at
) VALUES
(1, 1, 1, 1, '2026-07-06 09:00:00'),
(2, 1, 2, 2, '2026-07-11 09:00:00'),
(3, 1, 1, 3, '2026-07-06 09:10:00'),
(4, 2, 3, 1, '2026-07-07 09:00:00'),
(5, 2, 4, 2, '2026-07-14 09:00:00'),
(6, 3, 5, 3, '2026-06-06 09:00:00');

-- ---------------------------------------------------------------------
-- 이벤트 챌린지: 3건
-- ---------------------------------------------------------------------
INSERT INTO event_challenge_tbl (
    challenge_id,
    challenge_name,
    reward_point,
    max_level,
    max_target,
    start_date,
    end_date,
    created_at
) VALUES
(1, '한 달 출석 챌린지', 1000, 3, 20, '2026-07-01 00:00:00', '2026-07-31 23:59:59', '2026-06-25 10:00:00'),
(2, '절약 소비 챌린지', 1500, 5, 10, '2026-07-01 00:00:00', '2026-08-31 23:59:59', '2026-06-25 10:10:00'),
(3, '친구 정산 챌린지', 800, 2, 5, '2026-07-01 00:00:00', '2026-07-31 23:59:59', '2026-06-25 10:20:00');

-- ---------------------------------------------------------------------
-- 챌린지 참여: 6건
-- ---------------------------------------------------------------------
INSERT INTO event_challenge_user_tbl (
    user_challenge_id,
    user_id,
    challenge_id,
    current_level,
    current_target,
    status,
    updated_at
) VALUES
(1, 1, 1, 2, 12, 'PROCESS', '2026-07-24 08:00:00'),
(2, 1, 2, 3, 6, 'PROCESS', '2026-07-24 08:10:00'),
(3, 2, 1, 3, 20, 'COMPLETE', '2026-07-24 08:20:00'),
(4, 2, 3, 2, 5, 'REWARDED', '2026-07-24 08:30:00'),
(5, 3, 2, 1, 2, 'PROCESS', '2026-07-24 08:40:00'),
(6, 3, 3, 1, 3, 'PROCESS', '2026-07-24 08:50:00');

-- ---------------------------------------------------------------------
-- 카드사: 3건
-- ---------------------------------------------------------------------
INSERT INTO card_company_tbl (
    card_company_code,
    card_company_name
) VALUES
('KB', 'KB국민카드'),
('SH', '신한카드'),
('HN', '하나카드');

-- ---------------------------------------------------------------------
-- 연결 카드: 6건
-- ---------------------------------------------------------------------
INSERT INTO linked_card_tbl (
    linked_card_id,
    user_id,
    card_id,
    card_company_code,
    card_name,
    card_image_name,
    represent_yn
) VALUES
(1, 1, 1, 'KB', 'KB 대표카드', 'linked_kb_1.png', 'Y'),
(2, 1, 2, 'SH', '신한 생활카드', 'linked_sh_1.png', 'N'),
(3, 2, 3, 'SH', '신한 대표카드', 'linked_sh_2.png', 'Y'),
(4, 2, 4, 'HN', '하나 교통카드', 'linked_hn_2.png', 'N'),
(5, 3, 5, 'HN', '하나 대표카드', 'linked_hn_3.png', 'Y'),
(6, 3, 6, 'KB', 'KB 여행카드', 'linked_kb_3.png', 'N');

COMMIT;

-- 전체 테이블 건수 확인
SELECT 'tbl_member' AS table_name, COUNT(*) AS row_count FROM tbl_member
UNION ALL
SELECT 'tbl_member_auth' AS table_name, COUNT(*) AS row_count FROM tbl_member_auth
UNION ALL
SELECT 'user_tbl' AS table_name, COUNT(*) AS row_count FROM user_tbl
UNION ALL
SELECT 'bank_tbl' AS table_name, COUNT(*) AS row_count FROM bank_tbl
UNION ALL
SELECT 'linked_account_tbl' AS table_name, COUNT(*) AS row_count FROM linked_account_tbl
UNION ALL
SELECT 'agreement_tbl' AS table_name, COUNT(*) AS row_count FROM agreement_tbl
UNION ALL
SELECT 'user_agreement_tbl' AS table_name, COUNT(*) AS row_count FROM user_agreement_tbl
UNION ALL
SELECT 'wallet_tbl' AS table_name, COUNT(*) AS row_count FROM wallet_tbl
UNION ALL
SELECT 'verification_tbl' AS table_name, COUNT(*) AS row_count FROM verification_tbl
UNION ALL
SELECT 'profile_tbl' AS table_name, COUNT(*) AS row_count FROM profile_tbl
UNION ALL
SELECT 'notification_setting_tbl' AS table_name, COUNT(*) AS row_count FROM notification_setting_tbl
UNION ALL
SELECT 'refresh_token_tbl' AS table_name, COUNT(*) AS row_count FROM refresh_token_tbl
UNION ALL
SELECT 'spending_category_tbl' AS table_name, COUNT(*) AS row_count FROM spending_category_tbl
UNION ALL
SELECT 'point_wallet_tbl' AS table_name, COUNT(*) AS row_count FROM point_wallet_tbl
UNION ALL
SELECT 'point_transaction_tbl' AS table_name, COUNT(*) AS row_count FROM point_transaction_tbl
UNION ALL
SELECT 'user_random_box_tbl' AS table_name, COUNT(*) AS row_count FROM user_random_box_tbl
UNION ALL
SELECT 'attendance_tbl' AS table_name, COUNT(*) AS row_count FROM attendance_tbl
UNION ALL
SELECT 'point_conversion_history_tbl' AS table_name, COUNT(*) AS row_count FROM point_conversion_history_tbl
UNION ALL
SELECT 'spending_analysis_tbl' AS table_name, COUNT(*) AS row_count FROM spending_analysis_tbl
UNION ALL
SELECT 'spending_analysis_category_tbl' AS table_name, COUNT(*) AS row_count FROM spending_analysis_category_tbl
UNION ALL
SELECT 'kb_card_product_tbl' AS table_name, COUNT(*) AS row_count FROM kb_card_product_tbl
UNION ALL
SELECT 'card_benefit_tbl' AS table_name, COUNT(*) AS row_count FROM card_benefit_tbl
UNION ALL
SELECT 'card_recommendation_tbl' AS table_name, COUNT(*) AS row_count FROM card_recommendation_tbl
UNION ALL
SELECT 'kb_insurance_product_tbl' AS table_name, COUNT(*) AS row_count FROM kb_insurance_product_tbl
UNION ALL
SELECT 'kb_insurance_coverage_tbl' AS table_name, COUNT(*) AS row_count FROM kb_insurance_coverage_tbl
UNION ALL
SELECT 'kb_insurance_recommendation_tbl' AS table_name, COUNT(*) AS row_count FROM kb_insurance_recommendation_tbl
UNION ALL
SELECT 'friend_request_tbl' AS table_name, COUNT(*) AS row_count FROM friend_request_tbl
UNION ALL
SELECT 'friend_tbl' AS table_name, COUNT(*) AS row_count FROM friend_tbl
UNION ALL
SELECT 'settlement_tbl' AS table_name, COUNT(*) AS row_count FROM settlement_tbl
UNION ALL
SELECT 'settlement_member_tbl' AS table_name, COUNT(*) AS row_count FROM settlement_member_tbl
UNION ALL
SELECT 'notification_tbl' AS table_name, COUNT(*) AS row_count FROM notification_tbl
UNION ALL
SELECT 'financial_transaction_tbl' AS table_name, COUNT(*) AS row_count FROM financial_transaction_tbl
UNION ALL
SELECT 'account_dummy_tbl' AS table_name, COUNT(*) AS row_count FROM account_dummy_tbl
UNION ALL
SELECT 'account_transaction_tbl' AS table_name, COUNT(*) AS row_count FROM account_transaction_tbl
UNION ALL
SELECT 'wallet_transaction_tbl' AS table_name, COUNT(*) AS row_count FROM wallet_transaction_tbl
UNION ALL
SELECT 'card_tbl' AS table_name, COUNT(*) AS row_count FROM card_tbl
UNION ALL
SELECT 'registered_card_tbl' AS table_name, COUNT(*) AS row_count FROM registered_card_tbl
UNION ALL
SELECT 'payment_token_tbl' AS table_name, COUNT(*) AS row_count FROM payment_token_tbl
UNION ALL
SELECT 'receipt_memo_tbl' AS table_name, COUNT(*) AS row_count FROM receipt_memo_tbl
UNION ALL
SELECT 'feed_tbl' AS table_name, COUNT(*) AS row_count FROM feed_tbl
UNION ALL
SELECT 'feed_image_tbl' AS table_name, COUNT(*) AS row_count FROM feed_image_tbl
UNION ALL
SELECT 'feed_like_tbl' AS table_name, COUNT(*) AS row_count FROM feed_like_tbl
UNION ALL
SELECT 'feed_comment_tbl' AS table_name, COUNT(*) AS row_count FROM feed_comment_tbl
UNION ALL
SELECT 'card_asset_tbl' AS table_name, COUNT(*) AS row_count FROM card_asset_tbl
UNION ALL
SELECT 'file_image_tbl' AS table_name, COUNT(*) AS row_count FROM file_image_tbl
UNION ALL
SELECT 'custom_image_tbl' AS table_name, COUNT(*) AS row_count FROM custom_image_tbl
UNION ALL
SELECT 'card_application_history_tbl' AS table_name, COUNT(*) AS row_count FROM card_application_history_tbl
UNION ALL
SELECT 'event_tbl' AS table_name, COUNT(*) AS row_count FROM event_tbl
UNION ALL
SELECT 'event_reward_tbl' AS table_name, COUNT(*) AS row_count FROM event_reward_tbl
UNION ALL
SELECT 'event_participation_tbl' AS table_name, COUNT(*) AS row_count FROM event_participation_tbl
UNION ALL
SELECT 'event_reward_receive_tbl' AS table_name, COUNT(*) AS row_count FROM event_reward_receive_tbl
UNION ALL
SELECT 'event_challenge_tbl' AS table_name, COUNT(*) AS row_count FROM event_challenge_tbl
UNION ALL
SELECT 'event_challenge_user_tbl' AS table_name, COUNT(*) AS row_count FROM event_challenge_user_tbl
UNION ALL
SELECT 'card_company_tbl' AS table_name, COUNT(*) AS row_count FROM card_company_tbl
UNION ALL
SELECT 'linked_card_tbl' AS table_name, COUNT(*) AS row_count FROM linked_card_tbl
ORDER BY table_name;
