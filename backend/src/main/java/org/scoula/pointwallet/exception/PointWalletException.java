package org.scoula.pointwallet.exception;


import lombok.Getter;

@Getter
public class PointWalletException extends RuntimeException {

    private final PointWalletErrorCode errorCode;

    public PointWalletException(
            PointWalletErrorCode errorCode
    ) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PointWalletException(
            PointWalletErrorCode errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }

    public PointWalletException(
            PointWalletErrorCode errorCode,
            Throwable cause
    ) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public PointWalletException(
            PointWalletErrorCode errorCode,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}