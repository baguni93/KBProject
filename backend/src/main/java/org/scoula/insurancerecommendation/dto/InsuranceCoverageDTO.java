package org.scoula.insurancerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceCoverageDTO {

    private Integer insuranceCoverageId;
    private String coverageName;
    private Integer coverageAmount;
    private String coverageDescription;
    private String coverageLimit;
}
