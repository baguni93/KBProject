package org.scoula.settlement.service;

import org.scoula.settlement.dto.SettlementCreateRequestDTO;
import org.scoula.settlement.dto.SettlementResponseDTO;

import java.util.List;

public interface SettlementService {

    SettlementResponseDTO create(SettlementCreateRequestDTO request);
    List<SettlementResponseDTO> getMyList(int userId);
    SettlementResponseDTO get(int settlementId );
    SettlementResponseDTO payment(int settlementId, int userId);
    boolean cancel(int settlementId, int userId);

    boolean remine(int settlementId, int userId);
}
