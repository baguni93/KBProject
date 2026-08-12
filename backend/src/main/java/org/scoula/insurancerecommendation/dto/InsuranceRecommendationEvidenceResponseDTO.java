package org.scoula.insurancerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/* 보험추천 전체의 추천 근거 상세 화면 응답 DTO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationEvidenceResponseDTO {

    private Integer spendingAnalysisId;
    private Integer analysisPeriod;
    private String analysisStartDate;
    private String analysisEndDate;
    private Integer recommendationCount;
    private List<InsuranceRecommendationEvidenceCategoryDTO> categories;
    private String message;
}
