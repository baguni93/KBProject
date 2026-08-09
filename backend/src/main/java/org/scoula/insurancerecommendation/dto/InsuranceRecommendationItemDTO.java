package org.scoula.insurancerecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceRecommendationItemDTO {

    private Integer insuranceProductId;

    private String insuranceName;

    private String insuranceCategory;

    private String insuranceDescription;

    private Integer monthlyPremium;

    private String insuranceImage;

    private String applicationUrl;

    private Integer spendingCategoryId;

    private String categoryName;

    private String recommendationReason;

    private Integer priority;
}