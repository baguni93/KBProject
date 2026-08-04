package org.scoula.analysis.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisTransactionVO {
    private Integer transactionId;
    private String merchantName;
    private Integer amount;
    private LocalDateTime createdAt;
    private Integer spendingCategoryId;
    private String categoryName;
    private Integer parentCategoryId;
    private String parentCategoryName;
}
