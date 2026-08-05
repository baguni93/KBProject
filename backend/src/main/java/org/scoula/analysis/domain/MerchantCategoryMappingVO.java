package org.scoula.analysis.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCategoryMappingVO {

    // 가맹점 카테고리 매핑 ID
    private Integer merchantCategoryMappingId;

    // 가맹점 명
    private String merchantName;

    // 매핑된 소비 카테고리 ID
    private Integer spendingCategoryId;

    // 조인 조회용 필드
    private String categoryName;

    // 사용자의 수정 요청 건수
    private Integer correctionCount;

    // 매핑 생성일시
    private LocalDateTime createdAt;

    // 매핑 수정일시
    private LocalDateTime updatedAt;
}