package org.scoula.analysis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisDetailResponseDTO {

    // 조회한 소비분석 ID
    private Integer spendingAnalysisId;

    // 분석 기간의 개월 수
    private Integer period;

    // 화면 표시용 분석 기간 문구
    private String periodLabel;

    // 분석 대상 시작일
    private String analysisStartDate;

    // 분석 대상 종료일
    private String analysisEndDate;

    // 전체 소비 금액
    private Integer totalSpendingAmount;

    // 분석에 포함된 분류 완료 거래 건수
    private Integer classifiedTransactionCount;

    // 대표 소비 카테고리
    private Integer representativeCategoryId;
    private String representativeCategoryName;

    // AI 소비분석 결과
    private String aiTitle;
    private String aiAnalysisSummary;
    private String aiCardRecommendationSummary;
    private String aiInsuranceRecommendationSummary;

    // 분석 결과 생성일시
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // 카테고리별 소비 금액, 비율, 거래 건수
    private List<AnalysisCategoryResultDTO> categories;

    // 화면 안내 메시지
    private String message;
}