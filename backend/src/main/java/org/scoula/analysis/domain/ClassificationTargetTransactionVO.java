package org.scoula.analysis.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationTargetTransactionVO {

    // 카테고리를 수정할 거래 ID
    private Integer transactionId;

    // 가맹점 매핑 조회에 사용할 가맹점명
    private String merchantName;

    // 현재 거래에 저장된 소비 카테고리 ID
    private Integer spendingCategoryId;
}
