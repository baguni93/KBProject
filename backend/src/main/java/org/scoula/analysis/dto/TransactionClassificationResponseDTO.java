package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionClassificationResponseDTO {

    private Integer transactionId;
    private Integer spendingCategoryId;
    private String categoryName;
    private String message;
}