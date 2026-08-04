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
public class UnclassifiedTransactionListDTO {

    private Integer period;
    private String periodLabel;

    private String analysisStartDate;
    private String analysisEndDate;

    private Integer unclassifiedCount;

    private List<UnclassifiedTransactionDTO> transactions;
}