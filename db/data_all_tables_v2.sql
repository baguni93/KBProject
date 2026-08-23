USE kbproject;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `card_transaction_detail_tbl`;

DROP TABLE IF EXISTS `linked_card_tbl`;

DROP TABLE IF EXISTS `card_company_tbl`;

DROP TABLE IF EXISTS `event_challenge_user_tbl`;

DROP TABLE IF EXISTS `event_challenge_level_tbl`;

DROP TABLE IF EXISTS `event_challenge_tbl`;

DROP TABLE IF EXISTS `event_reward_receive_tbl`;

DROP TABLE IF EXISTS `event_participation_tbl`;

DROP TABLE IF EXISTS `event_reward_tbl`;

DROP TABLE IF EXISTS `event_tbl`;

DROP TABLE IF EXISTS `event_user_tbl`;

DROP TABLE IF EXISTS `card_application_history_tbl`;

DROP TABLE IF EXISTS `custom_image_tbl`;

DROP TABLE IF EXISTS `file_image_tbl`;

DROP TABLE IF EXISTS `card_asset_tbl`;

DROP TABLE IF EXISTS `feed_comment_tbl`;

DROP TABLE IF EXISTS `feed_like_tbl`;

DROP TABLE IF EXISTS `feed_image_tbl`;

DROP TABLE IF EXISTS `feed_tbl`;

DROP TABLE IF EXISTS `receipt_memo_tbl`;

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

DROP TABLE IF EXISTS `card_recommendation_detail_tbl`;

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

DROP TABLE IF EXISTS `account_verification_tbl`;

DROP TABLE IF EXISTS `linked_account_tbl`;

DROP TABLE IF EXISTS `bank_tbl`;

DROP TABLE IF EXISTS `user_tbl`;

DROP TABLE IF EXISTS `merchant_category_mapping_tbl`;
DROP TABLE IF EXISTS `custom_card_texts_tbl`;
DROP TABLE IF EXISTS `custom_card_emojis_tbl`;
DROP TABLE IF EXISTS `custom_card_tbl`;
DROP TABLE IF EXISTS `custom_card_image_tbl`;

-- 1. 회원 테이블
DROP TABLE IF EXISTS user_tbl;

CREATE TABLE user_tbl
(
    user_id         INT AUTO_INCREMENT PRIMARY KEY COMMENT '회원번호',
    user_name       VARCHAR(30)  NOT NULL COMMENT '이름',
    birth_date      DATE         NOT NULL COMMENT '생년월일',
    phone_number    VARCHAR(20)  NOT NULL UNIQUE COMMENT '휴대폰번호',
    pin_password    VARCHAR(255) NOT NULL COMMENT '암호화된 숫자 6자리 간편비밀번호',
    pin_fail_count  INT          NOT NULL DEFAULT 0 COMMENT 'PIN 로그인 실패 횟수',
    pin_locked_yn   CHAR(1)      NOT NULL DEFAULT 'N' COMMENT 'PIN 로그인 잠금 여부',
    user_status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '회원상태',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '수정일시',
    withdrawn_at    DATETIME     NULL COMMENT '탈퇴일시',
    last_login_at   DATETIME     NULL COMMENT '최근접속일시',

    CONSTRAINT chk_user_name_length
        CHECK (CHAR_LENGTH(user_name) BETWEEN 2 AND 30),

    CONSTRAINT chk_pin_fail_count
        CHECK (pin_fail_count BETWEEN 0 AND 5),

    CONSTRAINT chk_pin_locked_yn
        CHECK (pin_locked_yn IN ('Y', 'N')),

    CONSTRAINT chk_user_status
        CHECK (user_status IN ('ACTIVE', 'WITHDRAWN'))
) COMMENT = '회원';

-- 2. 은행 테이블
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

-- 59. 사용자 권한 테이블
DROP TABLE IF EXISTS user_auth_tbl;

CREATE TABLE user_auth_tbl
(
    user_id INT         NOT NULL COMMENT '회원번호',
    auth    VARCHAR(50) NOT NULL COMMENT '권한',

    PRIMARY KEY (user_id, auth),

    CONSTRAINT fk_user_auth_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_user_auth
        CHECK (
            auth IN (
                'ROLE_USER',
                'ROLE_MANAGER',
                'ROLE_ADMIN'
            )
        )
) COMMENT = '사용자 권한';

-- 3. 사용자계좌 테이블
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

-- 7. 인증 테이블 정의서
DROP TABLE IF EXISTS verification_tbl;

CREATE TABLE verification_tbl
(
    verification_id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '인증번호',
    user_id              INT         NULL COMMENT '회원번호',
    user_name            VARCHAR(30) NOT NULL COMMENT '인증이름',
    birth_date           DATE        NOT NULL COMMENT '생년월일',
    carrier_code         VARCHAR(20) NOT NULL COMMENT '통신사코드',
    phone_number         VARCHAR(20) NOT NULL COMMENT '휴대폰번호',
    verification_code    VARCHAR(6)  NOT NULL COMMENT '암호화된 인증코드',
    verification_purpose VARCHAR(30) NOT NULL COMMENT '인증목적',
    requested_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '인증요청일시',
    verified_yn          CHAR(1)     NOT NULL DEFAULT 'N' COMMENT '인증완료여부',
    fail_count           INT         NOT NULL DEFAULT 0 COMMENT '인증실패횟수',

    CONSTRAINT fk_verification_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT chk_verification_user_name_length
        CHECK (CHAR_LENGTH(user_name) BETWEEN 2 AND 30),

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
                                     'WITHDRAWAL',
                                     'NAME_CHANGE',
                                     'PHONE_CHANGE'
                )
            ),

    CONSTRAINT chk_verification_verified_yn
        CHECK (verified_yn IN ('Y', 'N')),

    CONSTRAINT chk_verification_fail_count
        CHECK (fail_count >= 0)
) COMMENT = '휴대폰인증';

-- 8. 프로필 테이블
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

-- 9. 알림설정 테이블
CREATE TABLE notification_setting_tbl
(
    notification_setting_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '알림설정번호',
    user_id                 INT      NOT NULL UNIQUE COMMENT '회원번호',
    finance_notification_yn CHAR(1)  NOT NULL DEFAULT 'Y' COMMENT '금융알림',
    friend_notification_yn  CHAR(1)  NOT NULL DEFAULT 'Y' COMMENT '친구요청알림',
    reward_notification_yn  CHAR(1)  NOT NULL DEFAULT 'Y' COMMENT '리워드알림',
    event_notification_yn   CHAR(1)  NOT NULL DEFAULT 'Y' COMMENT '이벤트혜택알림',
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_notification_setting_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT chk_finance_notification_yn
        CHECK (finance_notification_yn IN ('Y', 'N')),

    CONSTRAINT chk_friend_notification_yn
        CHECK (friend_notification_yn IN ('Y', 'N')),

    CONSTRAINT chk_reward_notification_yn
        CHECK (reward_notification_yn IN ('Y', 'N')),

    CONSTRAINT chk_event_notification_yn
        CHECK (event_notification_yn IN ('Y', 'N'))
);

-- 10. 리프레시토큰 테이블
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

-- 11. 소비 카테고리 테이블
DROP TABLE IF EXISTS spending_category_tbl;

CREATE TABLE spending_category_tbl
(
    spending_category_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카테고리 식별자',
    category_name        VARCHAR(100) NOT NULL UNIQUE COMMENT '카테고리명',
    parent_category_id   INT          NULL COMMENT '부모 카테고리 ID',

    CONSTRAINT fk_spending_category_parent
        FOREIGN KEY (parent_category_id)
            REFERENCES spending_category_tbl (spending_category_id)
);

-- 12.포인트 테이블 
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

-- 13.포인트거래내역 테이블
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

    CONSTRAINT chk_point_transaction_amount
        CHECK (point_amount > 0),

    CONSTRAINT chk_point_transaction_reason
        CHECK (reason_type IN ('ATTENDANCE', 'RANDOM_BOX', 'CONVERSION', 'EVENT'))
);

