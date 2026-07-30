package org.scoula.pointwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointConversionResultDTO {

    private Integer pointConversionId;
    private Integer convertedPoint;
    private Integer pointBalance;
    private Integer walletBalance;
    private String convertedAt;
}