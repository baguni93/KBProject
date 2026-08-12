package org.scoula.insurancerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/* 추천 여부와 무관하게 보험상품 자체의 상세정보를 보여주는 DTO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceProductDetailResponseDTO {

    private Integer insuranceProductId;
    private String insuranceName;
    private String insuranceCategory;
    private String insuranceDescription;
    private Integer monthlyPremium;
    private String insuranceImage;
    private String applicationUrl;
    private List<InsuranceCoverageDTO> coverages;
}
