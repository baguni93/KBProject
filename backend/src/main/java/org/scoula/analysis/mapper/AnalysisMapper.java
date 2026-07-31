package org.scoula.analysis.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.analysis.domain.*;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalysisMapper {

    // 선택한 기간 안에 존재하는 정상 결제 거래수와,
    // 카테고리 분류 완료 거래수를 조회한다. + 시간은 고려 X
    AnalysisCountVO selectAnalysisCount(
            @Param("userId") Integer userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    // 카테고리 미분류 목록 조회
    List<UnclassifiedTransactionVO> selectUnclassifiedTransactions(
            @Param("userId") Integer userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    // 소비 분석 결과 삽입
    int insertSpendingAnalysis(
            SpendingAnalysisVO spendingAnalysis
    );

    // 소비 카테고리 삽입
    int insertSpendingAnalysisCategories(
            @Param("categories")
            List<SpendingAnalysisCategoryVO> categories
    );

    List<SpendingCategoryVO> selectSpendingCategories();

    SpendingCategoryVO selectSpendingCategoryById(
            @Param("spendingCategoryId")
            Integer spendingCategoryId
    );

    int updateTransactionCategory(
            @Param("userId") Integer userId,
            @Param("transactionId") Integer transactionId,
            @Param("spendingCategoryId") Integer spendingCategoryId
    );

    List<AnalysisCategoryAggregateVO> selectAnalysisCategoryAggregates(
            @Param("userId") Integer userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );



}