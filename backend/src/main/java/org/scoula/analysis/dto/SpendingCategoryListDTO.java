package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 소비 카테고리 목록 출력
public class SpendingCategoryListDTO {

    private Integer categoryCount;
    private List<SpendingCategoryDTO> categories;
}