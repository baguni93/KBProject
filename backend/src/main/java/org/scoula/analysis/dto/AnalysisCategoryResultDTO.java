package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 카테고리별 분석결과 응답 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisCategoryResultDTO {

    private Integer spendingCategoryId;
    private String categoryName;
    private Integer spendingAmount;
    private BigDecimal spendingRatio;
    private Integer transactionCount;
}