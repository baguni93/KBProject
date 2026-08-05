package org.scoula.analysis.domain;

import org.scoula.analysis.dto.AnalysisTaskStatus;

public class AnalysisTaskState {
    private final AnalysisTaskStatus status;
    private final Integer spendingAnalysisId;
    private final String message;

    public AnalysisTaskState(
            AnalysisTaskStatus status,
            Integer spendingAnalysisId,
            String message
    ) {
        this.status = status;
        this.spendingAnalysisId = spendingAnalysisId;
        this.message = message;
    }

    public AnalysisTaskStatus getStatus() {
        return status;
    }

    public Integer getSpendingAnalysisId() {
        return spendingAnalysisId;
    }

    public String getMessage() {
        return message;
    }
}
