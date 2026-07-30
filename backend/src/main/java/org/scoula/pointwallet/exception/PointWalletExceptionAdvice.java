package org.scoula.pointwallet.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Log4j2
@Order(0)
@RestControllerAdvice(
        basePackages = "org.scoula.pointwallet.controller"
)
public class PointWalletExceptionAdvice {

    /*
     * 포인트 지갑 도메인에서 직접 발생시킨 업무 예외 처리
     */
    @ExceptionHandler(PointWalletException.class)
    public ResponseEntity<PointWalletErrorResponse>
    handlePointWalletException(
            PointWalletException exception
    ) {
        PointWalletErrorCode errorCode =
                exception.getErrorCode();

        log.warn(
                "포인트 지갑 업무 예외 code={}, message={}",
                errorCode.getCode(),
                exception.getMessage()
        );

        PointWalletErrorResponse response =
                PointWalletErrorResponse.of(
                        errorCode,
                        exception.getMessage()
                );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /*
     * 요청 Body 누락 또는 JSON 문법 오류
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<PointWalletErrorResponse>
    handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {
        log.warn(
                "요청 Body 변환 실패: {}",
                exception.getMessage()
        );

        PointWalletErrorCode errorCode =
                PointWalletErrorCode.INVALID_REQUEST;

        PointWalletErrorResponse response =
                PointWalletErrorResponse.of(
                        errorCode,
                        "요청 Body가 없거나 JSON 형식이 올바르지 않습니다."
                );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /*
     * PathVariable 또는 Query Parameter 타입 불일치
     *
     * 예:
     * /api/random-boxes/abc/open
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<PointWalletErrorResponse>
    handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {
        log.warn(
                "요청값 타입 불일치 parameter={}, value={}",
                exception.getName(),
                exception.getValue()
        );

        PointWalletErrorCode errorCode =
                PointWalletErrorCode.INVALID_REQUEST;

        String message =
                exception.getName()
                        + " 값의 형식이 올바르지 않습니다.";

        PointWalletErrorResponse response =
                PointWalletErrorResponse.of(
                        errorCode,
                        message
                );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /*
     * 필수 Query Parameter 누락
     */
    @ExceptionHandler(
            MissingServletRequestParameterException.class
    )
    public ResponseEntity<PointWalletErrorResponse>
    handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception
    ) {
        log.warn(
                "필수 요청 파라미터 누락 parameter={}",
                exception.getParameterName()
        );

        PointWalletErrorCode errorCode =
                PointWalletErrorCode.INVALID_REQUEST;

        String message =
                "필수 요청 파라미터가 누락되었습니다: "
                        + exception.getParameterName();

        PointWalletErrorResponse response =
                PointWalletErrorResponse.of(
                        errorCode,
                        message
                );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /*
     * 지원하지 않는 HTTP Method
     *
     * 예:
     * GET /api/point-conversions
     */
    @ExceptionHandler(
            HttpRequestMethodNotSupportedException.class
    )
    public ResponseEntity<PointWalletErrorResponse>
    handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception
    ) {
        log.warn(
                "지원하지 않는 HTTP Method method={}",
                exception.getMethod()
        );

        PointWalletErrorCode errorCode =
                PointWalletErrorCode.METHOD_NOT_ALLOWED;

        PointWalletErrorResponse response =
                PointWalletErrorResponse.from(
                        errorCode
                );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /*
     * Content-Type 오류
     *
     * 예:
     * Content-Type: text/plain
     */
    @ExceptionHandler(
            HttpMediaTypeNotSupportedException.class
    )
    public ResponseEntity<PointWalletErrorResponse>
    handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException exception
    ) {
        log.warn(
                "지원하지 않는 Content-Type contentType={}",
                exception.getContentType()
        );

        PointWalletErrorCode errorCode =
                PointWalletErrorCode.UNSUPPORTED_MEDIA_TYPE;

        PointWalletErrorResponse response =
                PointWalletErrorResponse.from(
                        errorCode
                );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /*
     * DB UNIQUE 제약 위반
     *
     * 서비스에서 구체적인 업무 예외로 변환하지 못한 경우의
     * 마지막 중복 방어 처리
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<PointWalletErrorResponse>
    handleDuplicateKeyException(
            DuplicateKeyException exception
    ) {
        log.warn(
                "DB 중복 데이터 예외",
                exception
        );

        PointWalletErrorCode errorCode =
                PointWalletErrorCode.DUPLICATE_DATA;

        PointWalletErrorResponse response =
                PointWalletErrorResponse.from(
                        errorCode
                );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /*
     * 아직 PointWalletException으로 교체되지 않은
     * 기존 요청값 검증 예외의 임시 호환 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PointWalletErrorResponse>
    handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        log.warn(
                "잘못된 요청값: {}",
                exception.getMessage()
        );

        PointWalletErrorCode errorCode =
                PointWalletErrorCode.INVALID_REQUEST;

        PointWalletErrorResponse response =
                PointWalletErrorResponse.of(
                        errorCode,
                        exception.getMessage()
                );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /*
     * 포인트 지갑 API 처리 중 예상하지 못한 오류
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PointWalletErrorResponse>
    handleException(
            Exception exception
    ) {
        log.error(
                "포인트 지갑 API 처리 중 예상하지 못한 오류",
                exception
        );

        PointWalletErrorCode errorCode =
                PointWalletErrorCode.INTERNAL_PROCESS_ERROR;

        PointWalletErrorResponse response =
                PointWalletErrorResponse.from(
                        errorCode
                );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }
}