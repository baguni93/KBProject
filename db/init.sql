DROP DATABASE IF EXISTS kbproject;

CREATE DATABASE kbproject;

USE kbproject;

DROP TABLE IF EXISTS tbl_member;

CREATE TABLE `tbl_member` (
  `username` varchar(50) NOT NULL,
  `password` varchar(128) NOT NULL,
  `email` varchar(50) NOT NULL,
  `reg_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_date` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS tbl_member_auth;

CREATE TABLE `tbl_member_auth` (
  `username` varchar(50) NOT NULL,
  `auth` varchar(50) NOT NULL,
  PRIMARY KEY (`username`,`auth`),
  CONSTRAINT `fk_authorities_users` FOREIGN KEY (`username`) REFERENCES `tbl_member` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



DROP TABLE IF EXISTS user_tbl;

-- 1. 회원 테이블 정의서
CREATE TABLE user_tbl (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '회원번호',

    user_name VARCHAR(30) NOT NULL COMMENT '이름',

    phone_number VARCHAR(20) NOT NULL UNIQUE COMMENT '휴대폰번호',

    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일(로그인 ID)',

    password VARCHAR(255) NOT NULL COMMENT 'BCrypt 암호화 비밀번호',

    user_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        COMMENT '회원상태 (ACTIVE, WD)',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '가입일시',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '수정일시',

    withdrawn_at DATETIME NULL
        COMMENT '탈퇴일시',

    last_login_at DATETIME NULL
        COMMENT '최근접속일시',

    CONSTRAINT chk_user_status
        CHECK (user_status IN ('ACTIVE', 'WD'))
) COMMENT = '회원';

-- 3. 은행 테이블
DROP TABLE IF EXISTS bank_tbl;

CREATE TABLE bank_tbl (
    bank_code VARCHAR(10) PRIMARY KEY COMMENT '은행코드',
    bank_name VARCHAR(50) NOT NULL UNIQUE COMMENT '은행명',
    bank_logo_name VARCHAR(255) NULL COMMENT '은행로고파일명',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용여부',

    CONSTRAINT chk_bank_use_yn
        CHECK (use_yn IN ('Y', 'N'))
);


-- 2. 사용자계좌 테이블
DROP TABLE IF EXISTS linked_account_tbl;

CREATE TABLE linked_account_tbl (
    linked_account_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '연결계좌번호',
    user_id INT NOT NULL COMMENT '회원번호',
    bank_code VARCHAR(10) NOT NULL COMMENT '은행코드',
    account_number VARCHAR(255) NOT NULL COMMENT '계좌번호',
    account_holder VARCHAR(50) NOT NULL COMMENT '예금주명',
    primary_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '대표계좌여부',
    connection_status VARCHAR(20) NOT NULL DEFAULT 'CONNECTED' COMMENT '연결상태',

    CONSTRAINT fk_linked_account_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_linked_account_bank
        FOREIGN KEY (bank_code)
        REFERENCES bank_tbl(bank_code),

    CONSTRAINT chk_primary_yn
        CHECK (primary_yn IN ('Y', 'N'))
);

-- 4. 약관 정보 테이블
DROP TABLE IF EXISTS agreement_tbl;

CREATE TABLE agreement_tbl (
    agreement_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '약관번호',
    agreement_type VARCHAR(30) NOT NULL COMMENT '약관유형',
    agreement_name VARCHAR(100) NOT NULL COMMENT '약관명',
    agreement_content TEXT NOT NULL COMMENT '약관내용',
    required_yn CHAR(1) NOT NULL COMMENT '필수여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용여부',

    CONSTRAINT chk_agreement_required_yn
        CHECK (required_yn IN ('Y', 'N')),

    CONSTRAINT chk_agreement_use_yn
        CHECK (use_yn IN ('Y', 'N'))
);

-- 5. 유저 약관 동의 테이블
DROP TABLE IF EXISTS user_agreement_tbl;

CREATE TABLE user_agreement_tbl (
    user_agreement_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '동의번호',
    user_id INT NOT NULL COMMENT '회원번호',
    agreement_id INT NOT NULL COMMENT '약관번호',
    agreed_yn CHAR(1) NOT NULL COMMENT '동의여부',
    agreed_at DATETIME NOT NULL COMMENT '동의처리일시',

    CONSTRAINT fk_user_agreement_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_user_agreement
        FOREIGN KEY (agreement_id)
        REFERENCES agreement_tbl(agreement_id),

    CONSTRAINT chk_user_agreement_agreed_yn
        CHECK (agreed_yn IN ('Y', 'N'))
);


-- 6. 전자지갑 테이블
DROP TABLE IF EXISTS wallet_tbl;

CREATE TABLE wallet_tbl (
    wallet_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '지갑번호',
    user_id INT NOT NULL UNIQUE COMMENT '회원번호',
    balance INT NOT NULL DEFAULT 0 COMMENT '지갑잔액',
    wallet_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '지갑상태',

    CONSTRAINT fk_wallet_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT chk_wallet_balance
        CHECK (balance >= 0),

    CONSTRAINT chk_wallet_status
        CHECK (wallet_status IN ('ACTIVE', 'CLOSED'))
);

-- 6-2 휴대폰인증 테이블
DROP TABLE IF EXISTS verification_tbl;

CREATE TABLE verification_tbl (
    verification_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '인증번호',

    user_id INT NULL COMMENT '회원번호',

    verification_type VARCHAR(20) NOT NULL COMMENT '인증수단',

    target_value VARCHAR(100) NOT NULL COMMENT '인증대상',

    verification_code VARCHAR(10) NOT NULL COMMENT '인증코드',

    verification_purpose VARCHAR(30) NOT NULL COMMENT '인증목적',

    requested_at DATETIME NOT NULL COMMENT '인증요청일시',

    expires_at DATETIME NOT NULL COMMENT '인증만료일시',

    verified_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '인증완료여부',

    verified_at DATETIME NULL COMMENT '인증완료일시',

    resend_count INT NOT NULL DEFAULT 0 COMMENT '재전송횟수',

    CONSTRAINT fk_verification_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT chk_verification_type
        CHECK (
            verification_type IN ('PHONE', 'EMAIL')
        ),

    CONSTRAINT chk_verification_purpose
        CHECK (
            verification_purpose IN (
                'SIGN_UP',
                'PHONE_CHANGE',
                'EMAIL_CHANGE',
                'PASSWORD_RESET'
            )
        ),

    CONSTRAINT chk_verification_yn
        CHECK (
            verified_yn IN ('Y', 'N')
        ),

    CONSTRAINT chk_resend_count
        CHECK (
            resend_count >= 0
        )
);


-- 6-3. 프로필 테이블
DROP TABLE IF EXISTS profile_tbl;

CREATE TABLE profile_tbl (
    profile_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '프로필번호',

    user_id INT NOT NULL UNIQUE COMMENT '회원번호',

    nickname VARCHAR(30) NOT NULL UNIQUE COMMENT '닉네임',

    introduction VARCHAR(300) NULL COMMENT '자기소개',

    original_name VARCHAR(255) NULL COMMENT '이미지원본명',

    stored_name VARCHAR(255) NULL COMMENT '이미지파일명',

    created_at DATETIME NOT NULL COMMENT '생성일시',

    updated_at DATETIME NOT NULL COMMENT '수정일시',

    CONSTRAINT fk_profile_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id)
);

-- 6-4. 알림설정 테이블
DROP TABLE IF EXISTS notification_setting_tbl;

CREATE TABLE notification_setting_tbl (
    notification_setting_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '알림설정번호',

    user_id INT NOT NULL UNIQUE COMMENT '회원번호',

    finance_notification_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '금융알림여부',

    reward_notification_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '리워드알림여부',

    event_benefit_notification_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '이벤트혜택알림여부',

    updated_at DATETIME NOT NULL COMMENT '수정일시',

    CONSTRAINT fk_notification_setting_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT chk_finance_notification_yn
        CHECK (finance_notification_yn IN ('Y', 'N')),

    CONSTRAINT chk_reward_notification_yn
        CHECK (reward_notification_yn IN ('Y', 'N')),

    CONSTRAINT chk_event_benefit_notification_yn
        CHECK (event_benefit_notification_yn IN ('Y', 'N'))
);

-- 6-5. 리프레시토큰 테이블
DROP TABLE IF EXISTS refresh_token_tbl;

CREATE TABLE refresh_token_tbl (
    refresh_token_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '토큰번호',

    user_id INT NOT NULL COMMENT '회원번호',

    refresh_token VARCHAR(500) NOT NULL UNIQUE COMMENT '리프레시토큰',

    issued_at DATETIME NOT NULL COMMENT '발급일시',

    expires_at DATETIME NOT NULL COMMENT '만료일시',

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id)
);


