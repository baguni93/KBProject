package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisAgreementListDTO {
    private Integer agreementCount;
    private Boolean requiredAgreementCompleted;
    private List<AnalysisAgreementItemDTO> agreements;
}
