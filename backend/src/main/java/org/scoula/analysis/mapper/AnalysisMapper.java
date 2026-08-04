package org.scoula.analysis.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.analysis.domain.*;
import org.scoula.analysis.dto.AnalysisCategoryResultDTO;

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

    // 선택 기간의 정상 결제 거래 목록 조회 (카테고리 수정 화면용)
    List<AnalysisTransactionVO> selectAnalysisTransactions(
            @Param("userId") Integer userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    // 현재 사용자의 정상 결제 거래 단건 조회
    AnalysisTransactionVO selectAnalysisTransactionById(
            @Param("userId") Integer userId,
            @Param("transactionId") Integer transactionId
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

    // 현재 사용자가 수정할 수 있는 정상 결제 거래 조회
    ClassificationTargetTransactionVO
    selectClassificationTargetTransaction(
            @Param("userId") Integer userId,
            @Param("transactionId") Integer transactionId
    );

    // 동일 가맹점의 전체 정상 결제 거래 건수 조회
    int countSuccessfulPaymentsByMerchantName(
            @Param("merchantName") String merchantName
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

    // 현재 사용자의 소비분석 기본정보 조회
    AnalysisDetailVO selectAnalysisDetail(
            @Param("userId") Integer userId,
            @Param("spendingAnalysisId") Integer spendingAnalysisId
    );

    // 저장된 카테고리별 소비분석 결과 조회
    List<AnalysisCategoryResultDTO> selectAnalysisDetailCategories(
            @Param("spendingAnalysisId") Integer spendingAnalysisId
    );

    // 현재 사용자가 선택 기간에 가장 최근에 생성한 소비분석 ID 조회
    Integer selectLatestAnalysisId(
            @Param("userId") Integer userId,
            @Param("period") Integer period
    );


}