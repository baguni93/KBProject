package org.scoula.insurancerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/* 보험추천 목록 화면 전체 응답 DTO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationListResponseDTO {

    private Integer spendingAnalysisId;
    private Integer analysisPeriod;
    private String analysisStartDate;
    private String analysisEndDate;
    private Integer recommendationCount;
    private List<InsuranceRecommendationItemDTO> recommendations;
    private String message;
}
