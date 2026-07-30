package org.scoula.pointwallet.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointWalletErrorResponse {

    private Integer status;
    private String code;
    private String message;
    private String timestamp;

    public static PointWalletErrorResponse from(
            PointWalletErrorCode errorCode
    ) {
        return PointWalletErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(currentTimestamp())
                .build();
    }

    public static PointWalletErrorResponse of(
            PointWalletErrorCode errorCode,
            String message
    ) {
        return PointWalletErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .message(message)
                .timestamp(currentTimestamp())
                .build();
    }

    private static String currentTimestamp() {
        return LocalDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm:ss"
                        )
                );
    }
}