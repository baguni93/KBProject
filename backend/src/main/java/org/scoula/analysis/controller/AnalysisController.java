package org.scoula.analysis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.dto.*;
import org.scoula.analysis.service.AnalysisAsyncService;
import org.scoula.analysis.service.AnalysisService;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "소비 분석 API")
@RestController
@RequestMapping("/api/spending-analyses")
@RequiredArgsConstructor
@Log4j2
public class AnalysisController {

    private final AnalysisService analysisService;
    private final AnalysisAsyncService analysisAsyncService;
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

    @ApiOperation("소비 분석 가능 여부 조회")
    @GetMapping("/availability")
    public ResponseEntity<AnalysisAvailabilityDTO>
    getAnalysisAvailability(
            HttpServletRequest httpRequest,
            @RequestParam(
                    value = "period",
                    defaultValue = "1"
            )
            Integer period
    ) {
        Integer userId = resolveUserId(httpRequest);

        AnalysisAvailabilityDTO response =
                analysisService.getAnalysisAvailability(
                        userId,
                        period
                );

        return ResponseEntity.ok(response);
    }

    @ApiOperation("소비 분석용 결제 거래 목록 조회")
    @GetMapping("/transactions")
    public ResponseEntity<AnalysisTransactionListDTO> getAnalysisTransactions(
            HttpServletRequest httpRequest,
            @RequestParam(value = "period", defaultValue = "1") Integer period
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                analysisService.getAnalysisTransactions(userId, period)
        );
    }

    @ApiOperation("전체 정상 결제 거래 목록 조회")
    @GetMapping("/transactions/all")
    public ResponseEntity<AnalysisTransactionListDTO> getAllPaymentTransactions(
            HttpServletRequest httpRequest
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                analysisService.getAllPaymentTransactions(userId)
        );
    }

    @ApiOperation("저장된 분석 결과의 전체 결제 거래 목록 조회")
    @GetMapping("/{spendingAnalysisId}/transactions")
    public ResponseEntity<AnalysisTransactionListDTO>
    getAnalysisResultTransactions(
            HttpServletRequest httpRequest,
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                analysisService.getAnalysisTransactionsByAnalysisId(
                        userId,
                        spendingAnalysisId
                )
        );
    }

    @ApiOperation("미분류 결제 거래 목록 조회")
    @GetMapping("/unclassified-transactions")
    public ResponseEntity<UnclassifiedTransactionListDTO>
    getUnclassifiedTransactions(
            HttpServletRequest httpRequest,
            @RequestParam(
                    value = "period",
                    defaultValue = "1"
            )
            Integer period
    ) {
        Integer userId = resolveUserId(httpRequest);

        UnclassifiedTransactionListDTO response =
                analysisService.getUnclassifiedTransactions(
                        userId,
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
            HttpServletRequest httpRequest,
            @PathVariable("transactionId") Integer transactionId
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                analysisService.getAnalysisTransaction(
                        userId,
                        transactionId
                )
        );
    }

    @ApiOperation("결제 거래 소비 카테고리 직접 분류")
    @PatchMapping("/transactions/{transactionId}/category")
    public ResponseEntity<TransactionClassificationResponseDTO>
    classifyTransaction(
            HttpServletRequest httpRequest,
            @PathVariable("transactionId")
            Integer transactionId,

            @RequestBody
            TransactionClassificationRequestDTO request
    ) {
        Integer userId = resolveUserId(httpRequest);

        TransactionClassificationResponseDTO response =
                analysisService.classifyTransaction(
                        userId,
                        transactionId,
                        request.getSpendingCategoryId()
                );

        return ResponseEntity.ok(response);
    }

    @ApiOperation("소비 분석 실행 및 결과 저장")
    @PostMapping
    public ResponseEntity<AnalysisExecutionResponseDTO>
    executeAnalysis(
            HttpServletRequest httpRequest,
            @RequestBody
            AnalysisExecutionRequestDTO request
    ) {
        Integer userId = resolveUserId(httpRequest);

        AnalysisExecutionResponseDTO response =
                analysisService.executeAnalysis(
                        userId,
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
            HttpServletRequest httpRequest,
            @RequestBody
            AnalysisExecutionRequestDTO request
    ) {
        Integer userId = resolveUserId(httpRequest);

        AnalysisTaskStatusDTO response =
                analysisAsyncService.startAnalysis(
                        userId,
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
            HttpServletRequest httpRequest,
            @RequestParam(value = "period", defaultValue = "1")
            Integer period
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                analysisAsyncService.getStatus(
                        userId,
                        period
                )
        );
    }

    @ApiOperation("저장된 소비 분석 상세 결과 조회")
    @GetMapping("/{spendingAnalysisId}")
    public ResponseEntity<AnalysisDetailResponseDTO>
    getAnalysisDetail(
            HttpServletRequest httpRequest,
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer userId = resolveUserId(httpRequest);

        AnalysisDetailResponseDTO response =
                analysisService.getAnalysisDetail(
                        userId,
                        spendingAnalysisId
                );

        return ResponseEntity.ok(response);
    }

    @ApiOperation("선택 기간의 가장 최근 소비 분석 상세 결과 조회")
    @GetMapping("/latest")
    public ResponseEntity<AnalysisDetailResponseDTO>
    getLatestAnalysisDetail(
            HttpServletRequest httpRequest,
            @RequestParam(value = "period", defaultValue = "1")
            Integer period
    ) {
        Integer userId = resolveUserId(httpRequest);

        AnalysisDetailResponseDTO response =
                analysisService.getLatestAnalysisDetail(
                        userId,
                        period
                );

        return ResponseEntity.ok(response);
    }
}
