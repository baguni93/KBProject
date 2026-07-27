package org.scoula.pointwallet.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.pointwallet.dto.PointWalletDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointWalletVO {

    private Integer pointWalletId;
    private Integer userId;
    private Integer pointBalance;
    private String updatedAt;

    // VO를 DTO로 변환
    public PointWalletDTO toDTO() {

        return PointWalletDTO.builder()
                .pointWalletId(pointWalletId)
                .userId(userId)
                .pointBalance(pointBalance)
                .updatedAt(updatedAt)
                .build();
    }

    // DTO를 VO로 변환
    public static PointWalletVO fromDTO(PointWalletDTO dto) {

        if (dto == null) {
            return null;
        }

        return PointWalletVO.builder()
                .pointWalletId(dto.getPointWalletId())
                .userId(dto.getUserId())
                .pointBalance(dto.getPointBalance())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}