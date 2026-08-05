package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisAgreementStatusDTO {
    private Boolean agreed;
    private Integer requiredCount;
    private Integer agreedRequiredCount;
    private String message;
}