-- 7. 소비 카테고리 테이블
DROP TABLE IF EXISTS spending_category_tbl;

CREATE TABLE spending_category_tbl (
    spending_category_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카테고리 식별자',
    category_name VARCHAR(100) NOT NULL UNIQUE COMMENT '카테고리명',
    parent_category_id INT NULL COMMENT '부모 카테고리 ID',

    CONSTRAINT fk_spending_category_parent
        FOREIGN KEY (parent_category_id)
        REFERENCES spending_category_tbl(spending_category_id)
        
-- spending_category_id가 AUTO_INCREMENT 컬럼이라 CHECK에서 사용할 수 없습니다. , 코드에서 확인
--     CONSTRAINT chk_spending_category_parent
--         CHECK (
--             parent_category_id IS NULL
--             OR parent_category_id <> spending_category_id
--         )
);


-- 8.포인트 테이블 
-- NIQUE(point_wallet_id, user_id)는 불필요합니다.
-- point_wallet_id가 PRIMARY KEY이므로 이미 유일합니다.
-- 따라서 (point_wallet_id, user_id) 복합 UNIQUE는 의미가 없습니다.
-- 정의서는 그대로 두더라도 DDL에서는 생략해도 동일한 효과입니다.

DROP TABLE IF EXISTS point_wallet_tbl;

CREATE TABLE point_wallet_tbl (
    point_wallet_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '포인트 지갑PK',
    user_id INT NOT NULL UNIQUE COMMENT '유저 ID',
    point_balance INT NOT NULL DEFAULT 0 COMMENT '포인트 잔액',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_point_wallet_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT chk_point_balance
        CHECK (point_balance >= 0)
);


-- 9.포인트거래내역 테이블
DROP TABLE IF EXISTS point_transaction_tbl;

CREATE TABLE point_transaction_tbl (
    point_transaction_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '포인트 내역 PK',
    point_wallet_id INT NOT NULL COMMENT '유저 포인트 지갑',
    transaction_type VARCHAR(20) NOT NULL COMMENT '거래 타입',
    point_amount INT NOT NULL COMMENT '증감된 포인트',
    reason_type VARCHAR(30) NOT NULL COMMENT '증감 사유',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '거래 일시',

    CONSTRAINT fk_point_transaction_wallet
        FOREIGN KEY (point_wallet_id)
        REFERENCES point_wallet_tbl(point_wallet_id),

    CONSTRAINT chk_point_transaction_type
        CHECK (transaction_type IN ('EARN', 'USE', 'EXPIRE', 'CANCEL')),

    CONSTRAINT chk_point_transaction_amount
        CHECK (point_amount > 0),

    CONSTRAINT chk_point_transaction_reason
        CHECK (reason_type IN ('EVENT'))
);

-- 10.랜덤박스 테이블
DROP TABLE IF EXISTS user_random_box_tbl;

CREATE TABLE user_random_box_tbl (
    user_random_box_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '랜덤박스 PK',
    user_id INT NOT NULL COMMENT '유저 ID',
    issue_reason VARCHAR(30) NOT NULL COMMENT '지급사유',
    box_status VARCHAR(20) NOT NULL COMMENT '개봉상태',
    reward_point INT NULL COMMENT '결과 포인트',
    issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '랜덤박스 지급일시',
    opened_at DATETIME NULL COMMENT '랜덤박스 개봉일시',

    CONSTRAINT fk_user_random_box_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT chk_user_random_box_status
        CHECK (box_status IN ('UNOPENED', 'OPENED')),

    CONSTRAINT chk_user_random_box_reward_point
        CHECK (reward_point IS NULL OR reward_point >= 0)
);


-- 11. 출석 내역 테이블
DROP TABLE IF EXISTS attendance_tbl;

