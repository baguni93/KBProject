package org.scoula.insurancerecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.insurancerecommendation.dto.InsuranceCoverageDTO;
import org.scoula.insurancerecommendation.dto.InsuranceProductDetailResponseDTO;
import org.scoula.insurancerecommendation.dto.InsuranceProductItemDTO;

import java.util.List;

/* kb_insurance_product_tbl 조회 결과용 VO. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceProductVO {

    private Integer insuranceProductId;
    private String insuranceName;
    private String insuranceCategory;
    private String insuranceDescription;
    private Integer monthlyPremium;
    private String insuranceImage;
    private String applicationUrl;

    public InsuranceProductItemDTO toDTO() {
        return InsuranceProductItemDTO.builder()
                .insuranceProductId(insuranceProductId)
                .insuranceName(insuranceName)
                .insuranceCategory(insuranceCategory)
                .insuranceDescription(insuranceDescription)
                .monthlyPremium(monthlyPremium)
                .insuranceImage(insuranceImage)
                .applicationUrl(applicationUrl)
                .build();
    }

    public InsuranceProductDetailResponseDTO toDetailDTO(
            List<InsuranceCoverageDTO> coverages
    ) {
        return InsuranceProductDetailResponseDTO.builder()
                .insuranceProductId(insuranceProductId)
                .insuranceName(insuranceName)
                .insuranceCategory(insuranceCategory)
                .insuranceDescription(insuranceDescription)
                .monthlyPremium(monthlyPremium)
                .insuranceImage(insuranceImage)
                .applicationUrl(applicationUrl)
                .coverages(coverages)
                .build();
    }
}
