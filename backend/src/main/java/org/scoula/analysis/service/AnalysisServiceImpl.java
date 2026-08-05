package org.scoula.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.domain.*;
import org.scoula.analysis.dto.*;
import org.scoula.analysis.mapper.AnalysisMapper;
import org.scoula.analysis.mapper.MerchantCategoryMappingMapper;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



@Log4j2
@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    // 분석에 필요한 최소 카테고리 분류 개수는 10개
    private static final int REQUIRED_TRANSACTION_COUNT = 10;

    // 소비분석 및 거래 조회 Mapper
    private final AnalysisMapper analysisMapper;

    // 가맹점 카테고리 매핑 조회·수정·삭제 Mapper
    private final MerchantCategoryMappingMapper merchantCategoryMappingMapper;

    private final AnalysisSaveService analysisSaveService;
    private final AnalysisNarrativeService analysisNarrativeService;


    @Override
    public AnalysisAvailabilityDTO getAnalysisAvailability(
            Integer userId,
            Integer period
    ) {
        validatePeriod(period);

        /*
         * 예:
         * 오늘이 2026-07-30이고 period(개월)가 1이면
         *
         * 시작일: 2026-06-30
         * 종료일: 2026-07-30
         */
        LocalDate analysisEndDate = LocalDate.now();
        LocalDate analysisStartDate =
                analysisEndDate.minusMonths(period);

        LocalDateTime startAt =
                analysisStartDate.atStartOfDay();

        /*
         * 종료일의 모든 거래를 포함하기 위해
         * 다음 날 00:00 미만으로 조회한다.
         */
        LocalDateTime endAt =
                analysisEndDate
                        .plusDays(1)
                        .atStartOfDay();

        AnalysisCountVO count =
                analysisMapper.selectAnalysisCount(
                        userId,
                        startAt,
                        endAt
                );

        // 결과 DTO로 부터, 가져오고 없으면 0으로 표기
        int totalPaymentCount =
                count == null
                        || count.getTotalPaymentCount() == null
                        ? 0
                        : count.getTotalPaymentCount();

        int classifiedPaymentCount =
                count == null
                        || count.getClassifiedPaymentCount() == null
                        ? 0
                        : count.getClassifiedPaymentCount();

        int unclassifiedPaymentCount =
                Math.max(
                        totalPaymentCount - classifiedPaymentCount,
                        0
                );

        int remainingCount =
                Math.max(
                        REQUIRED_TRANSACTION_COUNT
                                - classifiedPaymentCount,
                        0
                );

        boolean available =
                classifiedPaymentCount
                        >= REQUIRED_TRANSACTION_COUNT;

        String message;

        if (available) {
            message = "소비 분석이 가능합니다.";
        } else {
            message =
                    "분류 완료된 결제 거래가 "
                            + remainingCount
                            + "건 더 필요합니다.";
        }

        log.info(
                "소비 분석 가능 여부 조회 userId={}, period={}, "
                        + "totalCount={}, classifiedCount={}, available={}",
                userId,
                period,
                totalPaymentCount,
                classifiedPaymentCount,
                available
        );

        return AnalysisAvailabilityDTO.builder()
                .period(period)
                .periodLabel(createPeriodLabel(period))
                .analysisStartDate(analysisStartDate.toString())
                .analysisEndDate(analysisEndDate.toString())
                .totalPaymentCount(totalPaymentCount)
                .classifiedPaymentCount(classifiedPaymentCount)
                .unclassifiedPaymentCount(unclassifiedPaymentCount)
                .requiredCount(REQUIRED_TRANSACTION_COUNT)
                .remainingCount(remainingCount)
                .available(available)
                .message(message)
                .build();
    }

    // 1개월, 3개월, 12개월 인지 검증하기. ->DB에도 1,3,12 개월로 정해져있음.
    private void validatePeriod(Integer period) {
        if (period == null
                || (period != 1
                && period != 3
                && period != 12)) {

            throw new CustomException(
                    ErrorCode.INVALID_ANALYSIS_PERIOD
            );
        }
    }

    private String createPeriodLabel(Integer period) {
        return "최근 " + period + "개월";
    }


    @Override
    public AnalysisTransactionListDTO getAnalysisTransactions(
            Integer userId,
            Integer period
    ) {
        validatePeriod(period);

        LocalDate analysisEndDate = LocalDate.now();
        LocalDate analysisStartDate = analysisEndDate.minusMonths(period);
        LocalDateTime startAt = analysisStartDate.atStartOfDay();
        LocalDateTime endAt = analysisEndDate.plusDays(1).atStartOfDay();

        List<AnalysisTransactionDTO> transactions =
                analysisMapper.selectAnalysisTransactions(userId, startAt, endAt)
                        .stream()
                        .map(vo -> AnalysisTransactionDTO.builder()
                                .transactionId(vo.getTransactionId())
                                .merchantName(vo.getMerchantName())
                                .amount(vo.getAmount())
                                .createdAt(vo.getCreatedAt())
                                .spendingCategoryId(vo.getSpendingCategoryId())
                                .categoryName(vo.getCategoryName())
                                .parentCategoryId(vo.getParentCategoryId())
                                .parentCategoryName(vo.getParentCategoryName())
                                .build())
                        .collect(Collectors.toList());

        return AnalysisTransactionListDTO.builder()
                .period(period)
                .periodLabel(createPeriodLabel(period))
                .analysisStartDate(analysisStartDate.toString())
                .analysisEndDate(analysisEndDate.toString())
                .transactionCount(transactions.size())
                .transactions(transactions)
                .build();
    }

    @Override
    public AnalysisTransactionDTO getAnalysisTransaction(
            Integer userId,
            Integer transactionId
    ) {
        if (transactionId == null || transactionId <= 0) {
            throw new CustomException(
                    ErrorCode.CLASSIFICATION_TRANSACTION_NOT_FOUND
            );
        }

        AnalysisTransactionVO transaction =
                analysisMapper.selectAnalysisTransactionById(
                        userId,
                        transactionId
                );

        if (transaction == null) {
            throw new CustomException(
                    ErrorCode.CLASSIFICATION_TRANSACTION_NOT_FOUND
            );
        }

        return AnalysisTransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .merchantName(transaction.getMerchantName())
                .amount(transaction.getAmount())
                .createdAt(transaction.getCreatedAt())
                .spendingCategoryId(transaction.getSpendingCategoryId())
                .categoryName(transaction.getCategoryName())
                .parentCategoryId(transaction.getParentCategoryId())
                .parentCategoryName(transaction.getParentCategoryName())
                .build();
    }

    @Override
    public AnalysisTransactionListDTO getAnalysisTransactionsByAnalysisId(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        if (spendingAnalysisId == null || spendingAnalysisId <= 0) {
            throw new CustomException(ErrorCode.ANALYSIS_RESULT_NOT_FOUND);
        }

        AnalysisDetailVO analysisDetail = analysisMapper.selectAnalysisDetail(
                userId,
                spendingAnalysisId
        );

        if (analysisDetail == null) {
            throw new CustomException(ErrorCode.ANALYSIS_RESULT_NOT_FOUND);
        }

        LocalDate analysisEndDate = analysisDetail.getCreatedAt().toLocalDate();
        LocalDate analysisStartDate = analysisEndDate.minusMonths(
                analysisDetail.getAnalysisPeriod()
        );
        LocalDateTime startAt = analysisStartDate.atStartOfDay();
        LocalDateTime endAt = analysisEndDate.plusDays(1).atStartOfDay();

        List<AnalysisTransactionDTO> transactions =
                analysisMapper.selectAnalysisTransactions(userId, startAt, endAt)
                        .stream()
                        .map(vo -> AnalysisTransactionDTO.builder()
                                .transactionId(vo.getTransactionId())
                                .merchantName(vo.getMerchantName())
                                .amount(vo.getAmount())
                                .createdAt(vo.getCreatedAt())
                                .spendingCategoryId(vo.getSpendingCategoryId())
                                .categoryName(vo.getCategoryName())
                                .parentCategoryId(vo.getParentCategoryId())
                                .parentCategoryName(vo.getParentCategoryName())
                                .build())
                        .collect(Collectors.toList());

        return AnalysisTransactionListDTO.builder()
                .period(analysisDetail.getAnalysisPeriod())
                .periodLabel(createPeriodLabel(analysisDetail.getAnalysisPeriod()))
                .analysisStartDate(analysisStartDate.toString())
                .analysisEndDate(analysisEndDate.toString())
                .transactionCount(transactions.size())
                .transactions(transactions)
                .build();
    }

    @Override
    public UnclassifiedTransactionListDTO getUnclassifiedTransactions(
            Integer userId,
            Integer period
    ) {
        validatePeriod(period);

        LocalDate analysisEndDate = LocalDate.now();
        LocalDate analysisStartDate =
                analysisEndDate.minusMonths(period);

        LocalDateTime startAt =
                analysisStartDate.atStartOfDay();

        LocalDateTime endAt =
                analysisEndDate
                        .plusDays(1)
                        .atStartOfDay();

        List<UnclassifiedTransactionVO> transactionVOList =
                analysisMapper.selectUnclassifiedTransactions(
                        userId,
                        startAt,
                        endAt
                );

        List<UnclassifiedTransactionDTO> transactions =
                transactionVOList.stream()
                        .map(vo ->
                                UnclassifiedTransactionDTO.builder()
                                        .transactionId(
                                                vo.getTransactionId()
                                        )
                                        .merchantName(
                                                vo.getMerchantName()
                                        )
                                        .amount(
                                                vo.getAmount()
                                        )
                                        .createdAt(
                                                vo.getCreatedAt()
                                        )
                                        .build()
                        )
                        .collect(Collectors.toList());

        log.info(
                "미분류 결제 거래 조회 userId={}, period={}, count={}",
                userId,
                period,
                transactions.size()
        );

        return UnclassifiedTransactionListDTO.builder()
                .period(period)
                .periodLabel(createPeriodLabel(period))
                .analysisStartDate(
                        analysisStartDate.toString()
                )
                .analysisEndDate(
                        analysisEndDate.toString()
                )
                .unclassifiedCount(
                        transactions.size()
                )
                .transactions(
                        transactions
                )
                .build();
    }

    // 소비 카테고리 조회
    @Override
    public SpendingCategoryListDTO getSpendingCategories() {

        List<SpendingCategoryVO> categoryVOList =
                analysisMapper.selectSpendingCategories();

        List<SpendingCategoryDTO> categories =
                categoryVOList.stream()
                        .map(category ->
                                SpendingCategoryDTO.builder()
                                        .spendingCategoryId(
                                                category.getSpendingCategoryId()
                                        )
                                        .categoryName(
                                                category.getCategoryName()
                                        )
                                        .parentCategoryId(
                                                category.getParentCategoryId()
                                        )
                                        .build()
                        )
                        .collect(Collectors.toList());

        log.info(
                "소비 카테고리 목록 조회 count={}",
                categories.size()
        );

        return SpendingCategoryListDTO.builder()
                .categoryCount(categories.size())
                .categories(categories)
                .build();
    }

    // 사용자가 결제 거래의 소비 카테고리를 직접 지정하거나 수정
    @Override
    @Transactional
    public TransactionClassificationResponseDTO classifyTransaction(
            Integer userId,
            Integer transactionId,
            Integer spendingCategoryId
    ) {
        // 카테고리 ID가 입력되지 않은 경우
        if (spendingCategoryId == null) {
            throw new CustomException(
                    ErrorCode.SPENDING_CATEGORY_NOT_FOUND
            );
        }

        // 사용자가 선택한 카테고리가 실제 DB에 존재하는지 확인
        SpendingCategoryVO category =
                analysisMapper.selectSpendingCategoryById(
                        spendingCategoryId
                );

        if (category == null) {
            throw new CustomException(
                    ErrorCode.SPENDING_CATEGORY_NOT_FOUND
            );
        }

        /*
         * 현재 사용자가 수정할 수 있는 거래인지 먼저 확인한다.
         *
         * 조회 조건:
         * - 현재 사용자 거래
         * - PAYMENT 거래
         * - SUCCESS 상태
         */
        ClassificationTargetTransactionVO targetTransaction =
                analysisMapper
                        .selectClassificationTargetTransaction(
                                userId,
                                transactionId
                        );

        if (targetTransaction == null) {
            throw new CustomException(
                    ErrorCode.CLASSIFICATION_TRANSACTION_NOT_FOUND
            );
        }

        String merchantName =
                targetTransaction.getMerchantName();

        /*
         * 가맹점명이 존재하는 경우에만
         * 공통 가맹점 매핑의 신뢰도를 처리한다.
         */
        if (merchantName != null
                && !merchantName.isBlank()) {

            // 매핑 조회 기준은 앞뒤 공백만 제거한 가맹점명
            String trimmedMerchantName =
                    merchantName.trim();

            MerchantCategoryMappingVO mapping =
                    merchantCategoryMappingMapper
                            .selectByMerchantName(
                                    trimmedMerchantName
                            );

            /*
             * 기존 매핑이 존재하고,
             * 사용자가 선택한 카테고리가 매핑 카테고리와 다르면
             * 매핑에 대한 수정 요청으로 판단한다.
             */
            if (mapping != null
                    && !Objects.equals(
                    mapping.getSpendingCategoryId(),
                    spendingCategoryId
            )) {

                int increasedCount =
                        merchantCategoryMappingMapper
                                .increaseCorrectionCount(
                                        trimmedMerchantName
                                );

                if (increasedCount == 1) {
                    /*
                     * correction_count 증가 후 최신 값을 다시 조회한다.
                     * increaseCorrectionCount의 반환값은 수정된 행 개수이므로
                     * 실제 수정 횟수를 얻으려면 재조회가 필요하다.
                     */
                    MerchantCategoryMappingVO updatedMapping =
                            merchantCategoryMappingMapper
                                    .selectByMerchantName(
                                            trimmedMerchantName
                                    );

                    int correctionCount =
                            updatedMapping == null
                                    || updatedMapping
                                    .getCorrectionCount() == null
                                    ? 0
                                    : updatedMapping
                                    .getCorrectionCount();

                    // 동일 가맹점의 전체 정상 결제 거래 건수
                    int totalTransactionCount =
                            analysisMapper
                                    .countSuccessfulPaymentsByMerchantName(
                                            trimmedMerchantName
                                    );

                    /*
                     * 수정 비율이 정확히 5%이거나 5%를 초과하면
                     * 해당 가맹점 매핑을 삭제한다.
                     *
                     * 소수점 계산 대신 정수 곱셈으로 비교한다.
                     * correctionCount / totalTransactionCount >= 0.05
                     */
                    boolean mappingShouldBeDeleted =
                            totalTransactionCount > 0
                                    && correctionCount * 100
                                    >= totalTransactionCount * 5;

                    boolean mappingDeleted = false;

                    if (mappingShouldBeDeleted) {
                        int deletedCount =
                                merchantCategoryMappingMapper
                                        .deleteByMerchantName(
                                                trimmedMerchantName
                                        );

                        mappingDeleted =
                                deletedCount == 1;
                    }

                    log.info(
                            "가맹점 매핑 수정 요청 반영 "
                                    + "merchantName={}, "
                                    + "mappingCategoryId={}, "
                                    + "selectedCategoryId={}, "
                                    + "correctionCount={}, "
                                    + "totalTransactionCount={}, "
                                    + "mappingDeleted={}",
                            trimmedMerchantName,
                            mapping.getSpendingCategoryId(),
                            spendingCategoryId,
                            correctionCount,
                            totalTransactionCount,
                            mappingDeleted
                    );

                } else {
                    /*
                     * 조회 후 다른 요청에서 매핑이 먼저 삭제되는 등
                     * 매핑 수정 행이 없더라도 현재 거래 분류는 계속 진행한다.
                     */
                    log.warn(
                            "가맹점 매핑 수정 횟수 증가 실패 "
                                    + "merchantName={}",
                            trimmedMerchantName
                    );
                }
            }
        }

        /*
         * 매핑 삭제 여부와 관계없이 현재 거래에는
         * 사용자가 직접 선택한 카테고리를 저장한다.
         */
        int updatedCount =
                analysisMapper.updateTransactionCategory(
                        userId,
                        transactionId,
                        spendingCategoryId
                );

        if (updatedCount != 1) {
            /*
             * 이 예외가 발생하면 @Transactional에 의해
             * correction_count 증가와 매핑 삭제도 함께 롤백된다.
             */
            throw new CustomException(
                    ErrorCode.CLASSIFICATION_TRANSACTION_NOT_FOUND
            );
        }

        log.info(
                "결제 거래 직접 분류 완료 "
                        + "userId={}, transactionId={}, "
                        + "previousCategoryId={}, "
                        + "spendingCategoryId={}",
                userId,
                transactionId,
                targetTransaction.getSpendingCategoryId(),
                spendingCategoryId
        );

        return TransactionClassificationResponseDTO.builder()
                .transactionId(transactionId)
                .spendingCategoryId(
                        category.getSpendingCategoryId()
                )
                .categoryName(
                        category.getCategoryName()
                )
                .message(
                        "소비 카테고리 분류가 완료되었습니다."
                )
                .build();
    }



    @Override
    public AnalysisExecutionResponseDTO executeAnalysis(
            Integer userId,
            Integer period
    ) {
        validatePeriod(period);

        LocalDate analysisEndDate =
                LocalDate.now();

        LocalDate analysisStartDate =
                analysisEndDate.minusMonths(period);

        LocalDateTime startAt =
                analysisStartDate.atStartOfDay();

        LocalDateTime endAt =
                analysisEndDate
                        .plusDays(1)
                        .atStartOfDay();

        /*
         * 분류 완료된 성공 결제 거래가
         * 최소 10건 이상인지 다시 확인한다.
         *
         * 화면에서 가능 여부를 먼저 조회했더라도,
         * 실제 분석 실행 시점에 서버에서 반드시 재검증한다.
         */
        AnalysisCountVO count =
                analysisMapper.selectAnalysisCount(
                        userId,
                        startAt,
                        endAt
                );

        int classifiedTransactionCount =
                count == null
                        || count.getClassifiedPaymentCount() == null
                        ? 0
                        : count.getClassifiedPaymentCount();

        if (classifiedTransactionCount
                < REQUIRED_TRANSACTION_COUNT) {

            throw new CustomException(
                    ErrorCode
                            .ANALYSIS_TRANSACTION_COUNT_NOT_ENOUGH
            );
        }

        /*
         * 카테고리별 금액과 건수를 조회한다.
         *
         * SQL에서 의료 하위 카테고리는
         * 부모인 의료 카테고리로 합산된다.
         */
        List<AnalysisCategoryAggregateVO> aggregateList =
                analysisMapper
                        .selectAnalysisCategoryAggregates(
                                userId,
                                startAt,
                                endAt
                        );

        if (aggregateList == null
                || aggregateList.isEmpty()) {

            throw new CustomException(
                    ErrorCode.ANALYSIS_TOTAL_AMOUNT_ZERO
            );
        }

        long totalSpendingAmountLong =
                aggregateList.stream()
                        .mapToLong(aggregate ->
                                aggregate.getSpendingAmount() == null
                                        ? 0L
                                        : aggregate.getSpendingAmount()
                        )
                        .sum();

        if (totalSpendingAmountLong <= 0) {
            throw new CustomException(
                    ErrorCode.ANALYSIS_TOTAL_AMOUNT_ZERO
            );
        }

        /*
         * 현재 DB의 spending_amount가 INT이므로
         * 범위를 넘어가면 예외를 발생시킨다.
         */
        int totalSpendingAmount =
                Math.toIntExact(totalSpendingAmountLong);

        List<AnalysisCategoryResultDTO> categoryResults =
                createCategoryResults(
                        aggregateList,
                        totalSpendingAmountLong
                );

        /*
         * SQL이 금액 내림차순으로 정렬되므로
         * 첫 번째 항목이 대표 카테고리다.
         *
         * 금액이 같다면 거래 건수,
         * 거래 건수도 같다면 카테고리 ID 순이다.
         */
        AnalysisCategoryResultDTO representativeCategory =
                categoryResults.get(0);

        // 카테고리별 소비 패턴을 기반으로 AI 칭호와 분석 요약을 생성한다
        AnalysisNarrativeDTO narrative =
                analysisNarrativeService.createNarrative(
                        period,
                        representativeCategory,
                        categoryResults
                );

        SpendingAnalysisVO spendingAnalysis =
                SpendingAnalysisVO.builder()
                        .userId(userId)
                        .analysisPeriod(period)
                        .representativeCategoryId(
                                representativeCategory
                                        .getSpendingCategoryId()
                        )
                        .aiTitle(
                                narrative.getTitle()
                        )
                        .aiAnalysisSummary(
                                narrative.getSummary()
                        )
                        .build();

        List<SpendingAnalysisCategoryVO> saveCategories =
                categoryResults.stream()
                        .map(category ->
                                SpendingAnalysisCategoryVO.builder()
                                        .spendingCategoryId(
                                                category
                                                        .getSpendingCategoryId()
                                        )
                                        .spendingAmount(
                                                category
                                                        .getSpendingAmount()
                                        )
                                        .spendingRatio(
                                                category
                                                        .getSpendingRatio()
                                        )
                                        .transactionCount(
                                                category
                                                        .getTransactionCount()
                                        )
                                        .build()
                        )
                        .collect(Collectors.toList());

        Integer spendingAnalysisId =
                analysisSaveService.saveAnalysis(
                        spendingAnalysis,
                        saveCategories
                );

        log.info(
                "소비분석 실행 완료 userId={}, period={}, "
                        + "spendingAnalysisId={}, "
                        + "classifiedTransactionCount={}, "
                        + "totalSpendingAmount={}",
                userId,
                period,
                spendingAnalysisId,
                classifiedTransactionCount,
                totalSpendingAmount
        );

        return AnalysisExecutionResponseDTO.builder()
                .spendingAnalysisId(
                        spendingAnalysisId
                )
                .period(period)
                .periodLabel(
                        createPeriodLabel(period)
                )
                .analysisStartDate(
                        analysisStartDate.toString()
                )
                .analysisEndDate(
                        analysisEndDate.toString()
                )
                .totalSpendingAmount(
                        totalSpendingAmount
                )
                .classifiedTransactionCount(
                        classifiedTransactionCount
                )
                .representativeCategoryId(
                        representativeCategory
                                .getSpendingCategoryId()
                )
                .representativeCategoryName(
                        representativeCategory
                                .getCategoryName()
                )
                .aiTitle(
                        narrative.getTitle()
                )
                .aiAnalysisSummary(
                        narrative.getSummary()
                )
                .categories(
                        categoryResults
                )
                .message(
                        "소비 분석이 완료되었습니다."
                )
                .build();
    }

    private List<AnalysisCategoryResultDTO>
    createCategoryResults(
            List<AnalysisCategoryAggregateVO> aggregateList,
            long totalSpendingAmount
    ) {
        List<AnalysisCategoryResultDTO> results =
                new ArrayList<>();

        BigDecimal totalAmount =
                BigDecimal.valueOf(totalSpendingAmount);

        for (AnalysisCategoryAggregateVO aggregate
                : aggregateList) {

            long amount =
                    aggregate.getSpendingAmount() == null
                            ? 0L
                            : aggregate.getSpendingAmount();

            long transactionCount =
                    aggregate.getTransactionCount() == null
                            ? 0L
                            : aggregate.getTransactionCount();

            BigDecimal ratio =
                    BigDecimal.valueOf(amount)
                            .multiply(
                                    BigDecimal.valueOf(100)
                            )
                            .divide(
                                    totalAmount,
                                    2,
                                    RoundingMode.HALF_UP
                            );

            results.add(
                    AnalysisCategoryResultDTO.builder()
                            .spendingCategoryId(
                                    aggregate
                                            .getSpendingCategoryId()
                            )
                            .categoryName(
                                    aggregate.getCategoryName()
                            )
                            .spendingAmount(
                                    Math.toIntExact(amount)
                            )
                            .spendingRatio(ratio)
                            .transactionCount(
                                    Math.toIntExact(
                                            transactionCount
                                    )
                            )
                            .build()
            );
        }

        /*
         * 각 비율을 소수점 둘째 자리에서 반올림하면
         * 합계가 99.99 또는 100.01이 될 수 있다.
         *
         * 차이를 대표 카테고리에 반영하여
         * 최종 합계를 정확히 100.00으로 맞춘다.
         */
        BigDecimal ratioSum =
                results.stream()
                        .map(
                                AnalysisCategoryResultDTO
                                        ::getSpendingRatio
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal difference =
                new BigDecimal("100.00")
                        .subtract(ratioSum);

        AnalysisCategoryResultDTO firstResult =
                results.get(0);

        firstResult.setSpendingRatio(
                firstResult.getSpendingRatio()
                        .add(difference)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );

        return results;
    }

    // 저장된 소비분석 상세 결과 조회
    @Override
    public AnalysisDetailResponseDTO getAnalysisDetail(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        // 소비분석 ID가 없거나 올바르지 않으면 조회하지 않는다.
        if (spendingAnalysisId == null
                || spendingAnalysisId <= 0) {

            throw new CustomException(
                    ErrorCode.ANALYSIS_RESULT_NOT_FOUND
            );
        }

        /*
         * 소비분석 ID와 현재 사용자 ID를 함께 사용해 조회한다.
         * 다른 사용자의 분석 결과는 조회할 수 없다.
         */
        AnalysisDetailVO analysisDetail =
                analysisMapper.selectAnalysisDetail(
                        userId,
                        spendingAnalysisId
                );

        if (analysisDetail == null) {
            throw new CustomException(
                    ErrorCode.ANALYSIS_RESULT_NOT_FOUND
            );
        }

        // 저장된 카테고리별 분석 결과 조회
        List<AnalysisCategoryResultDTO> categories =
                analysisMapper.selectAnalysisDetailCategories(
                        spendingAnalysisId
                );

        /*
         * MyBatis는 일반적으로 조회 결과가 없으면 빈 List를 반환하지만,
         * null이 반환되는 경우도 안전하게 처리한다.
         */
        if (categories == null) {
            categories = List.of();
        }

        /*
         * 전체 소비 금액은 저장된 카테고리별 소비 금액을 합산한다.
         * 중간 계산은 long으로 처리한 뒤 Integer로 변환한다.
         */
        long totalSpendingAmountLong =
                categories.stream()
                        .mapToLong(category ->
                                category.getSpendingAmount() == null
                                        ? 0L
                                        : category.getSpendingAmount()
                        )
                        .sum();

        /*
         * 분석에 포함된 거래 건수도
         * 카테고리별 거래 건수를 합산해서 구한다.
         */
        long classifiedTransactionCountLong =
                categories.stream()
                        .mapToLong(category ->
                                category.getTransactionCount() == null
                                        ? 0L
                                        : category.getTransactionCount()
                        )
                        .sum();

        int totalSpendingAmount =
                Math.toIntExact(
                        totalSpendingAmountLong
                );

        int classifiedTransactionCount =
                Math.toIntExact(
                        classifiedTransactionCountLong
                );

        /*
         * 분석 기간의 종료일은 분석 결과가 생성된 날짜로 본다.
         * 시작일은 종료일에서 분석 개월 수를 뺀 날짜다.
         */
        LocalDate analysisEndDate =
                analysisDetail.getCreatedAt()
                        .toLocalDate();

        LocalDate analysisStartDate =
                analysisEndDate.minusMonths(
                        analysisDetail.getAnalysisPeriod()
                );

        log.info(
                "소비분석 상세조회 완료 "
                        + "userId={}, spendingAnalysisId={}, "
                        + "period={}, categoryCount={}",
                userId,
                spendingAnalysisId,
                analysisDetail.getAnalysisPeriod(),
                categories.size()
        );

        return AnalysisDetailResponseDTO.builder()
                .spendingAnalysisId(
                        analysisDetail.getSpendingAnalysisId()
                )
                .period(
                        analysisDetail.getAnalysisPeriod()
                )
                .periodLabel(
                        createPeriodLabel(
                                analysisDetail.getAnalysisPeriod()
                        )
                )
                .analysisStartDate(
                        analysisStartDate.toString()
                )
                .analysisEndDate(
                        analysisEndDate.toString()
                )
                .totalSpendingAmount(
                        totalSpendingAmount
                )
                .classifiedTransactionCount(
                        classifiedTransactionCount
                )
                .representativeCategoryId(
                        analysisDetail
                                .getRepresentativeCategoryId()
                )
                .representativeCategoryName(
                        analysisDetail
                                .getRepresentativeCategoryName()
                )
                .aiTitle(
                        analysisDetail.getAiTitle()
                )
                .aiAnalysisSummary(
                        analysisDetail.getAiAnalysisSummary()
                )
                .aiCardRecommendationSummary(
                        analysisDetail.getAiCardRecommendationSummary()
                )
                .aiInsuranceRecommendationSummary(
                        analysisDetail.getAiInsuranceRecommendationSummary()
                )
                .createdAt(
                        analysisDetail.getCreatedAt()
                )
                .categories(
                        categories
                )
                .message(
                        "소비 분석 결과를 조회했습니다."
                )
                .build();
    }

    // 현재 사용자의 선택 기간별 가장 최근 소비분석 상세 결과 조회
    @Override
    public AnalysisDetailResponseDTO getLatestAnalysisDetail(
            Integer userId,
            Integer period
    ) {
        validatePeriod(period);

        /*
         * 현재 사용자가 선택 기간에 생성한 소비분석 중
         * 가장 최근 분석 ID를 조회한다.
         */
        Integer latestSpendingAnalysisId =
                analysisMapper.selectLatestAnalysisId(
                        userId,
                        period
                );

        // 생성한 소비분석 결과가 하나도 없는 경우
        if (latestSpendingAnalysisId == null) {
            throw new CustomException(
                    ErrorCode.ANALYSIS_RESULT_NOT_FOUND
            );
        }

        log.info(
                "기간별 최근 소비분석 조회 "
                        + "userId={}, period={}, spendingAnalysisId={}",
                userId,
                period,
                latestSpendingAnalysisId
        );

        /*
         * 기본정보 조회, 카테고리 목록 조회 및 합산 로직은
         * 기존 상세조회 메서드를 그대로 재사용한다.
         */
        return getAnalysisDetail(
                userId,
                latestSpendingAnalysisId
        );
    }

}