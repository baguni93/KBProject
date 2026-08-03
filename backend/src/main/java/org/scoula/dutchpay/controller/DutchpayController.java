package org.scoula.dutchpay.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.settlement.dto.SettlementCreateRequestDTO;
import org.scoula.settlement.dto.SettlementResponseDTO;
import org.scoula.settlement.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequestMapping("/api/dutchpays")
@RequiredArgsConstructor
public class DutchpayController {

    private final SettlementService settlementService;

    // dutch-001: 더치페이 정산방 생성
    @PostMapping("")
    public ResponseEntity<SettlementResponseDTO> createDutchpay(
            @RequestBody SettlementCreateRequestDTO settlementCreateRequestDTO) {
        log.info("더치페이 정산방 생성 요청: {}", settlementCreateRequestDTO);
        SettlementResponseDTO response = settlementService.create(settlementCreateRequestDTO);
        return ResponseEntity.ok(response);
    }

    // dutch-002: 더치페이 정산 금액 분배 (1/N 또는 개별 분배 설정)
    @PostMapping("/{dutchpayId}/splits")
    public ResponseEntity<SettlementResponseDTO> setDutchpaySplits(
            @PathVariable int dutchpayId,
            @RequestBody SettlementCreateRequestDTO settlementCreateRequestDTO) {
        log.info("더치페이 정산 금액 분배 설정 요청 - ID: {}, 데이터: {}", dutchpayId, settlementCreateRequestDTO);
        // 정산방 ID 지정 후 갱신 및 조회 반환
        settlementCreateRequestDTO.toVo().setSettlementId(dutchpayId);
        SettlementResponseDTO response = settlementService.get(dutchpayId);
        return ResponseEntity.ok(response);
    }
}
