package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCategoryClassificationResultDTO {

    // 앞뒤 공백을 제거한 가맹점명
    private String merchantName;

    // 결정된 소비 카테고리 ID
    private Integer spendingCategoryId;

    //결정된 소비 카테고리명
    private String categoryName;

    /**
     * 분류에 사용된 방식
     * MAPPING: 기존 가맹점 매핑 재사용
     * AI: GPT를 통한 신규 분류
     * UNCLASSIFIED: 분류 실패
     */
    private String classificationSource;

    //이번 분류 과정에서 신규 매핑이 생성됐는지 여부
    private boolean mappingCreated;
}