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

DROP TABLE IF EXISTS `linked_card_tbl`;
DROP TABLE IF EXISTS `card_company_tbl`;
DROP TABLE IF EXISTS `event_challenge_user_tbl`;
DROP TABLE IF EXISTS `event_challenge_tbl`;
DROP TABLE IF EXISTS `event_reward_receive_tbl`;
DROP TABLE IF EXISTS `event_participation_tbl`;
DROP TABLE IF EXISTS `event_reward_tbl`;
DROP TABLE IF EXISTS `event_tbl`;
DROP TABLE IF EXISTS `card_application_history_tbl`;
DROP TABLE IF EXISTS `custom_image_tbl`;
DROP TABLE IF EXISTS `file_image_tbl`;
DROP TABLE IF EXISTS `card_asset_tbl`;
DROP TABLE IF EXISTS `feed_comment_tbl`;
DROP TABLE IF EXISTS `feed_like_tbl`;
DROP TABLE IF EXISTS `feed_image_tbl`;
DROP TABLE IF EXISTS `feed_tbl`;
DROP TABLE IF EXISTS `receipt_memo_tbl`;
DROP TABLE IF EXISTS `payment_token_tbl`;
DROP TABLE IF EXISTS `registered_card_tbl`;
DROP TABLE IF EXISTS `card_tbl`;
DROP TABLE IF EXISTS `wallet_transaction_tbl`;
DROP TABLE IF EXISTS `account_transaction_tbl`;
DROP TABLE IF EXISTS `account_dummy_tbl`;
DROP TABLE IF EXISTS `financial_transaction_tbl`;
DROP TABLE IF EXISTS `notification_tbl`;
DROP TABLE IF EXISTS `settlement_member_tbl`;
DROP TABLE IF EXISTS `settlement_tbl`;
DROP TABLE IF EXISTS `friend_tbl`;
DROP TABLE IF EXISTS `friend_request_tbl`;
DROP TABLE IF EXISTS `kb_insurance_recommendation_tbl`;
DROP TABLE IF EXISTS `kb_insurance_coverage_tbl`;
DROP TABLE IF EXISTS `kb_insurance_product_tbl`;
DROP TABLE IF EXISTS `card_recommendation_tbl`;
DROP TABLE IF EXISTS `card_benefit_tbl`;
DROP TABLE IF EXISTS `kb_card_product_tbl`;
DROP TABLE IF EXISTS `spending_analysis_category_tbl`;
DROP TABLE IF EXISTS `spending_analysis_tbl`;
DROP TABLE IF EXISTS `point_conversion_history_tbl`;
DROP TABLE IF EXISTS `attendance_tbl`;
DROP TABLE IF EXISTS `user_random_box_tbl`;
DROP TABLE IF EXISTS `point_transaction_tbl`;
DROP TABLE IF EXISTS `point_wallet_tbl`;
DROP TABLE IF EXISTS `spending_category_tbl`;
DROP TABLE IF EXISTS `refresh_token_tbl`;
DROP TABLE IF EXISTS `notification_setting_tbl`;
DROP TABLE IF EXISTS `profile_tbl`;
DROP TABLE IF EXISTS `verification_tbl`;
DROP TABLE IF EXISTS `wallet_tbl`;
DROP TABLE IF EXISTS `user_agreement_tbl`;
DROP TABLE IF EXISTS `agreement_tbl`;
DROP TABLE IF EXISTS `linked_account_tbl`;
DROP TABLE IF EXISTS `bank_tbl`;
DROP TABLE IF EXISTS `user_tbl`;
DROP TABLE IF EXISTS `tbl_member_auth`;
DROP TABLE IF EXISTS `tbl_member`;
DROP TABLE IF EXISTS `merchant_category_mapping_tbl`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `tbl_member`
(
    `username`    varchar(50)  NOT NULL,
    `password`    varchar(128) NOT NULL,
    `email`       varchar(50)  NOT NULL,
    `reg_date`    datetime DEFAULT CURRENT_TIMESTAMP,
    `update_date` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS tbl_member_auth;

CREATE TABLE `tbl_member_auth`
(
    `username` varchar(50) NOT NULL,
    `auth`     varchar(50) NOT NULL,
    PRIMARY KEY (`username`, `auth`),
    CONSTRAINT `fk_authorities_users` FOREIGN KEY (`username`) REFERENCES `tbl_member` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;



DROP TABLE IF EXISTS user_tbl;

-- 1. 회원 테이블 정의서
DROP TABLE IF EXISTS user_tbl;

CREATE TABLE user_tbl
(
    user_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '회원번호',
    user_name     VARCHAR(30)  NOT NULL COMMENT '이름',
    birth_date    CHAR(8)      NOT NULL COMMENT '생년월일',
    phone_number  VARCHAR(20)  NOT NULL UNIQUE COMMENT '휴대폰번호',
    pin_password  VARCHAR(255) NOT NULL COMMENT '암호화된 숫자 6자리 간편비밀번호',
    user_status   VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '회원상태',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '수정일시',
    withdrawn_at  DATETIME     NULL COMMENT '탈퇴일시',
    last_login_at DATETIME     NULL COMMENT '최근접속일시',

    CONSTRAINT chk_user_name_length
        CHECK (CHAR_LENGTH(user_name) BETWEEN 2 AND 30),

    CONSTRAINT chk_user_birth_date
        CHECK (birth_date REGEXP '^[0-9]{8}$'),

    CONSTRAINT chk_user_status
        CHECK (user_status IN ('ACTIVE', 'WITHDRAWN'))
) COMMENT = '회원';

-- 3. 은행 테이블
DROP TABLE IF EXISTS bank_tbl;

CREATE TABLE bank_tbl
(
    bank_code      VARCHAR(10) PRIMARY KEY COMMENT '은행코드',
    bank_name      VARCHAR(50)  NOT NULL UNIQUE COMMENT '은행명',
    bank_logo_name VARCHAR(255) NULL COMMENT '은행로고파일명',
    use_yn         CHAR(1)      NOT NULL DEFAULT 'Y' COMMENT '사용여부',

    CONSTRAINT chk_bank_use_yn
        CHECK (use_yn IN ('Y', 'N'))
);


-- 2. 사용자계좌 테이블
DROP TABLE IF EXISTS linked_account_tbl;

CREATE TABLE linked_account_tbl
(
    linked_account_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '연결계좌번호',
    user_id           INT          NOT NULL COMMENT '회원번호',
    bank_code         VARCHAR(10)  NOT NULL COMMENT '은행코드',
    account_number    VARCHAR(255) NOT NULL COMMENT '계좌번호',
    account_holder    VARCHAR(50)  NOT NULL COMMENT '예금주명',
    primary_yn        CHAR(1)      NOT NULL DEFAULT 'N' COMMENT '대표계좌여부',
    connection_status VARCHAR(20)  NOT NULL DEFAULT 'CONNECTED' COMMENT '연결상태',

    CONSTRAINT fk_linked_account_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_linked_account_bank
        FOREIGN KEY (bank_code)
            REFERENCES bank_tbl (bank_code),

    CONSTRAINT chk_primary_yn
        CHECK (primary_yn IN ('Y', 'N'))
);

-- 4. 약관 정보 테이블
DROP TABLE IF EXISTS agreement_tbl;

CREATE TABLE agreement_tbl
(
    agreement_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '약관번호',
    agreement_type    VARCHAR(30)  NOT NULL COMMENT '약관유형',
    agreement_name    VARCHAR(100) NOT NULL COMMENT '약관명',
    agreement_content TEXT         NOT NULL COMMENT '약관내용',
    required_yn       CHAR(1)      NOT NULL COMMENT '필수여부',
    use_yn            CHAR(1)      NOT NULL DEFAULT 'Y' COMMENT '사용여부',

    CONSTRAINT chk_agreement_required_yn
        CHECK (required_yn IN ('Y', 'N')),

    CONSTRAINT chk_agreement_use_yn
        CHECK (use_yn IN ('Y', 'N'))
);

-- 5. 유저 약관 동의 테이블
DROP TABLE IF EXISTS user_agreement_tbl;

CREATE TABLE user_agreement_tbl
(
    user_agreement_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '동의번호',
    user_id           INT      NOT NULL COMMENT '회원번호',
    agreement_id      INT      NOT NULL COMMENT '약관번호',
    agreed_yn         CHAR(1)  NOT NULL COMMENT '동의여부',
    agreed_at         DATETIME NOT NULL COMMENT '동의처리일시',

    CONSTRAINT fk_user_agreement_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_user_agreement
        FOREIGN KEY (agreement_id)
            REFERENCES agreement_tbl (agreement_id),

    CONSTRAINT chk_user_agreement_agreed_yn
        CHECK (agreed_yn IN ('Y', 'N'))
);


-- 6. 전자지갑 테이블
DROP TABLE IF EXISTS wallet_tbl;

CREATE TABLE wallet_tbl
(
    wallet_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '지갑번호',
    user_id       INT         NOT NULL UNIQUE COMMENT '회원번호',
    balance       INT         NOT NULL DEFAULT 0 COMMENT '지갑잔액',
    wallet_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '지갑상태',

    CONSTRAINT fk_wallet_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT chk_wallet_balance
        CHECK (balance >= 0),

    CONSTRAINT chk_wallet_status
        CHECK (wallet_status IN ('ACTIVE', 'CLOSED'))
);

-- 6-2. 인증 테이블 정의서
DROP TABLE IF EXISTS verification_tbl;

CREATE TABLE verification_tbl
(
    verification_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '인증번호',
    user_id              INT          NULL COMMENT '회원번호',
    user_name            VARCHAR(30)  NOT NULL COMMENT '인증이름',
    birth_date           CHAR(8)      NOT NULL COMMENT '생년월일',
    carrier_code         VARCHAR(20)  NOT NULL COMMENT '통신사코드',
    phone_number         VARCHAR(20)  NOT NULL COMMENT '휴대폰번호',
    verification_code    VARCHAR(255) NOT NULL COMMENT '암호화된 인증코드',
    verification_purpose VARCHAR(30)  NOT NULL COMMENT '인증목적',
    requested_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '인증요청일시',
    verified_yn          CHAR(1)      NOT NULL DEFAULT 'N' COMMENT '인증완료여부',
    fail_count           INT          NOT NULL DEFAULT 0 COMMENT '인증실패횟수',

    CONSTRAINT fk_verification_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT chk_verification_user_name_length
        CHECK (CHAR_LENGTH(user_name) BETWEEN 2 AND 30),

    CONSTRAINT chk_verification_birth_date
        CHECK (birth_date REGEXP '^[0-9]{8}$'),

    CONSTRAINT chk_verification_carrier
        CHECK (
            carrier_code IN (
                             'SKT',
                             'KT',
                             'LGU',
                             'SKT_MVNO',
                             'KT_MVNO',
                             'LGU_MVNO'
                )
            ),

    CONSTRAINT chk_verification_purpose
        CHECK (
            verification_purpose IN ('SIGN_UP',
                                     'PIN_RESET',
                                     'WITHDRAWAL'
                )
            ),

    CONSTRAINT chk_verification_verified_yn
        CHECK (verified_yn IN ('Y', 'N')),

    CONSTRAINT chk_verification_fail_count
        CHECK (fail_count >= 0)
) COMMENT = '휴대폰인증';


-- 6-3. 프로필 테이블
DROP TABLE IF EXISTS profile_tbl;

CREATE TABLE profile_tbl
(
    profile_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '프로필번호',
    user_id       INT          NOT NULL UNIQUE COMMENT '회원번호',
    nickname      VARCHAR(30)  NOT NULL UNIQUE COMMENT '닉네임',
    introduction  VARCHAR(300) NULL COMMENT '자기소개',
    original_name VARCHAR(255) NULL COMMENT '이미지원본명',
    stored_name   VARCHAR(255) NULL COMMENT '이미지파일명',
    created_at    DATETIME     NOT NULL COMMENT '생성일시',
    updated_at    DATETIME     NOT NULL COMMENT '수정일시',

    CONSTRAINT fk_profile_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id)
);

-- 6-4. 알림설정 테이블
DROP TABLE IF EXISTS notification_setting_tbl;

CREATE TABLE notification_setting_tbl
(
    notification_setting_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '알림설정번호',
    user_id                       INT      NOT NULL UNIQUE COMMENT '회원번호',
    finance_notification_yn       CHAR(1)  NOT NULL DEFAULT 'Y' COMMENT '금융알림여부',
    reward_notification_yn        CHAR(1)  NOT NULL DEFAULT 'Y' COMMENT '리워드알림여부',
    event_benefit_notification_yn CHAR(1)  NOT NULL DEFAULT 'N' COMMENT '이벤트혜택알림여부',
    updated_at                    DATETIME NOT NULL COMMENT '수정일시',

    CONSTRAINT fk_notification_setting_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT chk_finance_notification_yn
        CHECK (finance_notification_yn IN ('Y', 'N')),

    CONSTRAINT chk_reward_notification_yn
        CHECK (reward_notification_yn IN ('Y', 'N')),

    CONSTRAINT chk_event_benefit_notification_yn
        CHECK (event_benefit_notification_yn IN ('Y', 'N'))
);

-- 6-5. 리프레시토큰 테이블
DROP TABLE IF EXISTS refresh_token_tbl;

CREATE TABLE refresh_token_tbl
(
    refresh_token_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '토큰번호',
    user_id          INT          NOT NULL COMMENT '회원번호',
    refresh_token    VARCHAR(500) NOT NULL UNIQUE COMMENT '리프레시토큰',
    issued_at        DATETIME     NOT NULL COMMENT '발급일시',
    expires_at       DATETIME     NOT NULL COMMENT '만료일시',

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id)
);


