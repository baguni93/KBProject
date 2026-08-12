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

    // 선택 기간의 결제 거래 목록 조회 (카테고리 확인·수정)
    AnalysisTransactionListDTO getAnalysisTransactions(
            Integer userId,
            Integer period
    );

    // 분석 기간과 무관한 전체 정상 결제 거래 조회
    AnalysisTransactionListDTO getAllPaymentTransactions(
            Integer userId
    );

    // 현재 사용자의 결제 거래 단건 조회
    AnalysisTransactionDTO getAnalysisTransaction(
            Integer userId,
            Integer transactionId
    );

    // 저장된 분석 결과의 실제 분석 기간에 포함된 전체 결제 거래 조회
    AnalysisTransactionListDTO getAnalysisTransactionsByAnalysisId(
            Integer userId,
            Integer spendingAnalysisId
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


    // 분석 실행
    AnalysisExecutionResponseDTO executeAnalysis(
            Integer userId,
            Integer period
    );


    // 저장된 소비분석 상세 결과 조회
    AnalysisDetailResponseDTO getAnalysisDetail(
            Integer userId,
            Integer spendingAnalysisId
    );

    // 현재 사용자의 선택 기간별 가장 최근 소비분석 상세 결과 조회
    AnalysisDetailResponseDTO getLatestAnalysisDetail(
            Integer userId,
            Integer period
    );


}