package org.scoula.insurancerecommendation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* 추천 근거 상세 화면의 카테고리별 거래 집계 DTO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationEvidenceCategoryDTO {

    private Integer spendingCategoryId;
    private String categoryName;
    private Integer transactionCount;
    private Integer totalAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestTransactionAt;
}
