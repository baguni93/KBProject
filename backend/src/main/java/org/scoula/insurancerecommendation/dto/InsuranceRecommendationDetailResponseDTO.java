package org.scoula.insurancerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/* 보험 추천 한 건의 상세보기 응답 DTO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationDetailResponseDTO {

    private Integer insuranceRecommendationId;
    private Integer spendingAnalysisId;
    private Integer analysisPeriod;
    private String analysisStartDate;
    private String analysisEndDate;

    private Integer insuranceProductId;
    private String insuranceName;
    private String insuranceCategory;
    private String insuranceDescription;
    private Integer monthlyPremium;
    private String insuranceImage;
    private String applicationUrl;

    private Integer spendingCategoryId;
    private String categoryName;

    // 규칙 기반 추천 근거
    private String recommendationReason;

    // 현재 추천 보험 상품 전용 AI 맞춤 설명
    private String aiRecommendationSummary;

    // 보험 상세 화면의 주요 보장 내용
    private List<InsuranceCoverageDTO> coverages;

    // 이 상품을 추천한 근거가 된 분석기간 내 실제 거래
    private List<InsuranceRecommendationTransactionDTO> evidenceTransactions;
}
