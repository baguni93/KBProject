package org.scoula.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponseDTO {
    private int status;
    private String code;
    private String message;
}
