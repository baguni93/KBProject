package org.scoula.insurancerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/* 전체 보험 둘러보기 목록 응답 DTO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceProductListResponseDTO {

    private String category;
    private Integer productCount;
    private List<InsuranceProductItemDTO> products;
}