CREATE TABLE attendance_tbl (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '출석 PK',
    user_id INT NOT NULL COMMENT '출석된 사용자',
    attendance_date DATE NOT NULL COMMENT '출석 일자',

    CONSTRAINT uq_attendance_user_date
        UNIQUE (user_id, attendance_date),

    CONSTRAINT fk_attendance_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id)
);

-- point_wallet_tbl(point_wallet_id, user_id)를 참조하도록 되어 있는데, 현재 point_wallet_tbl에는 (point_wallet_id, user_id) 복합 UNIQUE가 없습니다.

-- 이전 DDL에서 point_wallet_id는 PK, user_id는 UNIQUE로 생성했습니다.
-- 따라서 복합 FK는 생성할 수 없습니다.
-- 현재 구조에서는 point_wallet_id만 FK로 참조하면 충분합니다.

-- 12. 포인트 전환 내역 테이블 정의서

-- 포인트 전환 내역 테이블
DROP TABLE IF EXISTS point_conversion_history_tbl;

CREATE TABLE point_conversion_history_tbl (
    point_conversion_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '포인트 전환내역 PK',
    user_id INT NOT NULL COMMENT '유저 ID',
    point_wallet_id INT NOT NULL COMMENT '포인트 지갑',
    linked_account_id INT NOT NULL COMMENT '입금계좌번호',
    converted_point INT NOT NULL COMMENT '변환 금액',
    converted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '변환 일시',

    CONSTRAINT fk_point_conversion_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_point_conversion_wallet
        FOREIGN KEY (point_wallet_id)
        REFERENCES point_wallet_tbl(point_wallet_id),

    CONSTRAINT fk_point_conversion_account
        FOREIGN KEY (linked_account_id)
        REFERENCES linked_account_tbl(linked_account_id),

    CONSTRAINT chk_point_conversion_point
        CHECK (converted_point > 0)
);

-- 13. 소비 분석 테이블
DROP TABLE IF EXISTS spending_analysis_tbl;

CREATE TABLE spending_analysis_tbl (
    spending_analysis_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '소비분석ID',
    user_id INT NOT NULL COMMENT '유저 ID',
    analysis_period INT NOT NULL COMMENT '분석 기간',
    representative_category_id INT NOT NULL COMMENT '가장 많이 소비한 대표 카테고리',
    ai_title VARCHAR(100) NOT NULL COMMENT 'AI 생성 칭호',
    ai_analysis_summary TEXT NOT NULL COMMENT 'AI가 생성한 소비 분석 요약',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '분석 일자',

    CONSTRAINT fk_spending_analysis_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_spending_analysis_category
        FOREIGN KEY (representative_category_id)
        REFERENCES spending_category_tbl(spending_category_id),

    CONSTRAINT chk_spending_analysis_period
        CHECK (analysis_period IN (1, 3, 12))
);


-- 14. 분석결과저장 테이블
DROP TABLE IF EXISTS spending_analysis_category_tbl;

CREATE TABLE spending_analysis_category_tbl (
    analysis_category_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카테고리별 분석 결과 식별자',
    spending_analysis_id INT NOT NULL COMMENT '소비분석ID',
    spending_category_id INT NOT NULL COMMENT '소비카테고리 ID',
    spending_amount INT NOT NULL COMMENT '카테고리 별 총 소비금액',
    spending_ratio DECIMAL(5,2) NOT NULL COMMENT '카테고리별 소비 비율',
    transaction_count INT NOT NULL COMMENT '카테고리별 거래 건수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '저장 일시',

    CONSTRAINT uq_analysis_category
        UNIQUE (spending_analysis_id, spending_category_id),

    CONSTRAINT fk_analysis_category_analysis
        FOREIGN KEY (spending_analysis_id)
        REFERENCES spending_analysis_tbl(spending_analysis_id),

    CONSTRAINT fk_analysis_category_category
        FOREIGN KEY (spending_category_id)
        REFERENCES spending_category_tbl(spending_category_id),

    CONSTRAINT chk_spending_amount
        CHECK (spending_amount >= 0),

    CONSTRAINT chk_spending_ratio
        CHECK (spending_ratio BETWEEN 0 AND 100),

    CONSTRAINT chk_transaction_count
        CHECK (transaction_count >= 0)
);

-- 15. KB 카드 상품 설명 테이블
DROP TABLE IF EXISTS kb_card_product_tbl;

CREATE TABLE kb_card_product_tbl (
    card_product_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카드 PK',
    card_name VARCHAR(150) NOT NULL COMMENT '카드명',
    card_type VARCHAR(20) NOT NULL COMMENT '신용/체크',
    card_description VARCHAR(1000) NULL COMMENT '카드 설명',
    card_image VARCHAR(500) NULL COMMENT '카드이미지명',
    application VARCHAR(500) NULL COMMENT '신청페이지 파일명',
    annual_fee INT NOT NULL COMMENT '연회비',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '카드 정보 등록 일시',

    CONSTRAINT chk_kb_card_product_type
        CHECK (card_type IN ('CREDIT', 'CHECK')),

    CONSTRAINT chk_kb_card_product_fee
        CHECK (annual_fee >= 0)
);

--  16. 카드 혜택 테이블
DROP TABLE IF EXISTS card_benefit_tbl;

CREATE TABLE card_benefit_tbl (
    card_benefit_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카드 혜택 PK',
    card_product_id INT NOT NULL COMMENT '혜택 적용 카드',
    spending_category_id INT NOT NULL COMMENT '소비 카테고리 ID',
    benefit_name VARCHAR(150) NOT NULL COMMENT '혜택명',
    benefit_amount INT NULL COMMENT '건당 할인 금액',
    benefit_rate DECIMAL(5,2) NULL COMMENT '할인율',
    monthly_limit INT NULL COMMENT '최대 혜택 금액',
    minimum_spending_amount INT NULL COMMENT '최소 이용 금액',
    benefit_description VARCHAR(1000) NULL COMMENT '혜택 상세 설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '혜택 등록 일시',

    CONSTRAINT fk_card_benefit_product
        FOREIGN KEY (card_product_id)
        REFERENCES kb_card_product_tbl(card_product_id),

    CONSTRAINT fk_card_benefit_category
        FOREIGN KEY (spending_category_id)
        REFERENCES spending_category_tbl(spending_category_id),

    CONSTRAINT chk_benefit_amount
        CHECK (benefit_amount IS NULL OR benefit_amount >= 0),

    CONSTRAINT chk_benefit_rate
        CHECK (benefit_rate IS NULL OR benefit_rate BETWEEN 0 AND 100),

    CONSTRAINT chk_monthly_limit
        CHECK (monthly_limit IS NULL OR monthly_limit >= 0),

    CONSTRAINT chk_minimum_spending_amount
        CHECK (minimum_spending_amount IS NULL OR minimum_spending_amount >= 0),

    CONSTRAINT chk_benefit_type
        CHECK (
            benefit_amount IS NOT NULL
            OR benefit_rate IS NOT NULL
        )
);


