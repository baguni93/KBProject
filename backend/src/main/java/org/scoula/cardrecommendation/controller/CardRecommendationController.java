package org.scoula.cardrecommendation.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.cardrecommendation.dto.CardRecommendationCreateResponseDTO;
import org.scoula.cardrecommendation.dto.CardRecommendationDetailResponseDTO;
import org.scoula.cardrecommendation.dto.CardRecommendationListResponseDTO;
import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatusDTO;
import org.scoula.cardrecommendation.service.CardRecommendationAsyncService;
import org.scoula.cardrecommendation.service.CardRecommendationService;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "카드 추천 API")
@RestController
@RequiredArgsConstructor
@Log4j2
public class CardRecommendationController {

    // 카드 추천 목록조회, 상세조회, 실제 추천 계산
    private final CardRecommendationService cardRecommendationService;
    // 비동기 작업 시작, 현재 작업 상태 조회
    private final CardRecommendationAsyncService
            cardRecommendationAsyncService;
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

    @ApiOperation("12개월 소비분석 기반 카드 추천 생성 또는 재사용")
    @PostMapping(
            "/api/spending-analyses/{spendingAnalysisId}/card-recommendations"
    )
    public ResponseEntity<CardRecommendationCreateResponseDTO>
    createOrReuseRecommendations(
            HttpServletRequest httpRequest,
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer userId = resolveUserId(httpRequest);

        CardRecommendationCreateResponseDTO response =
                cardRecommendationService.createOrReuse(
                        userId,
                        spendingAnalysisId
                );

        return ResponseEntity
                .status(
                        response.isCreated()
                                ? HttpStatus.CREATED
                                : HttpStatus.OK
                )
                .body(response);
    }

    // 카드 추천 비동기 시작 API
    @ApiOperation("카드 추천 비동기 생성 시작")
    @PostMapping(
            "/api/spending-analyses/{spendingAnalysisId}"
                    + "/card-recommendations/async"
    )
    public ResponseEntity<CardRecommendationTaskStatusDTO>
    startAsyncRecommendations(
            HttpServletRequest httpRequest,
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        cardRecommendationAsyncService.start(
                                userId,
                                spendingAnalysisId
                        )
                );
    }

    @ApiOperation("카드 추천 비동기 작업 상태 조회")
    @GetMapping(
            "/api/spending-analyses/{spendingAnalysisId}"
                    + "/card-recommendations/status"
    )
    public ResponseEntity<CardRecommendationTaskStatusDTO>
    getRecommendationStatus(
            HttpServletRequest httpRequest,
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                cardRecommendationAsyncService.getStatus(
                        userId,
                        spendingAnalysisId
                )
        );
    }

    @ApiOperation("카드 유형 및 연회비 모드별 추천 목록 조회")
    @GetMapping(
            "/api/spending-analyses/{spendingAnalysisId}/card-recommendations"
    )
    public ResponseEntity<CardRecommendationListResponseDTO>
    getRecommendations(
            HttpServletRequest httpRequest,
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId,

            @RequestParam(
                    value = "cardType",
                    defaultValue = "CREDIT"
            )
            String cardType,

            @RequestParam(
                    value = "feeMode",
                    defaultValue = "MAX_BENEFIT"
            )
            String feeMode
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                cardRecommendationService.getRecommendations(
                        userId,
                        spendingAnalysisId,
                        cardType,
                        feeMode
                )
        );
    }

    @ApiOperation("추천 카드 상세 조회")
    @GetMapping(
            "/api/card-recommendations/{cardRecommendationId}"
    )
    public ResponseEntity<CardRecommendationDetailResponseDTO>
    getRecommendationDetail(
            HttpServletRequest httpRequest,
            @PathVariable("cardRecommendationId")
            Integer cardRecommendationId,

            @RequestParam(
                    value = "feeMode",
                    defaultValue = "MAX_BENEFIT"
            )
            String feeMode
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                cardRecommendationService.getRecommendationDetail(
                        userId,
                        cardRecommendationId,
                        feeMode
                )
        );
    }
}
