package org.scoula.insurancerecommendation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* 보험 추천 근거로 사용된 실제 결제·송금·정산 거래 DTO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationTransactionDTO {

    private Integer transactionId;
    private String merchantName;
    private Integer amount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Integer spendingCategoryId;
    private String categoryName;
    private Integer parentCategoryId;
    private String parentCategoryName;
}
