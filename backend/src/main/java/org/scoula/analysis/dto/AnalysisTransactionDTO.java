package org.scoula.analysis.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisTransactionDTO {
    private Integer transactionId;
    private String merchantName;
    private String transactionLabel;
    private Integer amount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Integer spendingCategoryId;
    private String categoryName;
    private Integer parentCategoryId;
    private String parentCategoryName;
}
