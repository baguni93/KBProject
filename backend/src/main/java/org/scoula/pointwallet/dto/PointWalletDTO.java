package org.scoula.pointwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointWalletDTO {

    private Integer pointWalletId;
    private Integer userId;
    private Integer pointBalance;
    private String updatedAt;
}