-- 7. 소비 카테고리 테이블
DROP TABLE IF EXISTS spending_category_tbl;

CREATE TABLE spending_category_tbl
(
    spending_category_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카테고리 식별자',
    category_name        VARCHAR(100) NOT NULL UNIQUE COMMENT '카테고리명',
    parent_category_id   INT          NULL COMMENT '부모 카테고리 ID',

    CONSTRAINT fk_spending_category_parent
        FOREIGN KEY (parent_category_id)
            REFERENCES spending_category_tbl (spending_category_id)

--  spending_category_id가 AUTO_INCREMENT 컬럼이라 CHECK에서 사용할 수 없습니다. 코드에서 확인
--     CONSTRAINT chk_spending_category_parent
--         CHECK (
--             parent_category_id IS NULL
--             OR parent_category_id <> spending_category_id
--         )
);


-- 8.포인트 테이블 
-- UNIQUE(point_wallet_id, user_id)는 불필요합니다.
-- point_wallet_id가 PRIMARY KEY이므로 이미 유일합니다.
-- 따라서 (point_wallet_id, user_id) 복합 UNIQUE는 의미가 없습니다.
-- 정의서는 그대로 두더라도 DDL에서는 생략해도 동일한 효과입니다.

DROP TABLE IF EXISTS point_wallet_tbl;

CREATE TABLE point_wallet_tbl
(
    point_wallet_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '포인트 지갑PK',
    user_id         INT      NOT NULL UNIQUE COMMENT '유저 ID',
    point_balance   INT      NOT NULL DEFAULT 0 COMMENT '포인트 잔액',
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_point_wallet_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT chk_point_balance
        CHECK (point_balance >= 0)
);


-- 9.포인트거래내역 테이블
DROP TABLE IF EXISTS point_transaction_tbl;

CREATE TABLE point_transaction_tbl
(
    point_transaction_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '포인트 내역 PK',
    point_wallet_id      INT         NOT NULL COMMENT '유저 포인트 지갑',
    transaction_type     VARCHAR(20) NOT NULL COMMENT '거래 타입',
    point_amount         INT         NOT NULL COMMENT '증감된 포인트',
    reason_type          VARCHAR(30) NOT NULL COMMENT '증감 사유',
    created_at           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '거래 일시',

    CONSTRAINT fk_point_transaction_wallet
        FOREIGN KEY (point_wallet_id)
            REFERENCES point_wallet_tbl (point_wallet_id),

    CONSTRAINT chk_point_transaction_type
        CHECK (transaction_type IN ('EARN', 'USE', 'EXPIRE', 'CANCEL')),

    -- 0원 이상으로 하되, 상태값으로 증감처리.
    CONSTRAINT chk_point_transaction_amount
        CHECK (point_amount > 0),

    CONSTRAINT chk_point_transaction_reason
        CHECK (reason_type IN ('ATTENDANCE', 'RANDOM_BOX', 'CONVERSION', 'EVENT'))
);

-- 10.랜덤박스 테이블
DROP TABLE IF EXISTS user_random_box_tbl;

CREATE TABLE user_random_box_tbl
(
    user_random_box_id INT AUTO_INCREMENT
        PRIMARY KEY
        COMMENT '랜덤박스 PK',

    user_id            INT         NOT NULL
        COMMENT '랜덤박스를 지급받은 사용자 ID',

    issue_reason       VARCHAR(30) NOT NULL
        COMMENT '랜덤박스 지급 사유',

    source_id          INT         NOT NULL
        COMMENT '지급 원인이 된 데이터의 PK',

    target_account_id  INT         NULL
        COMMENT '송금 보상의 수취 계좌 ID',

    box_status         VARCHAR(20) NOT NULL
        DEFAULT 'UNOPENED'
        COMMENT '랜덤박스 개봉 상태',

    reward_point       INT         NULL
        COMMENT '랜덤박스 개봉 결과 포인트',

    issued_at          DATETIME    NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        COMMENT '랜덤박스 지급 일시',

    opened_at          DATETIME    NULL
        COMMENT '랜덤박스 개봉 일시',


    CONSTRAINT fk_user_random_box_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_random_box_target_wallet
        FOREIGN KEY (target_account_id)
            REFERENCES wallet_tbl (wallet_id),

    -- 출석, 피드공유하기, 송금, 이벤트

    CONSTRAINT chk_user_random_box_issue_reason
        CHECK (
            issue_reason IN (
                             'ATTENDANCE',
                             'FEED_SHARE',
                             'TRANSFER',
                             'EVENT'
                )
            ),

    CONSTRAINT chk_user_random_box_status
        CHECK (
            box_status IN (
                           'UNOPENED',
                           'OPENED'
                )
            ),

    CONSTRAINT chk_user_random_box_reward_point
        CHECK (
            reward_point IS NULL
                OR reward_point >= 0
            ),

    CONSTRAINT chk_user_random_box_open_state
        CHECK (
            (
                box_status = 'UNOPENED'
                    AND reward_point IS NULL
                    AND opened_at IS NULL
                )
                OR
            (
                box_status = 'OPENED'
                    AND reward_point IS NOT NULL
                    AND opened_at IS NOT NULL
                )
            ),

    /*
     * 출석·피드·송금 거래·이벤트 참여 이력 하나당
     * 랜덤박스 중복 지급 방지
     */
    CONSTRAINT uq_random_box_issue_source
        UNIQUE (
                user_id,
                issue_reason,
                source_id
        ),

    /*
     * 동일 사용자가 동일 수취 계좌로 반복 송금해도
     * 송금 랜덤박스는 한 번만 지급
     */
    CONSTRAINT uq_random_box_transfer_account
        UNIQUE (
                user_id,
                issue_reason,
                target_account_id
        ),

    /*
     * TRANSFER일 때만 target_account_id가 존재해야 한다.
     */
    CONSTRAINT chk_random_box_target_account
        CHECK (
            (
                issue_reason = 'TRANSFER'
                    AND target_account_id IS NOT NULL
                )
                OR
            (
                issue_reason <> 'TRANSFER'
                    AND target_account_id IS NULL
                )
            )


);


-- 11. 출석 내역 테이블
DROP TABLE IF EXISTS attendance_tbl;

CREATE TABLE attendance_tbl
(
    attendance_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '출석 PK',
    user_id         INT  NOT NULL COMMENT '출석된 사용자',
    attendance_date DATE NOT NULL COMMENT '출석 일자',

    CONSTRAINT uq_attendance_user_date
        UNIQUE (user_id, attendance_date),

    CONSTRAINT fk_attendance_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id)
);


-- 12. 포인트 전환 내역 테이블 정의서

-- 포인트 전환 내역 테이블
DROP TABLE IF EXISTS point_conversion_history_tbl;

CREATE TABLE point_conversion_history_tbl
(
    point_conversion_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '포인트 전환내역 PK',
    user_id             INT      NOT NULL COMMENT '유저 ID',
    point_wallet_id     INT      NOT NULL COMMENT '포인트 지갑 ID',
    wallet_id           INT      NOT NULL COMMENT '충전된 전자지갑 ID',
    converted_point     INT      NOT NULL COMMENT '전환 포인트',
    converted_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '전환 일시',

    CONSTRAINT fk_point_conversion_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_point_conversion_point_wallet
        FOREIGN KEY (point_wallet_id)
            REFERENCES point_wallet_tbl (point_wallet_id),

    CONSTRAINT fk_point_conversion_wallet
        FOREIGN KEY (wallet_id)
            REFERENCES wallet_tbl (wallet_id),

    CONSTRAINT chk_point_conversion_point
        CHECK (converted_point >= 100)
);

-- 13. 소비 분석 테이블
DROP TABLE IF EXISTS spending_analysis_tbl;

CREATE TABLE spending_analysis_tbl
(
    spending_analysis_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '소비분석ID',
    user_id                    INT          NOT NULL COMMENT '유저 ID',
    analysis_period            INT          NOT NULL COMMENT '분석 기간',
    representative_category_id INT          NOT NULL COMMENT '가장 많이 소비한 대표 카테고리',
    ai_title                   VARCHAR(100) NOT NULL COMMENT 'AI 생성 칭호',
    ai_analysis_summary        TEXT         NOT NULL COMMENT 'AI가 생성한 소비 분석 요약',
    created_at                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '분석 일자',

    CONSTRAINT fk_spending_analysis_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_spending_analysis_category
        FOREIGN KEY (representative_category_id)
            REFERENCES spending_category_tbl (spending_category_id),

    CONSTRAINT chk_spending_analysis_period
        CHECK (analysis_period IN (1, 3, 12))
);


-- 14. 분석결과저장 테이블
DROP TABLE IF EXISTS spending_analysis_category_tbl;

CREATE TABLE spending_analysis_category_tbl
(
    analysis_category_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카테고리별 분석 결과 식별자',
    spending_analysis_id INT           NOT NULL COMMENT '소비분석ID',
    spending_category_id INT           NOT NULL COMMENT '소비카테고리 ID',
    spending_amount      INT           NOT NULL COMMENT '카테고리 별 총 소비금액',
    spending_ratio       DECIMAL(5, 2) NOT NULL COMMENT '카테고리별 소비 비율',
    transaction_count    INT           NOT NULL COMMENT '카테고리별 거래 건수',
    created_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '저장 일시',

    CONSTRAINT uq_analysis_category
        UNIQUE (spending_analysis_id, spending_category_id),

    CONSTRAINT fk_analysis_category_analysis
        FOREIGN KEY (spending_analysis_id)
            REFERENCES spending_analysis_tbl (spending_analysis_id),

    CONSTRAINT fk_analysis_category_category
        FOREIGN KEY (spending_category_id)
            REFERENCES spending_category_tbl (spending_category_id),

    CONSTRAINT chk_spending_amount
        CHECK (spending_amount >= 0),

    CONSTRAINT chk_spending_ratio
        CHECK (spending_ratio BETWEEN 0 AND 100),

    CONSTRAINT chk_transaction_count
        CHECK (transaction_count >= 0)
);

-- 15. KB 카드 상품 설명 테이블
DROP TABLE IF EXISTS kb_card_product_tbl;

CREATE TABLE kb_card_product_tbl
(
    card_product_id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '카드 PK',
    card_name        VARCHAR(150)  NOT NULL COMMENT '카드명',
    card_type        VARCHAR(20)   NOT NULL COMMENT '신용/체크',
    card_description VARCHAR(1000) NULL COMMENT '카드 설명',
    card_image       VARCHAR(500)  NULL COMMENT '카드이미지명',
    application      VARCHAR(500)  NULL COMMENT '신청페이지 파일명',
    annual_fee       INT           NOT NULL COMMENT '연회비',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '카드 정보 등록 일시',

    CONSTRAINT chk_kb_card_product_type
        CHECK (card_type IN ('CREDIT', 'CHECK')),

    CONSTRAINT chk_kb_card_product_fee
        CHECK (annual_fee >= 0)
);

--  16. 카드 혜택 테이블
DROP TABLE IF EXISTS card_benefit_tbl;

