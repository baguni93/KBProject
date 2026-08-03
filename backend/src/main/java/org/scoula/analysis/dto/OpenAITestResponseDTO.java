package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OpenAITestResponseDTO {

    private boolean success;
    private String model;
    private String message;
}