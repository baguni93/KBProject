package org.scoula.insurancerecommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.dto.AnalysisDetailResponseDTO;
import org.scoula.analysis.service.AnalysisService;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.insurancerecommendation.domain.*;
import org.scoula.insurancerecommendation.dto.*;
import org.scoula.insurancerecommendation.mapper.InsuranceRecommendationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class InsuranceRecommendationServiceImpl
        implements InsuranceRecommendationService {

    /*
     * 보험추천 정책은 12개월 소비분석을 기준으로 한다.
     * 실제 추천 판단 데이터는 spending_analysis_category_tbl이 아니라
     * 해당 분석기간의 financial_transaction_tbl 실제 거래다.
     */
    private static final int REQUIRED_ANALYSIS_PERIOD = 12;
    private static final DateTimeFormatter RECOMMENDATION_REASON_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    private final InsuranceRecommendationMapper insuranceRecommendationMapper;
    private final AnalysisService analysisService;
    private final InsuranceRecommendationSaveService saveService;
    private final InsuranceRecommendationNarrativeService narrativeService;

    @Override
    public InsuranceRecommendationCreateResponseDTO createOrReuse(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        AnalysisDetailResponseDTO analysis =
                getValidatedAnalysis(userId, spendingAnalysisId);

        /*
         * 새 DB에서는 spending_analysis_tbl에 보험추천 완료 요약을 저장하지 않는다.
         * 저장된 추천 행이 하나라도 있으면 이미 생성된 추천 결과를 재사용한다.
         */
        int existingCount = insuranceRecommendationMapper
                .countRecommendations(spendingAnalysisId);

        if (existingCount > 0) {
            return InsuranceRecommendationCreateResponseDTO.builder()
                    .spendingAnalysisId(spendingAnalysisId)
                    .created(false)
                    .recommendationCount(existingCount)
                    .message("기존 보험 추천 결과를 불러왔습니다.")
                    .build();
        }

        AnalysisRange range = createAnalysisRange(analysis);

        List<InsuranceRecommendationCandidateVO> rawCandidates =
                insuranceRecommendationMapper.selectMatchedCandidates(
                        userId,
                        range.startAt,
                        range.endAt
                );

        List<InsuranceRecommendationCandidateVO> candidates =
                removeDuplicateProducts(rawCandidates);

        /*
         * 상품 카드의 "추천 이유"는 AI를 사용하지 않는다.
         * Mapper가 함께 조회한 실제 거래 1건의 날짜/표시명과
         * 보험 카테고리를 조합해 규칙 기반 문구를 만든다.
         */
        for (InsuranceRecommendationCandidateVO candidate : candidates) {
            candidate.setRecommendationReason(
                    createRuleBasedRecommendationReason(candidate)
            );
            candidate.setAiRecommendationSummary(null);
        }

        /*
         * GPT 호출은 보험 추천 한 번당 딱 1회만 수행한다.
         * 결과는 메인 화면 상단 "AI 추천 요약"에서만 사용하며,
         * 현재 DB 구조를 바꾸지 않기 위해 첫 번째 추천 행에만 저장한다.
         */
        if (!candidates.isEmpty()) {
            String aiSummary =
                    narrativeService.createInsuranceSummary(candidates);

            candidates.get(0).setAiRecommendationSummary(aiSummary);
        }

        saveService.saveRecommendations(
                spendingAnalysisId,
                candidates
        );

        log.info(
                "보험 추천 생성 완료 userId={}, analysisId={}, count={}",
                userId,
                spendingAnalysisId,
                candidates.size()
        );

        return InsuranceRecommendationCreateResponseDTO.builder()
                .spendingAnalysisId(spendingAnalysisId)
                .created(true)
                .recommendationCount(candidates.size())
                .message(
                        candidates.isEmpty()
                                ? "추천 가능한 보험 상품을 찾지 못했습니다."
                                : "보험 추천 결과를 생성했습니다."
                )
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public void validateRecommendationAnalysis(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        getValidatedAnalysis(userId, spendingAnalysisId);
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceProductListResponseDTO getInsuranceProducts(
            String category
    ) {
        String normalizedCategory =
                category == null || category.isBlank()
                        ? null
                        : category.trim();

        List<InsuranceProductVO> rows =
                insuranceRecommendationMapper.selectInsuranceProducts(
                        normalizedCategory
                );

        if (rows == null) {
            rows = List.of();
        }

        List<InsuranceProductItemDTO> products = rows.stream()
                .map(InsuranceProductVO::toDTO)
                .collect(Collectors.toList());

        return InsuranceProductListResponseDTO.builder()
                .category(normalizedCategory)
                .productCount(products.size())
                .products(products)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceProductDetailResponseDTO getInsuranceProductDetail(
            Integer insuranceProductId
    ) {
        if (insuranceProductId == null || insuranceProductId <= 0) {
            throw new CustomException(
                    ErrorCode.INSURANCE_PRODUCT_NOT_FOUND
            );
        }

        InsuranceProductVO product =
                insuranceRecommendationMapper.selectInsuranceProduct(
                        insuranceProductId
                );

        if (product == null) {
            throw new CustomException(
                    ErrorCode.INSURANCE_PRODUCT_NOT_FOUND
            );
        }

        List<InsuranceCoverageVO> coverageRows =
                insuranceRecommendationMapper.selectInsuranceCoverages(
                        insuranceProductId
                );

        if (coverageRows == null) {
            coverageRows = List.of();
        }

        List<InsuranceCoverageDTO> coverages = coverageRows.stream()
                .map(InsuranceCoverageVO::toDTO)
                .collect(Collectors.toList());

        return product.toDetailDTO(coverages);
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceRecommendationListResponseDTO getRecommendations(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        AnalysisDetailResponseDTO analysis =
                getValidatedAnalysis(userId, spendingAnalysisId);

        /*
         * 공통 summary 컬럼이 제거되었으므로 목록 자체를 조회한다.
         * 아직 추천을 실행하지 않은 경우와 실행 결과 0건인 경우를 DB만으로는
         * 구분할 수 없으며 둘 다 빈 목록으로 반환한다.
         */
        List<InsuranceRecommendationListItemVO> rows =
                insuranceRecommendationMapper.selectRecommendationList(
                        userId,
                        spendingAnalysisId
                );

        if (rows == null) {
            rows = List.of();
        }

        List<InsuranceRecommendationItemDTO> items = rows.stream()
                .map(InsuranceRecommendationListItemVO::toDTO)
                .collect(Collectors.toList());

        return InsuranceRecommendationListResponseDTO.builder()
                .spendingAnalysisId(spendingAnalysisId)
                .analysisPeriod(analysis.getPeriod())
                .analysisStartDate(analysis.getAnalysisStartDate())
                .analysisEndDate(analysis.getAnalysisEndDate())
                .recommendationCount(items.size())
                .recommendations(items)
                .message(
                        items.isEmpty()
                                ? "추천 가능한 보험 상품이 없습니다."
                                : "보험 추천 결과를 조회했습니다."
                )
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceRecommendationEvidenceResponseDTO getRecommendationEvidence(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        AnalysisDetailResponseDTO analysis =
                getValidatedAnalysis(userId, spendingAnalysisId);

        AnalysisRange range = createAnalysisRange(analysis);

        List<InsuranceRecommendationEvidenceCategoryVO> rows =
                insuranceRecommendationMapper
                        .selectRecommendationEvidenceCategories(
                                userId,
                                spendingAnalysisId,
                                range.startAt,
                                range.endAt
                        );

        if (rows == null) {
            rows = List.of();
        }

        List<InsuranceRecommendationEvidenceCategoryDTO> categories =
                rows.stream()
                        .map(InsuranceRecommendationEvidenceCategoryVO::toDTO)
                        .collect(Collectors.toList());

        int recommendationCount = insuranceRecommendationMapper
                .countRecommendations(spendingAnalysisId);

        return InsuranceRecommendationEvidenceResponseDTO.builder()
                .spendingAnalysisId(spendingAnalysisId)
                .analysisPeriod(analysis.getPeriod())
                .analysisStartDate(analysis.getAnalysisStartDate())
                .analysisEndDate(analysis.getAnalysisEndDate())
                .recommendationCount(recommendationCount)
                .categories(categories)
                .message("보험 추천에 사용된 실제 거래 근거를 조회했습니다.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceRecommendationDetailResponseDTO getRecommendationDetail(
            Integer userId,
            Integer insuranceRecommendationId
    ) {
        if (insuranceRecommendationId == null
                || insuranceRecommendationId <= 0) {
            throw new CustomException(
                    ErrorCode.INSURANCE_RECOMMENDATION_NOT_FOUND
            );
        }

        /*
         * 추천 ID와 현재 userId를 함께 조회하므로
         * 다른 사용자의 저장된 보험추천 상세를 열 수 없다.
         */
        InsuranceRecommendationDetailHeaderVO header =
                insuranceRecommendationMapper
                        .selectRecommendationDetailHeader(
                                userId,
                                insuranceRecommendationId
                        );

        if (header == null) {
            throw new CustomException(
                    ErrorCode.INSURANCE_RECOMMENDATION_NOT_FOUND
            );
        }

        if (header.getAnalysisPeriod() == null
                || header.getAnalysisPeriod() != REQUIRED_ANALYSIS_PERIOD) {
            throw new CustomException(
                    ErrorCode.INSURANCE_RECOMMENDATION_REQUIRES_12_MONTH_ANALYSIS
            );
        }

        LocalDate analysisEndDate =
                header.getAnalysisCreatedAt().toLocalDate();
        LocalDate analysisStartDate =
                analysisEndDate.minusMonths(header.getAnalysisPeriod());

        LocalDateTime startAt = analysisStartDate.atStartOfDay();
        LocalDateTime endAt = analysisEndDate.plusDays(1).atStartOfDay();

        List<InsuranceCoverageVO> coverageRows =
                insuranceRecommendationMapper.selectInsuranceCoverages(
                        header.getInsuranceProductId()
                );

        if (coverageRows == null) {
            coverageRows = List.of();
        }

        List<InsuranceCoverageDTO> coverages = coverageRows.stream()
                .map(InsuranceCoverageVO::toDTO)
                .collect(Collectors.toList());

        List<InsuranceRecommendationTransactionVO> transactionRows =
                insuranceRecommendationMapper.selectRecommendationTransactions(
                        userId,
                        header.getInsuranceProductId(),
                        startAt,
                        endAt
                );

        if (transactionRows == null) {
            transactionRows = List.of();
        }

        List<InsuranceRecommendationTransactionDTO> transactions =
                transactionRows.stream()
                        .map(InsuranceRecommendationTransactionVO::toDTO)
                        .collect(Collectors.toList());

        return header.toDTO(
                analysisStartDate.toString(),
                analysisEndDate.toString(),
                coverages,
                transactions
        );
    }

    /*
     * 소비분석 상세 조회를 재사용해
     * 1) 분석 존재 여부/소유권
     * 2) 12개월 분석 여부
     * 를 한 곳에서 검증한다.
     */
    private AnalysisDetailResponseDTO getValidatedAnalysis(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        AnalysisDetailResponseDTO analysis =
                analysisService.getAnalysisDetail(
                        userId,
                        spendingAnalysisId
                );

        if (analysis.getPeriod() == null
                || analysis.getPeriod() != REQUIRED_ANALYSIS_PERIOD) {
            throw new CustomException(
                    ErrorCode.INSURANCE_RECOMMENDATION_REQUIRES_12_MONTH_ANALYSIS
            );
        }

        return analysis;
    }

    /*
     * "오늘부터 1년"을 다시 계산하지 않는다.
     * 저장된 소비분석이 실제로 사용했던 시작일/종료일을 그대로 사용한다.
     */
    private AnalysisRange createAnalysisRange(
            AnalysisDetailResponseDTO analysis
    ) {
        LocalDate analysisStartDate =
                LocalDate.parse(analysis.getAnalysisStartDate());
        LocalDate analysisEndDate =
                LocalDate.parse(analysis.getAnalysisEndDate());

        LocalDateTime startAt = analysisStartDate.atStartOfDay();
        LocalDateTime endAt = analysisEndDate
                .plusDays(1)
                .atStartOfDay();

        return new AnalysisRange(startAt, endAt);
    }

    private List<InsuranceRecommendationCandidateVO> removeDuplicateProducts(
            List<InsuranceRecommendationCandidateVO> candidates
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, InsuranceRecommendationCandidateVO> unique =
                new LinkedHashMap<>();

        for (InsuranceRecommendationCandidateVO candidate : candidates) {
            if (candidate == null
                    || candidate.getInsuranceProductId() == null) {
                continue;
            }

            unique.putIfAbsent(
                    candidate.getInsuranceProductId(),
                    candidate
            );
        }

        return new ArrayList<>(unique.values());
    }

    /* Service 내부에서만 사용하는 분석기간 값 객체 */
    /*
     * 실제 결제·송금·정산 거래 한 건을 근거로 상품별 추천 사유를 만든다.
     *
     * "보험이 꼭 필요하다", "질병 위험이 있다"처럼 단정하지 않고
     * 확인된 거래 사실 + 관련 보장을 살펴볼 수 있다는 정도로만 안내한다.
     */
    private String createRuleBasedRecommendationReason(
            InsuranceRecommendationCandidateVO candidate
    ) {
        String coverageLabel =
                candidate.getInsuranceCategory() == null
                        || candidate.getInsuranceCategory().isBlank()
                        ? "관련"
                        : candidate.getInsuranceCategory();

        String merchantName = candidate.getEvidenceMerchantName();
        LocalDateTime createdAt = candidate.getEvidenceCreatedAt();

        if (createdAt != null
                && merchantName != null
                && !merchantName.isBlank()) {
            return createdAt.format(RECOMMENDATION_REASON_DATE_FORMAT)
                    + " "
                    + merchantName
                    + " 관련 거래가 확인되어, "
                    + coverageLabel
                    + " 관련 보장을 함께 살펴보실 수 있도록 이 상품을 추천드렸어요.";
        }

        if (merchantName != null && !merchantName.isBlank()) {
            return "최근 12개월 동안 "
                    + merchantName
                    + " 관련 거래가 확인되어, "
                    + coverageLabel
                    + " 관련 보장을 함께 살펴보실 수 있도록 이 상품을 추천드렸어요.";
        }

        if (createdAt != null) {
            return createdAt.format(RECOMMENDATION_REASON_DATE_FORMAT)
                    + " 관련 거래내역이 확인되어, "
                    + coverageLabel
                    + " 관련 보장을 함께 살펴보실 수 있도록 이 상품을 추천드렸어요.";
        }

        return "최근 12개월 소비내역에서 "
                + (candidate.getCategoryName() == null
                    || candidate.getCategoryName().isBlank()
                    ? "관련"
                    : candidate.getCategoryName())
                + " 관련 거래가 확인되어, "
                + coverageLabel
                + " 관련 보장을 함께 살펴보실 수 있도록 이 상품을 추천드렸어요.";
    }

    private static class AnalysisRange {
        private final LocalDateTime startAt;
        private final LocalDateTime endAt;

        private AnalysisRange(
                LocalDateTime startAt,
                LocalDateTime endAt
        ) {
            this.startAt = startAt;
            this.endAt = endAt;
        }
    }
}
