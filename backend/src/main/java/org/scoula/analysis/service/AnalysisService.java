package org.scoula.analysis.service;

import org.scoula.analysis.domain.SpendingAnalysisCategoryVO;
import org.scoula.analysis.domain.SpendingAnalysisVO;
import org.scoula.analysis.dto.*;

import java.util.List;

public interface AnalysisService {

    // 소비분석 가능한 상태(10건 이상 카테고리 분류)인지 체크하기.
    AnalysisAvailabilityDTO getAnalysisAvailability(
            Integer userId,
            Integer period
    );

    // 미분류 거래내역 조회
    UnclassifiedTransactionListDTO getUnclassifiedTransactions(
            Integer userId,
            Integer period
    );

    SpendingCategoryListDTO getSpendingCategories();

    TransactionClassificationResponseDTO classifyTransaction(
            Integer userId,
            Integer transactionId,
            Integer spendingCategoryId
    );

    // 분석 결과 저장
    Integer saveAnalysis(
            SpendingAnalysisVO spendingAnalysis, // spending_analysis_tbl에 저장할 분석 본문
            List<SpendingAnalysisCategoryVO> categories // 카테고리별 분석 결과를 저장
    );

    // 분석 실행
    AnalysisExecutionResponseDTO executeAnalysis(
            Integer userId,
            Integer period
    );
}