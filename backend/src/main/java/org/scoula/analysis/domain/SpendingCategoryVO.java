package org.scoula.analysis.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 소비 카테고리 DB 조회 결과
public class SpendingCategoryVO {

    private Integer spendingCategoryId;
    private String categoryName;
    private Integer parentCategoryId;
}