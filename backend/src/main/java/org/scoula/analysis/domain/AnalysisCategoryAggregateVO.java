package org.scoula.analysis.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//카테고리 집계 용도
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisCategoryAggregateVO {

    private Integer spendingCategoryId;
    private String categoryName;

    //카테고리 별 총 소비금액
    private Long spendingAmount;

    // 카테고리별 거래 건수
    private Long transactionCount;
}