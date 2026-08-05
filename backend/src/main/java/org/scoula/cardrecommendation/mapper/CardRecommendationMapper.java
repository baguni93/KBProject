package org.scoula.cardrecommendation.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.cardrecommendation.domain.*;

import java.time.LocalDateTime;
import java.util.List;

public interface CardRecommendationMapper {

    CardRecommendationAnalysisVO selectRecommendationAnalysis(
            @Param("userId") Integer userId,
            @Param("spendingAnalysisId") Integer spendingAnalysisId
    );

    List<CardRecommendationCategoryVO> selectAnalysisCategories(
            @Param("spendingAnalysisId") Integer spendingAnalysisId
    );

    int countRecommendations(
            @Param("spendingAnalysisId") Integer spendingAnalysisId
    );

    List<CardProductVO> selectCardProducts();

    List<CardBenefitVO> selectCardBenefits();

    List<CardRecommendationTransactionVO> selectRecommendationTransactions(
            @Param("userId") Integer userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    int insertCardRecommendation(CardRecommendationVO recommendation);

    int insertCardRecommendationDetails(
            @Param("details") List<CardRecommendationDetailVO> details
    );

    int updateCardRecommendationSummary(
            @Param("userId") Integer userId,
            @Param("spendingAnalysisId") Integer spendingAnalysisId,
            @Param("summary") String summary
    );

    List<CardRecommendationListItemVO> selectRecommendationList(
            @Param("userId") Integer userId,
            @Param("spendingAnalysisId") Integer spendingAnalysisId,
            @Param("cardType") String cardType
    );

    CardRecommendationDetailHeaderVO selectRecommendationDetailHeader(
            @Param("userId") Integer userId,
            @Param("cardRecommendationId") Integer cardRecommendationId
    );

    List<CardRecommendationBenefitDetailVO> selectRecommendationBenefitDetails(
            @Param("cardRecommendationId") Integer cardRecommendationId
    );
}
