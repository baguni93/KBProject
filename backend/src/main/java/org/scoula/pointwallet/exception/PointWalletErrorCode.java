package org.scoula.pointwallet.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointWalletErrorCode {

    // 400 Bad Request
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "요청값이 올바르지 않습니다."
    ),

    INVALID_POINT_AMOUNT(
            HttpStatus.BAD_REQUEST,
            "INVALID_POINT_AMOUNT",
            "포인트 금액은 0보다 커야 합니다."
    ),

    INVALID_TRANSACTION_TYPE(
            HttpStatus.BAD_REQUEST,
            "INVALID_TRANSACTION_TYPE",
            "올바르지 않은 포인트 거래 유형입니다."
    ),

    METHOD_NOT_ALLOWED(
            HttpStatus.METHOD_NOT_ALLOWED,
            "METHOD_NOT_ALLOWED",
            "지원하지 않는 HTTP 요청 방식입니다."
    ),

    UNSUPPORTED_MEDIA_TYPE(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "UNSUPPORTED_MEDIA_TYPE",
            "지원하지 않는 요청 형식입니다."
    ),

    // 404 Not Found
    POINT_WALLET_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "POINT_WALLET_NOT_FOUND",
            "포인트 지갑을 찾을 수 없습니다."
    ),

    WALLET_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "WALLET_NOT_FOUND",
            "전자지갑을 찾을 수 없습니다."
    ),

    RANDOM_BOX_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RANDOM_BOX_NOT_FOUND",
            "랜덤박스를 찾을 수 없습니다."
    ),

    // 409 Conflict
    ALREADY_ATTENDED(
            HttpStatus.CONFLICT,
            "ALREADY_ATTENDED",
            "오늘은 이미 출석을 완료했습니다."
    ),

    DUPLICATE_DATA(
            HttpStatus.CONFLICT,
            "DUPLICATE_DATA",
            "이미 처리된 요청입니다."
    ),



    RANDOM_BOX_ALREADY_ISSUED(
            HttpStatus.CONFLICT,
            "RANDOM_BOX_ALREADY_ISSUED",
            "이미 지급된 랜덤박스입니다."
    ),

    RANDOM_BOX_ALREADY_OPENED(
            HttpStatus.CONFLICT,
            "RANDOM_BOX_ALREADY_OPENED",
            "이미 개봉된 랜덤박스입니다."
    ),

    RANDOM_BOX_REVOKED(
            HttpStatus.CONFLICT,
            "RANDOM_BOX_REVOKED",
            "회수된 랜덤박스는 개봉할 수 없습니다."
    ),

    RANDOM_BOX_ALREADY_PROCESSED(
            HttpStatus.CONFLICT,
            "RANDOM_BOX_ALREADY_PROCESSED",
            "이미 처리된 랜덤박스입니다."
    ),

    RANDOM_BOX_DAILY_LIMIT_EXCEEDED(
            HttpStatus.CONFLICT,
            "RANDOM_BOX_DAILY_LIMIT_EXCEEDED",
            "오늘 받을 수 있는 랜덤박스의 최대 개수를 초과했습니다."
    ),

    INSUFFICIENT_POINT(
            HttpStatus.CONFLICT,
            "INSUFFICIENT_POINT",
            "보유 포인트가 부족합니다."
    ),

    WALLET_NOT_ACTIVE(
            HttpStatus.CONFLICT,
            "WALLET_NOT_ACTIVE",
            "전자지갑이 사용 가능한 상태가 아닙니다."
    ),



    // 500 Internal Server Error
    INTERNAL_PROCESS_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_PROCESS_ERROR",
            "요청을 처리하는 중 오류가 발생했습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;




}