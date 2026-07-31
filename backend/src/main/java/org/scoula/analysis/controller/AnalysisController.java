package org.scoula.analysis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.analysis.dto.*;
import org.scoula.analysis.service.AnalysisService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation("미분류 결제 거래 목록 조회")
    @GetMapping("/unclassified-transactions")
    public ResponseEntity<UnclassifiedTransactionListDTO>
    getUnclassifiedTransactions(
            @RequestParam(
                    value = "period",
                    defaultValue = "1"
            )
            Integer period
    ) {
        // TODO-AUTH:
        // 로그인 사용자 연동 전 임시 사용자
        Integer temporaryUserId = 1;

        UnclassifiedTransactionListDTO response =
                analysisService.getUnclassifiedTransactions(
                        temporaryUserId,
                        period
                );

        return ResponseEntity.ok(response);
    }

    @ApiOperation("소비 카테고리 목록 조회")
    @GetMapping("/categories")
    public ResponseEntity<SpendingCategoryListDTO>
    getSpendingCategories() {

        SpendingCategoryListDTO response =
                analysisService.getSpendingCategories();

        return ResponseEntity.ok(response);
    }

    @ApiOperation("결제 거래 소비 카테고리 직접 분류")
    @PatchMapping("/transactions/{transactionId}/category")
    public ResponseEntity<TransactionClassificationResponseDTO>
    classifyTransaction(
            @PathVariable("transactionId")
            Integer transactionId,

            @RequestBody
            TransactionClassificationRequestDTO request
    ) {
        // 로그인 연동 전 임시 사용자
        Integer temporaryUserId = 1;

        TransactionClassificationResponseDTO response =
                analysisService.classifyTransaction(
                        temporaryUserId,
                        transactionId,
                        request.getSpendingCategoryId()
                );

        return ResponseEntity.ok(response);
    }

    @ApiOperation("소비 분석 실행 및 결과 저장")
    @PostMapping
    public ResponseEntity<AnalysisExecutionResponseDTO>
    executeAnalysis(
            @RequestBody
            AnalysisExecutionRequestDTO request
    ) {
        // TODO-AUTH:
        // JWT 인증 정보의 로그인 사용자 ID로 변경
        Integer temporaryUserId = 1;

        AnalysisExecutionResponseDTO response =
                analysisService.executeAnalysis(
                        temporaryUserId,
                        request.getPeriod()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}