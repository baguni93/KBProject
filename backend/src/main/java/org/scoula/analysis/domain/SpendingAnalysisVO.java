package org.scoula.analysis.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 븐석 본문 저장용 VO
public class SpendingAnalysisVO {

    // mybatis에서 pk 자동 insert
    private Integer spendingAnalysisId;

    private Integer userId;
    private Integer analysisPeriod;
    private Integer representativeCategoryId;
    private String aiTitle;
    private String aiAnalysisSummary;
}