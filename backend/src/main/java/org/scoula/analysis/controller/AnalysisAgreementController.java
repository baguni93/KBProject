package org.scoula.analysis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.analysis.dto.AnalysisAgreementConsentRequestDTO;
import org.scoula.analysis.dto.AnalysisAgreementListDTO;
import org.scoula.analysis.dto.AnalysisAgreementStatusDTO;
import org.scoula.analysis.service.AnalysisAgreementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "소비 분석 약관 API")
@RestController
@RequestMapping("/api/analysis-agreements")
@RequiredArgsConstructor
public class AnalysisAgreementController {

    private final AnalysisAgreementService analysisAgreementService;

    @ApiOperation("소비 분석 필수 약관 동의 상태 조회")
    @GetMapping("/status")
    public ResponseEntity<AnalysisAgreementStatusDTO> getStatus() {
        Integer temporaryUserId = 1;
        return ResponseEntity.ok(
                analysisAgreementService.getStatus(temporaryUserId)
        );
    }

    @ApiOperation("소비 분석 약관 목록 조회")
    @GetMapping
    public ResponseEntity<AnalysisAgreementListDTO> getAgreements() {
        Integer temporaryUserId = 1;
        return ResponseEntity.ok(
                analysisAgreementService.getAgreements(temporaryUserId)
        );
    }

    @ApiOperation("소비 분석 약관 동의 저장")
    @PostMapping
    public ResponseEntity<AnalysisAgreementStatusDTO> saveAgreements(
            @RequestBody AnalysisAgreementConsentRequestDTO request
    ) {
        Integer temporaryUserId = 1;
        return ResponseEntity.ok(
                analysisAgreementService.saveAgreements(
                        temporaryUserId,
                        request
                )
        );
    }
}
