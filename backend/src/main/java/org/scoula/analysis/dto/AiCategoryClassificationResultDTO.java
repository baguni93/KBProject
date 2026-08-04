package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCategoryClassificationResultDTO {

     // AI가 선택한 소비 카테고리 ID
    private Integer spendingCategoryId;


     // AI가 선택한 소비 카테고리명
    private String categoryName;
}