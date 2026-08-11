package org.scoula.insurancerecommendation.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.insurancerecommendation.domain.*;

import java.time.LocalDateTime;
import java.util.List;

public interface InsuranceRecommendationMapper {

    /*
     * 분석기간의 실제 금융거래를 기준으로
     * 보험-소비카테고리 매핑에 맞는 추천 후보를 계산한다.
     */
    List<InsuranceRecommendationCandidateVO> selectMatchedCandidates(
            @Param("userId") Integer userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    // 해당 소비분석에 저장된 보험 추천 결과 개수
    int countRecommendations(
            @Param("spendingAnalysisId") Integer spendingAnalysisId
    );

    // 저장 전 이전/불완전 결과를 정리할 때 사용
    int deleteRecommendations(
            @Param("spendingAnalysisId") Integer spendingAnalysisId
    );

    // 계산된 추천 후보 한 건 저장
    int insertRecommendation(
            InsuranceRecommendationVO recommendation
    );

    // 전체 보험 둘러보기 목록 조회 (category가 null/blank면 전체)
    List<InsuranceProductVO> selectInsuranceProducts(
            @Param("category") String category
    );

    // 추천 여부와 무관한 보험상품 상세 조회
    InsuranceProductVO selectInsuranceProduct(
            @Param("insuranceProductId") Integer insuranceProductId
    );

    // 저장된 보험추천 목록 조회
    List<InsuranceRecommendationListItemVO> selectRecommendationList(
            @Param("userId") Integer userId,
            @Param("spendingAnalysisId") Integer spendingAnalysisId
    );

    // 보험추천 상세 헤더 조회 + 사용자 소유권 확인
    InsuranceRecommendationDetailHeaderVO selectRecommendationDetailHeader(
            @Param("userId") Integer userId,
            @Param("insuranceRecommendationId") Integer insuranceRecommendationId
    );

    // 보험 상품 주요 보장내용 조회
    List<InsuranceCoverageVO> selectInsuranceCoverages(
            @Param("insuranceProductId") Integer insuranceProductId
    );

    // 특정 추천 상품의 근거가 된 실제 거래 조회
    List<InsuranceRecommendationTransactionVO> selectRecommendationTransactions(
            @Param("userId") Integer userId,
            @Param("insuranceProductId") Integer insuranceProductId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    // 추천 근거 상세 화면용 카테고리별 실제 거래 집계
    List<InsuranceRecommendationEvidenceCategoryVO> selectRecommendationEvidenceCategories(
            @Param("userId") Integer userId,
            @Param("spendingAnalysisId") Integer spendingAnalysisId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