-- 17. 카드 추천 테이블
DROP TABLE IF EXISTS card_recommendation_tbl;

CREATE TABLE card_recommendation_tbl (
    card_recommendation_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카드 추천 PK',
    spending_analysis_id INT NOT NULL COMMENT '소비분석 ID',
    card_product_id INT NOT NULL COMMENT '추천 카드',
    recommendation_rank INT NOT NULL COMMENT '추천 순위',
    expected_benefit_amount INT NULL COMMENT '예상 할인 금액',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '추천 일시',

    CONSTRAINT uq_card_recommendation
        UNIQUE (spending_analysis_id, card_product_id),

    CONSTRAINT fk_card_recommendation_analysis
        FOREIGN KEY (spending_analysis_id)
        REFERENCES spending_analysis_tbl(spending_analysis_id),

    CONSTRAINT fk_card_recommendation_product
        FOREIGN KEY (card_product_id)
        REFERENCES kb_card_product_tbl(card_product_id),

    CONSTRAINT chk_card_recommendation_rank
        CHECK (recommendation_rank > 0),

    CONSTRAINT chk_expected_benefit_amount
        CHECK (
            expected_benefit_amount IS NULL
            OR expected_benefit_amount >= 0
        )
);


-- 18.KB 보험 상품 테이블
DROP TABLE IF EXISTS kb_insurance_product_tbl;

CREATE TABLE kb_insurance_product_tbl (
    insurance_product_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '보험 PK',
    insurance_name VARCHAR(200) NOT NULL COMMENT '보험상품 ID',
    insurance_category VARCHAR(50) NOT NULL COMMENT '보험 카테고리',
    insurance_description VARCHAR(1000) NULL COMMENT '보험 설명',
    monthly_premium INT NULL COMMENT '예상 보험료',
    insurance_image VARCHAR(500) NULL COMMENT '보험 상품 이미지 파일명',
    application_url VARCHAR(500) NULL COMMENT '가입 페이지 주소',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    CONSTRAINT chk_monthly_premium
        CHECK (
            monthly_premium IS NULL
            OR monthly_premium >= 0
        )
);

-- 19.KB 보험 보장 항목 테이블
DROP TABLE IF EXISTS kb_insurance_coverage_tbl;

CREATE TABLE kb_insurance_coverage_tbl (
    insurance_coverage_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '보험 PK',
    insurance_product_id INT NOT NULL COMMENT '보험상품명',
    coverage_name VARCHAR(200) NOT NULL COMMENT '보장 항목명',
    coverage_amount INT NULL COMMENT '보장금액',
    coverage_description VARCHAR(1000) NULL COMMENT '보장조건 상세설명',
    coverage_limit VARCHAR(100) NULL COMMENT '보장 횟수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '보험 등록일시',

    CONSTRAINT fk_insurance_coverage_product
        FOREIGN KEY (insurance_product_id)
        REFERENCES kb_insurance_product_tbl(insurance_product_id),

    CONSTRAINT chk_coverage_amount
        CHECK (
            coverage_amount IS NULL
            OR coverage_amount >= 0
        )
);


-- 20. KB 보험 추천 결과 테이블
DROP TABLE IF EXISTS kb_insurance_recommendation_tbl;

CREATE TABLE kb_insurance_recommendation_tbl (
    insurance_recommendation_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '보험 추천 PK',
    spending_analysis_id INT NOT NULL COMMENT '소비분석 ID',
    insurance_product_id INT NOT NULL COMMENT '추천 보험 ID',
    recommendation_reason VARCHAR(1000) NOT NULL COMMENT '추천이유',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '추천 생성 일시',

    CONSTRAINT uq_insurance_recommendation
        UNIQUE (spending_analysis_id, insurance_product_id),

    CONSTRAINT fk_insurance_recommendation_analysis
        FOREIGN KEY (spending_analysis_id)
        REFERENCES spending_analysis_tbl(spending_analysis_id),

    CONSTRAINT fk_insurance_recommendation_product
        FOREIGN KEY (insurance_product_id)
        REFERENCES kb_insurance_product_tbl(insurance_product_id)
);


-- 21. 친구 요청 테이블
DROP TABLE IF EXISTS friend_request_tbl;

