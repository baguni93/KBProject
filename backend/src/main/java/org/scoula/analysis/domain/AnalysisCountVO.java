package org.scoula.analysis.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisCountVO {

    private Integer totalPaymentCount;
    private Integer classifiedPaymentCount;
}