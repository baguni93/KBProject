package org.scoula.analysis.dto;

import lombok.Data;
import lombok.NoArgsConstructor;


// 직접 분류 요청
@Data
@NoArgsConstructor
public class TransactionClassificationRequestDTO {

    private Integer spendingCategoryId;
}