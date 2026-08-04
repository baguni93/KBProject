package org.scoula.agreement.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.agreement.dto.AgreementConsentDTO;
import org.scoula.agreement.dto.AgreementDTO;
import org.scoula.agreement.dto.AgreementDetailDTO;
import org.scoula.agreement.service.AgreementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AgreementController {

    private final AgreementService agreementService;


    // 약관 목록 조회
    @GetMapping("/agreements")
    public ResponseEntity<Map<String, Object>> getAgreements() {

        List<AgreementDTO> agreements =
                agreementService.getAgreements();

        Map<String, Object> response =
                new HashMap<>();

        response.put("success", true);
        response.put("data", agreements);

        return ResponseEntity.ok(response);
    }


    // 약관 상세 조회
    @GetMapping("/agreements/{agreementType}")
    public ResponseEntity<Map<String, Object>> getAgreementDetail(
            @PathVariable String agreementType
    ) {
        AgreementDetailDTO agreement =
                agreementService.getAgreementDetail(
                        agreementType
                );

        Map<String, Object> response =
                new HashMap<>();

        response.put("success", true);
        response.put("data", agreement);

        return ResponseEntity.ok(response);
    }


    // 회원 약관 동의 저장
    @PostMapping("/users/me/agreements")
    public ResponseEntity<Map<String, Object>> saveConsent(
            @RequestBody AgreementConsentDTO consentDTO
    ) {
        agreementService.saveConsent(
                consentDTO
        );

        Map<String, Object> response =
                new HashMap<>();

        response.put("success", true);
        response.put(
                "message",
                "약관 동의 정보가 저장되었습니다."
        );

        return ResponseEntity.ok(response);
    }
}