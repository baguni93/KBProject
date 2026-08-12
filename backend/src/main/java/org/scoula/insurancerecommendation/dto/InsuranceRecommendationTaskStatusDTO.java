package org.scoula.insurancerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* 프론트가 polling으로 확인할 보험추천 비동기 상태 DTO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceRecommendationTaskStatusDTO {

    private Integer spendingAnalysisId;
    private String status;
    private Integer recommendationCount;
    private Boolean created;
    private String message;
}
