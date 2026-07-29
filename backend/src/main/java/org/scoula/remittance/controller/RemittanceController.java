package org.scoula.remittance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.remittance.dto.RemittanceDTO;
import org.scoula.remittance.service.RemittanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/remittances") // 기본 공통 URL
@RequiredArgsConstructor
@Log4j2
public class RemittanceController {

    private final RemittanceService remittanceService;

    // 송금 실행 API (POST 요청)
    @PostMapping
    public ResponseEntity<String> sendMoney(@RequestBody RemittanceDTO remittanceDTO) {
        log.info("송금 요청 데이터: " + remittanceDTO);

        boolean result = remittanceService.sendMoney(remittanceDTO);

        if (result) {
            return ResponseEntity.ok("송금이 성공적으로 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("송금에 실패했습니다.");
        }
    }
}