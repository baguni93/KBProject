package org.scoula.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SettlementWebSocketDTO {

    private String type;
    private SettlementResponseDTO settlement;
}