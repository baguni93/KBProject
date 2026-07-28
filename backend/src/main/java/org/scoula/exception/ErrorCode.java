package org.scoula.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    DATA_ACCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "데이터 조회에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),

    // Feed
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "피드를 찾을 수 없습니다."),

    //Settlement
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "정산 요청을 찾을 수 없습니다."),
    SETTLEMENT_CAN_NOT_CANCEL(HttpStatus.NOT_FOUND, "S002", "정산 요청을 취소할 수 없습니다."),
    SETTLEMENT_ALREADY_PAYMENT(HttpStatus.NOT_FOUND, "S003", "이미 지불한 맴버입니다."),
    SETTLEMENT_CAN_NOT_REMINE(HttpStatus.NOT_FOUND, "S004", "리마인드 알림을 할 수 없습니다."),

    //Friend Request
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FR001", "친구 요청을 찾을 수 없습니다."),
    FRIEND_ALREADY_ACCEPT(HttpStatus.NOT_FOUND, "FR002", "이미 수락된 친구 요청입니다."),
    FRIEND_ALREADY_REJECT(HttpStatus.NOT_FOUND, "FR003", "이미 거절된 친구 요청입니다."),
    FRIEND_ALREADY_CANCEL(HttpStatus.NOT_FOUND, "FR004", "이미 취소된 친구 요청입니다."),
    FRIEND_REQUEST_ALREADY_EXISTS(HttpStatus.NOT_FOUND, "FR005", "이미 친구 요청이 존재합니다."),
    //Friend
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "친구를 찾을 수 없습니다."),

    // Wallet
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "W001", "잔액이 부족합니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
