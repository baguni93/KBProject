package org.scoula.analysis.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisDetailVO {

    // 소비분석 ID
    private Integer spendingAnalysisId;

    // 분석 결과의 소유 사용자 ID
    private Integer userId;

    // 분석 기간: 1개월, 3개월, 12개월
    private Integer analysisPeriod;

    // 대표 소비 카테고리 ID
    private Integer representativeCategoryId;

    // 대표 소비 카테고리명
    private String representativeCategoryName;

    // AI가 생성한 소비 칭호
    private String aiTitle;

    // AI가 생성한 소비분석 요약
    private String aiAnalysisSummary;

    // 분석 결과가 생성된 일시
    private LocalDateTime createdAt;
}