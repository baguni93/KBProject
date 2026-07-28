package org.scoula.pointwallet.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.pointwallet.dto.PointConversionResultDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointConversionVO {

    private Integer pointConversionId;
    private Integer userId;
    private Integer pointWalletId;
    private Integer walletId;
    private Integer convertedPoint;
    private String convertedAt;

    public PointConversionResultDTO toResultDTO(
            Integer pointBalance,
            Integer walletBalance
    ) {
        return PointConversionResultDTO.builder()
                .pointConversionId(pointConversionId)
                .convertedPoint(convertedPoint)
                .pointBalance(pointBalance)
                .walletBalance(walletBalance)
                .convertedAt(convertedAt)
                .build();
    }
}