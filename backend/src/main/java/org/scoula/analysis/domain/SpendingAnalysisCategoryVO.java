package org.scoula.analysis.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 카테고리별 분석 결과 저장 VO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingAnalysisCategoryVO {

    private Integer spendingAnalysisId;
    private Integer spendingCategoryId;
    private Integer spendingAmount;
    private BigDecimal spendingRatio;
    private Integer transactionCount;
}