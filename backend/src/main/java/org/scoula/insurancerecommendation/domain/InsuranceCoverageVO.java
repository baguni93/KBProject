package org.scoula.insurancerecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.insurancerecommendation.dto.InsuranceCoverageDTO;

/* 보험 상품의 주요 보장 항목 조회 결과용 VO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceCoverageVO {

    private Integer insuranceCoverageId;
    private Integer insuranceProductId;
    private String coverageName;
    private Integer coverageAmount;
    private String coverageDescription;
    private String coverageLimit;

    public InsuranceCoverageDTO toDTO() {
        return InsuranceCoverageDTO.builder()
                .insuranceCoverageId(insuranceCoverageId)
                .coverageName(coverageName)
                .coverageAmount(coverageAmount)
                .coverageDescription(coverageDescription)
                .coverageLimit(coverageLimit)
                .build();
    }
}