CREATE TABLE card_benefit_tbl
(
    card_benefit_id         INT AUTO_INCREMENT PRIMARY KEY COMMENT '카드 혜택 PK',
    card_product_id         INT           NOT NULL COMMENT '혜택 적용 카드',
    spending_category_id    INT           NOT NULL COMMENT '소비 카테고리 ID',
    benefit_name            VARCHAR(150)  NOT NULL COMMENT '혜택명',
    benefit_amount          INT           NULL COMMENT '건당 할인 금액',
    benefit_rate            DECIMAL(5, 2) NULL COMMENT '할인율',
    monthly_limit           INT           NULL COMMENT '최대 혜택 금액',
    minimum_spending_amount INT           NULL COMMENT '최소 이용 금액',
    benefit_description     VARCHAR(1000) NULL COMMENT '혜택 상세 설명',
    created_at              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '혜택 등록 일시',

    CONSTRAINT fk_card_benefit_product
        FOREIGN KEY (card_product_id)
            REFERENCES kb_card_product_tbl (card_product_id),

    CONSTRAINT fk_card_benefit_category
        FOREIGN KEY (spending_category_id)
            REFERENCES spending_category_tbl (spending_category_id),

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

CREATE TABLE card_recommendation_tbl
(
    card_recommendation_id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '카드 추천 PK',
    spending_analysis_id    INT      NOT NULL COMMENT '소비분석 ID',
    card_product_id         INT      NOT NULL COMMENT '추천 카드',
    recommendation_rank     INT      NOT NULL COMMENT '추천 순위',
    expected_benefit_amount INT      NULL COMMENT '예상 할인 금액',
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '추천 일시',

    CONSTRAINT uq_card_recommendation
        UNIQUE (spending_analysis_id, card_product_id),

    CONSTRAINT fk_card_recommendation_analysis
        FOREIGN KEY (spending_analysis_id)
            REFERENCES spending_analysis_tbl (spending_analysis_id),

    CONSTRAINT fk_card_recommendation_product
        FOREIGN KEY (card_product_id)
            REFERENCES kb_card_product_tbl (card_product_id),

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

CREATE TABLE kb_insurance_product_tbl
(
    insurance_product_id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '보험 PK',
    insurance_name        VARCHAR(200)  NOT NULL COMMENT '보험상품 ID',
    insurance_category    VARCHAR(50)   NOT NULL COMMENT '보험 카테고리',
    insurance_description VARCHAR(1000) NULL COMMENT '보험 설명',
    monthly_premium       INT           NULL COMMENT '예상 보험료',
    insurance_image       VARCHAR(500)  NULL COMMENT '보험 상품 이미지 파일명',
    application_url       VARCHAR(500)  NULL COMMENT '가입 페이지 주소',
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    CONSTRAINT chk_monthly_premium
        CHECK (
            monthly_premium IS NULL
                OR monthly_premium >= 0
            )
);

-- 19.KB 보험 보장 항목 테이블
DROP TABLE IF EXISTS kb_insurance_coverage_tbl;

CREATE TABLE kb_insurance_coverage_tbl
(
    insurance_coverage_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '보험 PK',
    insurance_product_id  INT           NOT NULL COMMENT '보험상품명',
    coverage_name         VARCHAR(200)  NOT NULL COMMENT '보장 항목명',
    coverage_amount       INT           NULL COMMENT '보장금액',
    coverage_description  VARCHAR(1000) NULL COMMENT '보장조건 상세설명',
    coverage_limit        VARCHAR(100)  NULL COMMENT '보장 횟수',
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '보험 등록일시',

    CONSTRAINT fk_insurance_coverage_product
        FOREIGN KEY (insurance_product_id)
            REFERENCES kb_insurance_product_tbl (insurance_product_id),

    CONSTRAINT chk_coverage_amount
        CHECK (
            coverage_amount IS NULL
                OR coverage_amount >= 0
            )
);


-- 20. KB 보험 추천 결과 테이블
DROP TABLE IF EXISTS kb_insurance_recommendation_tbl;

CREATE TABLE kb_insurance_recommendation_tbl
(
    insurance_recommendation_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '보험 추천 PK',
    spending_analysis_id        INT           NOT NULL COMMENT '소비분석 ID',
    insurance_product_id        INT           NOT NULL COMMENT '추천 보험 ID',
    recommendation_reason       VARCHAR(1000) NOT NULL COMMENT '추천이유',
    created_at                  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '추천 생성 일시',

    CONSTRAINT uq_insurance_recommendation
        UNIQUE (spending_analysis_id, insurance_product_id),

    CONSTRAINT fk_insurance_recommendation_analysis
        FOREIGN KEY (spending_analysis_id)
            REFERENCES spending_analysis_tbl (spending_analysis_id),

    CONSTRAINT fk_insurance_recommendation_product
        FOREIGN KEY (insurance_product_id)
            REFERENCES kb_insurance_product_tbl (insurance_product_id)
);


-- 21. 친구 요청 테이블
DROP TABLE IF EXISTS friend_request_tbl;

CREATE TABLE friend_request_tbl
(
    request_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '친구 요청 ID',

    requester_id INT         NOT NULL COMMENT '요청자 회원번호',

    receiver_id  INT         NOT NULL COMMENT '대상 회원번호',

    status       VARCHAR(20) NOT NULL DEFAULT 'REQUEST' COMMENT '친구요청상태',

    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT uq_friend_request
        UNIQUE (requester_id, receiver_id),

    CONSTRAINT fk_friend_request_requester
        FOREIGN KEY (requester_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_friend_request_receiver
        FOREIGN KEY (receiver_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT chk_friend_request_status
        CHECK (status IN ('REQUEST', 'ACCEPT', 'REJECT', 'CANCEL')),

    CONSTRAINT chk_friend_request_self
        CHECK (requester_id <> receiver_id)
);

-- 22.친구 테이블
DROP TABLE IF EXISTS friend_tbl;

CREATE TABLE friend_tbl
(
    friend_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '팔로우 ID',

    user_id        INT      NOT NULL COMMENT '요청자 회원번호',

    friend_user_id INT      NOT NULL COMMENT '친구 회원번호',

    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '친구 생성일시',

    CONSTRAINT uq_friend_user
        UNIQUE (user_id, friend_user_id),

    CONSTRAINT fk_friend_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_friend_friend_user
        FOREIGN KEY (friend_user_id)
            REFERENCES user_tbl (user_id)
);

-- 23. 정산 테이블
DROP TABLE IF EXISTS settlement_tbl;

CREATE TABLE settlement_tbl
(
    settlement_id        INT AUTO_INCREMENT PRIMARY KEY COMMENT '정산 ID',

    requester_id         INT         NOT NULL COMMENT '요청자 ID',

    title                VARCHAR(20) NULL COMMENT '정산 제목',

    total_amount         INT         NULL COMMENT '총 정산 금액',

    status               VARCHAR(20) NULL COMMENT '정산 상태',

    created_at           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    settlement_type      VARCHAR(10) NOT NULL COMMENT '정산 방식',

    spending_category_id INT         NULL COMMENT '소비 카테고리 ID',

    completed_at         DATETIME    NULL COMMENT '완료일시',

    CONSTRAINT fk_settlement_requester
        FOREIGN KEY (requester_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_settlement_category
        FOREIGN KEY (spending_category_id)
            REFERENCES spending_category_tbl (spending_category_id),

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

CREATE TABLE settlement_member_tbl
(
    settlement_member_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '정산 참여자 ID',

    settlement_id        INT         NOT NULL COMMENT '정산 ID',

    user_id              INT         NOT NULL COMMENT '사용자 ID',

    amount               INT         NULL COMMENT '정산 금액',

    status               VARCHAR(20) NULL COMMENT '정산 상태',

    created_at           DATETIME    NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    last_reminder_date   DATETIME    NULL DEFAULT CURRENT_TIMESTAMP COMMENT '마지막으로 리마인드한 날짜',

    completed_at         DATETIME    NULL COMMENT '완료일시',

    CONSTRAINT uq_settlement_member
        UNIQUE (settlement_id, user_id),

    CONSTRAINT fk_settlement_member_settlement
        FOREIGN KEY (settlement_id)
            REFERENCES settlement_tbl (settlement_id),

    CONSTRAINT fk_settlement_member_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

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

CREATE TABLE notification_tbl
(
    notification_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '알림번호',

    receiver_id       INT         NOT NULL COMMENT '수신자번호',

    sender_id         INT         NOT NULL COMMENT '발신자번호',

    notification_type VARCHAR(30) NULL COMMENT '알림유형',

    target_id         INT         NULL COMMENT '대상번호',

    is_read           CHAR(1)     NOT NULL DEFAULT 'N' COMMENT '읽음여부',

    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    CONSTRAINT fk_notification_receiver
        FOREIGN KEY (receiver_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_notification_sender
        FOREIGN KEY (sender_id)
            REFERENCES user_tbl (user_id),

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
-- ============================================
-- 26. 통합 거래 원장 테이블
-- ============================================

DROP TABLE IF EXISTS financial_transaction_tbl;

CREATE TABLE financial_transaction_tbl
(

    transaction_id        INT AUTO_INCREMENT PRIMARY KEY
        COMMENT '거래번호',

    parent_transaction_id INT          NULL
        COMMENT '상위 거래번호',

    user_id               INT          NOT NULL
        COMMENT '거래 요청자 회원번호',

    receive_id            INT          NULL
        COMMENT '거래 요청을 받는 회원번호',

    transaction_type      VARCHAR(30)  NOT NULL
        COMMENT '거래유형',

    source_type           VARCHAR(20)  NOT NULL
        COMMENT '거래 출처 유형',

    target_type           VARCHAR(20)  NOT NULL
        COMMENT '거래 대상 유형',

    transaction_status    VARCHAR(20)  NOT NULL
        COMMENT '거래상태',

    amount                INT          NOT NULL
        COMMENT '거래금액',

    merchant_name         VARCHAR(100) NULL
        COMMENT '결제 가맹점명',

    spending_category_id  INT          NULL
        COMMENT '소비 카테고리 ID',

    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '생성일시',


    -- 상위 거래 관계
    CONSTRAINT fk_financial_transaction_parent
        FOREIGN KEY (parent_transaction_id)
            REFERENCES financial_transaction_tbl (transaction_id),


    -- 요청자 회원
    CONSTRAINT fk_financial_transaction_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),


    -- 수신자 회원
    CONSTRAINT fk_financial_transaction_receiver
        FOREIGN KEY (receive_id)
            REFERENCES user_tbl (user_id),


    -- 소비 카테고리
    CONSTRAINT fk_financial_transaction_category
        FOREIGN KEY (spending_category_id)
            REFERENCES spending_category_tbl (spending_category_id),


    -- 거래 유형
    CONSTRAINT chk_financial_transaction_type
        CHECK (
            transaction_type IN (
                                 'CHARGE',
                                 'PAYMENT',
                                 'TRANSFER',
                                 'SETTLEMENT'
                )
            ),


    -- 출처 유형
    CONSTRAINT chk_financial_source_type
        CHECK (
            source_type IN (
                            'ACCOUNT',
                            'WALLET'
                )
            ),


    -- 대상 유형
    CONSTRAINT chk_financial_target_type
        CHECK (
            target_type IN (
                            'ACCOUNT',
                            'WALLET'
                )
            ),


    -- 거래 상태
    CONSTRAINT chk_financial_transaction_status
        CHECK (
            transaction_status IN (
                                   'SUCCESS',
                                   'FAILED',
                                   'CANCELLED'
                )
            ),


    -- 금액 검증
    CONSTRAINT chk_financial_transaction_amount
        CHECK (
            amount >= 0
            )
);

-- 27.은행 계좌 더미 테이블
DROP TABLE IF EXISTS account_dummy_tbl;

CREATE TABLE account_dummy_tbl
(
    account_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '계좌 ID',

    user_id          INT          NOT NULL COMMENT '회원번호',

    bank_code        VARCHAR(3)   NOT NULL COMMENT '은행코드',

    account_number   VARCHAR(30)  NULL UNIQUE COMMENT '계좌번호',

    owner_name       VARCHAR(50)  NOT NULL COMMENT '예금주명',

    balance          INT          NOT NULL COMMENT '보유잔액',

    account_password VARCHAR(255) NOT NULL COMMENT '계좌비밀번호',

    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '개설일시',

    CONSTRAINT fk_account_dummy_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_account_dummy_bank
        FOREIGN KEY (bank_code)
            REFERENCES bank_tbl (bank_code),

    CONSTRAINT chk_account_dummy_balance
        CHECK (
            balance >= 0
            )
);


-- 28.계좌 거래 상세 테이블
DROP TABLE IF EXISTS account_transaction_tbl;

CREATE TABLE account_transaction_tbl
(
    account_transaction_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '계좌 거래 ID',

    transaction_id         INT         NOT NULL COMMENT '거래번호',

    user_id                INT         NOT NULL COMMENT '회원번호',

    direction              VARCHAR(10) NOT NULL COMMENT '입출금 구분',

    account_id             INT         NOT NULL COMMENT '계좌 ID',

    balance_before         INT         NOT NULL COMMENT '거래 전 잔액',

    balance_after          INT         NOT NULL COMMENT '거래 후 잔액',

    CONSTRAINT fk_account_transaction_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES financial_transaction_tbl (transaction_id),

    CONSTRAINT fk_account_transaction_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_account_transaction_account
        FOREIGN KEY (account_id)
            REFERENCES account_dummy_tbl (account_id),

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

CREATE TABLE wallet_transaction_tbl
(
    wallet_transaction_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '지갑 거래 ID',

    transaction_id        INT         NOT NULL COMMENT '거래번호',

    user_id               INT         NOT NULL COMMENT '회원번호',

    direction             VARCHAR(10) NOT NULL COMMENT '입출금 구분',

    wallet_id             INT         NOT NULL COMMENT '지갑 ID',

    balance_before        INT         NOT NULL COMMENT '거래 전 잔액',

    balance_after         INT         NOT NULL COMMENT '거래 후 잔액',

    CONSTRAINT fk_wallet_transaction_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES financial_transaction_tbl (transaction_id),

    CONSTRAINT fk_wallet_transaction_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_wallet_transaction_wallet
        FOREIGN KEY (wallet_id)
            REFERENCES wallet_tbl (wallet_id),

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

CREATE TABLE card_tbl
(
    card_code          VARCHAR(20) PRIMARY KEY COMMENT '카드코드',

    account_id         INT          NOT NULL UNIQUE COMMENT '계좌 ID',

    card_img_file_name VARCHAR(255) NULL COMMENT '카드이미지파일명',

    card_num           VARCHAR(255) NOT NULL COMMENT '카드번호',

    expiry_date        CHAR(5)      NOT NULL COMMENT '유효기간',

    cvv                VARCHAR(255) NOT NULL COMMENT 'cvv',

    CONSTRAINT fk_card_account
        FOREIGN KEY (account_id)
            REFERENCES account_dummy_tbl (account_id)
);

-- 32.등록실물카드 테이블
DROP TABLE IF EXISTS registered_card_tbl;

CREATE TABLE registered_card_tbl
(
    card_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '카드id',

    account_id    INT          NULL COMMENT '계좌 ID',

    user_id       INT          NOT NULL COMMENT '회원번호',

    card_num      VARCHAR(255) NOT NULL COMMENT '카드번호',

    expiry_date   CHAR(5)      NOT NULL COMMENT '유효기간',

    cvv           VARCHAR(255) NOT NULL COMMENT 'cvv',

    card_password VARCHAR(255) NOT NULL COMMENT '카드 비밀번호 4자리',

    represent_yn  CHAR(1)      NOT NULL DEFAULT 'N' COMMENT '대표카드여부',

    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    delete_yn     CHAR(1)      NOT NULL DEFAULT 'N' COMMENT '삭제여부',

    CONSTRAINT uq_registered_card_user
        UNIQUE (card_id, user_id),

    CONSTRAINT fk_registered_card_account
        FOREIGN KEY (account_id)
            REFERENCES account_dummy_tbl (account_id),

    CONSTRAINT fk_registered_card_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

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

CREATE TABLE payment_token_tbl
(
    token_value VARCHAR(255) PRIMARY KEY COMMENT '토큰값',

    user_id     INT      NOT NULL COMMENT '회원번호',

    card_id     INT      NULL COMMENT '매핑카드id',

    expired_at  DATETIME NOT NULL COMMENT '만료일시',

    used_yn     CHAR(1)  NOT NULL DEFAULT 'N' COMMENT '사용여부',

    CONSTRAINT fk_payment_token_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_payment_token_card
        FOREIGN KEY (card_id, user_id)
            REFERENCES registered_card_tbl (card_id, user_id),

    CONSTRAINT chk_payment_token_used_yn
        CHECK (
            used_yn IN ('Y', 'N')
            )
);

-- 34.영수증메모 테이블
DROP TABLE IF EXISTS receipt_memo_tbl;

CREATE TABLE receipt_memo_tbl
(
    memo_id        INT AUTO_INCREMENT PRIMARY KEY COMMENT '메모id',

    transaction_id INT          NOT NULL UNIQUE COMMENT '거래id',

    memo_content   VARCHAR(300) NOT NULL COMMENT '메모내용',

    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_receipt_memo_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES financial_transaction_tbl (transaction_id)
);

-- 35.피드 테이블
-- transaction_id 컬럼의 UK 설정이 정의서상 Y입니다.
-- 비고: 거래당 1개의 피드 생성 UNIQUE(transaction_id)
-- 따라서 UNIQUE(transaction_id) 적용했습니다.
-- feed_id가 PK이면서 AUTO_INCREMENT인 구조는 정상입니다.
DROP TABLE IF EXISTS feed_tbl;

CREATE TABLE feed_tbl
(
    feed_id        INT AUTO_INCREMENT PRIMARY KEY COMMENT '피드번호',

    user_id        INT         NOT NULL COMMENT '회원번호',

    transaction_id INT         NULL UNIQUE COMMENT '거래번호',

    feed_type      VARCHAR(20) NULL COMMENT '피드유형',

    content        VARCHAR(20) NULL COMMENT '피드내용',

    visibility     VARCHAR(20) NULL COMMENT '공개범위',

    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    updated_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_feed_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_feed_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES financial_transaction_tbl (transaction_id),

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

CREATE TABLE feed_image_tbl
(
    image_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '이미지번호',

    feed_id    INT          NOT NULL COMMENT '피드번호',

    image_name VARCHAR(500) NOT NULL COMMENT '이미지이름',

    CONSTRAINT fk_feed_image_feed
        FOREIGN KEY (feed_id)
            REFERENCES feed_tbl (feed_id)
);

-- 37.좋아요 테이블
DROP TABLE IF EXISTS feed_like_tbl;

CREATE TABLE feed_like_tbl
(
    like_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '좋아요 ID',

    feed_id    INT      NOT NULL COMMENT '피드 ID',

    user_id    INT      NOT NULL COMMENT '사용자 ID',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    CONSTRAINT uq_feed_like
        UNIQUE (feed_id, user_id),

    CONSTRAINT fk_feed_like_feed
        FOREIGN KEY (feed_id)
            REFERENCES feed_tbl (feed_id),

    CONSTRAINT fk_feed_like_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id)
);

-- 38.댓글 테이블
DROP TABLE IF EXISTS feed_comment_tbl;

CREATE TABLE feed_comment_tbl
(
    comment_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '댓글 ID',

    feed_id    INT         NOT NULL COMMENT '피드 ID',

    user_id    INT         NOT NULL COMMENT '사용자 ID',

    content    VARCHAR(20) NULL COMMENT '댓글 내용',

    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_feed_comment_feed
        FOREIGN KEY (feed_id)
            REFERENCES feed_tbl (feed_id),

    CONSTRAINT fk_feed_comment_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id)
);


-- 39.커스텀 도구 에셋 테이블
DROP TABLE IF EXISTS card_asset_tbl;

CREATE TABLE card_asset_tbl
(
    asset_id         INT AUTO_INCREMENT PRIMARY KEY COMMENT '에셋ID',

    asset_type       VARCHAR(30)  NULL COMMENT '유형코드',

    asset_name       VARCHAR(255) NULL COMMENT '에셋명칭',

    src_url          VARCHAR(255) NULL COMMENT '에셋파일경로',

    background_color VARCHAR(255) NULL COMMENT '배경색상코드',

    font_type        VARCHAR(30)  NULL COMMENT '폰트종류',

    use_yn           CHAR(1)      NOT NULL DEFAULT 'Y' COMMENT '사용여부',

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

CREATE TABLE file_image_tbl
(
    file_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '첨부파일ID',

    user_id    INT          NOT NULL COMMENT '사용자ID',

    file_name  VARCHAR(255) NOT NULL COMMENT '파일명',

    file_size  BIGINT       NOT NULL COMMENT '파일크기',

    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    CONSTRAINT fk_file_image_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id)
);


-- 41.커스텀 이미지 테이블

DROP TABLE IF EXISTS custom_image_tbl;

CREATE TABLE custom_image_tbl
(
    custom_image_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '커스텀이미지 첨부파일ID',

    user_id           INT          NOT NULL COMMENT '사용자ID',

    asset_id          INT          NOT NULL COMMENT '에셋ID',

    file_id           INT          NOT NULL COMMENT '첨부파일ID',

    custom_image_path VARCHAR(255) NOT NULL COMMENT '커스텀이미지 경로명',

    custom_image_name VARCHAR(255) NOT NULL COMMENT '커스텀이미지 파일명',

    custom_image_size BIGINT       NOT NULL COMMENT '커스텀이미지 파일크기',

    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    CONSTRAINT fk_custom_image_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_custom_image_asset
        FOREIGN KEY (asset_id)
            REFERENCES card_asset_tbl (asset_id),

    CONSTRAINT fk_custom_image_file
        FOREIGN KEY (file_id)
            REFERENCES file_image_tbl (file_id)
);

-- 42.커스텀카드 신청이력 테이블

DROP TABLE IF EXISTS card_application_history_tbl;

CREATE TABLE card_application_history_tbl
(
    apply_id        INT AUTO_INCREMENT PRIMARY KEY COMMENT '신청ID',

    user_id         INT         NOT NULL COMMENT '사용자ID',

    custom_image_id INT         NOT NULL COMMENT '커스텀이미지 첨부파일ID',

    card_code       VARCHAR(20) NULL COMMENT '카드코드',

    card_name       VARCHAR(50) NULL COMMENT '카드명',

    card_status     VARCHAR(20) NULL COMMENT '카드신청상태',

    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_card_application_history_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_card_application_history_image
        FOREIGN KEY (custom_image_id)
            REFERENCES custom_image_tbl (custom_image_id),

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

CREATE TABLE event_tbl
(
    event_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '이벤트ID',

    event_name     VARCHAR(100) NULL COMMENT '이벤트명',

    event_desc     TEXT         NULL COMMENT '이벤트상세설명',

    event_type     VARCHAR(20)  NULL COMMENT '이벤트유형',

    event_status   VARCHAR(10)  NOT NULL DEFAULT 'OPEN' COMMENT '이벤트 진행 토글',

    event_img_name VARCHAR(255) NULL COMMENT '이벤트 이미지 이름',

    event_target   INT          NOT NULL COMMENT '이벤트 최종 목표',

    event_level    INT          NOT NULL DEFAULT 1 COMMENT '이벤트 최종 난이도',

    start_at       DATETIME     NULL COMMENT '시작일시',

    end_at         DATETIME     NULL COMMENT '종료일시',

    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

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

CREATE TABLE event_reward_tbl
(
    reward_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '리워드ID',

    event_id     INT     NOT NULL COMMENT '이벤트ID',

    event_level  INT     NOT NULL DEFAULT 1 COMMENT '이벤트 난이도',

    reward_point INT     NULL     DEFAULT 0 COMMENT '리워드포인트',

    reward_exe   INT     NULL COMMENT '리워드경험치',

    req_count    INT     NOT NULL COMMENT '레벨별 필요달성치',

    use_yn       CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용여부',

    CONSTRAINT fk_event_reward_event
        FOREIGN KEY (event_id)
            REFERENCES event_tbl (event_id),

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
            use_yn IN ('Y', 'N')
            )
);
-- 45.이벤트 참여이력 테이블

DROP TABLE IF EXISTS event_participation_tbl;

CREATE TABLE event_participation_tbl
(
    participation_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '참여ID',

    event_id         INT      NOT NULL COMMENT '이벤트ID',

    user_id          INT      NOT NULL COMMENT '사용자ID',

    participated_at  DATETIME NULL COMMENT '참여일시',

    CONSTRAINT fk_event_participation_event
        FOREIGN KEY (event_id)
            REFERENCES event_tbl (event_id),

    CONSTRAINT fk_event_participation_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

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
CREATE TABLE event_reward_receive_tbl
(

    recv_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '리워드수령ID',

    event_id    INT      NOT NULL COMMENT '이벤트ID',

    reward_id   INT      NOT NULL COMMENT '리워드ID',

    user_id     INT      NOT NULL COMMENT '사용자ID',

    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수령일시',


    CONSTRAINT uk_event_reward_receive
        UNIQUE (event_id, user_id),


    CONSTRAINT fk_event_reward_receive_event
        FOREIGN KEY (event_id)
            REFERENCES event_tbl (event_id),


    CONSTRAINT fk_event_reward_receive_reward
        FOREIGN KEY (reward_id)
            REFERENCES event_reward_tbl (reward_id),


    CONSTRAINT fk_event_reward_receive_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id)

) COMMENT ='이벤트 리워드 수령이력';

-- 47. 이벤트 챌린지 테이블 정의서

CREATE TABLE event_challenge_tbl
(

    challenge_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '챌린지ID',

    challenge_name VARCHAR(100) NULL COMMENT '챌린지명칭',

    reward_point   INT          NOT NULL DEFAULT 0 COMMENT '달성 시 지급 포인트',

    max_level      INT          NOT NULL COMMENT '챌린지 목표 난이도',

    max_target     INT          NOT NULL COMMENT '챌린지 목표 수치',

    start_date     DATETIME     NOT NULL COMMENT '챌린지 시작일',

    end_date       DATETIME     NOT NULL COMMENT '챌린지 종료일',

    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시'


) COMMENT ='이벤트 챌린지';

-- 48. 이벤트 챌린지 참여이력 테이블 정의서
CREATE TABLE event_challenge_user_tbl
(

    user_challenge_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '챌린지참여ID',

    user_id           INT         NOT NULL COMMENT '사용자ID',

    challenge_id      INT         NOT NULL COMMENT '챌린지ID',

    current_level     INT         NOT NULL COMMENT '현재 달성 레벨',

    current_target    INT         NOT NULL COMMENT '현재 누적 수치',

    status            VARCHAR(20) NOT NULL DEFAULT 'PROCESS' COMMENT '현재 상태',

    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 갱신시간',


    CONSTRAINT uk_event_challenge_user
        UNIQUE (user_id, challenge_id),


    CONSTRAINT fk_event_challenge_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),


    CONSTRAINT fk_event_challenge
        FOREIGN KEY (challenge_id)
            REFERENCES event_challenge_tbl (challenge_id),


    CONSTRAINT chk_event_challenge_status
        CHECK (
            status IN (
                       'PROCESS',
                       'COMPLETE',
                       'REWARDED'
                )
            )

) COMMENT ='이벤트 챌린지 참여이력';

-- 49. 카드사 테이블 정의서
DROP TABLE IF EXISTS card_company_tbl;

CREATE TABLE card_company_tbl
(
    card_company_code VARCHAR(10) PRIMARY KEY COMMENT '카드사코드',

    card_company_name VARCHAR(50) NOT NULL UNIQUE COMMENT '카드사명',

    CONSTRAINT chk_card_company_name
        CHECK (CHAR_LENGTH(TRIM(card_company_name)) > 0)
) COMMENT = '카드사';


-- 50. 연결카드 테이블 정의서
DROP TABLE IF EXISTS linked_card_tbl;

CREATE TABLE linked_card_tbl
(
    linked_card_id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '연결카드번호',
    user_id           INT          NOT NULL COMMENT '회원번호',

    card_id           INT          NOT NULL UNIQUE COMMENT '등록카드번호',

    card_company_code VARCHAR(10)  NOT NULL COMMENT '카드사코드',

    card_name         VARCHAR(100) NOT NULL COMMENT '카드명',

    card_image_name   VARCHAR(255) NULL COMMENT '카드이미지파일명',

    represent_yn      CHAR(1)      NOT NULL DEFAULT 'N' COMMENT '대표카드여부',

    CONSTRAINT fk_linked_card_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_linked_card_registered_card
        FOREIGN KEY (card_id)
            REFERENCES registered_card_tbl (card_id),

    CONSTRAINT fk_linked_card_company
        FOREIGN KEY (card_company_code)
            REFERENCES card_company_tbl (card_company_code),

    CONSTRAINT chk_linked_card_name_length
        CHECK (CHAR_LENGTH(card_name) BETWEEN 1 AND 100),

    CONSTRAINT chk_linked_card_represent_yn
        CHECK (represent_yn IN ('Y', 'N'))
) COMMENT = '연결카드';

-- 51. 카테고리 분류 저장 테이블
CREATE TABLE merchant_category_mapping_tbl
(
    merchant_category_mapping_id INT AUTO_INCREMENT
        COMMENT '가맹점 카테고리 매핑 ID',

    merchant_name                VARCHAR(100) NOT NULL
        COMMENT '매핑 조회용 가맹점명',

    spending_category_id         INT          NOT NULL
        COMMENT '매핑된 소비 카테고리 ID',

    correction_count             INT          NOT NULL DEFAULT 0
        COMMENT '사용자의 카테고리 수정 요청 건수',

    created_at                   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '매핑 생성일시',

    updated_at                   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '매핑 수정일시',

    PRIMARY KEY (
                 merchant_category_mapping_id
        ),

    UNIQUE KEY uk_merchant_category_mapping_name (
                                                  merchant_name
        ),

    CONSTRAINT fk_merchant_category_mapping_category
        FOREIGN KEY (
                     spending_category_id
            )
            REFERENCES spending_category_tbl (
                                              spending_category_id
                ),

    CONSTRAINT chk_merchant_mapping_correction_count
        CHECK (
            correction_count >= 0
            )
);

USE kbproject;

START TRANSACTION;

-- ---------------------------------------------------------------------
-- 1. tbl_member (3건)
-- ---------------------------------------------------------------------
INSERT INTO tbl_member (username,
                        password,
                        email,
                        reg_date,
                        update_date)
VALUES ('test1', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'test1@kbproject.local',
        '2026-07-01 09:00:00', '2026-07-01 09:00:00'),
       ('test2', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'test2@kbproject.local',
        '2026-07-02 09:00:00', '2026-07-02 09:00:00'),
       ('test3', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 'test3@kbproject.local',
        '2026-07-03 09:00:00', '2026-07-03 09:00:00');

-- ---------------------------------------------------------------------
-- 2. tbl_member_auth (6건)
-- ---------------------------------------------------------------------
INSERT INTO tbl_member_auth (username,
                             auth)
VALUES ('test1', 'ROLE_USER'),
       ('test1', 'ROLE_ADMIN'),
       ('test2', 'ROLE_USER'),
       ('test2', 'ROLE_MANAGER'),
       ('test3', 'ROLE_USER'),
       ('test3', 'ROLE_TESTER');

-- ---------------------------------------------------------------------
-- 3. user_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO user_tbl (user_id,
                      user_name,
                      birth_date,
                      phone_number,
                      pin_password,
                      user_status,
                      created_at,
                      updated_at,
                      withdrawn_at,
                      last_login_at)
VALUES (1, '테스트회원1', '20000115', '01011112222', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        'ACTIVE', '2026-07-01 09:00:00', '2026-07-24 08:30:00', NULL, '2026-07-24 08:30:00'),
       (2, '테스트회원2', '19990321', '01022223333', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        'ACTIVE', '2026-07-02 10:00:00', '2026-07-23 19:10:00', NULL, '2026-07-23 19:10:00'),
       (3, '테스트회원3', '20010509', '01033334444', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        'ACTIVE', '2026-07-03 11:00:00', '2026-07-22 14:20:00', NULL, '2026-07-22 14:20:00');

-- ---------------------------------------------------------------------
-- 4. bank_tbl (10건)
-- ---------------------------------------------------------------------
INSERT INTO bank_tbl (bank_code,
                      bank_name,
                      bank_logo_name,
                      use_yn)
VALUES ('004', 'KB국민은행', 'kb.png', 'Y'),
       ('088', '신한은행', 'shinhan.png', 'Y'),
       ('081', '하나은행', 'hana.png', 'Y'),
       ('020', '우리은행', 'woori.png', 'Y'),
       ('011', 'NH농협은행', 'nh.png', 'Y'),
       ('003', 'IBK기업은행', 'ibk.png', 'Y'),
       ('090', '카카오뱅크', 'kakaobank.png', 'Y'),
       ('092', '토스뱅크', 'tossbank.png', 'Y'),
       ('089', '케이뱅크', 'kbank.png', 'Y'),
       ('023', 'SC제일은행', 'sc.png', 'Y');

-- ---------------------------------------------------------------------
-- 5. linked_account_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO linked_account_tbl (linked_account_id,
                                user_id,
                                bank_code,
                                account_number,
                                account_holder,
                                primary_yn,
                                connection_status)
VALUES (1, 1, '004', '110-111-111111', '테스트회원1', 'Y', 'CONNECTED'),
       (2, 1, '088', '110-111-222222', '테스트회원1', 'N', 'CONNECTED'),
       (3, 2, '088', '110-222-111111', '테스트회원2', 'Y', 'CONNECTED'),
       (4, 2, '081', '110-222-222222', '테스트회원2', 'N', 'CONNECTED'),
       (5, 3, '081', '110-333-111111', '테스트회원3', 'Y', 'CONNECTED'),
       (6, 3, '004', '110-333-222222', '테스트회원3', 'N', 'CONNECTED');

-- ---------------------------------------------------------------------
-- 6. agreement_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO agreement_tbl (agreement_id,
                           agreement_type,
                           agreement_name,
                           agreement_content,
                           required_yn,
                           use_yn)
VALUES (1, 'SERVICE', '서비스 이용약관', '본 약관은 서비스 이용에 관한 기본적인 사항을 규정합니다. 회원은 서비스를 이용함으로써 본 약관에 동의한 것으로 간주됩니다.', 'Y', 'Y'),
       (2, 'PRIVACY', '개인정보 처리방침', '회사는 관련 법령에 따라 회원의 개인정보를 안전하게 보호하며, 수집·이용 목적 범위 내에서만 개인정보를 처리합니다.', 'Y', 'Y'),
       (3, 'MARKETING', '마케팅 정보 수신 동의', '이벤트와 혜택 정보 수신을 위한 선택 약관입니다.', 'N', 'Y');

-- ---------------------------------------------------------------------
-- 7. user_agreement_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO user_agreement_tbl (user_agreement_id,
                                user_id,
                                agreement_id,
                                agreed_yn,
                                agreed_at)
VALUES (1, 1, 1, 'Y', '2026-07-01 09:05:00'),
       (2, 1, 2, 'Y', '2026-07-01 09:05:10'),
       (3, 2, 1, 'Y', '2026-07-02 10:05:00'),
       (4, 2, 2, 'Y', '2026-07-02 10:05:10'),
       (5, 3, 1, 'Y', '2026-07-03 11:05:00'),
       (6, 3, 3, 'N', '2026-07-03 11:05:10');

-- ---------------------------------------------------------------------
-- 8. wallet_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO wallet_tbl (wallet_id,
                        user_id,
                        balance,
                        wallet_status)
VALUES (1, 1, 107000, 'ACTIVE'),
       (2, 2, 185000, 'ACTIVE'),
       (3, 3, 145000, 'ACTIVE');

-- ---------------------------------------------------------------------
-- 9. verification_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO verification_tbl (verification_id,
                              user_id,
                              user_name,
                              birth_date,
                              carrier_code,
                              phone_number,
                              verification_code,
                              verification_purpose,
                              requested_at,
                              verified_yn,
                              fail_count)
VALUES (1, 1, '테스트회원1', '20000115', 'SKT', '01011112222', 'ENC-111111', 'SIGN_UP', '2026-07-01 08:55:00', 'Y', 0),
       (2, 2, '테스트회원2', '19990321', 'KT', '01022223333', 'ENC-222222', 'SIGN_UP', '2026-07-02 09:55:00', 'Y', 0),
       (3, 3, '테스트회원3', '20010509', 'LGU', '01033334444', 'ENC-333333', 'SIGN_UP', '2026-07-03 10:55:00', 'Y', 0),
       (4, 1, '테스트회원1', '20000115', 'SKT', '01011112222', 'ENC-444444', 'PIN_RESET', '2026-07-20 10:00:00', 'Y', 1),
       (5, 2, '테스트회원2', '19990321', 'KT_MVNO', '01022223333', 'ENC-555555', 'PIN_RESET', '2026-07-21 11:00:00', 'N', 2),
       (6, NULL, '가입대기회원', '20021212', 'LGU_MVNO', '01099998888', 'ENC-666666', 'SIGN_UP', '2026-07-24 09:00:00', 'N',
        0);

-- ---------------------------------------------------------------------
-- 10. profile_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO profile_tbl (profile_id,
                         user_id,
                         nickname,
                         introduction,
                         original_name,
                         stored_name,
                         created_at,
                         updated_at)
VALUES (1, 1, '노랑지갑', '포인트를 모으는 중입니다.', 'profile1.png', 'profile_1.png', '2026-07-01 09:10:00', '2026-07-20 12:00:00'),
       (2, 2, '절약왕', '합리적인 소비를 좋아합니다.', 'profile2.png', 'profile_2.png', '2026-07-02 10:10:00', '2026-07-21 12:00:00'),
       (3, 3, '여행저축러', '여행 자금을 모으는 중입니다.', 'profile3.png', 'profile_3.png', '2026-07-03 11:10:00',
        '2026-07-22 12:00:00');

-- ---------------------------------------------------------------------
-- 11. notification_setting_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO notification_setting_tbl (notification_setting_id,
                                      user_id,
                                      finance_notification_yn,
                                      reward_notification_yn,
                                      event_benefit_notification_yn,
                                      updated_at)
VALUES (1, 1, 'Y', 'Y', 'Y', '2026-07-20 12:00:00'),
       (2, 2, 'Y', 'N', 'N', '2026-07-21 12:00:00'),
       (3, 3, 'N', 'Y', 'Y', '2026-07-22 12:00:00');

-- ---------------------------------------------------------------------
-- 12. refresh_token_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO refresh_token_tbl (refresh_token_id,
                               user_id,
                               refresh_token,
                               issued_at,
                               expires_at)
VALUES (1, 1, 'refresh-token-user1-a', '2026-07-23 08:00:00', '2026-08-23 08:00:00'),
       (2, 1, 'refresh-token-user1-b', '2026-07-24 08:00:00', '2026-08-24 08:00:00'),
       (3, 2, 'refresh-token-user2-a', '2026-07-23 09:00:00', '2026-08-23 09:00:00'),
       (4, 2, 'refresh-token-user2-b', '2026-07-24 09:00:00', '2026-08-24 09:00:00'),
       (5, 3, 'refresh-token-user3-a', '2026-07-23 10:00:00', '2026-08-23 10:00:00'),
       (6, 3, 'refresh-token-user3-b', '2026-07-24 10:00:00', '2026-08-24 10:00:00');

-- ---------------------------------------------------------------------
-- 13. spending_category_tbl (20건)
-- ---------------------------------------------------------------------
INSERT INTO spending_category_tbl (spending_category_id,
                                   category_name,
                                   parent_category_id)
VALUES (1, '식비', NULL),
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
       (14, '산부인과', 13),
       (15, '안과', 13),
       (16, '내과', 13),
       (17, '정형외과', 13),
       (18, '한의원', 13),
       (19, '치과', 13),
       (20, '소아과', 13);

-- ---------------------------------------------------------------------
-- 14. point_wallet_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO point_wallet_tbl (point_wallet_id,
                              user_id,
                              point_balance,
                              updated_at)
VALUES (1, 1, 500, '2026-07-24 09:10:00'),
       (2, 2, 5000, '2026-07-24 09:20:00'),
       (3, 3, 7500, '2026-07-24 09:30:00');

-- ---------------------------------------------------------------------
-- 15. point_transaction_tbl (7건)
-- ---------------------------------------------------------------------
INSERT INTO point_transaction_tbl (point_transaction_id,
                                   point_wallet_id,
                                   transaction_type,
                                   point_amount,
                                   reason_type,
                                   created_at)
VALUES (1, 1, 'EARN', 5000, 'EVENT', '2026-07-20 09:00:00'),
       (2, 1, 'USE', 1000, 'CONVERSION', '2026-07-23 09:10:00'),
       (3, 1, 'CANCEL', 3500, 'RANDOM_BOX', '2026-07-24 09:10:00'),
       (4, 2, 'EARN', 7000, 'EVENT', '2026-07-21 09:00:00'),
       (5, 2, 'USE', 2000, 'CONVERSION', '2026-07-24 09:20:00'),
       (6, 3, 'EARN', 10000, 'EVENT', '2026-07-22 09:00:00'),
       (7, 3, 'USE', 2500, 'CONVERSION', '2026-07-24 09:30:00');

-- ---------------------------------------------------------------------
-- 16. user_random_box_tbl (7건)
-- ---------------------------------------------------------------------
INSERT INTO user_random_box_tbl (user_random_box_id,
                                 user_id,
                                 issue_reason,
                                 source_id,
                                 target_account_id,
                                 box_status,
                                 reward_point,
                                 issued_at,
                                 opened_at)
VALUES (1, 1, 'ATTENDANCE', 1, NULL, 'OPENED', 500, '2026-07-18 09:00:00', '2026-07-18 09:05:00'),
       (2, 1, 'EVENT', 1, NULL, 'UNOPENED', NULL, '2026-07-24 09:00:00', NULL),
       (3, 2, 'ATTENDANCE', 3, NULL, 'OPENED', 1000, '2026-07-19 09:00:00', '2026-07-19 09:05:00'),
       (4, 2, 'EVENT', 2, NULL, 'UNOPENED', NULL, '2026-07-24 09:10:00', NULL),
       (5, 3, 'ATTENDANCE', 5, NULL, 'OPENED', 300, '2026-07-20 09:00:00', '2026-07-20 09:05:00'),
       (6, 1, 'FEED_SHARE', 1, NULL, 'UNOPENED', NULL, '2026-07-24 10:00:00', NULL),
       (7, 1, 'TRANSFER', 2, 2, 'UNOPENED', NULL, '2026-07-24 11:00:00', NULL);

-- ---------------------------------------------------------------------
-- 17. attendance_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO attendance_tbl (attendance_id,
                            user_id,
                            attendance_date)
VALUES (1, 1, '2026-07-22'),
       (2, 1, '2026-07-23'),
       (3, 2, '2026-07-22'),
       (4, 2, '2026-07-24'),
       (5, 3, '2026-07-23'),
       (6, 3, '2026-07-24');

-- ---------------------------------------------------------------------
-- 18. point_conversion_history_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO point_conversion_history_tbl (point_conversion_id,
                                          user_id,
                                          point_wallet_id,
                                          wallet_id,
                                          converted_point,
                                          converted_at)
VALUES (1, 1, 1, 1, 500, '2026-07-18 12:00:00'),
       (2, 1, 1, 1, 1000, '2026-07-19 12:00:00'),
       (3, 2, 2, 2, 500, '2026-07-20 12:00:00'),
       (4, 2, 2, 2, 1000, '2026-07-21 12:00:00'),
       (5, 3, 3, 3, 500, '2026-07-22 12:00:00'),
       (6, 3, 3, 3, 1000, '2026-07-23 12:00:00');

-- ---------------------------------------------------------------------
-- 19. spending_analysis_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO spending_analysis_tbl (spending_analysis_id,
                                   user_id,
                                   analysis_period,
                                   representative_category_id,
                                   ai_title,
                                   ai_analysis_summary,
                                   created_at)
VALUES (1, 1, 1, 1, '한 달 외식 탐험가', '최근 한 달 동안 식비 지출 비중이 가장 높습니다.', '2026-07-01 00:00:00'),
       (2, 1, 3, 2, '카페 단골 손님', '최근 세 달 동안 카페 이용이 꾸준히 증가했습니다.', '2026-07-02 00:00:00'),
       (3, 2, 1, 6, '대중교통 마스터', '교통비 비중이 높고 이동이 잦은 소비 패턴입니다.', '2026-07-03 00:00:00'),
       (4, 2, 12, 1, '알뜰 식비 관리자', '연간 식비가 안정적으로 관리되고 있습니다.', '2026-07-04 00:00:00'),
       (5, 3, 3, 2, '커피와 함께하는 사람', '카페와 간식 관련 결제가 많은 편입니다.', '2026-07-05 00:00:00'),
       (6, 3, 12, 6, '움직이는 저축러', '교통 지출과 저축이 균형을 이루고 있습니다.', '2026-07-06 00:00:00');

-- ---------------------------------------------------------------------
-- 20. spending_analysis_category_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO spending_analysis_category_tbl (analysis_category_id,
                                            spending_analysis_id,
                                            spending_category_id,
                                            spending_amount,
                                            spending_ratio,
                                            transaction_count,
                                            created_at)
VALUES (1, 1, 1, 180000, 60.00, 12, '2026-07-01 00:05:00'),
       (2, 1, 2, 120000, 40.00, 8, '2026-07-01 00:05:00'),
       (3, 2, 2, 260000, 65.00, 20, '2026-07-02 00:05:00'),
       (4, 2, 6, 140000, 35.00, 14, '2026-07-02 00:05:00'),
       (5, 3, 6, 90000, 75.00, 30, '2026-07-03 00:05:00'),
       (6, 3, 1, 30000, 25.00, 5, '2026-07-03 00:05:00');

-- ---------------------------------------------------------------------
-- 21. kb_card_product_tbl (4건)
-- ---------------------------------------------------------------------
INSERT INTO kb_card_product_tbl (card_product_id,
                                 card_name,
                                 card_type,
                                 card_description,
                                 card_image,
                                 application,
                                 annual_fee,
                                 created_at)
VALUES (1, 'KB국민 My WE:SH 카드', 'CREDIT', '나만을 위한 맞춤형 혜택을 제공하는 신용카드입니다. 음식, 배달, 커피 등 일상생활 영역에서 할인 혜택을 받을 수 있습니다.',
        'my_wesh_card.png', 'my_wesh_apply.html', 15000, '2026-07-01 09:00:00'),
       (2, 'KB국민 톡톡O 카드', 'CREDIT', '온라인 쇼핑과 디지털 생활에 특화된 카드로 쇼핑몰, OTT 등 다양한 영역에서 할인 혜택을 제공합니다.', 'toktok_o_card.png',
        'toktok_o_apply.html', 12000, '2026-07-01 09:10:00'),
       (3, 'KB국민 노리2 체크카드', 'CHECK', '대중교통, 카페, 편의점 등 생활 밀착 영역에서 할인 혜택을 제공하는 체크카드입니다.', 'nori2_card.png',
        'nori2_apply.html', 0, '2026-07-01 09:20:00'),
       (4, 'KB국민 직장인보너스체크카드', 'CHECK', '직장인을 위한 생활 할인형 체크카드로 교통, 외식, 쇼핑 영역에서 혜택을 제공합니다.', 'bonus_check_card.png',
        'bonus_check_apply.html', 0, '2026-07-01 09:30:00');

-- ---------------------------------------------------------------------
-- 22. card_benefit_tbl (7건)
-- ---------------------------------------------------------------------
INSERT INTO card_benefit_tbl (card_benefit_id,
                              card_product_id,
                              spending_category_id,
                              benefit_name,
                              benefit_amount,
                              benefit_rate,
                              monthly_limit,
                              minimum_spending_amount,
                              benefit_description,
                              created_at)
VALUES (1, 1, 2, '카페 이용 할인', NULL, 10.00, 10000, 300000, '스타벅스 등 카페 이용 시 10% 할인 혜택을 제공합니다.', '2026-07-01 10:00:00'),
       (2, 1, 1, '음식점 할인', 1000, NULL, 15000, 300000, '음식점 결제 건당 1,000원 할인 혜택을 제공합니다.', '2026-07-01 10:10:00'),
       (3, 2, 4, '온라인 쇼핑 할인', NULL, 10.00, 20000, 400000, '온라인 쇼핑몰 이용 시 10% 할인 혜택을 제공합니다.', '2026-07-01 10:20:00'),
       (4, 2, 13, '병원 이용 할인', NULL, 5.00, 10000, 400000, '병원 및 의료 관련 업종 이용 시 5% 할인 혜택을 제공합니다.', '2026-07-01 10:30:00'),
       (5, 3, 6, '대중교통 할인', NULL, 10.00, 5000, 200000, '버스와 지하철 이용 금액의 10% 할인 혜택을 제공합니다.', '2026-07-01 10:40:00'),
       (6, 3, 2, '카페 할인', 500, NULL, 5000, 200000, '카페 이용 시 건당 500원 할인 혜택을 제공합니다.', '2026-07-01 10:50:00'),
       (7, 4, 7, '자동차 관련 할인', NULL, 5.00, 10000, 300000, '주유 및 자동차 관련 가맹점 이용 시 5% 할인 혜택을 제공합니다.',
        '2026-07-01 11:00:00');

-- ---------------------------------------------------------------------
-- 23. card_recommendation_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO card_recommendation_tbl (card_recommendation_id,
                                     spending_analysis_id,
                                     card_product_id,
                                     recommendation_rank,
                                     expected_benefit_amount,
                                     created_at)
VALUES (1, 1, 1, 1, 12000, '2026-07-01 01:00:00'),
       (2, 1, 2, 2, 8000, '2026-07-01 01:00:00'),
       (3, 2, 1, 1, 15000, '2026-07-02 01:00:00'),
       (4, 3, 3, 1, 7000, '2026-07-03 01:00:00'),
       (5, 4, 2, 1, 9000, '2026-07-04 01:00:00'),
       (6, 5, 3, 1, 6000, '2026-07-05 01:00:00');

-- ---------------------------------------------------------------------
-- 24. kb_insurance_product_tbl (5건)
-- ---------------------------------------------------------------------
INSERT INTO kb_insurance_product_tbl (insurance_product_id,
                                      insurance_name,
                                      insurance_category,
                                      insurance_description,
                                      monthly_premium,
                                      insurance_image,
                                      application_url,
                                      created_at)
VALUES (1, 'KB손해보험 다이렉트 자동차보험', '자동차', '자동차 사고 발생 시 대인, 대물, 자기신체손해 등을 보장하는 온라인 전용 자동차보험입니다.', 50000,
        'kb_auto_insurance.png', 'kb_auto_apply.html', '2026-07-01 09:00:00'),
       (2, 'KB손해보험 건강보험', '건강', '질병 및 상해로 인한 의료비 부담을 대비할 수 있는 종합 건강보험 상품입니다.', 30000, 'kb_health_insurance.png',
        'kb_health_apply.html', '2026-07-01 09:10:00'),
       (3, 'KB손해보험 실손의료비보험', '실손', '병원 진료 및 치료 과정에서 발생하는 의료비를 보장하는 실손형 보험 상품입니다.', 15000, 'kb_silson_insurance.png',
        'kb_silson_apply.html', '2026-07-01 09:20:00'),
       (4, 'KB손해보험 운전자보험', '운전자', '자동차 사고 발생 시 운전자에게 필요한 법률 비용 및 사고 관련 위험을 보장합니다.', 12000, 'kb_driver_insurance.png',
        'kb_driver_apply.html', '2026-07-01 09:30:00'),
       (5, 'KB손해보험 여행자보험', '여행', '국내외 여행 중 발생할 수 있는 사고, 질병, 휴대품 손해 등을 보장합니다.', 8000, 'kb_travel_insurance.png',
        'kb_travel_apply.html', '2026-07-01 09:40:00');

-- ---------------------------------------------------------------------
-- 25. kb_insurance_coverage_tbl (12건)
-- ---------------------------------------------------------------------
INSERT INTO kb_insurance_coverage_tbl (insurance_coverage_id,
                                       insurance_product_id,
                                       coverage_name,
                                       coverage_amount,
                                       coverage_description,
                                       coverage_limit,
                                       created_at)
VALUES (1, 1, '대인배상', 100000000, '자동차 사고로 타인의 신체 피해 발생 시 손해를 보장합니다.', '사고당', '2026-07-01 10:00:00'),
       (2, 1, '대물배상', 200000000, '자동차 사고로 타인의 차량 및 재산 피해 발생 시 보장합니다.', '사고당', '2026-07-01 10:10:00'),
       (3, 1, '자기신체사고', 50000000, '자동차 사고로 본인에게 발생한 상해를 보장합니다.', '사고당', '2026-07-01 10:20:00'),
       (4, 2, '암 진단비', 30000000, '암 진단 확정 시 진단비를 지급합니다.', '1회', '2026-07-01 10:30:00'),
       (5, 2, '질병 입원비', 50000, '질병으로 입원 치료 시 입원 일당을 지급합니다.', '입원일 기준', '2026-07-01 10:40:00'),
       (6, 2, '수술비', 1000000, '질병 및 상해 수술 발생 시 수술비를 지급합니다.', '수술별', '2026-07-01 10:50:00'),
       (7, 3, '입원 의료비', 50000000, '입원 치료 과정에서 발생한 의료비를 보장합니다.', '연간 한도', '2026-07-01 11:00:00'),
       (8, 3, '통원 의료비', 30000000, '외래 진료 및 통원 치료 비용을 보장합니다.', '연간 한도', '2026-07-01 11:10:00'),
       (9, 4, '교통사고 처리 지원금', 50000000, '교통사고 발생 시 형사 합의 관련 비용을 지원합니다.', '사고당', '2026-07-01 11:20:00'),
       (10, 4, '벌금 보장', 20000000, '자동차 사고 관련 벌금 발생 시 보장합니다.', '사고당', '2026-07-01 11:30:00'),
       (11, 5, '여행 중 상해 의료비', 10000000, '여행 중 발생한 상해 치료 비용을 보장합니다.', '사고당', '2026-07-01 11:40:00'),
       (12, 5, '휴대품 손해', 1000000, '여행 중 휴대품 분실 및 파손 발생 시 보장합니다.', '사고당', '2026-07-01 11:50:00');

-- ---------------------------------------------------------------------
-- 26. kb_insurance_recommendation_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO kb_insurance_recommendation_tbl (insurance_recommendation_id,
                                             spending_analysis_id,
                                             insurance_product_id,
                                             recommendation_reason,
                                             created_at)
VALUES (1, 1, 2, '의료와 생활 소비를 함께 고려해 건강보험을 추천합니다.', '2026-07-01 02:00:00'),
       (2, 1, 3, '병원비 부담을 줄이기 위해 실손보험을 추천합니다.', '2026-07-01 02:10:00'),
       (3, 2, 3, '정기적인 의료비 지출 가능성에 대비할 수 있습니다.', '2026-07-02 02:00:00'),
       (4, 3, 5, '이동이 잦은 소비 패턴을 고려해 여행자보험을 추천합니다.', '2026-07-03 02:00:00'),
       (5, 4, 2, '장기적인 건강 관리에 적합한 상품입니다.', '2026-07-04 02:00:00'),
       (6, 5, 5, '여가와 외출 소비가 많아 여행 보장이 유용합니다.', '2026-07-05 02:00:00');

-- ---------------------------------------------------------------------
-- 27. friend_request_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO friend_request_tbl (request_id,
                                requester_id,
                                receiver_id,
                                status,
                                created_at,
                                updated_at)
VALUES (1, 1, 2, 'ACCEPT', '2026-07-10 10:00:00', '2026-07-10 10:05:00'),
       (2, 1, 3, 'REQUEST', '2026-07-11 10:00:00', '2026-07-11 10:00:00'),
       (3, 2, 1, 'ACCEPT', '2026-07-12 10:00:00', '2026-07-12 10:05:00'),
       (4, 2, 3, 'REJECT', '2026-07-13 10:00:00', '2026-07-13 10:05:00'),
       (5, 3, 1, 'CANCEL', '2026-07-14 10:00:00', '2026-07-14 10:05:00'),
       (6, 3, 2, 'REQUEST', '2026-07-15 10:00:00', '2026-07-15 10:00:00');

-- ---------------------------------------------------------------------
-- 28. friend_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO friend_tbl (friend_id,
                        user_id,
                        friend_user_id,
                        created_at)
VALUES (1, 1, 2, '2026-07-10 10:05:00'),
       (2, 2, 1, '2026-07-10 10:05:00'),
       (3, 1, 3, '2026-07-16 10:05:00'),
       (4, 3, 1, '2026-07-16 10:05:00'),
       (5, 2, 3, '2026-07-17 10:05:00'),
       (6, 3, 2, '2026-07-17 10:05:00');

-- ---------------------------------------------------------------------
-- 29. settlement_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO settlement_tbl (settlement_id,
                            requester_id,
                            title,
                            total_amount,
                            status,
                            created_at,
                            settlement_type,
                            spending_category_id,
                            completed_at)
VALUES (1, 1, '저녁 식사 정산', 30000, 'REQUEST', '2026-07-20 19:00:00', 'EQUAL', 1, NULL),
       (2, 2, '카페 모임 정산', 24000, 'COMPLETE', '2026-07-21 15:00:00', 'EQUAL', 2, '2026-07-21 18:00:00'),
       (3, 3, '택시비 정산', 18000, 'CANCEL', '2026-07-22 23:00:00', 'UNEQUAL', 6, NULL);

-- ---------------------------------------------------------------------
-- 30. settlement_member_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO settlement_member_tbl (settlement_member_id,
                                   settlement_id,
                                   user_id,
                                   amount,
                                   status,
                                   created_at,
                                   last_reminder_date,
                                   completed_at)
VALUES (1, 1, 2, 15000, 'REQUEST', '2026-07-20 19:01:00', '2026-07-21 09:00:00', NULL),
       (2, 1, 3, 15000, 'REQUEST', '2026-07-20 19:01:00', '2026-07-21 09:00:00', NULL),
       (3, 2, 1, 12000, 'COMPLETE', '2026-07-21 15:01:00', '2026-07-21 16:00:00', '2026-07-21 17:30:00'),
       (4, 2, 3, 12000, 'COMPLETE', '2026-07-21 15:01:00', '2026-07-21 16:00:00', '2026-07-21 18:00:00'),
       (5, 3, 1, 8000, 'CANCEL', '2026-07-22 23:01:00', '2026-07-23 09:00:00', NULL),
       (6, 3, 2, 10000, 'CANCEL', '2026-07-22 23:01:00', '2026-07-23 09:00:00', NULL);

-- ---------------------------------------------------------------------
-- 31. notification_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO notification_tbl (notification_id,
                              receiver_id,
                              sender_id,
                              notification_type,
                              target_id,
                              is_read,
                              created_at)
VALUES (1, 2, 1, 'FRIEND_REQUEST', 1, 'Y', '2026-07-10 10:00:00'),
       (2, 3, 1, 'FRIEND_REQUEST', 2, 'N', '2026-07-11 10:00:00'),
       (3, 2, 1, 'SETTLEMENT', 1, 'N', '2026-07-20 19:01:00'),
       (4, 3, 2, 'COMMENT', 1, 'Y', '2026-07-21 20:00:00'),
       (5, 1, 3, 'LIKE', 2, 'N', '2026-07-22 20:00:00'),
       (6, 1, 2, 'SETTLEMENT', 2, 'Y', '2026-07-23 20:00:00');

-- ---------------------------------------------------------------------
-- 32. financial_transaction_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO financial_transaction_tbl (transaction_id,
                                       parent_transaction_id,
                                       user_id,
                                       receive_id,
                                       transaction_type,
                                       source_type,
                                       target_type,
                                       transaction_status,
                                       amount,
                                       merchant_name,
                                       spending_category_id,
                                       created_at)
VALUES (1, NULL, 1, NULL, 'CHARGE', 'ACCOUNT', 'WALLET', 'SUCCESS', 10000, NULL, NULL, '2026-07-20 09:00:00'),
       (2, NULL, 1, 2, 'TRANSFER', 'WALLET', 'ACCOUNT', 'SUCCESS', 8000, NULL, NULL, '2026-07-20 10:00:00'),
       (3, NULL, 2, NULL, 'CHARGE', 'ACCOUNT', 'WALLET', 'SUCCESS', 12000, NULL, NULL, '2026-07-21 09:00:00'),
       (4, NULL, 2, 1, 'SETTLEMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 15000, NULL, 1, '2026-07-21 18:00:00'),
       (5, NULL, 3, 1, 'TRANSFER', 'ACCOUNT', 'WALLET', 'SUCCESS', 20000, NULL, NULL, '2026-07-22 11:00:00'),
       (6, NULL, 3, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 5000, '스타벅스 동성로점', 6, '2026-07-23 08:00:00'),
       (7, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 18500, '배달의민족', 1, '2026-07-24 12:30:00'),
       (8, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 24000, '동성로 한식당', 1, '2026-07-25 18:20:00'),
       (9, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 6200, '스타벅스 대구점', 2, '2026-07-26 09:10:00'),
       (10, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 5500, '투썸플레이스', 2, '2026-07-27 14:40:00'),
       (11, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 9800, 'CU 계명대점', 3, '2026-07-28 16:15:00'),
       (12, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 42900, '쿠팡', 4, '2026-07-29 20:30:00'),
       (13, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 14500, '카카오T', 6, '2026-07-30 22:10:00'),
       (14, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 65000, '스마일치과', 19, '2026-07-31 11:40:00'),
        -- 미분류 거래 4건
       (15, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        4900, '메가MGC커피', NULL, '2026-08-01 09:20:00'),

       (16, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        12500, '한솥도시락', NULL, '2026-08-01 12:30:00'),

       (17, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        27600, '오늘의집', NULL, '2026-08-01 18:40:00'),

       (18, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        18000, '교보문고', NULL, '2026-08-02 13:10:00');

-- ---------------------------------------------------------------------
-- 33. account_dummy_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO account_dummy_tbl (account_id,
                               user_id,
                               bank_code,
                               account_number,
                               owner_name,
                               balance,
                               account_password,
                               created_at)
VALUES (1, 1, '004', '111-001-000001', '테스트회원1', 510000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        '2026-07-01 09:20:00'),
       (2, 1, '088', '111-001-000002', '테스트회원1', 250000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        '2026-07-01 09:30:00'),
       (3, 2, '088', '222-002-000001', '테스트회원2', 305000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        '2026-07-02 10:20:00'),
       (4, 2, '081', '222-002-000002', '테스트회원2', 180000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        '2026-07-02 10:30:00'),
       (5, 3, '081', '333-003-000001', '테스트회원3', 680000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        '2026-07-03 11:20:00'),
       (6, 3, '004', '333-003-000002', '테스트회원3', 205000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        '2026-07-03 11:30:00');

-- ---------------------------------------------------------------------
-- 34. account_transaction_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO account_transaction_tbl (account_transaction_id,
                                     transaction_id,
                                     user_id,
                                     direction,
                                     account_id,
                                     balance_before,
                                     balance_after)
VALUES (1, 1, 1, 'DEBIT', 1, 500000, 490000),
       (2, 2, 2, 'CREDIT', 3, 297000, 305000),
       (3, 3, 2, 'DEBIT', 3, 309000, 297000),
       (4, 4, 1, 'CREDIT', 1, 495000, 510000),
       (5, 5, 3, 'DEBIT', 5, 700000, 680000),
       (6, 6, 3, 'CREDIT', 6, 200000, 205000);

-- ---------------------------------------------------------------------
-- 35. wallet_transaction_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO wallet_transaction_tbl (wallet_transaction_id,
                                    transaction_id,
                                    user_id,
                                    direction,
                                    wallet_id,
                                    balance_before,
                                    balance_after)
VALUES (1, 1, 1, 'CREDIT', 1, 90000, 100000),
       (2, 2, 1, 'DEBIT', 1, 100000, 92000),
       (3, 3, 2, 'CREDIT', 2, 188000, 200000),
       (4, 4, 2, 'DEBIT', 2, 200000, 185000),
       (5, 5, 1, 'CREDIT', 1, 92000, 112000),
       (6, 6, 3, 'DEBIT', 3, 150000, 145000);

-- ---------------------------------------------------------------------
-- 36. card_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO card_tbl (card_code,
                      account_id,
                      card_img_file_name,
                      card_num,
                      expiry_date,
                      cvv)
VALUES ('KB-CARD-001', 1, 'card_001.png', 'ENC-CARD-1111', '12/30', 'ENC-111'),
       ('KB-CARD-002', 3, 'card_002.png', 'ENC-CARD-2222', '11/30', 'ENC-222'),
       ('KB-CARD-003', 5, 'card_003.png', 'ENC-CARD-3333', '10/30', 'ENC-333');

-- ---------------------------------------------------------------------
-- 37. registered_card_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO registered_card_tbl (card_id,
                                 account_id,
                                 user_id,
                                 card_num,
                                 expiry_date,
                                 cvv,
                                 card_password,
                                 represent_yn,
                                 created_at,
                                 delete_yn)
VALUES (1, 1, 1, 'ENC-REG-1111', '12/30', 'ENC-111', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        'Y', '2026-07-01 09:40:00', 'N'),
       (2, 2, 1, 'ENC-REG-1112', '09/30', 'ENC-112', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        'N', '2026-07-01 09:50:00', 'N'),
       (3, 3, 2, 'ENC-REG-2221', '11/30', 'ENC-221', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        'Y', '2026-07-02 10:40:00', 'N'),
       (4, 4, 2, 'ENC-REG-2222', '08/30', 'ENC-222', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        'N', '2026-07-02 10:50:00', 'N'),
       (5, 5, 3, 'ENC-REG-3331', '10/30', 'ENC-331', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        'Y', '2026-07-03 11:40:00', 'N'),
       (6, 6, 3, 'ENC-REG-3332', '07/30', 'ENC-332', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
        'N', '2026-07-03 11:50:00', 'N');

-- ---------------------------------------------------------------------
-- 38. payment_token_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO payment_token_tbl (token_value,
                               user_id,
                               card_id,
                               expired_at,
                               used_yn)
VALUES ('pay-token-001', 1, 1, '2026-07-24 10:10:00', 'Y'),
       ('pay-token-002', 1, 2, '2026-07-24 11:10:00', 'N'),
       ('pay-token-003', 2, 3, '2026-07-24 12:10:00', 'Y'),
       ('pay-token-004', 2, 4, '2026-07-24 13:10:00', 'N'),
       ('pay-token-005', 3, 5, '2026-07-24 14:10:00', 'Y'),
       ('pay-token-006', 3, 6, '2026-07-24 15:10:00', 'N');

-- ---------------------------------------------------------------------
-- 39. receipt_memo_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO receipt_memo_tbl (memo_id,
                              transaction_id,
                              memo_content,
                              created_at,
                              updated_at)
VALUES (1, 1, '포인트 지갑 충전', '2026-07-20 09:01:00', '2026-07-20 09:01:00'),
       (2, 2, '친구에게 송금', '2026-07-20 10:01:00', '2026-07-20 10:01:00'),
       (3, 3, '월급 계좌 충전', '2026-07-21 09:01:00', '2026-07-21 09:01:00'),
       (4, 4, '저녁 식사 정산', '2026-07-21 18:01:00', '2026-07-21 18:01:00'),
       (5, 5, '여행 경비 송금', '2026-07-22 11:01:00', '2026-07-22 11:01:00'),
       (6, 6, '교통비 결제', '2026-07-23 08:01:00', '2026-07-23 08:01:00');

-- ---------------------------------------------------------------------
-- 40. feed_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO feed_tbl (feed_id,
                      user_id,
                      transaction_id,
                      feed_type,
                      content,
                      visibility,
                      created_at,
                      updated_at)
VALUES (1, 1, 1, 'SHARE', '지갑 충전 완료', 'PUBLIC', '2026-07-20 09:05:00', '2026-07-20 09:05:00'),
       (2, 1, 2, 'TRANSFER', '친구에게 송금', 'FRIEND', '2026-07-20 10:05:00', '2026-07-20 10:05:00'),
       (3, 2, 3, 'SHARE', '지갑 충전 완료', 'PRIVATE', '2026-07-21 09:05:00', '2026-07-21 09:05:00'),
       (4, 2, 4, 'SETTLEMENT', '정산 완료', 'FRIEND', '2026-07-21 18:05:00', '2026-07-21 18:05:00'),
       (5, 3, 5, 'TRANSFER', '여행비 송금', 'PUBLIC', '2026-07-22 11:05:00', '2026-07-22 11:05:00'),
       (6, 3, 6, 'PAYMENT', '교통비 결제', 'PRIVATE', '2026-07-23 08:05:00', '2026-07-23 08:05:00');

-- ---------------------------------------------------------------------
-- 41. feed_image_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO feed_image_tbl (image_id,
                            feed_id,
                            image_name)
VALUES (1, 1, 'feed_1_1.png'),
       (2, 2, 'feed_2_1.png'),
       (3, 3, 'feed_3_1.png'),
       (4, 4, 'feed_4_1.png'),
       (5, 5, 'feed_5_1.png'),
       (6, 6, 'feed_6_1.png');

-- ---------------------------------------------------------------------
-- 42. feed_like_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO feed_like_tbl (like_id,
                           feed_id,
                           user_id,
                           created_at)
VALUES (1, 1, 2, '2026-07-20 09:10:00'),
       (2, 1, 3, '2026-07-20 09:11:00'),
       (3, 2, 2, '2026-07-20 10:10:00'),
       (4, 4, 1, '2026-07-21 18:10:00'),
       (5, 5, 1, '2026-07-22 11:10:00'),
       (6, 5, 2, '2026-07-22 11:11:00');

-- ---------------------------------------------------------------------
-- 43. feed_comment_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO feed_comment_tbl (comment_id,
                              feed_id,
                              user_id,
                              content,
                              created_at,
                              updated_at)
VALUES (1, 1, 2, '충전 축하해요', '2026-07-20 09:15:00', '2026-07-20 09:15:00'),
       (2, 2, 2, '송금 확인했어요', '2026-07-20 10:15:00', '2026-07-20 10:15:00'),
       (3, 3, 1, '좋은 습관이에요', '2026-07-21 09:15:00', '2026-07-21 09:15:00'),
       (4, 4, 3, '정산 고마워요', '2026-07-21 18:15:00', '2026-07-21 18:15:00'),
       (5, 5, 1, '즐거운 여행!', '2026-07-22 11:15:00', '2026-07-22 11:15:00'),
       (6, 6, 2, '교통비 절약!', '2026-07-23 08:15:00', '2026-07-23 08:15:00');

-- ---------------------------------------------------------------------
-- 44. card_asset_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO card_asset_tbl (asset_id,
                            asset_type,
                            asset_name,
                            src_url,
                            background_color,
                            font_type,
                            use_yn)
VALUES (1, 'BACKGROUND_SOLID', 'KB 옐로우', '/assets/yellow.png', '#FFCC00', 'PRETENDARD', 'Y'),
       (2, 'BACKGROUND_GRADIENT', '선셋 그라데이션', '/assets/sunset.png', '#FF9966', 'PRETENDARD', 'Y'),
       (3, 'STICKER', '별 스티커', '/assets/star.png', NULL, NULL, 'Y');

-- ---------------------------------------------------------------------
-- 45. file_image_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO file_image_tbl (file_id,
                            user_id,
                            file_name,
                            file_size,
                            created_at)
VALUES (1, 1, 'upload_user1_1.png', 120000, '2026-07-20 14:00:00'),
       (2, 1, 'upload_user1_2.png', 130000, '2026-07-20 14:10:00'),
       (3, 2, 'upload_user2_1.png', 140000, '2026-07-21 14:00:00'),
       (4, 2, 'upload_user2_2.png', 150000, '2026-07-21 14:10:00'),
       (5, 3, 'upload_user3_1.png', 160000, '2026-07-22 14:00:00'),
       (6, 3, 'upload_user3_2.png', 170000, '2026-07-22 14:10:00');

-- ---------------------------------------------------------------------
-- 46. custom_image_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO custom_image_tbl (custom_image_id,
                              user_id,
                              asset_id,
                              file_id,
                              custom_image_path,
                              custom_image_name,
                              custom_image_size,
                              created_at)
VALUES (1, 1, 1, 1, '/custom/user1/', 'custom_1.png', 220000, '2026-07-20 15:00:00'),
       (2, 1, 2, 2, '/custom/user1/', 'custom_2.png', 230000, '2026-07-20 15:10:00'),
       (3, 2, 2, 3, '/custom/user2/', 'custom_3.png', 240000, '2026-07-21 15:00:00'),
       (4, 2, 3, 4, '/custom/user2/', 'custom_4.png', 250000, '2026-07-21 15:10:00'),
       (5, 3, 1, 5, '/custom/user3/', 'custom_5.png', 260000, '2026-07-22 15:00:00'),
       (6, 3, 3, 6, '/custom/user3/', 'custom_6.png', 270000, '2026-07-22 15:10:00');

-- ---------------------------------------------------------------------
-- 47. card_application_history_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO card_application_history_tbl (apply_id,
                                          user_id,
                                          custom_image_id,
                                          card_code,
                                          card_name,
                                          card_status,
                                          created_at,
                                          updated_at)
VALUES (1, 1, 1, 'KB-CARD-001', '나만의 옐로우카드', 'REQUEST', '2026-07-20 16:00:00', '2026-07-20 16:00:00'),
       (2, 1, 2, 'KB-CARD-001', '선셋 카드', 'ISSUED', '2026-07-20 16:10:00', '2026-07-22 10:00:00'),
       (3, 2, 3, 'KB-CARD-002', '노을 카드', 'REQUEST', '2026-07-21 16:00:00', '2026-07-21 16:00:00'),
       (4, 2, 4, 'KB-CARD-002', '별빛 카드', 'CANCELLED', '2026-07-21 16:10:00', '2026-07-22 11:00:00'),
       (5, 3, 5, 'KB-CARD-003', '노랑 여행카드', 'ISSUED', '2026-07-22 16:00:00', '2026-07-24 09:00:00'),
       (6, 3, 6, 'KB-CARD-003', '별 여행카드', 'REQUEST', '2026-07-22 16:10:00', '2026-07-22 16:10:00');

-- ---------------------------------------------------------------------
-- 48. event_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO event_tbl (event_id,
                       event_name,
                       event_desc,
                       event_type,
                       event_status,
                       event_img_name,
                       event_target,
                       event_level,
                       start_at,
                       end_at,
                       created_at)
VALUES (1, '7월 출석 이벤트', '매일 출석하고 포인트를 받으세요.', 'ATTENDANCE', 'OPEN', 'attendance_july.png', 20, 3, '2026-07-01 00:00:00',
        '2026-07-31 23:59:59', '2026-06-25 09:00:00'),
       (2, '주말 출석 이벤트', '주말 출석 시 추가 포인트를 제공합니다.', 'ATTENDANCE', 'OPEN', 'attendance_weekend.png', 8, 2,
        '2026-07-01 00:00:00', '2026-07-31 23:59:59', '2026-06-25 09:10:00'),
       (3, '6월 출석 이벤트', '지난달 출석 이벤트입니다.', 'ATTENDANCE', 'CLOSE', 'attendance_june.png', 20, 3, '2026-06-01 00:00:00',
        '2026-06-30 23:59:59', '2026-05-25 09:00:00');

-- ---------------------------------------------------------------------
-- 49. event_reward_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO event_reward_tbl (reward_id,
                              event_id,
                              event_level,
                              reward_point,
                              reward_exe,
                              req_count,
                              use_yn)
VALUES (1, 1, 1, 100, 10, 5, 'Y'),
       (2, 1, 2, 300, 30, 10, 'Y'),
       (3, 2, 1, 200, 20, 2, 'Y'),
       (4, 2, 2, 500, 50, 6, 'Y'),
       (5, 3, 1, 100, 10, 5, 'N'),
       (6, 3, 2, 300, 30, 10, 'N');

-- ---------------------------------------------------------------------
-- 50. event_participation_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO event_participation_tbl (participation_id,
                                     event_id,
                                     user_id,
                                     participated_at)
VALUES (1, 1, 1, '2026-07-01 08:00:00'),
       (2, 1, 2, '2026-07-01 08:10:00'),
       (3, 1, 3, '2026-07-01 08:20:00'),
       (4, 2, 1, '2026-07-05 08:00:00'),
       (5, 2, 2, '2026-07-05 08:10:00'),
       (6, 3, 3, '2026-06-01 08:20:00');

-- ---------------------------------------------------------------------
-- 51. event_reward_receive_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO event_reward_receive_tbl (recv_id,
                                      event_id,
                                      reward_id,
                                      user_id,
                                      received_at)
VALUES (1, 1, 1, 1, '2026-07-06 09:00:00'),
       (2, 1, 2, 2, '2026-07-11 09:00:00'),
       (3, 1, 1, 3, '2026-07-06 09:10:00'),
       (4, 2, 3, 1, '2026-07-07 09:00:00'),
       (5, 2, 4, 2, '2026-07-14 09:00:00'),
       (6, 3, 5, 3, '2026-06-06 09:00:00');

-- ---------------------------------------------------------------------
-- 52. event_challenge_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO event_challenge_tbl (challenge_id,
                                 challenge_name,
                                 reward_point,
                                 max_level,
                                 max_target,
                                 start_date,
                                 end_date,
                                 created_at)
VALUES (1, '한 달 출석 챌린지', 1000, 3, 20, '2026-07-01 00:00:00', '2026-07-31 23:59:59', '2026-06-25 10:00:00'),
       (2, '절약 소비 챌린지', 1500, 5, 10, '2026-07-01 00:00:00', '2026-08-31 23:59:59', '2026-06-25 10:10:00'),
       (3, '친구 정산 챌린지', 800, 2, 5, '2026-07-01 00:00:00', '2026-07-31 23:59:59', '2026-06-25 10:20:00');

-- ---------------------------------------------------------------------
-- 53. event_challenge_user_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO event_challenge_user_tbl (user_challenge_id,
                                      user_id,
                                      challenge_id,
                                      current_level,
                                      current_target,
                                      status,
                                      updated_at)
VALUES (1, 1, 1, 2, 12, 'PROCESS', '2026-07-24 08:00:00'),
       (2, 1, 2, 3, 6, 'PROCESS', '2026-07-24 08:10:00'),
       (3, 2, 1, 3, 20, 'COMPLETE', '2026-07-24 08:20:00'),
       (4, 2, 3, 2, 5, 'REWARDED', '2026-07-24 08:30:00'),
       (5, 3, 2, 1, 2, 'PROCESS', '2026-07-24 08:40:00'),
       (6, 3, 3, 1, 3, 'PROCESS', '2026-07-24 08:50:00');

-- ---------------------------------------------------------------------
-- 54. card_company_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO card_company_tbl (card_company_code,
                              card_company_name)
VALUES ('KB', 'KB국민카드'),
       ('SH', '신한카드'),
       ('HN', '하나카드');

-- ---------------------------------------------------------------------
-- 55. linked_card_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO linked_card_tbl (linked_card_id,
                             user_id,
                             card_id,
                             card_company_code,
                             card_name,
                             card_image_name,
                             represent_yn)
VALUES (1, 1, 1, 'KB', 'KB 대표카드', 'linked_kb_1.png', 'Y'),
       (2, 1, 2, 'SH', '신한 생활카드', 'linked_sh_1.png', 'N'),
       (3, 2, 3, 'SH', '신한 대표카드', 'linked_sh_2.png', 'Y'),
       (4, 2, 4, 'HN', '하나 교통카드', 'linked_hn_2.png', 'N'),
       (5, 3, 5, 'HN', '하나 대표카드', 'linked_hn_3.png', 'Y'),
       (6, 3, 6, 'KB', 'KB 여행카드', 'linked_kb_3.png', 'N');

-- ---------------------------------------------------------------------
-- 56. merchant_category_mapping_tbl (21건)
-- ---------------------------------------------------------------------

INSERT INTO merchant_category_mapping_tbl (merchant_name,
                                           spending_category_id,
                                           correction_count)
VALUES
    -- 식비
    ('버거킹', 1, 0),
    ('맥도날드', 1, 0),
    ('배달의민족', 1, 0),

    -- 카페
    ('스타벅스', 2, 0),
    ('투썸플레이스', 2, 0),
    ('메가커피', 2, 0),

    -- 생활
    ('이마트', 3, 0),
    ('다이소', 3, 0),

    -- 온라인쇼핑
    ('쿠팡', 4, 0),
    ('무신사', 4, 0),

    -- 뷰티/미용
    ('올리브영', 5, 0),
    ('준오헤어', 5, 0),

    -- 교통
    ('서울교통공사', 6, 0),
    ('카카오T', 6, 0),

    -- 자동차
    ('SK에너지', 7, 0),

    -- 주거/통신
    ('SK텔레콤', 8, 0),

    -- 여행
    ('야놀자', 10, 0),

    -- 교육
    ('교보문고', 11, 0),

    -- 반려동물
    ('펫프렌즈', 12, 0),

    -- 병원 하위 카테고리
    ('서울내과', 16, 0),
    ('스마일치과', 19, 0);
COMMIT;