-- 14.랜덤박스 테이블
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

    CONSTRAINT chk_user_random_box_issue_reason
        CHECK (
            issue_reason IN (
                             'ATTENDANCE',
                             'FEED_SHARE',
                             'TRANSFER',
                             'PAYMENT',
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

    CONSTRAINT uq_random_box_issue_source
        UNIQUE (
                user_id,
                issue_reason,
                source_id
        ),

    CONSTRAINT uq_random_box_transfer_account
        UNIQUE (
                user_id,
                issue_reason,
                target_account_id
        ),

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

-- 15. 출석 내역 테이블
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

-- 16. 포인트 전환 내역 테이블
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

-- 17. 소비 분석 테이블
DROP TABLE IF EXISTS spending_analysis_tbl;

CREATE TABLE spending_analysis_tbl
(
    spending_analysis_id                INT AUTO_INCREMENT PRIMARY KEY COMMENT '소비분석ID',
    user_id                             INT          NOT NULL COMMENT '유저 ID',
    analysis_period                     INT          NOT NULL COMMENT '분석 기간',
    representative_category_id          INT          NOT NULL COMMENT '가장 많이 소비한 대표 카테고리',
    ai_title                            VARCHAR(100) NOT NULL COMMENT 'AI 생성 칭호',
    ai_analysis_summary                 TEXT         NOT NULL COMMENT 'AI가 생성한 소비 분석 요약',
    created_at                          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '분석 일자',

    CONSTRAINT fk_spending_analysis_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_spending_analysis_category
        FOREIGN KEY (representative_category_id)
            REFERENCES spending_category_tbl (spending_category_id),

    CONSTRAINT chk_spending_analysis_period
        CHECK (analysis_period IN (1, 3, 12))
);

-- 18. 분석결과저장 테이블
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

-- 19. KB 카드 상품 설명 테이블
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

-- 20. 카드 혜택 테이블
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

-- 21. 카드 추천 테이블
DROP TABLE IF EXISTS card_recommendation_tbl;

CREATE TABLE card_recommendation_tbl
(
    card_recommendation_id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '카드 추천 PK',
    spending_analysis_id    INT      NOT NULL COMMENT '소비분석 ID',
    card_product_id         INT      NOT NULL COMMENT '추천 카드',
    recommendation_rank       INT      NOT NULL COMMENT '추천 순위',
    expected_benefit_amount   INT      NULL COMMENT '예상 할인 금액',
    ai_recommendation_summary TEXT     NULL COMMENT 'AI가 생성한 카드별 추천 요약',
    created_at                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '추천 일시',

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

-- 22. 카드 추천 상세 테이블
DROP TABLE IF EXISTS card_recommendation_detail_tbl;

CREATE TABLE card_recommendation_detail_tbl
(
    card_recommendation_detail_id INT AUTO_INCREMENT PRIMARY KEY
        COMMENT '카드 추천 상세 PK',

    card_recommendation_id        INT      NOT NULL
        COMMENT '카드 추천 ID',

    card_benefit_id               INT      NOT NULL
        COMMENT '계산에 적용된 카드 혜택 ID',

    eligible_spending_amount      INT      NOT NULL
        COMMENT '전월 실적을 충족하여 혜택 계산에 반영된 거래금액 합계',

    eligible_transaction_count    INT      NOT NULL
        COMMENT '전월 실적을 충족하여 혜택 계산에 반영된 거래 건수',

    eligible_month_count          INT      NOT NULL
        COMMENT '해당 혜택이 계산에 반영된 월 수',

    expected_benefit_amount       INT      NOT NULL
        COMMENT '해당 카드 혜택의 연간 예상 할인액',

    created_at                    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '카드 추천 상세 생성일시',

    CONSTRAINT uq_card_recommendation_detail
        UNIQUE (card_recommendation_id, card_benefit_id),

    CONSTRAINT fk_card_recommendation_detail_recommendation
        FOREIGN KEY (card_recommendation_id)
            REFERENCES card_recommendation_tbl (card_recommendation_id),

    CONSTRAINT fk_card_recommendation_detail_benefit
        FOREIGN KEY (card_benefit_id)
            REFERENCES card_benefit_tbl (card_benefit_id),

    CONSTRAINT chk_card_recommendation_detail_spending
        CHECK (eligible_spending_amount >= 0),

    CONSTRAINT chk_card_recommendation_detail_transaction_count
        CHECK (eligible_transaction_count >= 0),

    CONSTRAINT chk_card_recommendation_detail_month_count
        CHECK (eligible_month_count >= 0),

    CONSTRAINT chk_card_recommendation_detail_benefit_amount
        CHECK (expected_benefit_amount >= 0)
);

-- 23. KB 보험 상품 테이블
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

-- 24. KB 보험 보장 항목 테이블
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

-- 25. KB 보험 추천 결과 테이블
DROP TABLE IF EXISTS kb_insurance_recommendation_tbl;

CREATE TABLE kb_insurance_recommendation_tbl
(
    insurance_recommendation_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '보험 추천 PK',
    spending_analysis_id        INT           NOT NULL COMMENT '소비분석 ID',
    insurance_product_id        INT           NOT NULL COMMENT '추천 보험 ID',
    recommendation_reason       VARCHAR(1000) NOT NULL COMMENT '규칙 기반 추천 이유',
    ai_recommendation_summary   TEXT          NULL COMMENT 'AI가 생성한 보험 상품별 추천 요약',
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

-- 26. 친구 요청 테이블
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

-- 27. 친구 테이블
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

-- 28. 정산 테이블
DROP TABLE IF EXISTS settlement_tbl;

CREATE TABLE settlement_tbl
(
    settlement_id        INT AUTO_INCREMENT PRIMARY KEY COMMENT '정산 ID',
    requester_id         INT         NOT NULL COMMENT '요청자 ID',
    title                VARCHAR(20) NULL COMMENT '정산 제목',
    content              VARCHAR(20) NULL COMMENT '피드 내용',
    total_amount         INT         NULL COMMENT '총 정산 금액',
    status               VARCHAR(20) NULL     DEFAULT 'REQUEST' COMMENT '정산 상태',
    created_at           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    settlement_type      VARCHAR(10) NOT NULL COMMENT '정산 방식',
    spending_category_id INT         NULL COMMENT '소비 카테고리 ID',
    last_reminder_date   DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '마지막으로 리마인드한 날짜',
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

-- 29. 정산 멤버 테이블
DROP TABLE IF EXISTS settlement_member_tbl;

CREATE TABLE settlement_member_tbl
(
    settlement_member_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '정산 참여자 ID',
    settlement_id        INT         NOT NULL COMMENT '정산 ID',
    user_id              INT         NOT NULL COMMENT '사용자 ID',
    amount               INT         NULL COMMENT '정산 금액',
    status               VARCHAR(20) NULL DEFAULT 'REQUEST' COMMENT '정산 상태',
    created_at           DATETIME    NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
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

-- 30. 알림 테이블
DROP TABLE IF EXISTS notification_tbl;

CREATE TABLE notification_tbl (
    notification_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '알림번호',
    receiver_id INT NOT NULL COMMENT '수신자번호',
    sender_id INT NOT NULL COMMENT '발신자번호',
    notification_type VARCHAR(30) NULL COMMENT '알림유형',
    target_id INT NULL COMMENT '대상번호',
    status VARCHAR(10) NOT NULL DEFAULT 'UNREAD' COMMENT '읽음상태',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
	actor_count INT NOT NULL DEFAULT 1 COMMENT '그룹 알림 참여자 수',
    CONSTRAINT fk_notification_receiver
        FOREIGN KEY (receiver_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_notification_sender
        FOREIGN KEY (sender_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT chk_notification_type
        CHECK (
            notification_type IN (
                'LIKE',
                'COMMENT',
                'FRIEND_REQUEST',
                'FRIEND_ACCEPT',
                'FRIEND_REJECT',
                'SETTLEMENT_REQUEST',
                'SETTLEMENT_PAYMENT',
                'SETTLEMENT_CANCEL',
                'SETTLEMENT_COMPLETE',
				'SETTLEMENT_COMPLETE_OWNER',
                'SETTLEMENT_REMIND'
            )
        ),

    CONSTRAINT chk_notification_status
        CHECK (
            status IN (
                'READ',
                'UNREAD'
            )
        )
);
-- 30-1 좋아요 알림 테이블
DROP TABLE IF EXISTS notification_group_tbl;

CREATE TABLE notification_group_tbl (
    group_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '알림 그룹 번호',
    receiver_id INT NOT NULL COMMENT '알림 수신자 번호',
    feed_id INT NOT NULL COMMENT '피드 번호',
    notification_type VARCHAR(30) NOT NULL COMMENT '알림 그룹 유형',
    actor_count INT NOT NULL DEFAULT 0 COMMENT '그룹 참여자 수',
    status VARCHAR(10) NOT NULL DEFAULT 'WAITING' COMMENT '그룹 상태',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '그룹 생성일시',
    completed_at DATETIME NULL COMMENT '알림 확정일시',

    CONSTRAINT fk_notification_group_receiver
        FOREIGN KEY (receiver_id)
        REFERENCES user_tbl (user_id),

    CONSTRAINT fk_notification_group_feed
        FOREIGN KEY (feed_id)
        REFERENCES feed_tbl (feed_id),

    CONSTRAINT chk_notification_group_type
        CHECK (
            notification_type IN (
                'LIKE',
                'COMMENT'
            )
        ),

    CONSTRAINT chk_notification_group_status
        CHECK (
            status IN (
                'WAITING',
                'COMPLETED'
            )
        )
);
-- 30-3 좋아요 액터 테이블
DROP TABLE IF EXISTS notification_group_actor_tbl;

CREATE TABLE notification_group_actor_tbl (
    group_id INT NOT NULL COMMENT '알림 그룹 번호',
    user_id INT NOT NULL COMMENT '그룹 참여 사용자 번호',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '그룹 참여일시',

    PRIMARY KEY (group_id, user_id),

    CONSTRAINT fk_notification_group_actor_group
        FOREIGN KEY (group_id)
        REFERENCES notification_group_tbl (group_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_notification_group_actor_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl (user_id)
);

CREATE INDEX idx_notification_group_waiting
ON notification_group_tbl (
    feed_id,
    receiver_id,
    notification_type,
    status
);

-- 31. 통합 거래 원장 테이블
DROP TABLE IF EXISTS financial_transaction_tbl;

CREATE TABLE financial_transaction_tbl
(
    transaction_id        INT AUTO_INCREMENT PRIMARY KEY
        COMMENT '거래번호',
    parent_transaction_id INT          NULL
        COMMENT '상위 거래번호',

    settlement_id         INT          NULL
        COMMENT '정산 ID',

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

    CONSTRAINT fk_financial_transaction_parent
        FOREIGN KEY (parent_transaction_id)
            REFERENCES financial_transaction_tbl (transaction_id),


    -- 정산 관계
    CONSTRAINT fk_financial_transaction_settlement
        FOREIGN KEY (settlement_id)
            REFERENCES settlement_tbl (settlement_id),


    -- 요청자 회원
    CONSTRAINT fk_financial_transaction_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_financial_transaction_receiver
        FOREIGN KEY (receive_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_financial_transaction_category
        FOREIGN KEY (spending_category_id)
            REFERENCES spending_category_tbl (spending_category_id),

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

    -- 송금/정산은 사용자가 소비 카테고리를 반드시 선택한다.
    -- PAYMENT는 AI 자동분류 실패 시 미분류(NULL)를 허용한다.
);

-- 32. 은행 계좌 더미 테이블
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
    issue_type          varchar(20) default 'NORMAL' null,
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

-- 33. 계좌 거래 상세 테이블
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

-- 34. 지갑 거래 상세 테이블
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

-- 35. 카드 더미 테이블
DROP TABLE IF EXISTS card_tbl;
CREATE TABLE card_tbl
(
    card_code          INT         AUTO_INCREMENT PRIMARY KEY COMMENT '카드 코드 (PK)',

    card_img_file_name VARCHAR(255) NULL COMMENT '카드이미지파일명',

    card_num           VARCHAR(255) NOT NULL COMMENT '카드번호',
    expiry_date        CHAR(5)     NOT NULL COMMENT '유효기간',
    cvv                VARCHAR(255) NOT NULL COMMENT 'cvv',
    card_password      VARCHAR(255) NOT NULL COMMENT '카드 비밀번호 4자리',
    card_name          VARCHAR(255) NULL COMMENT '카드 이름'

    
) COMMENT = '실물 카드 원장';

-- 38. 영수증메모 테이블
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

-- 39. 피드 테이블
DROP TABLE IF EXISTS feed_tbl;

CREATE TABLE feed_tbl
(
    feed_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '피드번호',
    user_id     INT         NOT NULL COMMENT '회원번호',
    target_id   INT         NULL COMMENT '피드 유형에 따른 대상 ID',
    feed_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '피드상태',
    feed_type   VARCHAR(20) NOT NULL COMMENT '피드유형',
    content     VARCHAR(20) NULL COMMENT '피드내용',
    visibility  VARCHAR(20) NOT NULL COMMENT '공개범위',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT fk_feed_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT chk_feed_status
        CHECK (
            feed_status IN (
                            'ACTIVE',
                            'DELETED'
                )
            ),

    CONSTRAINT chk_feed_type
        CHECK (
            feed_type IN (
                          'TRANSFER',
                          'PAYMENT',
                          'SETTLEMENT',
                          'CARD',
                          'ANALYSIS',
                          'EVENT'
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

-- 40. 피드 이미지 테이블
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

-- 41. 좋아요 테이블
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

-- 42. 댓글 테이블
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

DROP TABLE IF EXISTS custom_card_tbl;
-- 42-1. 카드 메인 테이블
CREATE TABLE custom_card_tbl (
    custom_card_id Int AUTO_INCREMENT PRIMARY KEY,
    user_id Int NOT NULL, -- 사용자 테이블이 있다고 가정
    -- 배경 정보를 타입과 값으로 분리하여 저장
    background_type VARCHAR(20), -- 'IMAGE', 'GRADIENT', 'COLOR'
    background_value TEXT,       -- 실제 경로, CSS string 등
    pattern_path VARCHAR(255),
    drawing_image_url MEDIUMTEXT, -- Base64 문자열이 길 수 있음
    card_chip VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS custom_card_image_tbl;

CREATE TABLE custom_card_image_tbl
(
    custom_card_image_id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '신청ID',

    custom_card_id INT  NOT NULL COMMENT '커스텀 카드 id',

    custom_card_image_name VARCHAR(255)  NULL COMMENT '카드 이미지 ',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

	CONSTRAINT fk_custom_card_image_tbl_custom_card_id
	FOREIGN KEY (custom_card_id)
		REFERENCES custom_card_tbl (custom_card_id)
);

DROP TABLE IF EXISTS custom_card_texts_tbl;
-- 42-2. 텍스트 아이템 테이블
CREATE TABLE custom_card_texts_tbl (
    text_id Int AUTO_INCREMENT PRIMARY KEY,
    custom_card_id Int NOT NULL,
    content VARCHAR(500),
    x_pos FLOAT,
    y_pos FLOAT,
    rotation FLOAT,
    font_family VARCHAR(100),
    font_color VARCHAR(50),
    font_size VARCHAR(20),
    is_bold BOOLEAN ,
    FOREIGN KEY (custom_card_id) REFERENCES custom_card_tbl(custom_card_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS custom_card_emojis_tbl;
--  42-3.  이모지 아이템 테이블
CREATE TABLE custom_card_emojis_tbl (
    emoji_id Int AUTO_INCREMENT PRIMARY KEY, -- JSON의 id를 그대로 사용하거나 auto_increment 사용
    custom_card_id Int NOT NULL,
    emoji_url VARCHAR(255),
    x_pos FLOAT,
    y_pos FLOAT,
    rotation FLOAT,
    emoji_type VARCHAR(50), -- 'SVG' : 'ICON'  등 분류
    FOREIGN KEY (custom_card_id) REFERENCES custom_card_tbl(custom_card_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 46. 커스텀카드 신청이력 테이블
DROP TABLE IF EXISTS card_application_history_tbl;

CREATE TABLE card_application_history_tbl
(
    apply_id        INT AUTO_INCREMENT PRIMARY KEY COMMENT '신청ID',

    custom_card_id INT  NOT NULL COMMENT '커스텀 카드 id',

    card_code       INT NULL COMMENT '카드코드',

    apply_status VARCHAR(20) NOT NULL DEFAULT 'REQUEST' COMMENT '발급 상태',

    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

			CONSTRAINT fk_card_application_history_custom_card_id
	FOREIGN KEY (custom_card_id)
		REFERENCES custom_card_tbl (custom_card_id),
            
			  CONSTRAINT fk_card_application_history_card_code
        FOREIGN KEY (card_code)
            REFERENCES card_tbl (card_code)
);

-- 47. 이벤트 테이블
DROP TABLE IF EXISTS event_tbl;

CREATE TABLE event_tbl
(
    event_id       INT AUTO_INCREMENT PRIMARY KEY COMMENT '이벤트ID',
    event_name     VARCHAR(100) NULL COMMENT '이벤트명',
    event_desc     TEXT         NULL COMMENT '이벤트상세설명',
    event_type     VARCHAR(20)  NULL COMMENT '이벤트유형',
	event_category	VARCHAR(20) NULL COMMENT '이벤트 카테고리',
    event_status   VARCHAR(10)  NOT NULL DEFAULT 'OPEN' COMMENT '이벤트 진행 토글',
    event_img_name VARCHAR(255) NULL COMMENT '이벤트 이미지 이름',
    event_target   INT          NOT NULL COMMENT '이벤트 최종 목표',
    event_level    INT          NOT NULL DEFAULT 1 COMMENT '이벤트 최종 난이도',
    event_daily_limit_count INT NOT NULL DEFAULT 0 COMMENT '이벤트 일일 참여 가능 횟수',
    start_at       DATETIME     NULL COMMENT '시작일시',
    end_at         DATETIME     NULL COMMENT '종료일시',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    CONSTRAINT chk_event_type
        CHECK (
            event_type IN ('ATTENDANCE', 'PERMANENT', 'LIMITED', 'SEASON', 'PROMOTION', 'LUCKYDRAW' )
            ),
            	
   CONSTRAINT chk_event_category
        CHECK (
            event_category IN ('ATTENDANCE', 'FEED', 'CARD', 'REMITTANCE', 'WALLET', 'ANALYSIS', 'RANDOMBOX', 'ETC', 'SETTLEMENT')
            ),

    CONSTRAINT chk_event_status
        CHECK (
            event_status IN ('OPEN', 'CLOSE')
            ),

    CONSTRAINT chk_event_daily_limit_count 
        CHECK (
            event_daily_limit_count >= 0
           )
);

-- 48. 이벤트 리워드 테이블
DROP TABLE IF EXISTS event_reward_tbl;

CREATE TABLE event_reward_tbl
(
    reward_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '리워드ID',
    event_id     INT     NOT NULL COMMENT '이벤트ID',
    reward_point INT     NULL     DEFAULT 0 COMMENT '리워드포인트',
    reward_exp   INT     NULL COMMENT '리워드경험치',

    CONSTRAINT fk_event_reward_event
        FOREIGN KEY (event_id)
            REFERENCES event_tbl (event_id),

    CONSTRAINT chk_event_reward_point
        CHECK (
            reward_point >= 0
            ),

    CONSTRAINT chk_event_reward_exp
        CHECK (
            reward_exp >= 0
            )
);

-- 49. 이벤트 참여이력 테이블
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
            REFERENCES user_tbl (user_id)


);

-- 45-1. 이벤트 참여 관리 테이블
DROP TABLE IF EXISTS event_user_tbl;

CREATE TABLE event_user_tbl
(
    event_user_id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event_user_event
        FOREIGN KEY (event_id)
        REFERENCES event_tbl(event_id),

    CONSTRAINT fk_event_user_member
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),

    UNIQUE KEY uk_event_user (event_id, user_id)
);

-- 50. 이벤트 - 출석체크 참여이력 테이블
DROP TABLE IF EXISTS event_attendance_tbl;

CREATE TABLE event_attendance_tbl (
    participation_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '참여ID',
    event_id INT NOT NULL COMMENT '이벤트ID',
    user_id INT NOT NULL COMMENT '사용자ID',
    participated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '참여일시',

    CONSTRAINT fk_event_attendance_event
        FOREIGN KEY (event_id)
        REFERENCES event_tbl(event_id),

    CONSTRAINT fk_event_attendance_user
        FOREIGN KEY (user_id)
        REFERENCES user_tbl(user_id),
        
    CONSTRAINT uk_event_attendance_date
        UNIQUE (event_id, user_id, participated_at)
) COMMENT='이벤트 - 출석체크 참여이력 테이블';

-- 51. 이벤트 리워드 수령이력 테이블
DROP TABLE IF EXISTS event_reward_receive_tbl;

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

-- 52. 이벤트 챌린지 테이블
DROP TABLE IF EXISTS event_challenge_tbl;

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

-- 52-1. 이벤트 챌린지 레벨 관리 테이블
DROP TABLE IF EXISTS event_challenge_level_tbl;

CREATE TABLE event_challenge_level_tbl
(
    challenge_level_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '챌린지 레벨 ID',
    challenge_id       INT NOT NULL COMMENT '챌린지 ID',
    level              INT NOT NULL COMMENT '챌린지 레벨',
    required_exp       INT NOT NULL COMMENT '해당 레벨 보상 수령에 필요한 누적 경험치',
    reward_point       INT NOT NULL COMMENT '레벨 달성 보상 포인트',

    CONSTRAINT uk_event_challenge_level
        UNIQUE (challenge_id, level),

    CONSTRAINT fk_event_challenge_level_challenge
        FOREIGN KEY (challenge_id)
            REFERENCES event_challenge_tbl (challenge_id),

    CONSTRAINT chk_event_challenge_level_required_exp
        CHECK (required_exp >= 0),

    CONSTRAINT chk_event_challenge_level_reward_point
        CHECK (reward_point >= 0)
) COMMENT ='이벤트 챌린지 레벨 관리';

-- 53. 이벤트 챌린지 참여이력 테이블
DROP TABLE IF EXISTS event_challenge_user_tbl;

CREATE TABLE event_challenge_user_tbl
(
    user_challenge_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '챌린지참여ID',
    user_id           INT         NOT NULL COMMENT '사용자ID',
    challenge_id      INT         NOT NULL COMMENT '챌린지ID',
    current_level     INT         NOT NULL COMMENT '현재 달성 레벨',
    current_target    INT         NOT NULL COMMENT '현재 누적 수치',
    exp               INT         NOT NULL DEFAULT 0 COMMENT '현재 누적 경험치',
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
            ),

    CONSTRAINT chk_event_challenge_exp
        CHECK (exp >= 0)
) COMMENT ='이벤트 챌린지 참여이력';

-- 54. 카드사 테이블
DROP TABLE IF EXISTS card_company_tbl;

CREATE TABLE card_company_tbl
(
    card_company_code VARCHAR(10) PRIMARY KEY COMMENT '카드사코드',
    card_company_name VARCHAR(50) NOT NULL UNIQUE COMMENT '카드사명',

    CONSTRAINT chk_card_company_name
        CHECK (CHAR_LENGTH(TRIM(card_company_name)) > 0)
) COMMENT = '카드사';

-- 55. 연결카드 테이블
DROP TABLE IF EXISTS linked_card_tbl;

CREATE TABLE linked_card_tbl
(
    linked_card_id      INT         AUTO_INCREMENT PRIMARY KEY COMMENT '연결카드번호',
    user_id             INT         NOT NULL COMMENT '회원번호',
    card_code           INT         NOT NULL COMMENT '카드 ID',
    card_company_code   VARCHAR(10) NOT NULL COMMENT '카드사코드',
    represent_yn        CHAR(1)     NOT NULL DEFAULT 'N' COMMENT '대표카드여부',
    delete_yn           CHAR(1)     NOT NULL DEFAULT 'N' COMMENT '삭제여부',
    created_at          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    CONSTRAINT fk_linked_card_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_linked_card_company
        FOREIGN KEY (card_company_code)
            REFERENCES card_company_tbl (card_company_code),

    CONSTRAINT fk_linked_card_card
        FOREIGN KEY (card_code)
            REFERENCES card_tbl (card_code),

    CONSTRAINT chk_linked_card_represent_yn
        CHECK (represent_yn IN ('Y', 'N')),

    CONSTRAINT chk_linked_card_delete_yn
        CHECK (delete_yn IN ('Y', 'N'))
) COMMENT = '사용자 연결 카드';

-- 55-1. 카드 결제 상세 테이블
DROP TABLE IF EXISTS card_transaction_detail_tbl;

CREATE TABLE card_transaction_detail_tbl
(
    card_transaction_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카드 거래 상세 ID',
    linked_card_id      INT         NOT NULL COMMENT '연결카드번호',
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '결제 상태',
    created_at          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    transaction_id      INT         NULL COMMENT '금융 거래 원장 ID',

    CONSTRAINT fk_card_tx_detail_linked_card
        FOREIGN KEY (linked_card_id)
            REFERENCES linked_card_tbl (linked_card_id),

    CONSTRAINT fk_card_tx_detail_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES financial_transaction_tbl (transaction_id),

    CONSTRAINT chk_card_tx_detail_status
        CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'))
) COMMENT = '카드 결제 거래 상세';

-- 56. 계좌인증 테이블
DROP TABLE IF EXISTS account_verification_tbl;

CREATE TABLE account_verification_tbl
(
    verification_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '계좌인증번호',
    user_id           INT          NOT NULL COMMENT '회원번호',
    bank_code         VARCHAR(10)  NOT NULL COMMENT '은행코드',
    account_number    VARCHAR(255) NOT NULL COMMENT '계좌번호',
    account_holder    VARCHAR(50)  NOT NULL COMMENT '예금주',
    verification_code CHAR(4)      NOT NULL COMMENT '입금자명4자리',
    verified_yn       CHAR(1)      NOT NULL DEFAULT 'N' COMMENT '인증여부',
    requested_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '인증요청일시',
    fail_count        INT          NOT NULL DEFAULT 0 COMMENT '인증번호 실패 횟수',
    resend_count      INT          NOT NULL DEFAULT 0 COMMENT '인증번호 재발급 횟수',
    locked_until      DATETIME     NULL COMMENT '계좌 인증 잠금 해제 일시',

    CONSTRAINT fk_account_verification_user
        FOREIGN KEY (user_id)
            REFERENCES user_tbl (user_id),

    CONSTRAINT fk_account_verification_bank
        FOREIGN KEY (bank_code)
            REFERENCES bank_tbl (bank_code),

    CONSTRAINT chk_account_verification_verified_yn
        CHECK (verified_yn IN ('Y', 'N')),

    CONSTRAINT chk_account_verification_fail_count
        CHECK (fail_count BETWEEN 0 AND 5),

    CONSTRAINT chk_account_verification_resend_count
        CHECK (resend_count BETWEEN 0 AND 1)
) COMMENT = '계좌인증';

-- 57. 카테고리 분류 저장 테이블
DROP TABLE IF EXISTS merchant_category_mapping_tbl;

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

-- 58. 소비카테고리 <-> 보험 종류 매칭 정책 테이블
DROP TABLE IF EXISTS kb_insurance_category_match_tbl;

CREATE TABLE kb_insurance_category_match_tbl
(
    insurance_category_match_id INT AUTO_INCREMENT PRIMARY KEY
        COMMENT '보험 추천 카테고리 매핑 ID',
    insurance_product_id        INT          NOT NULL
        COMMENT '보험 상품 ID',
    spending_category_id        INT          NOT NULL
        COMMENT '소비 카테고리 ID',
    recommendation_reason       VARCHAR(255) NULL
        COMMENT '추천 사유 기본 문구',
    priority                    INT          NOT NULL DEFAULT 1
        COMMENT '같은 카테고리 내 표시 순서',
    created_at                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '생성 일시',

    CONSTRAINT fk_insurance_match_product
        FOREIGN KEY (insurance_product_id)
            REFERENCES kb_insurance_product_tbl (insurance_product_id),

    CONSTRAINT fk_insurance_match_spending_category
        FOREIGN KEY (spending_category_id)
            REFERENCES spending_category_tbl (spending_category_id),

    CONSTRAINT uk_insurance_category_match
        UNIQUE (insurance_product_id, spending_category_id)
);






USE kbproject;

START TRANSACTION;

-- ---------------------------------------------------------------------
-- 1. user_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO user_tbl (
    user_id,
    user_name,
    birth_date,
    phone_number,
    pin_password,
    pin_fail_count,
    pin_locked_yn,
    user_status,
    created_at,
    updated_at,
    withdrawn_at,
    last_login_at
)
VALUES (1, '테스트회원1', '20000115', '01011112222', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 0, 'N',
		'ACTIVE', '2026-07-01 09:00:00', '2026-07-24 08:30:00', NULL, '2026-07-24 08:30:00'),
		(2, '테스트회원2', '19990321', '01022223333', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 0, 'N',
		'ACTIVE', '2026-07-02 10:00:00', '2026-07-23 19:10:00', NULL, '2026-07-23 19:10:00'),
		(3, '테스트회원3', '20010509', '01033334444', '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK', 0, 'N',
		'ACTIVE', '2026-07-03 11:00:00', '2026-07-22 14:20:00', NULL, '2026-07-22 14:20:00');

-- ---------------------------------------------------------------------
-- 59. user_auth_tbl
-- ---------------------------------------------------------------------
INSERT INTO user_auth_tbl (user_id, auth)
VALUES (1, 'ROLE_USER'),
       (2, 'ROLE_USER'),
       (3, 'ROLE_USER');
       
-- ---------------------------------------------------------------------
-- 2. bank_tbl (10건)
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
-- 3. linked_account_tbl (6건)
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
-- 4. agreement_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO agreement_tbl (agreement_id,
                           agreement_type,
                           agreement_name,
                           agreement_content,
                           required_yn,
                           use_yn)
VALUES (1,
        'SERVICE',
        '서비스 이용약관',
        '제1조 (목적)\n본 약관은 KB 금융 플랫폼(이하 "서비스")의 이용과 관련하여 회사와 회원 간의 권리, 의무 및 책임사항을 규정하는 것을 목적으로 합니다.\n\n제2조 (회원가입)\n1. 회원은 본인 명의의 휴대폰 인증을 통해 가입할 수 있습니다.\n2. 허위 정보 또는 타인의 정보를 이용한 경우 서비스 이용이 제한될 수 있습니다.\n\n제3조 (서비스 이용)\n회원은 다음과 같은 서비스를 이용할 수 있습니다.\n1. 전자지갑 생성 및 이용\n2. 본인 명의 계좌 연결\n3. 포인트 조회 및 이용\n4. 피드 작성 및 조회\n5. 카드 추천 및 관련 서비스 이용\n\n제4조 (회원의 의무)\n회원은 다음 행위를 해서는 안 됩니다.\n1. 타인의 개인정보를 도용하는 행위\n2. 거짓 정보를 입력하거나 제공하는 행위\n3. 서비스의 정상적인 운영을 방해하는 행위\n4. 관련 법령 또는 본 약관을 위반하는 행위\n\n제5조 (서비스 이용 제한)\n회사는 회원이 관련 법령 또는 본 약관을 위반한 경우 서비스 이용을 제한하거나 회원 자격을 정지할 수 있습니다.',
        'Y',
        'Y'),
       (2,
        'PRIVACY',
        '개인정보 수집 및 이용 동의',
        '1. 수집하는 개인정보 항목\n회사는 회원가입 및 서비스 제공을 위해 다음 정보를 수집합니다.\n- 이름\n- 휴대폰번호\n- 이메일\n- 암호화된 비밀번호\n- 닉네임\n\n2. 개인정보 수집 및 이용 목적\n수집한 개인정보는 다음 목적으로 이용됩니다.\n- 회원가입 및 본인 확인\n- 회원 식별 및 계정 관리\n- 고객 문의 및 서비스 안내\n- 전자지갑 생성과 금융 서비스 제공\n- 부정 이용 방지 및 서비스 보안\n\n3. 개인정보 보유 및 이용 기간\n회사는 회원 탈퇴 시까지 개인정보를 보유하며, 관계 법령에 따라 보관이 필요한 경우 해당 기간 동안 별도로 보관합니다.\n\n4. 동의 거부 권리 및 불이익\n회원은 개인정보 수집 및 이용에 대한 동의를 거부할 권리가 있습니다. 다만, 필수 정보 수집에 동의하지 않는 경우 회원가입 및 서비스 이용이 제한될 수 있습니다.',
        'Y',
        'Y'),
       (3,
        'ELECTRONIC_FINANCE',
        '전자금융거래 이용약관',
        '제1조 (이용 가능한 금융 서비스)\n회원은 다음과 같은 금융 관련 서비스를 이용할 수 있습니다.\n1. 전자지갑 생성 및 이용\n2. 본인 명의 계좌 연결\n3. 포인트 적립 및 사용\n4. 결제 및 송금 서비스\n\n제2조 (전자지갑 생성)\n회원가입이 완료되면 회원의 서비스 이용을 위한 전자지갑이 자동으로 생성될 수 있습니다.\n\n제3조 (계좌 연결)\n회원은 본인 명의의 계좌만 연결할 수 있으며, 계좌 연결 과정에서 추가적인 본인 인증이 요구될 수 있습니다.\n\n제4조 (서비스 이용 제한)\n다음의 경우 금융 서비스 이용이 제한될 수 있습니다.\n1. 본인 인증에 실패한 경우\n2. 비정상적이거나 의심스러운 금융 거래가 확인된 경우\n3. 타인 명의의 계좌를 연결한 경우\n4. 관련 법령 또는 약관을 위반한 경우\n\n제5조 (회원의 책임)\n회원은 본인의 인증정보와 계정정보를 안전하게 관리해야 하며, 이를 타인에게 제공하거나 공유해서는 안 됩니다.',
        'Y',
        'Y'),
       (4,
        'ANALYSIS_REQUIRED',
        '소비정보 수집 및 이용 동의',
        '1. 수집하는 소비정보 항목\n회사는 소비 분석 서비스 제공을 위해 다음 정보를 수집합니다.\n- 가맹점명\n- 결제금액\n- 결제일시\n- 소비 카테고리\n- 결제 및 거래 내역\n\n2. 소비정보 수집 및 이용 목적\n수집한 소비정보는 다음 목적으로 이용됩니다.\n- 소비 내역 조회 및 관리\n- 소비 카테고리 분류\n- 기간별 소비 패턴 분석\n- 소비 분석 결과 및 통계 제공\n- 맞춤형 소비 관리 서비스 제공\n\n3. 소비정보 보유 및 이용 기간\n회사는 소비 분석 서비스 이용 기간 동안 소비정보를 보유하며, 회원 탈퇴 또는 서비스 이용 동의 철회 시 해당 정보를 지체 없이 삭제합니다. 다만, 관계 법령에 따라 보관이 필요한 경우에는 해당 기간 동안 별도로 보관합니다.\n\n4. 동의 거부 권리 및 불이익\n회원은 소비정보 수집 및 이용에 대한 동의를 거부할 권리가 있습니다. 다만, 본 동의는 소비 분석 서비스 제공을 위한 필수 동의이므로 동의하지 않는 경우 소비 분석 기능을 이용할 수 없습니다.',
        'Y',
        'Y'),
       (5,
        'MARKETING',
        '마케팅 정보 수신 동의',
        '1. 수신 가능한 마케팅 정보\n회사는 회원의 동의를 받은 경우 다음과 같은 정보를 제공할 수 있습니다.\n- 이벤트 및 프로모션 안내\n- 신규 서비스 및 기능 안내\n- 카드 및 금융상품 관련 정보\n- 맞춤형 금융 혜택\n- 포인트 및 리워드 관련 정보\n\n2. 마케팅 정보 수신 방법\n마케팅 정보는 다음 방법으로 제공될 수 있습니다.\n- 앱 푸시 알림\n- SMS 문자메시지\n- 이메일\n\n3. 동의 거부 및 철회\n회원은 마케팅 정보 수신에 동의하지 않아도 기본 서비스를 이용할 수 있습니다.\n동의 후에도 언제든지 설정 화면에서 수신 여부를 변경하거나 철회할 수 있습니다.\n\n4. 안내사항\n마케팅 수신 동의와 관계없이 서비스 이용, 보안, 거래 내역 등 필수 안내는 제공될 수 있습니다.',
        'N',
        'Y'),
       (6,
        'ANALYSIS_OPTIONAL',
        '금융상품 추천 정보 활용 동의',
        '1. 활용하는 정보 항목\n회사는 맞춤형 금융상품 추천 서비스를 제공하기 위해 다음 정보를 활용합니다.\n- 소비 카테고리별 지출 내역\n- 기간별 소비금액\n- 소비 패턴 분석 결과\n- 보유하거나 연결한 계좌 및 카드 정보\n- 금융상품 이용 및 관심 정보\n\n2. 정보 활용 목적\n수집 및 분석된 정보는 다음 목적으로 활용됩니다.\n- 회원의 소비 성향 분석\n- 카드 및 금융상품 추천\n- 회원별 맞춤형 금융 혜택 안내\n- 금융상품 추천 서비스 개선\n\n3. 정보 보유 및 이용 기간\n회사는 맞춤형 금융상품 추천 서비스 이용 기간 동안 해당 정보를 활용하며, 회원 탈퇴 또는 동의 철회 시 지체 없이 활용을 중단하고 관련 정보를 삭제합니다. 다만, 관계 법령에 따라 보관이 필요한 경우에는 해당 기간 동안 별도로 보관합니다.\n\n4. 동의 거부 및 철회\n회원은 맞춤형 금융상품 추천을 위한 정보 활용에 동의하지 않을 권리가 있으며, 동의 후에도 언제든지 설정 화면에서 철회할 수 있습니다. 동의하지 않더라도 소비 분석을 포함한 기본 서비스는 정상적으로 이용할 수 있습니다.',
        'N',
        'Y'),
(
    7,
    'CHECK_CARD', 
    '개인정보 수집·이용 동의', 
    '1. 수집·이용 목적: 체크카드 발급 적격 심사, 회원 가입 및 회원 관리, 본인 식별, 실물 카드 배송, 고객 상담 및 민원 처리, 부정이용 방지
    
2. 수집 항목: [필수] 성명, 생년월일, 휴대전화번호, 이메일, 주소, 직장 정보

3. 보유 및 이용 기간: 회원 탈퇴 및 카드 유효기간 만료 후 5년까지 보유 (단, 관계 법령에 의하여 보존할 필요가 있는 경우 해당 기간 동안 보존)', 
    'Y', 
    'Y'
),
(
    8,
    'CHECK_CARD', 
    '고유식별정보 처리 동의', 
    '1. 수집·이용 목적: 여신전문금융업법 및 관련 법령에 따른 본인 확인, 실명 확인 및 체크카드 발급 심사
    
2. 수집 항목: [주민등록번호/외국인등록번호/여권번호] 등 고유식별정보

3. 보유 및 이용 기간: 동의일로부터 회원 탈퇴 및 카드 발급 계약 종료 후 관계 법령이 정하는 기간까지', 
    'Y', 
    'Y'
),
(
    9,
    'CHECK_CARD', 
    '개인정보 제3자 제공 동의', 
    '1. 제공받는 자: 주식회사 OO택배, 코리아결제네트워크(VAN사), 신용카드사, 제휴 브랜드사(VISA/Mastercard 등)
    
2. 제공받는 자의 이용 목적: 체크카드 실물 배송, 결제 승인 및 매입 처리, 제휴 서비스 제공

3. 제공하는 항목: 성명, 휴대전화번호, 배송지 주소, 카드 승인 정보

4. 보유 및 이용 기간: 개인정보 이용 목적 달성 시까지 (단, 관계 법령에 따라 법정 의무 보유 기간 동안 보관)', 
    'Y', 
    'Y'
),
(
    10,
    'CHECK_CARD', 
    '직불/체크카드 회원 표준약관', 
    '제1조(목적) 이 약관은 직불/체크카드 회원이 카드를 이용함에 있어 회사와 회원 사이의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.
    
제2조(이용 한도 및 이용 시간) 1. 체크카드는 회원의 지정 계좌 잔액 범위 내에서 이용할 수 있습니다. 2. 1일/월간 이용 한도는 카드 발급 시 설정한 한도 내에서 적용되며, 회원의 요청에 따라 변경할 수 있습니다.

제3조(분실·도난 신고 및 책임) 회원이 카드의 분실 또는 도난 등의 사유로 회사에 신고한 경우, 회사는 신고 접수 시점 이후 발생한 부정사용 금액에 대하여 책임을 부담합니다.', 
    'Y', 
    'Y'
),
(
    11,
    'CHECK_CARD', 
    '출금계좌 이용약관 (금융거래 기본약관)', 
    '제1조(목적) 본 약관은 체크카드 이용 시 결제 대금을 회원의 지정 계좌에서 실시간으로 인출(출금)하는 자동이체 거래에 관하여 정함을 목적으로 합니다.
    
제2조(출금 처리) 체크카드 사용 승인이 이루어지는 즉시 회원의 지정 계좌에서 해당 결제 금액이 출금되며, 계좌 잔액 부족 시 거래가 거절될 수 있습니다.

제3조(계좌 변경 및 해지) 지정 계좌의 변경 또는 해지는 회사의 앱 또는 고객센터를 통해 처리할 수 있으며, 정상적인 계좌가 등록되어 있지 않은 경우 체크카드 이용이 제한될 수 있습니다.', 
    'Y', 
    'Y'
);

            -- ---------------------------------------------------------------------
-- 5. user_agreement_tbl (6건)
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
-- 6. wallet_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO wallet_tbl (wallet_id,
                        user_id,
                        balance,
                        wallet_status)
VALUES (1, 1, 107000, 'ACTIVE'),
       (2, 2, 185000, 'ACTIVE'),
       (3, 3, 145000, 'ACTIVE');

-- ---------------------------------------------------------------------
-- 7. verification_tbl (6건)
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
VALUES (1, 1, '테스트회원1', '20000115', 'SKT', '01011112222', '111111', 'SIGN_UP', '2026-07-01 08:55:00', 'Y', 0),
       (2, 2, '테스트회원2', '19990321', 'KT', '01022223333', '222222', 'SIGN_UP', '2026-07-02 09:55:00', 'Y', 0),
       (3, 3, '테스트회원3', '20010509', 'LGU', '01033334444', '333333', 'SIGN_UP', '2026-07-03 10:55:00', 'Y', 0),
       (4, 1, '테스트회원1', '20000115', 'SKT', '01011112222', '444444', 'PIN_RESET', '2026-07-20 10:00:00', 'Y', 1),
       (5, 2, '테스트회원2', '19990321', 'KT_MVNO', '01022223333', '555555', 'PIN_RESET', '2026-07-21 11:00:00', 'N', 2),
       (6, NULL, '가입대기회원', '20021212', 'LGU_MVNO', '01099998888', '666666', 'SIGN_UP', '2026-07-24 09:00:00', 'N',
        0);

-- ---------------------------------------------------------------------
-- 8. profile_tbl (3건)
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
-- 9. notification_setting_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO notification_setting_tbl (notification_setting_id,
                                      user_id,
                                      finance_notification_yn,
                                      friend_notification_yn,
                                      reward_notification_yn,
                                      event_notification_yn,
                                      updated_at)
VALUES (1, 1, 'Y', 'Y', 'Y', 'Y', '2026-07-20 12:00:00'),
       (2, 2, 'Y', 'N', 'N', 'Y', '2026-07-21 12:00:00'),
       (3, 3, 'N', 'Y', 'Y', 'N', '2026-07-22 12:00:00');

-- ---------------------------------------------------------------------
-- 10. refresh_token_tbl (6건)
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
-- 11. spending_category_tbl (20건)
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
-- 12. point_wallet_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO point_wallet_tbl (
    point_wallet_id,
    user_id,
    point_balance,
    updated_at
)
VALUES
    (1, 1, 500, '2026-07-18 09:05:00'),
    (2, 2, 5000, '2026-07-24 09:20:00'),
    (3, 3, 7500, '2026-07-24 09:30:00');


-- ---------------------------------------------------------------------
-- 13. point_transaction_tbl (5건)
-- ---------------------------------------------------------------------
INSERT INTO point_transaction_tbl (
    point_transaction_id,
    point_wallet_id,
    transaction_type,
    point_amount,
    reason_type,
    created_at
)
VALUES
    (1, 1, 'EARN', 500, 'RANDOM_BOX', '2026-07-18 09:05:00'),

    (2, 2, 'EARN', 7000, 'EVENT', '2026-07-21 09:00:00'),
    (3, 2, 'USE', 2000, 'CONVERSION', '2026-07-24 09:20:00'),

    (4, 3, 'EARN', 10000, 'EVENT', '2026-07-22 09:00:00'),
    (5, 3, 'USE', 2500, 'CONVERSION', '2026-07-24 09:30:00');


-- ---------------------------------------------------------------------
-- 14. user_random_box_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO user_random_box_tbl (
    user_random_box_id,
    user_id,
    issue_reason,
    source_id,
    target_account_id,
    box_status,
    reward_point,
    issued_at,
    opened_at
)
VALUES
    -- 1번 사용자: 과거 출석으로 받은 랜덤박스를 개봉하여 500P 획득
    (1, 1, 'ATTENDANCE', 1, NULL, 'OPENED', 500,
     '2026-07-18 09:00:00', '2026-07-18 09:05:00'),

    (2, 2, 'ATTENDANCE', 3, NULL, 'OPENED', 1000,
     '2026-07-19 09:00:00', '2026-07-19 09:05:00'),

    (3, 2, 'EVENT', 2, NULL, 'UNOPENED', NULL,
     '2026-07-24 09:10:00', NULL),

    (4, 3, 'ATTENDANCE', 5, NULL, 'OPENED', 300,
     '2026-07-20 09:00:00', '2026-07-20 09:05:00'),

    (5, 1, 'FEED_SHARE', 1, NULL, 'UNOPENED', NULL,
     '2026-07-24 10:00:00', NULL),

    (6, 1, 'TRANSFER', 2, 2, 'UNOPENED', NULL,
     '2026-07-24 11:00:00', NULL);


-- ---------------------------------------------------------------------
-- 15. attendance_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO attendance_tbl (
    attendance_id,
    user_id,
    attendance_date
)
VALUES
    (1, 1, '2026-07-22'),
    (2, 1, '2026-07-23'),
    (3, 2, '2026-07-22'),
    (4, 2, '2026-07-24'),
    (5, 3, '2026-07-23'),
    (6, 3, '2026-07-24');


-- ---------------------------------------------------------------------
-- 16. point_conversion_history_tbl (4건)
-- ---------------------------------------------------------------------
INSERT INTO point_conversion_history_tbl (
    point_conversion_id,
    user_id,
    point_wallet_id,
    wallet_id,
    converted_point,
    converted_at
)
VALUES
    (1, 2, 2, 2, 500, '2026-07-20 12:00:00'),
    (2, 2, 2, 2, 1000, '2026-07-21 12:00:00'),
    (3, 3, 3, 3, 500, '2026-07-22 12:00:00'),
    (4, 3, 3, 3, 1000, '2026-07-23 12:00:00');
-- ---------------------------------------------------------------------
-- 17. spending_analysis_tbl (6건)
-- ---------------------------------------------------------------------

-- # INSERT INTO spending_analysis_tbl (spending_analysis_id,
-- #                                    user_id,
-- #                                    analysis_period,
-- #                                    representative_category_id,
-- #                                    ai_title,
-- #                                    ai_analysis_summary,
-- #                                    created_at)
-- 시연용으로 잠시 빼두겠습니다.
-- # INSERT INTO spending_analysis_tbl (spending_analysis_id,
-- #                                    user_id,
-- #                                    analysis_period,
-- #                                    representative_category_id,
-- #                                    ai_title,
-- #                                    ai_analysis_summary,
-- #                                    created_at)
-- # VALUES (1, 1, 1, 4, '한 달 온라인 쇼핑 탐험가', '최근 한 달 동안 온라인쇼핑 지출 비중이 가장 높고 자동차와 생활 지출이 뒤를 잇고 있습니다.',
-- #         '2026-07-01 00:00:00'),
-- #        (2, 1, 3, 10,
-- #         '여행에 진심인 소비자',
-- #         '최근 세 달 동안 여행 지출이 가장 높고 온라인쇼핑과 주거·통신 지출도 큰 편입니다.',
-- #         '2026-07-02 00:00:00'),
-- #        (3, 2, 1, 6,
-- #         '대중교통 마스터',
-- #         '교통비 비중이 높고 이동이 잦은 소비 패턴입니다.',
-- #         '2026-07-03 00:00:00'),
-- #        (4, 2, 12, 1,
-- #         '알뜰 식비 관리자',
-- #         '연간 식비가 안정적으로 관리되고 있습니다.',
-- #         '2026-07-04 00:00:00'),
-- #        (5, 3, 3, 2,
-- #         '커피와 함께하는 사람',
-- #         '카페와 간식 관련 결제가 많은 편입니다.',
-- #         '2026-07-05 00:00:00'),
-- #        (6, 3, 12, 6,
-- #         '움직이는 저축러',
-- #         '교통 지출과 저축이 균형을 이루고 있습니다.',
-- #         '2026-07-06 00:00:00'),
-- #        (7, 1, 12, 10,
-- #         '여행에 미친 지갑의 순례자',
-- #         '최근 12개월 동안 여행 지출 비중이 가장 높고, 주거·통신과 교육 지출도 큰 편입니다.',
-- #         '2026-08-03 09:00:00');
-- ---------------------------------------------------------------------
-- 18. spending_analysis_category_tbl (6건)
-- ---------------------------------------------------------------------
# 분석 시연용 주석처리
# INSERT INTO spending_analysis_category_tbl (analysis_category_id,
#                                             spending_analysis_id,
#                                             spending_category_id,
#                                             spending_amount,
#                                             spending_ratio,
#                                             transaction_count,
#                                             created_at)
# VALUES (1, 1, 4, 119000, 23.27, 1, '2026-07-01 00:05:00'),
#        (2, 1, 7, 72000, 14.08, 1, '2026-07-01 00:05:00'),
#        (7, 1, 3, 68400, 13.38, 1, '2026-07-01 00:05:00'),
#        (8, 1, 8, 55000, 10.75, 1, '2026-07-01 00:05:00'),
#        (9, 1, 12, 47000, 9.19, 1, '2026-07-01 00:05:00'),
#        (10, 1, 6, 43800, 8.56, 1, '2026-07-01 00:05:00'),
#        (11, 1, 5, 32900, 6.43, 1, '2026-07-01 00:05:00'),
#        (12, 1, 11, 28000, 5.48, 1, '2026-07-01 00:05:00'),
#        (13, 1, 13, 23500, 4.60, 1, '2026-07-01 00:05:00'),
#        (14, 1, 1, 21800, 4.26, 1, '2026-07-01 00:05:00'),
#        (3, 2, 10, 636000, 37.75, 3, '2026-07-02 00:05:00'),
#        (4, 2, 4, 261800, 15.54, 3, '2026-07-02 00:05:00'),
#        (5, 3, 6, 90000, 75.00, 30, '2026-07-03 00:05:00'),
#        (6, 3, 1, 30000, 25.00, 5, '2026-07-03 00:05:00'),
#        (15, 2, 8, 143000, 8.49, 2, '2026-07-02 00:05:00'),
#        (16, 2, 11, 123000, 7.30, 2, '2026-07-02 00:05:00'),
#        (17, 2, 3, 86700, 5.15, 2, '2026-07-02 00:05:00'),
#        (18, 2, 12, 85500, 5.08, 2, '2026-07-02 00:05:00'),
#        (19, 2, 5, 77900, 4.62, 2, '2026-07-02 00:05:00'),
#        (20, 2, 7, 72000, 4.27, 1, '2026-07-02 00:05:00'),
#        (21, 2, 6, 60600, 3.60, 2, '2026-07-02 00:05:00'),
#        (22, 2, 1, 55800, 3.31, 2, '2026-07-02 00:05:00'),
#        (23, 2, 9, 52000, 3.09, 1, '2026-07-02 00:05:00'),
#        (24, 2, 13, 23500, 1.39, 1, '2026-07-02 00:05:00'),
#        (25, 2, 2, 6900, 0.41, 1, '2026-07-02 00:05:00'),
#        (26, 7, 10, 636000, 17.10, 3, '2026-08-03 09:00:05'),
#        (27, 7, 8, 602000, 16.19, 4, '2026-08-03 09:00:05'),
#        (28, 7, 11, 592000, 15.92, 4, '2026-08-03 09:00:05'),
#        (29, 7, 4, 442700, 11.90, 5, '2026-08-03 09:00:05'),
#        (30, 7, 7, 315000, 8.47, 3, '2026-08-03 09:00:05'),
#        (31, 7, 13, 295500, 7.95, 4, '2026-08-03 09:00:05'),
#        (32, 7, 3, 209500, 5.63, 4, '2026-08-03 09:00:05'),
#        (33, 7, 1, 154300, 4.15, 5, '2026-08-03 09:00:05'),
#        (34, 7, 9, 152000, 4.09, 2, '2026-08-03 09:00:05'),
#        (35, 7, 12, 140500, 3.78, 3, '2026-08-03 09:00:05'),
#        (36, 7, 5, 77900, 2.09, 2, '2026-08-03 09:00:05'),
#        (37, 7, 6, 75100, 2.02, 3, '2026-08-03 09:00:05'),
#        (38, 7, 2, 26400, 0.71, 4, '2026-08-03 09:00:05');
-- ---------------------------------------------------------------------
-- 19. kb_card_product_tbl (4건)
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
-- 20. card_benefit_tbl (7건)
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
-- 21. card_recommendation_tbl (4건)
-- ---------------------------------------------------------------------
# 카드 추천 시연용 주석처리
# INSERT INTO card_recommendation_tbl
# (card_recommendation_id,
#  spending_analysis_id,
#  card_product_id,
#  recommendation_rank,
#  expected_benefit_amount,
#  ai_recommendation_summary,
#  created_at)
# VALUES
#     -- CREDIT
#     (1, 7, 2, 1, 27355,
#      '온라인쇼핑 지출 비중이 높은 소비패턴과 잘 맞는 카드입니다. 온라인 쇼핑 할인 혜택을 중심으로 가장 높은 예상 할인액을 받을 수 있어 신용카드 1위로 추천합니다.',
#      '2026-08-03 09:10:05'),
#     (2, 7, 1, 2, 2690,
#      '카페와 음식점 등 일상생활 영역을 자주 이용하는 소비자에게 잘 맞는 카드입니다. 생활 밀착형 혜택을 활용할 수 있어 신용카드 2위로 추천합니다.',
#      '2026-08-03 09:10:05'),
#
#     -- CHECK
#     (3, 7, 4, 1, 13600,
#      '자동차 관련 지출이 있는 소비패턴과 잘 맞는 체크카드입니다. 자동차 관련 가맹점 할인 혜택을 활용할 수 있어 체크카드 1위로 추천합니다.',
#      '2026-08-03 09:10:05'),
#     (4, 7, 3, 2, 6560,
#      '교통과 카페 등 생활 영역의 소비에 혜택을 받을 수 있는 체크카드입니다. 일상적인 이동과 카페 이용 패턴을 고려해 체크카드 2위로 추천합니다.',
#      '2026-08-03 09:10:05');
-- ---------------------------------------------------------------------
-- 22. card_recommendation_detail_tbl (7건)
-- ---------------------------------------------------------------------
# 소비분석 용 주석처리
# INSERT INTO card_recommendation_detail_tbl
# (card_recommendation_detail_id,
#  card_recommendation_id,
#  card_benefit_id,
#  eligible_spending_amount,
#  eligible_transaction_count,
#  eligible_month_count,
#  expected_benefit_amount,
#  created_at)
# VALUES
#     -- card_recommendation_id = 1
#     -- KB국민 톡톡O 카드
#     (1, 1, 3, 261800, 3, 3, 26180, '2026-08-03 09:10:10'),
#     (2, 1, 4, 23500, 1, 1, 1175, '2026-08-03 09:10:10'),
#
#     -- card_recommendation_id = 2
#     -- KB국민 My WE:SH 카드
#     (3, 2, 1, 6900, 1, 1, 690, '2026-08-03 09:10:10'),
#     (4, 2, 2, 55800, 2, 2, 2000, '2026-08-03 09:10:10'),
#
#     -- card_recommendation_id = 3
#     -- KB국민 직장인보너스체크카드
#     (5, 3, 7, 315000, 3, 2, 13600, '2026-08-03 09:10:10'),
#
#     -- card_recommendation_id = 4
#     -- KB국민 노리2 체크카드
#     (6, 4, 5, 60600, 2, 2, 6060, '2026-08-03 09:10:10'),
#     (7, 4, 6, 6900, 1, 1, 500, '2026-08-03 09:10:10');

-- ---------------------------------------------------------------------
-- 23. kb_insurance_product_tbl (5건)
-- ---------------------------------------------------------------------
INSERT INTO kb_insurance_product_tbl (insurance_product_id,
                                      insurance_name,
                                      insurance_category,
                                      insurance_description,
                                      monthly_premium,
                                      insurance_image,
                                      application_url,
                                      created_at)
VALUES (1, 'KB 5.10.10 플러스 건강보험', '건강·실비',
        '질병과 상해에 따른 진단, 입원, 수술 위험을 종합적으로 대비하는 건강보험 상품입니다.',
        45000, 'kb_51010_health_insurance.png', 'kb_51010_health_apply.html', '2026-07-01 09:00:00'),

       (2, 'KB손보 실손의료비보장보험', '건강·실비',
        '질병 또는 상해로 실제 부담한 입원 및 통원 의료비를 보장하는 실손의료보험 상품입니다.',
        18000, 'kb_medical_expense_insurance.png', 'kb_medical_expense_apply.html', '2026-07-01 09:10:00'),

       (3, 'KB손보 간편가입 실손의료비보장보험', '건강·실비',
        '간편한 가입 절차를 통해 질병 또는 상해로 발생한 실제 의료비 부담을 대비하는 실손의료보험 상품입니다.',
        23000, 'kb_easy_medical_expense_insurance.png', 'kb_easy_medical_expense_apply.html', '2026-07-01 09:20:00'),

       (4, '해외여행보험', '여행자',
        '해외여행 중 발생할 수 있는 상해, 질병, 휴대품 손해 등의 위험을 대비하는 여행자보험입니다.',
        15000, 'kb_overseas_travel_insurance.png', 'kb_overseas_travel_apply.html', '2026-07-01 09:30:00'),

       (5, '해외장기체류(유학연수생)보험', '여행자',
        '해외 유학이나 연수 기간 중 발생할 수 있는 상해와 질병 등의 위험을 장기간 보장하는 보험입니다.',
        60000, 'kb_study_abroad_insurance.png', 'kb_study_abroad_apply.html', '2026-07-01 09:40:00'),

       (6, '해외장기체류(출장주재원)보험', '여행자',
        '해외 출장 또는 주재 기간 중 발생할 수 있는 상해와 질병 등의 위험을 장기간 보장하는 보험입니다.',
        70000, 'kb_overseas_worker_insurance.png', 'kb_overseas_worker_apply.html', '2026-07-01 09:50:00'),

       (7, 'KB자동차보험', '운전자',
        '자동차 사고로 인한 대인, 대물 및 차량 관련 손해를 대비하는 자동차보험 상품입니다.',
        65000, 'kb_auto_insurance.png', 'kb_auto_apply.html', '2026-07-01 10:00:00'),

       (8, 'KB 플러스 운전자 상해보험', '운전자',
        '운전 중 발생할 수 있는 상해와 교통사고 처리 비용 등 운전자 관련 위험을 대비하는 보험입니다.',
        15000, 'kb_driver_injury_insurance.png', 'kb_driver_injury_apply.html', '2026-07-01 10:10:00'),

       (9, 'KB The 건강한 치아보험', '치아',
        '충치 치료, 보철 치료 등 치과 진료로 발생할 수 있는 비용 부담을 대비하는 치아보험 상품입니다.',
        25000, 'kb_dental_insurance.png', 'kb_dental_apply.html', '2026-07-01 10:20:00'),

       (10, 'KB 금쪽같은 펫보험(강아지)', '펫',
        '강아지의 질병과 상해로 인한 동물병원 치료비 등의 부담을 대비하는 반려동물보험입니다.',
        35000, 'kb_pet_dog_insurance.png', 'kb_pet_dog_apply.html', '2026-07-01 10:30:00'),

       (11, 'KB 금쪽같은 펫보험(고양이)', '펫',
        '고양이의 질병과 상해로 인한 동물병원 치료비 등의 부담을 대비하는 반려동물보험입니다.',
        30000, 'kb_pet_cat_insurance.png', 'kb_pet_cat_apply.html', '2026-07-01 10:40:00');


-- ---------------------------------------------------------------------
-- 24. kb_insurance_coverage_tbl (12건)
-- ---------------------------------------------------------------------
INSERT INTO kb_insurance_coverage_tbl (insurance_coverage_id,
                                       insurance_product_id,
                                       coverage_name,
                                       coverage_amount,
                                       coverage_description,
                                       coverage_limit,
                                       created_at)
VALUES (1, 1, '질병 진단비', 30000000, '약관에서 정한 주요 질병으로 진단 확정된 경우 진단비를 지급합니다.', '최초 1회', '2026-07-01 11:00:00'),
       (2, 1, '질병·상해 수술비', 2000000, '질병 또는 상해로 약관에서 정한 수술을 받은 경우 수술비를 지급합니다.', '수술 1회당', '2026-07-01 11:05:00'),

       (3, 2, '입원 의료비', 50000000, '질병 또는 상해로 입원 치료 시 실제 부담한 의료비를 약관에 따라 보장합니다.', '연간 한도', '2026-07-01 11:10:00'),
       (4, 2, '통원 의료비', 30000000, '외래 진료 및 처방 조제 시 실제 부담한 의료비를 약관에 따라 보장합니다.', '연간 한도', '2026-07-01 11:15:00'),

       (5, 3, '간편가입 입원 의료비', 50000000, '간편가입 대상자가 입원 치료 시 실제 부담한 의료비를 약관에 따라 보장합니다.', '연간 한도', '2026-07-01 11:20:00'),
       (6, 3, '간편가입 통원 의료비', 30000000, '간편가입 대상자가 통원 치료 시 실제 부담한 의료비를 약관에 따라 보장합니다.', '연간 한도', '2026-07-01 11:25:00'),

       (7, 4, '해외여행 중 상해 의료비', 30000000, '해외여행 중 상해로 치료를 받은 경우 의료비를 약관에 따라 보장합니다.', '여행 기간 중', '2026-07-01 11:30:00'),
       (8, 4, '휴대품 손해', 1000000, '해외여행 중 휴대품의 도난 또는 파손으로 발생한 손해를 보장합니다.', '여행 기간 중', '2026-07-01 11:35:00'),

       (9, 5, '유학·연수 중 상해 의료비', 50000000, '해외 유학 또는 연수 중 상해로 발생한 의료비를 약관에 따라 보장합니다.', '보험기간 중', '2026-07-01 11:40:00'),
       (10, 5, '유학·연수 중 질병 의료비', 50000000, '해외 유학 또는 연수 중 질병으로 발생한 의료비를 약관에 따라 보장합니다.', '보험기간 중',
        '2026-07-01 11:45:00'),

       (11, 6, '출장·주재 중 상해 의료비', 50000000, '해외 출장 또는 주재 중 상해로 발생한 의료비를 약관에 따라 보장합니다.', '보험기간 중', '2026-07-01 11:50:00'),
       (12, 6, '출장·주재 중 질병 의료비', 50000000, '해외 출장 또는 주재 중 질병으로 발생한 의료비를 약관에 따라 보장합니다.', '보험기간 중',
        '2026-07-01 11:55:00'),

       (13, 7, '대인배상', 100000000, '자동차 사고로 타인의 신체에 피해가 발생한 경우 손해를 보장합니다.', '사고당', '2026-07-01 12:00:00'),
       (14, 7, '대물배상', 200000000, '자동차 사고로 타인의 차량이나 재산에 피해가 발생한 경우 손해를 보장합니다.', '사고당', '2026-07-01 12:05:00'),

       (15, 8, '교통사고 처리 지원금', 50000000, '운전 중 교통사고로 형사합의 비용 등이 발생한 경우 약관에 따라 지원합니다.', '사고당', '2026-07-01 12:10:00'),
       (16, 8, '운전자 상해 보장', 30000000, '교통사고로 운전자 본인에게 상해가 발생한 경우 약관에 따라 보장합니다.', '사고당', '2026-07-01 12:15:00'),

       (17, 9, '충전 치료비', 300000, '충치 등으로 충전 치료를 받은 경우 약관에서 정한 금액을 지급합니다.', '치아 1개당', '2026-07-01 12:20:00'),
       (18, 9, '보철 치료비', 1000000, '임플란트, 브리지 등 보철 치료를 받은 경우 약관에서 정한 금액을 지급합니다.', '치아 1개당', '2026-07-01 12:25:00'),

       (19, 10, '강아지 질병·상해 치료비', 10000000, '강아지가 질병 또는 상해로 동물병원 치료를 받은 경우 비용을 보장합니다.', '연간 한도', '2026-07-01 12:30:00'),
       (20, 10, '강아지 수술비', 2000000, '강아지가 질병 또는 상해로 수술을 받은 경우 비용을 보장합니다.', '수술 1회당', '2026-07-01 12:35:00'),

       (21, 11, '고양이 질병·상해 치료비', 10000000, '고양이가 질병 또는 상해로 동물병원 치료를 받은 경우 비용을 보장합니다.', '연간 한도', '2026-07-01 12:40:00'),
       (22, 11, '고양이 수술비', 2000000, '고양이가 질병 또는 상해로 수술을 받은 경우 비용을 보장합니다.', '수술 1회당', '2026-07-01 12:45:00');
-- ---------------------------------------------------------------------
-- 25. kb_insurance_recommendation_tbl (6건)
-- ---------------------------------------------------------------------
# 시연용 주석처리
# INSERT INTO kb_insurance_recommendation_tbl (insurance_recommendation_id,
#                                              spending_analysis_id,
#                                              insurance_product_id,
#                                              recommendation_reason,
#                                              ai_recommendation_summary,
#                                              created_at)
# VALUES (1, 1, 1,
#         '최근 3개월 내 병원 관련 소비가 확인되어 건강 위험에 대비할 수 있는 상품을 추천합니다.',
#         '최근 병원 관련 지출이 확인되어 예상치 못한 질병이나 치료비 부담에 대비할 필요가 있습니다. 건강 관련 보장을 폭넓게 준비하려는 소비패턴에 잘 맞는 상품입니다.',
#         '2026-07-01 13:00:00'),
#        (2, 1, 2,
#         '최근 3개월 내 병원 관련 소비가 확인되어 실제 의료비 부담을 줄일 수 있는 상품을 추천합니다.',
#         '병원 이용 지출이 확인된 만큼 실제 치료 과정에서 발생할 수 있는 의료비 부담을 줄이는 데 초점을 둔 상품입니다. 반복적인 의료비 지출에 대비하려는 경우 활용도가 높습니다.',
#         '2026-07-01 13:05:00'),
#        (3, 2, 9,
#         '최근 3개월 내 치과 관련 소비가 확인되어 치과 치료비에 대비할 수 있는 상품을 추천합니다.',
#         '치과 관련 소비가 확인되어 충치 치료나 보철 등 치과 진료비에 대한 대비 필요성이 있습니다. 치과 이용 패턴을 고려했을 때 관련 보장을 준비하는 데 적합한 상품입니다.',
#         '2026-07-02 13:00:00'),
#        (4, 3, 4,
#         '최근 3개월 내 여행 관련 소비가 확인되어 해외여행 중 발생할 수 있는 위험에 대비하는 상품을 추천합니다.',
#         '여행 관련 지출이 나타나는 소비패턴을 고려하면 여행 중 발생할 수 있는 사고나 의료비 위험을 함께 대비하는 것이 좋습니다. 여행 빈도가 있는 사용자에게 잘 맞는 상품입니다.',
#         '2026-07-03 13:00:00'),
#        (5, 4, 8,
#         '최근 3개월 내 자동차 관련 소비가 확인되어 운전자 사고와 상해 위험에 대비하는 상품을 추천합니다.',
#         '자동차 관련 소비가 확인되어 운전 중 발생할 수 있는 사고와 비용 위험을 대비할 필요가 있습니다. 차량 이용이 꾸준한 소비패턴에 적합한 운전자 보장 상품입니다.',
#         '2026-07-04 13:00:00'),
#        (6, 5, 10,
#         '최근 3개월 내 반려동물 관련 소비가 확인되어 강아지의 질병과 상해 치료비에 대비하는 상품을 추천합니다.',
#         '반려동물 관련 지출이 확인되어 갑작스러운 질병이나 상해로 발생하는 동물병원 비용에 대비할 필요가 있습니다. 강아지 의료비 부담을 줄이는 데 초점을 둔 상품입니다.',
#         '2026-07-05 13:00:00');

-- ---------------------------------------------------------------------
-- 26. friend_request_tbl (6건)
-- ---------------------------------------------------------------------
-- INSERT INTO friend_request_tbl (request_id,
--                                 requester_id,
--                                 receiver_id,
--                                 status,
--                                 created_at,
--                                 updated_at)
-- VALUES (1, 1, 2, 'ACCEPT', '2026-07-10 10:00:00', '2026-07-10 10:05:00'),
--        (2, 1, 3, 'REQUEST', '2026-07-11 10:00:00', '2026-07-11 10:00:00'),
--        (3, 2, 1, 'ACCEPT', '2026-07-12 10:00:00', '2026-07-12 10:05:00'),
--        (4, 2, 3, 'REJECT', '2026-07-13 10:00:00', '2026-07-13 10:05:00'),
--        (5, 3, 1, 'CANCEL', '2026-07-14 10:00:00', '2026-07-14 10:05:00'),
--        (6, 3, 2, 'REQUEST', '2026-07-15 10:00:00', '2026-07-15 10:00:00');

-- ---------------------------------------------------------------------
-- 27. friend_tbl (6건)
-- ---------------------------------------------------------------------
-- INSERT INTO friend_tbl (friend_id,
--                         user_id,
--                         friend_user_id,
--                         created_at)
-- VALUES (1, 1, 2, '2026-07-10 10:05:00'),
--        (2, 2, 1, '2026-07-10 10:05:00'),
--        (3, 1, 3, '2026-07-16 10:05:00'),
--        (4, 3, 1, '2026-07-16 10:05:00'),
--        (5, 2, 3, '2026-07-17 10:05:00'),
--        (6, 3, 2, '2026-07-17 10:05:00');

-- ---------------------------------------------------------------------
-- 28. settlement_tbl (8건)
-- ---------------------------------------------------------------------
-- INSERT INTO settlement_tbl (settlement_id, requester_id, title, content, total_amount,
--                             status, created_at, settlement_type, spending_category_id,
--                             last_reminder_date, completed_at)
-- VALUES (1, 1, '저녁 식사 정산', '저녁 식사 정산', 30000, 'REQUEST', '2026-07-20 19:00:00', 'EQUAL', 1, NULL, NULL),
--        (2, 2, '카페 모임 정산', '카페 모임 정산', 24000, 'COMPLETE', '2026-07-21 15:00:00', 'EQUAL', 2, '2026-07-21 17:00:00',
--         '2026-07-21 18:00:00'),
--        (3, 3, '택시비 정산', '택시비 정산', 18000, 'CANCEL', '2026-07-22 23:00:00', 'UNEQUAL', 6, NULL, NULL),
-- (4, 2, '저녁 식사 더치페이', '저녁 식사 더치페이', 18000, 'COMPLETE', '2026-08-08 20:00:00', 'EQUAL', 1, NULL, '2026-08-08 20:10:00'),
-- (5, 3, '카페 모임 더치페이', '카페 모임 더치페이', 8000, 'COMPLETE', '2026-07-24 16:30:00', 'EQUAL', 2, NULL, '2026-07-24 16:40:00'),
-- (6, 2, '택시비 더치페이', '택시비 더치페이', 14000, 'COMPLETE', '2026-06-21 00:10:00', 'EQUAL', 6, NULL, '2026-06-21 00:20:00'),
-- (7, 3, '숙소비 더치페이', '숙소비 더치페이', 60000, 'COMPLETE', '2026-05-29 20:20:00', 'EQUAL', 10, NULL, '2026-05-29 20:30:00'),
-- (8, 2, '병원비 더치페이', '병원비 더치페이', 35000, 'COMPLETE', '2026-04-15 15:10:00', 'EQUAL', 16, NULL, '2026-04-15 15:20:00');

-- ---------------------------------------------------------------------
-- 29. settlement_member_tbl (6건)
-- ---------------------------------------------------------------------

-- INSERT INTO settlement_member_tbl (settlement_member_id, settlement_id, user_id,
--                                    amount, status, created_at, completed_at)
-- VALUES (1, 1, 2, 15000, 'REQUEST', '2026-07-20 19:01:00', NULL),
--        (2, 1, 3, 15000, 'REQUEST', '2026-07-20 19:01:00', NULL),
--        (3, 2, 1, 12000, 'COMPLETE', '2026-07-21 15:01:00', '2026-07-21 17:30:00'),
--        (4, 2, 3, 12000, 'COMPLETE', '2026-07-21 15:01:00', '2026-07-21 18:00:00'),
--        (5, 3, 1, 8000, 'CANCEL', '2026-07-22 23:01:00', NULL),
--        (6, 3, 2, 10000, 'CANCEL', '2026-07-22 23:01:00', NULL);

-- ---------------------------------------------------------------------
-- 30. notification_tbl (6건)
-- ---------------------------------------------------------------------

-- INSERT INTO notification_tbl (notification_id, receiver_id, sender_id,
--                               notification_type, target_id, status, created_at)
-- VALUES (1, 2, 1, 'FRIEND_REQUEST', 1, 'READ', '2026-07-10 10:00:00'),
--        (2, 3, 1, 'FRIEND_REQUEST', 2, 'UNREAD', '2026-07-11 10:00:00'),
--        (3, 2, 1, 'SETTLEMENT_REQUEST', 1, 'UNREAD', '2026-07-20 19:01:00'),
--        (4, 3, 2, 'COMMENT', 1, 'READ', '2026-07-21 20:00:00'),
--        (5, 1, 3, 'LIKE', 2, 'UNREAD', '2026-07-22 20:00:00'),
--        (6, 1, 2, 'SETTLEMENT_REQUEST', 2, 'READ', '2026-07-23 20:00:00');

-- ---------------------------------------------------------------------
-- 31. financial_transaction_tbl (6건)
-- ---------------------------------------------------------------------
INSERT INTO financial_transaction_tbl (transaction_id,
                                       parent_transaction_id,
                                       settlement_id,
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
VALUES (1, NULL, NULL, 1, NULL, 'CHARGE', 'ACCOUNT', 'WALLET', 'SUCCESS', 10000, NULL, NULL, '2026-07-20 09:00:00'),
       (2, NULL, NULL, 1, 2, 'TRANSFER', 'WALLET', 'ACCOUNT', 'SUCCESS', 8000, NULL, 1, '2026-07-20 10:00:00'),
       (3, NULL, NULL, 2, NULL, 'CHARGE', 'ACCOUNT', 'WALLET', 'SUCCESS', 12000, NULL, NULL, '2026-07-21 09:00:00'),
       (4, NULL, 1, 2, 1, 'SETTLEMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 15000, NULL, 1, '2026-07-21 18:00:00'),
       (5, NULL, NULL, 3, 1, 'TRANSFER', 'ACCOUNT', 'WALLET', 'SUCCESS', 20000, NULL, 10, '2026-07-22 11:00:00'),
       (6, NULL, NULL, 3, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 5000, '스타벅스 동성로점', 6, '2026-07-23 08:00:00'),
       (7, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        18500, '배달의민족', 1, '2026-08-01 08:10:00'),
       (8, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        24000, '동성로 한식당', 1, '2026-08-01 10:40:00'),
       (9, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        6200, '스타벅스 대구점', 2, '2026-08-01 12:10:00'),
       (10, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        5500, '투썸플레이스', 2, '2026-08-01 14:40:00'),
       (11, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        9800, 'CU 계명대점', 3, '2026-08-01 16:15:00'),
       (12, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        42900, '쿠팡', 4, '2026-08-01 20:30:00'),
       (13, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        14500, '카카오T', 6, '2026-08-01 22:10:00'),
       (14, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        65000, '스마일치과', 19, '2026-08-02 09:40:00'),
       (15, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        4900, '메가MGC커피', NULL, '2026-08-02 10:20:00'),
       (16, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        12500, '한솥도시락', NULL, '2026-08-02 12:30:00'),
       (17, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        27600, '오늘의집', NULL, '2026-08-02 18:40:00'),
       (18, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        18000, '교보문고', NULL, '2026-08-02 20:10:00'),
       (19, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 78500, '무신사', 4, '2026-07-02 19:10:00'),
       (20, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 68400, '이마트 월배점', 3, '2026-06-29 18:20:00'),
       (21, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 32900, '올리브영 동성로점', 5, '2026-06-26 16:40:00'),
       (22, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 55000, 'SKT 통신요금', 8, '2026-06-23 09:00:00'),
       (23, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 72000, 'S-OIL 대구주유소', 7, '2026-06-20 14:15:00'),
       (24, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 119000, '네이버쇼핑', 4, '2026-06-17 21:05:00'),
       (25, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 43800, '코레일 동대구역', 6, '2026-06-14 07:30:00'),
       (26, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 28000, '교보문고 대구점', 11, '2026-06-11 17:50:00'),
       (27, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 47000, '24시 동물병원', 12, '2026-06-08 11:20:00'),
       (28, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 23500, '계명내과', 16, '2026-06-05 10:10:00'),
       (29, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 21800, '배달의민족', 1, '2026-06-02 20:35:00'),
       (30, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 6900, '스타벅스 성서점', 2, '2026-05-30 13:10:00'),
       (31, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 64300, '쿠팡', 4, '2026-05-27 22:15:00'),
       (32, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 18300, '다이소 계명대점', 3, '2026-05-24 15:40:00'),
       (33, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 45000, '준오헤어 대구점', 5, '2026-05-21 14:00:00'),
       (34, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 88000, '아파트 관리비', 8, '2026-05-18 08:30:00'),
       (35, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 52000, 'KB손해보험', 9, '2026-05-15 09:00:00'),
       (36, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 126000, '야놀자', 10, '2026-05-12 19:25:00'),
       (37, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 95000, '대구컴퓨터학원', 11, '2026-05-10 18:00:00'),
       (38, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 38500, '펫프렌즈', 12, '2026-05-08 12:45:00'),
       (39, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 34000, '동성로 파스타집', 1, '2026-05-06 19:40:00'),
       (40, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 16800, '카카오T', 6, '2026-05-04 23:10:00'),

       -- 최근 3개월 범위 밖, 최근 12개월 범위 안: 16건
       (41, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 298000, '제주항공', 10, '2026-04-22 10:25:00'),
       (42, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 212000, '신라스테이 제주', 10, '2026-04-18 16:30:00'),
       (43, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 420000, '월세 자동이체', 8, '2026-03-25 09:00:00'),
       (44, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 39000, 'KT 인터넷', 8, '2026-03-10 09:00:00'),
       (45, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 165000, '오토큐 성서점', 7, '2026-02-22 13:35:00'),
       (46, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 78000, 'GS칼텍스', 7, '2026-02-14 17:20:00'),
       (47, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 380000, '계명대학교 등록금', 11, '2026-01-28 11:00:00'),
       (48, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 89000, '인프런 온라인강의', 11, '2026-01-12 20:10:00'),
       (49, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 138000, '현대백화점 대구점', 4, '2025-12-24 18:50:00'),
       (50, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 113000, '홈플러스 성서점', 3, '2025-12-03 19:10:00'),
       (51, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 87000, '대구정형외과', 17, '2025-11-20 15:20:00'),
       (52, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 120000, '스마일치과', 19, '2025-11-05 11:40:00'),
       (53, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 55000, '몽글몽글 펫살롱', 12, '2025-10-18 14:25:00'),
       (54, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 100000, 'KB국민은행 적금', 9, '2025-09-27 09:30:00'),
       (55, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 56000, '수성못 한식당', 1, '2025-09-11 19:15:00'),
       (56, NULL, NULL, 1, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS', 7800, '블루보틀 대구점', 2, '2025-08-16 10:40:00'),
-- 회원 2 식비 소비내역 추가 (최근 1개월 이내) -> 회원 테이블 2는 식비만 결제할 것임
       -- 너무어지럽다.
    (57, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    12500, '한솥도시락 동성로점', 1, '2026-08-09 12:20:00'),

(58, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    21800, '배달의민족', 1, '2026-08-07 19:30:00'),

(59, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    9500, '김밥천국 대구점', 1, '2026-08-05 13:10:00'),

(60, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    32000, '동성로 한식당', 1, '2026-08-03 18:40:00'),

(61, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    17800, '맥도날드 대구점', 1, '2026-08-01 12:15:00'),

(62, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    26500, '배달의민족', 1, '2026-07-30 20:05:00'),

(63, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    14000, '홍콩반점 대구점', 1, '2026-07-28 13:25:00'),

(64, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    38500, '아웃백 대구점', 1, '2026-07-25 19:10:00'),

(65, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    11800, '한솥도시락 계명대점', 1, '2026-07-22 12:35:00'),

(66, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    22900, '요기요', 1, '2026-07-19 20:15:00'),

(67, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    16500, '본죽 대구점', 1, '2026-07-15 11:50:00'),

(68, NULL, NULL, 2, NULL, 'PAYMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
    28700, '배달의민족', 1, '2026-07-11 19:40:00'),
-- 식비 : 친구에게 식사비 송금
       (69, NULL, NULL, 1, 2, 'TRANSFER', 'WALLET', 'ACCOUNT', 'SUCCESS',
        22000, NULL, 1, '2026-08-03 19:20:00'),

-- 카페 : 카페 비용 송금
       (70, NULL, NULL, 1, 3, 'TRANSFER', 'WALLET', 'ACCOUNT', 'SUCCESS',
        9500, NULL, 2, '2026-07-27 15:10:00'),

-- 교통 : 택시비 송금
       (71, NULL, NULL, 1, 2, 'TRANSFER', 'WALLET', 'ACCOUNT', 'SUCCESS',
        32000, NULL, 6, '2026-07-18 23:20:00'),

-- 여행 : 여행 경비 송금
       (72, NULL, NULL, 1, 3, 'TRANSFER', 'ACCOUNT', 'WALLET', 'SUCCESS',
        75000, NULL, 10, '2026-06-30 10:30:00'),

-- 교육 : 강의비 송금
       (73, NULL, NULL, 1, 2, 'TRANSFER', 'WALLET', 'ACCOUNT', 'SUCCESS',
        45000, NULL, 11, '2026-06-16 18:10:00'),


-- ---------------------------------------------------------
-- SETTLEMENT 5건
-- ---------------------------------------------------------

-- 식비 : 저녁 식사 더치페이
       (74, NULL, 4, 1, 2, 'SETTLEMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        18000, NULL, 1, '2026-08-08 20:10:00'),

-- 카페 : 카페 모임 더치페이
       (75, NULL, 5, 1, 3, 'SETTLEMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        8000, NULL, 2, '2026-07-24 16:40:00'),

-- 교통 : 택시비 더치페이
       (76, NULL, 6, 1, 2, 'SETTLEMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        14000, NULL, 6, '2026-06-21 00:20:00'),

-- 여행 : 숙소비 더치페이
       (77, NULL, 7, 1, 3, 'SETTLEMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        60000, NULL, 10, '2026-05-29 20:30:00'),

-- 병원(내과) : 병원비 더치페이
       (78, NULL, 8, 1, 2, 'SETTLEMENT', 'WALLET', 'ACCOUNT', 'SUCCESS',
        35000, NULL, 16, '2026-04-15 15:20:00');


-- ---------------------------------------------------------------------
-- 32. account_dummy_tbl (6건)
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
        -- 커스텀 카드 진행 시 필요한 국민 은행 계좌
 --        (7, 4, '004', '94650301429776', '테스트회원3', 100000, '$2y$10$du1EXjznqV1UChQm4Lc20eULZzTo8VtgPmKSotjgnDXkYmBQzjrzK',
--         '2026-07-03 11:30:00');
-- ---------------------------------------------------------------------
-- 33. account_transaction_tbl (6건)
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
-- 34. wallet_transaction_tbl (6건)
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
-- 35. card_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO card_tbl (card_num, expiry_date, cvv, card_password, card_img_file_name, card_name)
VALUES 
('9445-4100-1111-2222', '12/28', '777', '1234', '07986_img.png', '노리2 체크카드 (Play)'),
('4579-7300-3333-4444', '11/29', '888', '1234', '09137_img.png', 'KB국민 마이 위시(My WE:SH) 카드'),
('9445-4300-5555-6666', '10/30', '999', '1234', '09106_img.png', 'KB국민 다담카드');


-- ---------------------------------------------------------------------
-- 38. receipt_memo_tbl (6건)
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
-- 39. feed_tbl (6건)
-- ---------------------------------------------------------------------
-- INSERT INTO feed_tbl (feed_id,
--                       user_id,
--                       target_id,
--                       feed_status,
--                       feed_type,
--                       content,
--                       visibility,
--                       created_at,
--                       updated_at)
-- VALUES (10, 1, 1, 'ACTIVE', 'PAYMENT', '지갑 충전 완료', 'PUBLIC', '2026-07-20 09:05:00', '2026-07-20 09:05:00'),
--        (12, 1, 2, 'ACTIVE', 'TRANSFER', '친구에게 송금', 'FRIEND', '2026-07-20 10:05:00', '2026-07-20 10:05:00'),
--        (13, 2, 3, 'ACTIVE', 'PAYMENT', '지갑 충전 완료', 'PRIVATE', '2026-07-21 09:05:00', '2026-07-21 09:05:00'),
--        (14, 2, 4, 'ACTIVE', 'SETTLEMENT', '정산 완료', 'FRIEND', '2026-07-21 18:05:00', '2026-07-21 18:05:00'),
--        (15, 3, 5, 'ACTIVE', 'TRANSFER', '여행비 송금', 'PUBLIC', '2026-07-22 11:05:00', '2026-07-22 11:05:00'),
--        (16, 1, 6, 'ACTIVE', 'CARD', '내 커스텀 카드 자랑하기', 'PUBLIC', '2026-07-23 08:05:00', '2026-07-23 08:05:00'),
--        (17, 1, 5, 'ACTIVE', 'EVENT', '7월 출석 이벤트 달성', 'PUBLIC', '2026-07-22 11:05:00', '2026-07-22 11:05:00');
-- ---------------------------------------------------------------------
-- 40. feed_image_tbl (6건)
-- ---------------------------------------------------------------------
-- INSERT INTO feed_image_tbl (image_id,
--                             feed_id,
--                             image_name)
-- VALUES (1, 1, 'feed_1_1.png'),
--        (2, 2, 'feed_2_1.png'),
--        (3, 3, 'feed_3_1.png'),
--        (4, 4, 'feed_4_1.png'),
--        (5, 5, 'feed_5_1.png'),
--        (6, 6, 'feed_6_1.png');

-- ---------------------------------------------------------------------
-- 41. feed_like_tbl (6건)
-- ---------------------------------------------------------------------
-- INSERT INTO feed_like_tbl (like_id,
--                            feed_id,
--                            user_id,
--                            created_at)
-- VALUES (1, 1, 2, '2026-07-20 09:10:00'),
--        (2, 1, 3, '2026-07-20 09:11:00'),
--        (3, 2, 2, '2026-07-20 10:10:00'),
--        (4, 4, 1, '2026-07-21 18:10:00'),
--        (5, 5, 1, '2026-07-22 11:10:00'),
--        (6, 5, 2, '2026-07-22 11:11:00');

-- ---------------------------------------------------------------------
-- 42. feed_comment_tbl (6건)
-- ---------------------------------------------------------------------
-- INSERT INTO feed_comment_tbl (comment_id,
--                               feed_id,
--                               user_id,
--                               content,
--                               created_at,
--                               updated_at)
-- VALUES (1, 1, 2, '충전 축하해요', '2026-07-20 09:15:00', '2026-07-20 09:15:00'),
--        (2, 2, 2, '송금 확인했어요', '2026-07-20 10:15:00', '2026-07-20 10:15:00'),
--        (3, 3, 1, '좋은 습관이에요', '2026-07-21 09:15:00', '2026-07-21 09:15:00'),
--        (4, 4, 3, '정산 고마워요', '2026-07-21 18:15:00', '2026-07-21 18:15:00'),
--        (5, 5, 1, '즐거운 여행!', '2026-07-22 11:15:00', '2026-07-22 11:15:00'),
--        (6, 6, 2, '교통비 절약!', '2026-07-23 08:15:00', '2026-07-23 08:15:00');



-- ---------------------------------------------------------------------
-- 46. card_application_history_tbl (6건)
-- ---------------------------------------------------------------------
-- INSERT INTO card_application_history_tbl (apply_id,
--                                           user_id,
--                                           custom_image_name,
--                                           custom_card_id,
--                                           card_code,
--                                           card_name,
--                                           created_at)
-- VALUES (1, 1, 1, 1, '나만의 옐로우카드', 'REQUEST', '2026-07-20 16:00:00'),
--        (2, 1, 2, 2, '선셋 카드', 'ISSUED', '2026-07-20 16:10:00' ),
--        (3, 2, 3, 3, '노을 카드', 'REQUEST', '2026-07-21 16:00:00');

-- ---------------------------------------------------------------------
-- 47. event_tbl (20건)
-- ---------------------------------------------------------------------
INSERT INTO event_tbl (event_id,
                       event_name,
                       event_desc,
                       event_type,
                       event_category,
                       event_status,
                       event_img_name,
                       event_target,
                       event_level,
                       event_daily_limit_count,
                       start_at,
                       end_at,
                       created_at)
VALUES
(1,
 '매일매일 출석체크',
 '매일 앱에 방문하여 출석 도장을 찍고 혜택을 받아보세요!',
 'ATTENDANCE',
 'ATTENDANCE',
 'OPEN',
 'attendance_event.png',
 10,
 3,
 1,
 '2026-01-01 00:00:00',
 '2026-12-01 00:00:00',
 '2026-01-01 00:00:00'),
(2,
 '초보자를 위한 가이드 : 첫 피드 작성',
 '첫 일상 피드를 등록하고 웰컴 포인트를 받으세요!',
 'PERMANENT',
 'FEED',
 'OPEN',
 'attendance_event.png',
 1,
 1,
 1,
 '2026-07-01 00:00:00',
 '2026-12-01 00:00:00',
 '2026-07-01 00:00:00'),
(3,
 '나만의 스타일, 커스텀 카드 만들기',
 '내 취향대로 디자인하는 커스텀 카드를 신규 등록해 보세요.',
 'PERMANENT',
 'CARD',
 'OPEN',
 'attendance_event.png',
 1,
 1,
 1,
 '2026-07-15 00:00:00',
 '2026-12-01 00:00:00',
 '2026-07-01 00:00:00'),
 (4,
  '프로 피드러로 가는 길! 피드 3회 등록',
  '피드를 3개 등록하여 자유롭게 다른 사람들과 일상을 공유해 보세요.',
  'PERMANENT',
  'FEED',
  'OPEN',
  'feed.png',
  3,
  2,
  1,
  '2026-07-15 00:00:00',
  '2026-12-01 00:00:00',
  '2026-07-01 00:00:00'),
(5,
 '스마트한 소비 생활! 지갑 결제 사용해보기',
 '편의점, 카페 어디서나 편리한 지갑 결제를 이용하고 포인트를 받으세요.',
 'PERMANENT',
 'WALLET',
 'CLOSE',
 'wallet.png',
 1,
 1,
 1,
 '2026-07-01 00:00:00',
 '2026-12-01 00:00:00',
 '2026-07-01 00:00:00'),

(6,
 'N분의 1도 간편하게! 정산하기 체험',
 '친구들과의 모임 회비를 정산 기능을 통해 편리하게 요청하고 정산해 보세요.',
 'PERMANENT',
 'SETTLEMENT',
 'OPEN',
 'wallet.png',
 1,
 1,
 1,
 '2026-07-01 00:00:00',
 '2026-12-01 00:00:00',
 '2026-07-01 00:00:00'),
(7,
    '두근두근 대박 기회! 랜덤박스 열기',
    '랜덤박스를 열고 포인트를 받아가세요!',
    'LUCKYDRAW',
    'RANDOMBOX',
    'OPEN',
    'surprise_box.png',
    1,
    1,
    1,
    '2026-07-20 10:00:00',
    '2026-12-01 00:00:00',
    '2026-07-01 00:00:00'),
(8,
    '자산 관리의 시작, 소비패턴 분석',
    '소비패턴 분석 서비스를 체험하고 소비 분석 리포트를 받아보세요.',
    'PERMANENT',
    'ANALYSIS',
    'OPEN',
    'analysis.png',
    1,
    1,
    1,
    '2026-07-01 00:00:00',
    '2026-12-01 00:00:00',
    '2026-07-01 00:00:00');

-- ---------------------------------------------------------------------
-- 48. event_reward_tbl (5건)
-- ---------------------------------------------------------------------
INSERT INTO event_reward_tbl (
    reward_id,
    event_id,
    reward_point,
    reward_exp
)
VALUES
    (1, 1, 100, 10),   -- 이벤트 출석
    (2, 2, 200, 50),   -- 첫 피드
    (3, 3, 300, 60),   -- 최초 커스텀 카드
    (4, 4, 100, 50),   -- 피드 3회
    (5, 5, 200, 50),   -- 지갑 결제
    (6, 6, 100, 50),   -- 정산
    (7, 7, 50, 10),    -- 랜덤박스
    (8, 8, 500, 100);  -- 소비패턴 분석

-- ---------------------------------------------------------------------
-- 49. event_participation_tbl (5건)
-- ---------------------------------------------------------------------
-- INSERT INTO event_participation_tbl (participation_id,
--                                      event_id,
--                                      user_id,
--                                      participated_at)
-- VALUES (1, 1, 1, '2026-07-01 08:00:00'),
-- 	(2, 2, 1, '2026-07-01 08:20:00'),
-- 	(3, 2, 2, '2026-08-01 08:00:00'),
-- 	(4, 3, 1, '2026-07-05 08:10:00'),
-- 	(5, 1, 2, '2026-08-01 08:20:00');

-- ---------------------------------------------------------------------
-- 51. event_reward_receive_tbl (4건)
-- ---------------------------------------------------------------------
-- INSERT INTO event_reward_receive_tbl (
--     recv_id,
--     event_id,
--     reward_id,
--     user_id,
--     received_at
-- )
-- VALUES
--     (2, 1, 1, 2, '2026-07-01 08:25:00'),
--     (3, 2, 2, 1, '2026-07-01 08:50:00'),
--     (4, 2, 2, 2, '2026-07-05 09:00:00'),
--     (5, 1, 1, 1, '2026-07-01 08:10:00');

-- ---------------------------------------------------------------------
-- 52. event_challenge_tbl (4건)
-- ---------------------------------------------------------------------
INSERT INTO event_challenge_tbl (challenge_id,
                                 challenge_name,
                                 reward_point,
                                 max_level,
                                 max_target,
                                 start_date,
                                 end_date,
                                 created_at)
VALUES (1, 'SUMMER SEASON 이벤트 챌린지', 5000, 5, 20, '2026-07-01 00:00:00', '2026-08-31 23:59:59', '2026-07-01 00:00:00');

-- ---------------------------------------------------------------------
-- 52-1. event_challenge_level_tbl (5건)
-- EXP는 차감하지 않는 누적 방식이므로 required_exp는 누적 기준값이다.
-- ---------------------------------------------------------------------
INSERT INTO event_challenge_level_tbl (challenge_level_id,
                                       challenge_id,
                                       level,
                                       required_exp,
                                       reward_point)
VALUES (1, 1, 1, 100, 1000),
       (2, 1, 2, 500, 1500),
       (3, 1, 3, 1000, 2000),
       (4, 1, 4, 1500, 2500),
       (5, 1, 5, 2000, 3000);

-- ---------------------------------------------------------------------
-- 53. event_challenge_user_tbl (6건)
-- ---------------------------------------------------------------------
-- 사용자 챌린지 데이터는 이벤트 메인 최초 조회 시 자동 생성된다.
-- INSERT INTO event_challenge_user_tbl (user_challenge_id,
--                                       user_id,
--                                       challenge_id,
--                                       current_level,
--                                       current_target,
--                                       exp,
--                                       status,
--                                       updated_at)
-- VALUES (1, 1, 1, 1, 0, 0, 'PROCESS', '2026-07-24 08:00:00');


-- ---------------------------------------------------------------------
-- 54. card_company_tbl (3건)
-- ---------------------------------------------------------------------
INSERT INTO card_company_tbl (card_company_code,
                              card_company_name)
VALUES ('KB', 'KB국민카드'),
       ('SH', '신한카드'),
       ('HN', '하나카드');

-- ---------------------------------------------------------------------
-- 55. linked_card_tbl (4건)
-- ---------------------------------------------------------------------
INSERT INTO linked_card_tbl (user_id, card_code, card_company_code, represent_yn, delete_yn)
VALUES 
(1, 1, 'KB', 'Y', 'N'), -- 1번 유저가 1번 실물 카드를 대표 카드로 연동 (노리2 Play)
(1, 2, 'KB', 'N', 'N'), -- 1번 유저가 2번 실물 카드를 일반 연동 (마이 위시)
(1, 3, 'KB', 'N', 'N'), -- 1번 유저가 3번 실물 카드를 일반 연동 (다담카드)
(2, 3, 'KB', 'Y', 'N'); -- 2번 유저가 3번 실물 카드를 대표 카드로 연동 (다담카드)

-- ---------------------------------------------------------------------
-- 57. merchant_category_mapping_tbl (21건)
-- AI 분류 저장 후 재활용 테이블
-- 오류율 5퍼센트 넘으면 삭제 한다.
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


-- ---------------------------------------------------------------------
-- 58. kb_insurance_category_match_tbl (11건)
-- 소비 카테고리<-> 보험 종류 매칭 규칙 저장 테이블
-- ex) 여행-> 여행자보험, 치아-> 치과보험
-- ---------------------------------------------------------------------

INSERT INTO kb_insurance_category_match_tbl
(insurance_product_id,
 spending_category_id,
 recommendation_reason,
 priority)
VALUES
    -- 병원 → 건강·실비
    (1, 13, '최근 병원 이용 내역을 바탕으로 건강 보장 상품을 추천합니다.', 1),
    (2, 13, '최근 병원 이용 내역을 바탕으로 실손의료비 보장 상품을 추천합니다.', 2),
    (3, 13, '최근 병원 이용 내역을 바탕으로 간편가입 실손 상품을 추천합니다.', 3),

    -- 여행 → 여행자보험
    (4, 10, '최근 여행 관련 소비가 있어 해외여행보험을 추천합니다.', 1),
    (5, 10, '최근 여행 관련 소비가 있어 장기체류 보험을 함께 추천합니다.', 2),
    (6, 10, '최근 여행 관련 소비가 있어 출장·주재원 보험을 함께 추천합니다.', 3),

    -- 자동차 → 자동차보험·운전자보험
    (7, 7, '최근 자동차 관련 소비가 있어 자동차보험을 추천합니다.', 1),
    (8, 7, '최근 자동차 관련 소비가 있어 운전자 상해보험을 추천합니다.', 2),

    -- 치과 → 치아보험
    (9, 19, '최근 치과 이용 내역이 있어 치아보험을 추천합니다.', 1),

    -- 반려동물 → 펫보험
    (10, 12, '최근 반려동물 관련 소비가 있어 강아지 펫보험을 추천합니다.', 1),
    (11, 12, '최근 반려동물 관련 소비가 있어 고양이 펫보험을 추천합니다.', 2);

COMMIT;

