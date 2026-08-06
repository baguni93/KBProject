package org.scoula.cardrecommendation.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.cardrecommendation.dto.CardRecommendationCreateResponseDTO;
import org.scoula.cardrecommendation.dto.CardRecommendationDetailResponseDTO;
import org.scoula.cardrecommendation.dto.CardRecommendationListResponseDTO;
import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatusDTO;
import org.scoula.cardrecommendation.service.CardRecommendationAsyncService;
import org.scoula.cardrecommendation.service.CardRecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "카드 추천 API")
@RestController
@RequiredArgsConstructor
public class CardRecommendationController {

    // 카드 추천 목록조회, 상세조회, 실제 추천 계산
    private final CardRecommendationService cardRecommendationService;
    // 비동기 작업 시작, 현재 작업 상태 조회
    private final CardRecommendationAsyncService
            cardRecommendationAsyncService;

    @ApiOperation("12개월 소비분석 기반 카드 추천 생성 또는 재사용")
    @PostMapping(
            "/api/spending-analyses/{spendingAnalysisId}/card-recommendations"
    )
    public ResponseEntity<CardRecommendationCreateResponseDTO>
    createOrReuseRecommendations(
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer temporaryUserId = 1;

        CardRecommendationCreateResponseDTO response =
                cardRecommendationService.createOrReuse(
                        temporaryUserId,
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
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer temporaryUserId = 1;

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        cardRecommendationAsyncService.start(
                                temporaryUserId,
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
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        // TODO-auth : 이 임시 사용자 JWT로 바꿔야 합니다.
        Integer temporaryUserId = 1;

        return ResponseEntity.ok(
                cardRecommendationAsyncService.getStatus(
                        temporaryUserId,
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
        // TODO-auth : 임시 사용자 1번 교체해야한다.
        Integer temporaryUserId = 1;

        return ResponseEntity.ok(
                cardRecommendationService.getRecommendations(
                        temporaryUserId,
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
            @PathVariable("cardRecommendationId")
            Integer cardRecommendationId,

            @RequestParam(
                    value = "feeMode",
                    defaultValue = "MAX_BENEFIT"
            )
            String feeMode
    ) {
        Integer temporaryUserId = 1;

        return ResponseEntity.ok(
                cardRecommendationService.getRecommendationDetail(
                        temporaryUserId,
                        cardRecommendationId,
                        feeMode
                )
        );
    }
}
