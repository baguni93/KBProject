package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisAgreementItemDTO {
    private Integer agreementId;
    private String agreementType;
    private String agreementName;
    private String agreementContent;
    private String requiredYn;
    private String agreedYn;
}
