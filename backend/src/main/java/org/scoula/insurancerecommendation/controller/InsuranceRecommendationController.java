package org.scoula.insurancerecommendation.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.insurancerecommendation.dto.*;
import org.scoula.insurancerecommendation.service.InsuranceRecommendationAsyncService;
import org.scoula.insurancerecommendation.service.InsuranceRecommendationService;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "보험 추천 API")
@RestController
@RequiredArgsConstructor
@Log4j2
public class InsuranceRecommendationController {

    // 보험추천 생성/재사용, 목록, 근거, 상세 조회
    private final InsuranceRecommendationService insuranceRecommendationService;

    // 비동기 작업 시작 및 polling 상태 조회
    private final InsuranceRecommendationAsyncService insuranceRecommendationAsyncService;

    // 프로젝트에서 이미 사용 중인 JWT 사용자 식별 방식 재사용
    private final JwtProcessor jwtProcessor;

    /* 현재 요청의 Authorization Bearer Token에서 로그인 사용자 ID 추출 */
    private Integer resolveUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new CustomException(
                    ErrorCode.AUTHENTICATION_REQUIRED
            );
        }

        try {
            String token = authHeader.substring(7);
            Long userId = jwtProcessor.getUserId(token);
            return userId.intValue();

        } catch (Exception e) {
            log.warn(
                    "토큰에서 userId 추출 실패: {}",
                    e.getMessage()
            );
            throw new CustomException(
                    ErrorCode.AUTHENTICATION_REQUIRED
            );
        }
    }

    /*
     * 동기 생성 API.
     * 개발/API 테스트용으로도 사용할 수 있고 이미 저장된 결과가 있으면 재사용한다.
     * 실제 프론트에서는 아래 async API를 우선 사용할 수 있다.
     */
    @ApiOperation("12개월 소비분석 기간 기반 보험 추천 생성 또는 재사용")
    @PostMapping(
            "/api/spending-analyses/{spendingAnalysisId}"
                    + "/insurance-recommendations"
    )
    public ResponseEntity<InsuranceRecommendationCreateResponseDTO>
    createOrReuseRecommendations(
            HttpServletRequest httpRequest,
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer userId = resolveUserId(httpRequest);

        InsuranceRecommendationCreateResponseDTO response =
                insuranceRecommendationService.createOrReuse(
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

    /* 프론트에서 보험추천 분석을 시작할 때 사용하는 비동기 API */
    @ApiOperation("보험 추천 비동기 생성 시작")
    @PostMapping(
            "/api/spending-analyses/{spendingAnalysisId}"
                    + "/insurance-recommendations/async"
    )
    public ResponseEntity<InsuranceRecommendationTaskStatusDTO>
    startAsyncRecommendations(
            HttpServletRequest httpRequest,
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(
                        insuranceRecommendationAsyncService.start(
                                userId,
                                spendingAnalysisId
                        )
                );
    }

    /* 비동기 추천의 IDLE / PROCESSING / COMPLETED / FAILED 상태 polling */
    @ApiOperation("보험 추천 비동기 작업 상태 조회")
    @GetMapping(
            "/api/spending-analyses/{spendingAnalysisId}"
                    + "/insurance-recommendations/status"
    )
    public ResponseEntity<InsuranceRecommendationTaskStatusDTO>
    getRecommendationStatus(
            HttpServletRequest httpRequest,
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                insuranceRecommendationAsyncService.getStatus(
                        userId,
                        spendingAnalysisId
                )
        );
    }

    /* 전체 보험 둘러보기 목록. category 미지정 시 모든 보험상품 반환 */
    @ApiOperation("전체 보험 상품 목록 조회")
    @GetMapping("/api/insurance-products")
    public ResponseEntity<InsuranceProductListResponseDTO>
    getInsuranceProducts(
            HttpServletRequest httpRequest,
            @RequestParam(
                    value = "category",
                    required = false
            )
            String category
    ) {
        // 앱 내부 보험 화면이므로 상품 자체는 공통 데이터여도 로그인 여부는 확인한다.
        resolveUserId(httpRequest);

        return ResponseEntity.ok(
                insuranceRecommendationService.getInsuranceProducts(
                        category
                )
        );
    }

    /* 전체 보험 둘러보기에서 진입하는 보험상품 상세 */
    @ApiOperation("보험 상품 상세 조회")
    @GetMapping("/api/insurance-products/{insuranceProductId}")
    public ResponseEntity<InsuranceProductDetailResponseDTO>
    getInsuranceProductDetail(
            HttpServletRequest httpRequest,
            @PathVariable("insuranceProductId")
            Integer insuranceProductId
    ) {
        resolveUserId(httpRequest);

        return ResponseEntity.ok(
                insuranceRecommendationService.getInsuranceProductDetail(
                        insuranceProductId
                )
        );
    }

    /* 저장된 보험추천 전체 목록 조회 */
    @ApiOperation("저장된 보험 추천 목록 조회")
    @GetMapping(
            "/api/spending-analyses/{spendingAnalysisId}"
                    + "/insurance-recommendations"
    )
    public ResponseEntity<InsuranceRecommendationListResponseDTO>
    getRecommendations(
            HttpServletRequest httpRequest,
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                insuranceRecommendationService.getRecommendations(
                        userId,
                        spendingAnalysisId
                )
        );
    }

    /* 추천 근거 상세 화면: 보험과 연결된 카테고리의 거래 건수/금액/최근일시 */
    @ApiOperation("보험 추천 근거 상세 조회")
    @GetMapping(
            "/api/spending-analyses/{spendingAnalysisId}"
                    + "/insurance-recommendations/evidence"
    )
    public ResponseEntity<InsuranceRecommendationEvidenceResponseDTO>
    getRecommendationEvidence(
            HttpServletRequest httpRequest,
            @PathVariable("spendingAnalysisId")
            Integer spendingAnalysisId
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                insuranceRecommendationService.getRecommendationEvidence(
                        userId,
                        spendingAnalysisId
                )
        );
    }

    /* 추천 보험 한 건의 상품정보 + 보장내용 + 실제 추천근거 거래 상세 */
    @ApiOperation("추천 보험 상세 조회")
    @GetMapping(
            "/api/insurance-recommendations/{insuranceRecommendationId}"
    )
    public ResponseEntity<InsuranceRecommendationDetailResponseDTO>
    getRecommendationDetail(
            HttpServletRequest httpRequest,
            @PathVariable("insuranceRecommendationId")
            Integer insuranceRecommendationId
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                insuranceRecommendationService.getRecommendationDetail(
                        userId,
                        insuranceRecommendationId
                )
        );
    }
}
