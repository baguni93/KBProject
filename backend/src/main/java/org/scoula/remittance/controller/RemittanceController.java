package org.scoula.remittance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.remittance.dto.BankRemittanceInfoDTO;
import org.scoula.remittance.dto.RemittanceDTO;
import org.scoula.remittance.service.RemittanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/remittances")
@RequiredArgsConstructor
@Log4j2
public class RemittanceController {

    private final RemittanceService remittanceService;

    // remit-bank-001: 송금 가능 은행 및 최근 계좌 조회 (GET /api/remittances/banks)
    @GetMapping("/banks")
    public ResponseEntity<BankRemittanceInfoDTO> getBankRemittanceInfo(
            @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        log.info("최근 계좌 및 은행 목록 조회 - 회원 ID: {}", userId);
        BankRemittanceInfoDTO info = remittanceService.getBankRemittanceInfo(userId);
        return ResponseEntity.ok(info);
    }

    // remit-003: 송금 실행 API (POST /api/remittances)
    @PostMapping
    public ResponseEntity<RemittanceDTO> sendMoney(@RequestBody RemittanceDTO remittanceDTO) {
        log.info("송금 요청 데이터: " + remittanceDTO);

        boolean result = remittanceService.sendMoney(remittanceDTO);

        if (result) {
            return ResponseEntity.ok(remittanceDTO);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}