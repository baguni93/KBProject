package org.scoula.analysis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.dto.AnalysisAgreementConsentRequestDTO;
import org.scoula.analysis.dto.AnalysisAgreementListDTO;
import org.scoula.analysis.dto.AnalysisAgreementStatusDTO;
import org.scoula.analysis.service.AnalysisAgreementService;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "소비 분석 약관 API")
@RestController
@RequestMapping("/api/analysis-agreements")
@RequiredArgsConstructor
@Log4j2
public class AnalysisAgreementController {

    private final AnalysisAgreementService analysisAgreementService;
    private final JwtProcessor jwtProcessor;

    // 사용자 인증 토큰 처리
    private Integer resolveUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        try {
            String token = authHeader.substring(7);
            Long userId = jwtProcessor.getUserId(token);
            return userId.intValue();
        } catch (Exception e) {
            log.warn("토큰에서 userId 추출 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }

    @ApiOperation("소비 분석 필수 약관 동의 상태 조회")
    @GetMapping("/status")
    public ResponseEntity<AnalysisAgreementStatusDTO> getStatus(
            HttpServletRequest httpRequest
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                analysisAgreementService.getStatus(userId)
        );
    }

    @ApiOperation("소비 분석 약관 목록 조회")
    @GetMapping
    public ResponseEntity<AnalysisAgreementListDTO> getAgreements(
            HttpServletRequest httpRequest
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                analysisAgreementService.getAgreements(userId)
        );
    }

    @ApiOperation("소비 분석 약관 동의 저장")
    @PostMapping
    public ResponseEntity<AnalysisAgreementStatusDTO> saveAgreements(
            HttpServletRequest httpRequest,
            @RequestBody AnalysisAgreementConsentRequestDTO request
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                analysisAgreementService.saveAgreements(
                        userId,
                        request
                )
        );
    }
}
