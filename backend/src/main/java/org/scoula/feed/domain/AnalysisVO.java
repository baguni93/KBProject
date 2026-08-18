package org.scoula.feed.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisVO {

    private int spendingAnalysisId;
    private String aiTitle;
    private String aiAnalysisSummary;
    private String categoryName;
}
