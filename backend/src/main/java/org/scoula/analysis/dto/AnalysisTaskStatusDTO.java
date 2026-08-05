package org.scoula.analysis.dto;

public class AnalysisTaskStatusDTO {
    private Integer period;
    private String status;
    private Integer spendingAnalysisId;
    private String message;

    public AnalysisTaskStatusDTO() {
    }

    public AnalysisTaskStatusDTO(
            Integer period,
            String status,
            Integer spendingAnalysisId,
            String message
    ) {
        this.period = period;
        this.status = status;
        this.spendingAnalysisId = spendingAnalysisId;
        this.message = message;
    }

    public Integer getPeriod() {
        return period;
    }

    public String getStatus() {
        return status;
    }

    public Integer getSpendingAnalysisId() {
        return spendingAnalysisId;
    }

    public String getMessage() {
        return message;
    }
}