CREATE TABLE friend_request_tbl (
    request_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '친구 요청 ID',

    requester_id INT NOT NULL COMMENT '요청자 회원번호',

    receiver_id INT NOT NULL COMMENT '대상 회원번호',

    status VARCHAR(20) NOT NULL DEFAULT 'REQUEST' COMMENT '친구요청상태',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT uq_friend_request
        UNIQUE (requester_id, receiver_id),

    CONSTRAINT fk_friend_request_requester
        FOREIGN KEY (requester_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_friend_request_receiver
        FOREIGN KEY (receiver_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT chk_friend_request_status
        CHECK (status IN ('REQUEST', 'ACCEPT', 'REJECT', 'CANCEL')),

    CONSTRAINT chk_friend_request_self
        CHECK (requester_id <> receiver_id)
);

-- 22.친구 테이블
DROP TABLE IF EXISTS friend_tbl;

CREATE TABLE friend_tbl (
    friend_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '팔로우 ID',

    user_id INT NOT NULL COMMENT '요청자 회원번호',

    friend_user_id INT NOT NULL COMMENT '친구 회원번호',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '친구 생성일시',

    CONSTRAINT uq_friend_user
        UNIQUE (user_id, friend_user_id),

    CONSTRAINT fk_friend_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_friend_friend_user
        FOREIGN KEY (friend_user_id)
        REFERENCES user_tbl(user_id)
);

-- 23. 정산 테이블
DROP TABLE IF EXISTS settlement_tbl;

CREATE TABLE settlement_tbl (
    settlement_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '정산 ID',

    requester_id INT NOT NULL COMMENT '요청자 ID',

    title VARCHAR(20) NULL COMMENT '정산 제목',

    total_amount INT NULL COMMENT '총 정산 금액',

    status VARCHAR(20) NULL COMMENT '정산 상태',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    settlement_type VARCHAR(10) NOT NULL COMMENT '정산 방식',

    spending_category_id INT NULL COMMENT '소비 카테고리 ID',

    completed_at DATETIME NULL COMMENT '완료일시',

    CONSTRAINT fk_settlement_requester
        FOREIGN KEY (requester_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_settlement_category
        FOREIGN KEY (spending_category_id)
        REFERENCES spending_category_tbl(spending_category_id),

    CONSTRAINT chk_settlement_total_amount
        CHECK (
            total_amount IS NULL
            OR total_amount >= 0
        ),

    CONSTRAINT chk_settlement_status
        CHECK (
            status IN ('REQUEST', 'COMPLETE', 'CANCEL')
        ),

    CONSTRAINT chk_settlement_type
        CHECK (
            settlement_type IN ('EQUAL', 'UNEQUAL')
        )
);

-- 24. 정산 멤버 테이블
DROP TABLE IF EXISTS settlement_member_tbl;

CREATE TABLE settlement_member_tbl (
    settlement_member_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '정산 참여자 ID',

    settlement_id INT NOT NULL COMMENT '정산 ID',

    user_id INT NOT NULL COMMENT '사용자 ID',

    amount INT NULL COMMENT '정산 금액',

    status VARCHAR(20) NULL COMMENT '정산 상태',

    created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    last_reminder_date DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '마지막으로 리마인드한 날짜',

    completed_at DATETIME NULL COMMENT '완료일시',

    CONSTRAINT uq_settlement_member
        UNIQUE (settlement_id, user_id),

    CONSTRAINT fk_settlement_member_settlement
        FOREIGN KEY (settlement_id)
        REFERENCES settlement_tbl(settlement_id),

    CONSTRAINT fk_settlement_member_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT chk_settlement_member_amount
        CHECK (
            amount IS NULL
            OR amount >= 0
        ),

    CONSTRAINT chk_settlement_member_status
        CHECK (
            status IN ('REQUEST', 'COMPLETE', 'CANCEL')
        )
);

-- 25.알림 테이블
DROP TABLE IF EXISTS notification_tbl;

CREATE TABLE notification_tbl (
    notification_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '알림번호',

    receiver_id INT NOT NULL COMMENT '수신자번호',

    sender_id INT NOT NULL COMMENT '발신자번호',

    notification_type VARCHAR(30) NULL COMMENT '알림유형',

    target_id INT NULL COMMENT '대상번호',

    is_read CHAR(1) NOT NULL DEFAULT 'N' COMMENT '읽음여부',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    CONSTRAINT fk_notification_receiver
        FOREIGN KEY (receiver_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_notification_sender
        FOREIGN KEY (sender_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT chk_notification_type
        CHECK (
            notification_type IN ('LIKE', 'COMMENT', 'SETTLEMENT', 'FRIEND_REQUEST')
        ),

    CONSTRAINT chk_notification_read
        CHECK (
            is_read IN ('Y', 'N')
        )
);


-- 26. 통합 거래 원장 테이블
DROP TABLE IF EXISTS financial_transaction_tbl;

CREATE TABLE financial_transaction_tbl (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '거래번호',

    parent_transaction_id INT NULL COMMENT '상위 거래번호',

    user_id INT NOT NULL COMMENT '거래 요청자 회원번호',

    transaction_type VARCHAR(30) NOT NULL COMMENT '거래유형',

    source_type VARCHAR(20) NOT NULL COMMENT '거래 출처 유형',

    target_type VARCHAR(20) NOT NULL COMMENT '거래 대상 유형',

    transaction_status VARCHAR(20) NOT NULL COMMENT '거래상태',

    amount INT NOT NULL COMMENT '거래금액',

    spending_category_id INT NULL COMMENT '소비 카테고리 ID',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    CONSTRAINT fk_financial_transaction_parent
        FOREIGN KEY (parent_transaction_id)
        REFERENCES financial_transaction_tbl(transaction_id),

    CONSTRAINT fk_financial_transaction_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_financial_transaction_category
        FOREIGN KEY (spending_category_id)
        REFERENCES spending_category_tbl(spending_category_id),

    CONSTRAINT chk_financial_transaction_type
        CHECK (
            transaction_type IN (
                'CHARGE',
                'PAYMENT',
                'TRANSFER',
                'SETTLEMENT'
            )
        ),

    CONSTRAINT chk_financial_source_type
        CHECK (
            source_type IN (
                'ACCOUNT',
                'WALLET'
            )
        ),

    CONSTRAINT chk_financial_target_type
        CHECK (
            target_type IN (
                'ACCOUNT',
                'WALLET'
            )
        ),

    CONSTRAINT chk_financial_transaction_status
        CHECK (
            transaction_status IN (
                'SUCCESS',
                'FAILED',
                'CANCELLED'
            )
        ),

    CONSTRAINT chk_financial_transaction_amount
        CHECK (
            amount >= 0
        )
);

-- 27.은행 계좌 더미 테이블
DROP TABLE IF EXISTS account_dummy_tbl;

CREATE TABLE account_dummy_tbl (
    account_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '계좌 ID',

    user_id INT NOT NULL COMMENT '회원번호',

    bank_code VARCHAR(3) NOT NULL COMMENT '은행코드',

    account_number VARCHAR(30) NULL UNIQUE COMMENT '계좌번호',

    owner_name VARCHAR(50) NOT NULL COMMENT '예금주명',

    balance INT NOT NULL COMMENT '보유잔액',

    account_password VARCHAR(255) NOT NULL COMMENT '계좌비밀번호',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '개설일시',

    CONSTRAINT fk_account_dummy_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_account_dummy_bank
        FOREIGN KEY (bank_code)
        REFERENCES bank_tbl(bank_code),

    CONSTRAINT chk_account_dummy_balance
        CHECK (
            balance >= 0
        )
);

-- 28.계좌 거래 상세 테이블
DROP TABLE IF EXISTS account_transaction_tbl;

CREATE TABLE account_transaction_tbl (
    account_transaction_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '계좌 거래 ID',

    transaction_id INT NOT NULL COMMENT '거래번호',

    user_id INT NOT NULL COMMENT '회원번호',

    direction VARCHAR(10) NOT NULL COMMENT '입출금 구분',

    account_id INT NOT NULL COMMENT '계좌 ID',

    balance_before INT NOT NULL COMMENT '거래 전 잔액',

    balance_after INT NOT NULL COMMENT '거래 후 잔액',

    CONSTRAINT fk_account_transaction_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES financial_transaction_tbl(transaction_id),

    CONSTRAINT fk_account_transaction_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_account_transaction_account
        FOREIGN KEY (account_id)
        REFERENCES account_dummy_tbl(account_id),

    CONSTRAINT chk_account_transaction_direction
        CHECK (
            direction IN ('DEBIT', 'CREDIT')
        ),

    CONSTRAINT chk_account_transaction_balance_before
        CHECK (
            balance_before >= 0
        ),

    CONSTRAINT chk_account_transaction_balance_after
        CHECK (
            balance_after >= 0
        )
);

-- 29. 지갑 거래 상세 테이블
DROP TABLE IF EXISTS wallet_transaction_tbl;

CREATE TABLE wallet_transaction_tbl (
    wallet_transaction_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '지갑 거래 ID',

    transaction_id INT NOT NULL COMMENT '거래번호',

    user_id INT NOT NULL COMMENT '회원번호',

    direction VARCHAR(10) NOT NULL COMMENT '입출금 구분',

    wallet_id INT NOT NULL COMMENT '지갑 ID',

    balance_before INT NOT NULL COMMENT '거래 전 잔액',

    balance_after INT NOT NULL COMMENT '거래 후 잔액',

    CONSTRAINT fk_wallet_transaction_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES financial_transaction_tbl(transaction_id),

    CONSTRAINT fk_wallet_transaction_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_wallet_transaction_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallet_tbl(wallet_id),

    CONSTRAINT chk_wallet_transaction_direction
        CHECK (
            direction IN ('DEBIT', 'CREDIT')
        ),

    CONSTRAINT chk_wallet_transaction_balance_before
        CHECK (
            balance_before >= 0
        ),

    CONSTRAINT chk_wallet_transaction_balance_after
        CHECK (
            balance_after >= 0
        )
);


-- 30. 카드 더미 테이블
DROP TABLE IF EXISTS card_tbl;

CREATE TABLE card_tbl (
    card_code VARCHAR(20) PRIMARY KEY COMMENT '카드코드',

    account_id INT NOT NULL UNIQUE COMMENT '계좌 ID',

    card_img_file_name VARCHAR(255) NULL COMMENT '카드이미지파일명',

    card_num VARCHAR(255) NOT NULL COMMENT '카드번호',

    expiry_date CHAR(5) NOT NULL COMMENT '유효기간',

    cvv VARCHAR(255) NOT NULL COMMENT 'cvv',

    CONSTRAINT fk_card_account
        FOREIGN KEY (account_id)
        REFERENCES account_dummy_tbl(account_id)
);

-- 31.간편비밀번호 테이블
DROP TABLE IF EXISTS pin_password_tbl;

CREATE TABLE pin_password_tbl (
    pin_password_id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'pin인증번호',

    user_id INT NOT NULL UNIQUE COMMENT '회원번호',

    hash_password VARCHAR(255) NOT NULL COMMENT '해시비밀번호',

    fail_count INT NOT NULL DEFAULT 0 COMMENT '오류누적횟수',

    locked_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '잠금여부',

    locked_at DATETIME NULL COMMENT '잠금일시',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_pin_password_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT chk_pin_password_fail_count
        CHECK (
            fail_count >= 0
        ),

    CONSTRAINT chk_pin_password_locked_yn
        CHECK (
            locked_yn IN ('Y', 'N')
        )
);

-- 32.등록실물카드 테이블
DROP TABLE IF EXISTS registered_card_tbl;

CREATE TABLE registered_card_tbl (
    card_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카드id',

    account_id INT NULL COMMENT '계좌 ID',

    user_id INT NOT NULL COMMENT '회원번호',

    card_num VARCHAR(255) NOT NULL COMMENT '카드번호',

    expiry_date CHAR(5) NOT NULL COMMENT '유효기간',

    cvv VARCHAR(255) NOT NULL COMMENT 'cvv',
    
    card_password VARCHAR(255) NOT NULL COMMENT '카드 비밀번호 4자리',

    represent_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '대표카드여부',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    delete_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '삭제여부',

    CONSTRAINT uq_registered_card_user
        UNIQUE (card_id, user_id),

    CONSTRAINT fk_registered_card_account
        FOREIGN KEY (account_id)
        REFERENCES account_dummy_tbl(account_id),

    CONSTRAINT fk_registered_card_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT chk_registered_card_represent_yn
        CHECK (
            represent_yn IN ('Y', 'N')
        ),

    CONSTRAINT chk_registered_card_delete_yn
        CHECK (
            delete_yn IN ('Y', 'N')
        )
);

-- 33.결제일회성토큰 테이블
-- card_id 복합 FK 설정 오류 가능성이 있습니다.

-- 정의서:

-- FOREIGN KEY (card_id, user_id)
-- REFERENCES registered_card_tbl(card_id, user_id)
-- 그런데 registered_card_tbl에서는 UNIQUE(card_id, user_id)가 설정되어 있어 현재 구조로는 참조 가능합니다.
-- 따라서 그대로 반영했습니다.

DROP TABLE IF EXISTS payment_token_tbl;

CREATE TABLE payment_token_tbl (
    token_value VARCHAR(255) PRIMARY KEY COMMENT '토큰값',

    user_id INT NOT NULL COMMENT '회원번호',

    card_id INT NULL COMMENT '매핑카드id',

    expired_at DATETIME NOT NULL COMMENT '만료일시',

    used_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '사용여부',

    CONSTRAINT fk_payment_token_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_payment_token_card
        FOREIGN KEY (card_id, user_id)
        REFERENCES registered_card_tbl(card_id, user_id),

    CONSTRAINT chk_payment_token_used_yn
        CHECK (
            used_yn IN ('Y', 'N')
        )
);

-- 34.영수증메모 테이블
DROP TABLE IF EXISTS receipt_memo_tbl;

CREATE TABLE receipt_memo_tbl (
    memo_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '메모id',

    transaction_id INT NOT NULL UNIQUE COMMENT '거래id',

    memo_content VARCHAR(300) NOT NULL COMMENT '메모내용',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_receipt_memo_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES financial_transaction_tbl(transaction_id)
);

-- 35.피드 테이블
-- transaction_id 컬럼의 UK 설정이 정의서상 Y입니다.
-- 비고: 거래당 1개의 피드 생성 UNIQUE(transaction_id)
-- 따라서 UNIQUE(transaction_id) 적용했습니다.
-- feed_id가 PK이면서 AUTO_INCREMENT인 구조는 정상입니다.
DROP TABLE IF EXISTS feed_tbl;

CREATE TABLE feed_tbl (
    feed_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '피드번호',

    user_id INT NOT NULL COMMENT '회원번호',

    transaction_id INT NULL UNIQUE COMMENT '거래번호',

    feed_type VARCHAR(20) NULL COMMENT '피드유형',

    content VARCHAR(20) NULL COMMENT '피드내용',

    visibility VARCHAR(20) NULL COMMENT '공개범위',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_feed_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_feed_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES financial_transaction_tbl(transaction_id),

    CONSTRAINT chk_feed_type
        CHECK (
            feed_type IN (
                'TRANSFER',
                'PAYMENT',
                'SETTLEMENT',
                'SHARE'
            )
        ),

    CONSTRAINT chk_feed_visibility
        CHECK (
            visibility IN (
                'PUBLIC',
                'FRIEND',
                'PRIVATE'
            )
        )
);

-- 36.피드 이미지 테이블
DROP TABLE IF EXISTS feed_image_tbl;

CREATE TABLE feed_image_tbl (
    image_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '이미지번호',

    feed_id INT NOT NULL COMMENT '피드번호',

    image_name VARCHAR(500) NOT NULL COMMENT '이미지이름',

    CONSTRAINT fk_feed_image_feed
        FOREIGN KEY (feed_id)
        REFERENCES feed_tbl(feed_id)
);

-- 37.좋아요 테이블
DROP TABLE IF EXISTS feed_like_tbl;

CREATE TABLE feed_like_tbl (
    like_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '좋아요 ID',

    feed_id INT NOT NULL COMMENT '피드 ID',

    user_id INT NOT NULL COMMENT '사용자 ID',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    CONSTRAINT uq_feed_like
        UNIQUE (feed_id, user_id),

    CONSTRAINT fk_feed_like_feed
        FOREIGN KEY (feed_id)
        REFERENCES feed_tbl(feed_id),

    CONSTRAINT fk_feed_like_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id)
);

-- 38.댓글 테이블
DROP TABLE IF EXISTS feed_comment_tbl;

CREATE TABLE feed_comment_tbl (
    comment_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '댓글 ID',

    feed_id INT NOT NULL COMMENT '피드 ID',

    user_id INT NOT NULL COMMENT '사용자 ID',

    content VARCHAR(20) NULL COMMENT '댓글 내용',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_feed_comment_feed
        FOREIGN KEY (feed_id)
        REFERENCES feed_tbl(feed_id),

    CONSTRAINT fk_feed_comment_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id)
);


-- 39.커스텀 도구 에셋 테이블
DROP TABLE IF EXISTS card_asset_tbl;

CREATE TABLE card_asset_tbl (
    asset_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '에셋ID',

    asset_type VARCHAR(30) NULL COMMENT '유형코드',

    asset_name VARCHAR(255) NULL COMMENT '에셋명칭',

    src_url VARCHAR(255) NULL COMMENT '에셋파일경로',

    background_color VARCHAR(255) NULL COMMENT '배경색상코드',

    font_type VARCHAR(30) NULL COMMENT '폰트종류',

    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용여부',

    CONSTRAINT chk_card_asset_type
        CHECK (
            asset_type IN (
                'BACKGROUND_SOLID',
                'BACKGROUND_GRADIENT',
                'BACKGROUND_SPECIAL',
                'EMOJI',
                'STICKER',
                'PATTERN'
            )
        ),

    CONSTRAINT chk_card_asset_use_yn
        CHECK (
            use_yn IN ('Y', 'N')
        )
);



-- 40.이미지 첨부파일 테이블

DROP TABLE IF EXISTS file_image_tbl;

CREATE TABLE file_image_tbl (
    file_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '첨부파일ID',

    user_id INT NOT NULL COMMENT '사용자ID',

    file_name VARCHAR(255) NOT NULL COMMENT '파일명',

    file_size BIGINT NOT NULL COMMENT '파일크기',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    CONSTRAINT fk_file_image_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id)
);


-- 41.커스텀 이미지 테이블

DROP TABLE IF EXISTS custom_image_tbl;

CREATE TABLE custom_image_tbl (
    custom_image_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '커스텀이미지 첨부파일ID',

    user_id INT NOT NULL COMMENT '사용자ID',

    asset_id INT NOT NULL COMMENT '에셋ID',

    file_id INT NOT NULL COMMENT '첨부파일ID',

    custom_image_path VARCHAR(255) NOT NULL COMMENT '커스텀이미지 경로명',

    custom_image_name VARCHAR(255) NOT NULL COMMENT '커스텀이미지 파일명',

    custom_image_size BIGINT NOT NULL COMMENT '커스텀이미지 파일크기',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    CONSTRAINT fk_custom_image_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_custom_image_asset
        FOREIGN KEY (asset_id)
        REFERENCES card_asset_tbl(asset_id),

    CONSTRAINT fk_custom_image_file
        FOREIGN KEY (file_id)
        REFERENCES file_image_tbl(file_id)
);

-- 42.커스텀카드 신청이력 테이블

DROP TABLE IF EXISTS card_application_history_tbl;

CREATE TABLE card_application_history_tbl (
    apply_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '신청ID',

    user_id INT NOT NULL COMMENT '사용자ID',

    custom_image_id INT NOT NULL COMMENT '커스텀이미지 첨부파일ID',

    card_code VARCHAR(20) NULL COMMENT '카드코드',

    card_name VARCHAR(50) NULL COMMENT '카드명',

    card_status VARCHAR(20) NULL COMMENT '카드신청상태',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_card_application_history_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT fk_card_application_history_image
        FOREIGN KEY (custom_image_id)
        REFERENCES custom_image_tbl(custom_image_id),

    CONSTRAINT chk_card_application_history_status
        CHECK (
            card_status IN (
                'REQUEST',
                'ISSUED',
                'CANCELLED'
            )
        )
);


-- 43.이벤트 테이블
-- event_type의 CHECK 조건이 정의서에는 ('ATTENDANCE', )만 존재합니다.
-- → 현재 기준 그대로 작성합니다.
DROP TABLE IF EXISTS event_tbl;

CREATE TABLE event_tbl (
    event_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '이벤트ID',

    event_name VARCHAR(100) NULL COMMENT '이벤트명',

    event_desc TEXT NULL COMMENT '이벤트상세설명',

    event_type VARCHAR(20) NULL COMMENT '이벤트유형',

    event_status VARCHAR(10) NOT NULL DEFAULT 'OPEN' COMMENT '이벤트 진행 토글',

    event_img_name VARCHAR(255) NULL COMMENT '이벤트 이미지 이름',

    event_target INT NOT NULL COMMENT '이벤트 최종 목표',

    event_level INT NOT NULL DEFAULT 1 COMMENT '이벤트 최종 난이도',

    start_at DATETIME NULL COMMENT '시작일시',

    end_at DATETIME NULL COMMENT '종료일시',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    CONSTRAINT chk_event_type
        CHECK (
            event_type IN ('ATTENDANCE')
        ),

    CONSTRAINT chk_event_status
        CHECK (
            event_status IN ('OPEN', 'CLOSE')
        )
);

-- 44.이벤트 리워드 테이블

DROP TABLE IF EXISTS event_reward_tbl;

CREATE TABLE event_reward_tbl (
    reward_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '리워드ID',

    event_id INT NOT NULL COMMENT '이벤트ID',

    event_level INT NOT NULL DEFAULT 1 COMMENT '이벤트 난이도',

    reward_point INT NULL DEFAULT 0 COMMENT '리워드포인트',

    reward_exe INT NULL COMMENT '리워드경험치',

    req_count INT NOT NULL COMMENT '레벨별 필요달성치',

    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용여부',

    CONSTRAINT fk_event_reward_event
        FOREIGN KEY (event_id)
        REFERENCES event_tbl(event_id),

    CONSTRAINT chk_event_reward_point
        CHECK (
            reward_point >= 0
        ),

    CONSTRAINT chk_event_reward_exe
        CHECK (
            reward_exe >= 0
        ),

    CONSTRAINT chk_event_reward_req_count
        CHECK (
            req_count >= 1
        ),

    CONSTRAINT chk_event_reward_use_yn
        CHECK (
            use_yn IN ('Y','N')
        )
);
-- 45.이벤트 참여이력 테이블

DROP TABLE IF EXISTS event_participation_tbl;

CREATE TABLE event_participation_tbl (
    participation_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '참여ID',

    event_id INT NOT NULL COMMENT '이벤트ID',

    user_id INT NOT NULL COMMENT '사용자ID',

    participated_at DATETIME NULL COMMENT '참여일시',

    CONSTRAINT fk_event_participation_event
        FOREIGN KEY (event_id)
        REFERENCES event_tbl(event_id),

    CONSTRAINT fk_event_participation_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    CONSTRAINT uk_event_participation
        UNIQUE (event_id, user_id)
);


-- 46. 이벤트 리워드 수령이력 테이블 정의서
-- UNIQUE(event_id, user_id)

-- 유지하면 의미는:

-- 한 사용자는 하나의 이벤트에서 리워드를 1번만 받을 수 있다

-- 라는 정책입니다.

-- 예:

-- event_id	user_id	reward_id	결과
-- 1	100	1	가능
-- 1	100	2	불가능 (이미 해당 이벤트 보상 수령)
CREATE TABLE event_reward_receive_tbl (

    recv_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '리워드수령ID',

    event_id INT NOT NULL COMMENT '이벤트ID',

    reward_id INT NOT NULL COMMENT '리워드ID',

    user_id INT NOT NULL COMMENT '사용자ID',

    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수령일시',


    CONSTRAINT uk_event_reward_receive
        UNIQUE (event_id, user_id),


    CONSTRAINT fk_event_reward_receive_event
        FOREIGN KEY (event_id)
        REFERENCES event_tbl(event_id),


    CONSTRAINT fk_event_reward_receive_reward
        FOREIGN KEY (reward_id)
        REFERENCES event_reward_tbl(reward_id),


    CONSTRAINT fk_event_reward_receive_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id)

) COMMENT='이벤트 리워드 수령이력';

-- 47. 이벤트 챌린지 테이블 정의서

CREATE TABLE event_challenge_tbl (

    challenge_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '챌린지ID',

    challenge_name VARCHAR(100) NULL COMMENT '챌린지명칭',

    reward_point INT NOT NULL DEFAULT 0 COMMENT '달성 시 지급 포인트',

    max_level INT NOT NULL COMMENT '챌린지 목표 난이도',

    max_target INT NOT NULL COMMENT '챌린지 목표 수치',

    start_date DATETIME NOT NULL COMMENT '챌린지 시작일',

    end_date DATETIME NOT NULL COMMENT '챌린지 종료일',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시'


) COMMENT='이벤트 챌린지';

-- 48. 이벤트 챌린지 참여이력 테이블 정의서
CREATE TABLE event_challenge_user_tbl (

    user_challenge_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '챌린지참여ID',

    user_id INT NOT NULL COMMENT '사용자ID',

    challenge_id INT NOT NULL COMMENT '챌린지ID',

    current_level INT NOT NULL COMMENT '현재 달성 레벨',

    current_target INT NOT NULL COMMENT '현재 누적 수치',

    status VARCHAR(20) NOT NULL DEFAULT 'PROCESS' COMMENT '현재 상태',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 갱신시간',


    CONSTRAINT uk_event_challenge_user
        UNIQUE(user_id, challenge_id),


    CONSTRAINT fk_event_challenge_user
        FOREIGN KEY(user_id)
        REFERENCES user_tbl(user_id),


    CONSTRAINT fk_event_challenge
        FOREIGN KEY(challenge_id)
        REFERENCES event_challenge_tbl(challenge_id),


    CONSTRAINT chk_event_challenge_status
        CHECK(
            status IN(
                'PROCESS',
                'COMPLETE',
                'REWARDED'
            )
        )

) COMMENT='이벤트 챌린지 참여이력';