package org.scoula.exception;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.config.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Log4j2
public class ApiExceptionAdvice {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDTO> handle(CustomException e) {

        ErrorCode error = e.getErrorCode();

        return ResponseEntity
                .status(error.getStatus())
                .body(
                        ErrorResponseDTO.builder()
                                .status(error.getStatus().value())
                                .code(error.getCode())
                                .message(error.getMessage())
                                .build()
                );
    }

    // 기타 예외
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponseDTO> handleException(Exception e) {
        log.error(e.getMessage(), e);

        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(
                        ErrorResponseDTO.builder()
                                .status(500)
                                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                                .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                                .build()
                );
    }
}
