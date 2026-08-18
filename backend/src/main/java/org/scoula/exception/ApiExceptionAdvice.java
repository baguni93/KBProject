package org.scoula.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
                                .message(e.getMessage())
                                .build()
                );
    }

    // 잘못된 요청값 예외
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        log.warn(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorResponseDTO.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .code("C001")
                                .message(e.getMessage())
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
