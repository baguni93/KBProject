package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingCategoryDTO {

    private Integer spendingCategoryId;
    private String categoryName;

    /**
     * 최상위 카테고리라면 null
     * 하위 카테고리라면 부모 카테고리 ID
     */
    private Integer parentCategoryId;
}