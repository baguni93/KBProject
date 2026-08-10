package org.scoula.insurancerecommendation.service;

import lombok.RequiredArgsConstructor;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.insurancerecommendation.domain.InsuranceRecommendationCandidateVO;
import org.scoula.insurancerecommendation.domain.InsuranceRecommendationVO;
import org.scoula.insurancerecommendation.mapper.InsuranceRecommendationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceRecommendationSaveServiceImpl
        implements InsuranceRecommendationSaveService {

    private final InsuranceRecommendationMapper insuranceRecommendationMapper;

    /*
     * 기존 추천 삭제 -> 새 추천 저장을 한 트랜잭션으로 묶는다.
     * recommendation_reason은 규칙 기반 근거,
     * ai_recommendation_summary는 해당 상품 전용 AI 설명이다.
     */
    @Override
    @Transactional
    public void saveRecommendations(
            Integer spendingAnalysisId,
            List<InsuranceRecommendationCandidateVO> candidates
    ) {
        try {
            insuranceRecommendationMapper.deleteRecommendations(
                    spendingAnalysisId
            );

            for (InsuranceRecommendationCandidateVO candidate : candidates) {
                InsuranceRecommendationVO recommendation =
                        InsuranceRecommendationVO.builder()
                                .spendingAnalysisId(spendingAnalysisId)
                                .insuranceProductId(
                                        candidate.getInsuranceProductId()
                                )
                                // DB 컬럼은 NOT NULL이므로 null 매핑 문구는 빈 문자열로 저장한다.
                                .recommendationReason(
                                        candidate.getRecommendationReason() == null
                                                ? ""
                                                : candidate.getRecommendationReason()
                                )
                                .aiRecommendationSummary(
                                        candidate.getAiRecommendationSummary()
                                )
                                .build();

                int inserted = insuranceRecommendationMapper
                        .insertRecommendation(recommendation);

                if (inserted != 1
                        || recommendation.getInsuranceRecommendationId() == null) {
                    throw new CustomException(
                            ErrorCode.INSURANCE_RECOMMENDATION_SAVE_FAILED
                    );
                }
            }

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(
                    ErrorCode.INSURANCE_RECOMMENDATION_SAVE_FAILED
            );
        }
    }
}
