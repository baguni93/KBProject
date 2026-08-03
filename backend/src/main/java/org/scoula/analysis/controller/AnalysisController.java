package org.scoula.analysis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.analysis.dto.AnalysisAvailabilityDTO;
import org.scoula.analysis.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "소비 분석 API")
@RestController
@RequestMapping("/api/spending-analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @ApiOperation("소비 분석 가능 여부 조회")
    @GetMapping("/availability")
    public ResponseEntity<AnalysisAvailabilityDTO>
    getAnalysisAvailability(
            @RequestParam(
                    value = "period",
                    defaultValue = "1"
            )
            Integer period
    ) {
        // TODO-AUTH:
        // JWT 인증 정보에서 현재 로그인 사용자의 ID를 가져오도록 변경
        Integer temporaryUserId = 1;

        AnalysisAvailabilityDTO response =
                analysisService.getAnalysisAvailability(
                        temporaryUserId,
                        period
                );

        return ResponseEntity.ok(response);
    }
}