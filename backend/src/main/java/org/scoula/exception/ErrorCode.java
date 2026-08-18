package org.scoula.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    DATA_ACCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "데이터 조회에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다."),

    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTH001", "로그인이 필요하거나 Access Token이 유효하지 않습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),

    // Feed
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "피드를 찾을 수 없습니다."),

    // Event
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EV001", "이벤트 또는 보상 정보를 찾을 수 없습니다."),
    EVENT_NOT_JOINED(HttpStatus.BAD_REQUEST, "EV002", "참여하지 않은 이벤트입니다."),
    EVENT_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "EV003", "이벤트 달성 조건을 충족하지 못했습니다."),
    EVENT_REWARD_ALREADY_RECEIVED(HttpStatus.CONFLICT, "EV004", "이미 수령한 이벤트 보상입니다."),
    EVENT_CHALLENGE_NOT_READY(HttpStatus.BAD_REQUEST, "EV005", "레벨 보상을 받을 수 있는 상태가 아닙니다."),

    //Settlement
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "정산 요청을 찾을 수 없습니다."),
    SETTLEMENT_CAN_NOT_CANCEL(HttpStatus.NOT_FOUND, "S002", "정산 요청을 취소할 수 없습니다."),
    SETTLEMENT_ALREADY_PAYMENT(HttpStatus.NOT_FOUND, "S003", "이미 지불한 맴버입니다."),
    SETTLEMENT_CAN_NOT_REMINE(HttpStatus.NOT_FOUND, "S004", "리마인드 알림을 할 수 없습니다."),
    SETTLEMENT_CAN_NOT_CREATE(HttpStatus.NOT_FOUND, "S005", "맴버 없이는 정산요청 할 수 없습니다."),
    SETTLEMENT_NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "S006", "정산 멤버를 찾을 수 없습니다."),

    //Friend Request
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FR001", "친구 요청을 찾을 수 없습니다."),
    FRIEND_ALREADY_ACCEPT(HttpStatus.NOT_FOUND, "FR002", "이미 수락된 친구 요청입니다."),
    FRIEND_ALREADY_REJECT(HttpStatus.NOT_FOUND, "FR003", "이미 거절된 친구 요청입니다."),
    FRIEND_ALREADY_CANCEL(HttpStatus.NOT_FOUND, "FR004", "이미 취소된 친구 요청입니다."),
    FRIEND_REQUEST_ALREADY_EXISTS(HttpStatus.NOT_FOUND, "FR005", "이미 친구 요청이 존재합니다."),
    //Friend
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "FD001", "친구를 찾을 수 없습니다."),

    //Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "댓글이 존재하지않습니다."),

    // Wallet
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "W001", "잔액이 부족합니다."),


    // Point Wallet / Random Box
    POINT_WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "PW001", "포인트 지갑을 찾을 수 없습니다."),
    INVALID_POINT_AMOUNT(HttpStatus.BAD_REQUEST, "PW002", "포인트 금액이 올바르지 않습니다."),
    INVALID_TRANSACTION_TYPE(HttpStatus.BAD_REQUEST, "PW003", "올바르지 않은 포인트 거래 유형입니다."),
    RANDOM_BOX_NOT_FOUND(HttpStatus.NOT_FOUND, "PW004", "랜덤박스를 찾을 수 없습니다."),
    ALREADY_ATTENDED(HttpStatus.CONFLICT, "PW005", "오늘은 이미 출석을 완료했습니다."),
    RANDOM_BOX_ALREADY_ISSUED(HttpStatus.CONFLICT, "PW006", "이미 지급된 랜덤박스입니다."),
    RANDOM_BOX_ALREADY_OPENED(HttpStatus.CONFLICT, "PW007", "이미 개봉된 랜덤박스입니다."),
    RANDOM_BOX_ALREADY_PROCESSED(HttpStatus.CONFLICT, "PW008", "이미 처리된 랜덤박스입니다."),
    RANDOM_BOX_DAILY_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "PW009", "오늘 받을 수 있는 랜덤박스의 최대 개수를 초과했습니다."),
    INSUFFICIENT_POINT(HttpStatus.CONFLICT, "PW010", "보유 포인트가 부족합니다."),
    POINT_CONVERSION_WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "PW011", "포인트 전환 대상 전자지갑을 찾을 수 없습니다."),
    POINT_CONVERSION_WALLET_NOT_ACTIVE(HttpStatus.CONFLICT, "PW012", "포인트 전환 대상 전자지갑이 사용 가능한 상태가 아닙니다."),
    POINT_WALLET_PROCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PW999", "포인트 지갑 요청을 처리하는 중 오류가 발생했습니다."),

    //Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "알림을 찾을 수 없습니다."),
    // Analysis
    INVALID_ANALYSIS_PERIOD(HttpStatus.BAD_REQUEST, "AN001", "분석 기간은 1개월, 3개월, 12개월 중 하나여야 합니다."),
    SPENDING_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "AN002", "존재하지 않는 소비 카테고리입니다."),
    CLASSIFICATION_TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "AN003", "분류할 수 있는 결제 거래를 찾을 수 없습니다."),
    ANALYSIS_TRANSACTION_COUNT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "AN004", "소비 분석을 위해 분류 완료된 결제 거래가 10건 이상 필요합니다."),
    ANALYSIS_TOTAL_AMOUNT_ZERO(HttpStatus.BAD_REQUEST, "AN005", "분석할 수 있는 소비 금액이 없습니다."),
    ANALYSIS_RESULT_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AN006", "소비 분석 결과 저장에 실패했습니다."),
    ANALYSIS_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "AN007", "소비 분석 결과를 찾을 수 없습니다."),
    ANALYSIS_AGREEMENT_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "AN008", "소비 분석 약관이 설정되어 있지 않습니다."),
    ANALYSIS_REQUIRED_AGREEMENT_MISSING(HttpStatus.BAD_REQUEST, "AN009", "필수 소비 분석 약관에 동의해야 합니다."),

    // Card Recommendation
    CARD_RECOMMENDATION_REQUIRES_12_MONTH_ANALYSIS(HttpStatus.BAD_REQUEST, "CR001", "카드 추천을 이용하려면 12개월 소비분석이 필요합니다."),
    CARD_RECOMMENDATION_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "CR002", "카드 추천에 사용할 소비분석 결과를 찾을 수 없습니다."),
    CARD_RECOMMENDATION_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "CR003", "추천할 수 있는 KB 카드 상품이 없습니다."),
    CARD_RECOMMENDATION_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CR004", "카드 추천 결과 저장에 실패했습니다."),
    CARD_RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CR005", "카드 추천 결과를 찾을 수 없습니다."),
    INVALID_CARD_RECOMMENDATION_OPTION(HttpStatus.BAD_REQUEST, "CR006", "카드 유형 또는 연회비 적용 옵션이 올바르지 않습니다."),

    // Insurance Recommendation
    INSURANCE_RECOMMENDATION_REQUIRES_12_MONTH_ANALYSIS(HttpStatus.BAD_REQUEST, "IR001", "보험 추천을 이용하려면 12개월 소비분석이 필요합니다."),
    INSURANCE_RECOMMENDATION_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IR002", "보험 추천 결과 저장에 실패했습니다."),
    INSURANCE_RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "IR003", "보험 추천 결과를 찾을 수 없습니다."),
    INSURANCE_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "IR004", "보험 상품을 찾을 수 없습니다."),

    //Custom Card
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "CC001", "계좌가 존재하지않습니다."),


    //VERIFICATION Account
    VERIFICATION_ACCOUNT_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "VA001", "계좌 인증번호 식별값이 필요합니다."),
    VERIFICATION_ACCOUNT_CODE_EMPTY(HttpStatus.NOT_FOUND, "VA002", "인증번호는 숫자 4자리로 입력해주세요."),
    VERIFICATION_ACCOUNT_CODE_NOT_REQUEST(HttpStatus.NOT_FOUND, "VA003", "계좌 인증 요청을 찾을 수 없습니다."),
    VERIFICATION_ACCOUNT_CODE_NOT_INCORRECT(HttpStatus.NOT_FOUND, "VA004", "계좌 인증번호가 일치하지 않습니다.");
    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
