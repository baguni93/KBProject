package org.scoula.analysis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.analysis.dto.*;
import org.scoula.analysis.service.AnalysisAsyncService;
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
    private final AnalysisAsyncService analysisAsyncService;

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

    @ApiOperation("소비 분석용 결제 거래 목록 조회")
    @GetMapping("/transactions")
    public ResponseEntity<AnalysisTransactionListDTO> getAnalysisTransactions(
            @RequestParam(value = "period", defaultValue = "1") Integer period
    ) {
        Integer temporaryUserId = 1;
        return ResponseEntity.ok(
                analysisService.getAnalysisTransactions(temporaryUserId, period)
        );
    }

    @ApiOperation("저장된 분석 결과의 전체 결제 거래 목록 조회")
    @GetMapping("/{spendingAnalysisId}/transactions")
    public ResponseEntity<AnalysisTransactionListDTO>
    getAnalysisResultTransactions(
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer temporaryUserId = 1;
        return ResponseEntity.ok(
                analysisService.getAnalysisTransactionsByAnalysisId(
                        temporaryUserId,
                        spendingAnalysisId
                )
        );
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

    @ApiOperation("결제 거래 단건 조회")
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<AnalysisTransactionDTO> getAnalysisTransaction(
            @PathVariable("transactionId") Integer transactionId
    ) {
        Integer temporaryUserId = 1;
        return ResponseEntity.ok(
                analysisService.getAnalysisTransaction(
                        temporaryUserId,
                        transactionId
                )
        );
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

    @ApiOperation("소비 분석 비동기 실행")
    @PostMapping("/async")
    public ResponseEntity<AnalysisTaskStatusDTO>
    executeAnalysisAsync(
            @RequestBody
            AnalysisExecutionRequestDTO request
    ) {
        Integer temporaryUserId = 1;

        AnalysisTaskStatusDTO response =
                analysisAsyncService.startAnalysis(
                        temporaryUserId,
                        request.getPeriod()
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    @ApiOperation("소비 분석 비동기 작업 상태 조회")
    @GetMapping("/status")
    public ResponseEntity<AnalysisTaskStatusDTO>
    getAnalysisTaskStatus(
            @RequestParam(value = "period", defaultValue = "1")
            Integer period
    ) {
        Integer temporaryUserId = 1;

        return ResponseEntity.ok(
                analysisAsyncService.getStatus(
                        temporaryUserId,
                        period
                )
        );
    }

    @ApiOperation("저장된 소비 분석 상세 결과 조회")
    @GetMapping("/{spendingAnalysisId}")
    public ResponseEntity<AnalysisDetailResponseDTO>
    getAnalysisDetail(
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        // TODO-AUTH:
        // JWT 인증 정보의 로그인 사용자 ID로 변경
        Integer temporaryUserId = 1;

        AnalysisDetailResponseDTO response =
                analysisService.getAnalysisDetail(
                        temporaryUserId,
                        spendingAnalysisId
                );

        return ResponseEntity.ok(response);
    }

    @ApiOperation("선택 기간의 가장 최근 소비 분석 상세 결과 조회")
    @GetMapping("/latest")
    public ResponseEntity<AnalysisDetailResponseDTO>
    getLatestAnalysisDetail(
            @RequestParam(value = "period", defaultValue = "1")
            Integer period
    ) {
        // TODO-AUTH:
        // JWT 인증 정보의 로그인 사용자 ID로 변경
        Integer temporaryUserId = 1;

        AnalysisDetailResponseDTO response =
                analysisService.getLatestAnalysisDetail(
                        temporaryUserId,
                        period
                );

        return ResponseEntity.ok(response);
    }

}