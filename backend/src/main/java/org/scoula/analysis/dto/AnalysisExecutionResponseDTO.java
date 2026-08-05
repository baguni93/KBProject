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
public class AnalysisExecutionResponseDTO {

    private Integer spendingAnalysisId;

    private Integer period;
    private String periodLabel;

    private String analysisStartDate;
    private String analysisEndDate;

    private Integer totalSpendingAmount;
    private Integer classifiedTransactionCount;

    private Integer representativeCategoryId;
    private String representativeCategoryName;

    private String aiTitle;
    private String aiAnalysisSummary;

    private List<AnalysisCategoryResultDTO> categories;

    private String message;

}