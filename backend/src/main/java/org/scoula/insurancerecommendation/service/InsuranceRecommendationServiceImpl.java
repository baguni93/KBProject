package org.scoula.insurancerecommendation.service;

import lombok.RequiredArgsConstructor;
import org.scoula.analysis.dto.AnalysisDetailResponseDTO;
import org.scoula.analysis.service.AnalysisService;
import org.scoula.insurancerecommendation.domain.InsuranceRecommendationCandidateVO;
import org.scoula.insurancerecommendation.mapper.InsuranceRecommendationMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsuranceRecommendationServiceImpl
        implements InsuranceRecommendationService {

    private static final int REQUIRED_ANALYSIS_PERIOD = 3;

    private final InsuranceRecommendationMapper insuranceRecommendationMapper;
    private final AnalysisService analysisService;

    @Override
    public List<InsuranceRecommendationCandidateVO> getRecommendationCandidates(
            Integer userId,
            Integer spendingAnalysisId
    ) {

        // 소비 분석 상세 조회
        AnalysisDetailResponseDTO analysis =
                analysisService.getAnalysisDetail(
                        userId,
                        spendingAnalysisId
                );

        // 보험 추천은 3개월 소비 분석만 사용
        if (analysis.getPeriod() == null
                || analysis.getPeriod() != REQUIRED_ANALYSIS_PERIOD) {

            throw new IllegalArgumentException(
                    "보험 추천은 3개월 소비 분석 결과가 필요합니다."
            );
        }

        // 소비분석에서 이미 계산된 시작일과 종료일 사용
        LocalDate analysisStartDate =
                LocalDate.parse(
                        analysis.getAnalysisStartDate()
                );

        LocalDate analysisEndDate =
                LocalDate.parse(
                        analysis.getAnalysisEndDate()
                );

        // 시작일 00:00:00부터 조회
        LocalDateTime startAt =
                analysisStartDate.atStartOfDay();

        /*
         * 종료일도 하루 전체를 포함해야 하기 때문에
         * 종료일 다음 날 00:00:00 미만으로 조회한다.
         */
        LocalDateTime endAt =
                analysisEndDate
                        .plusDays(1)
                        .atStartOfDay();

        return insuranceRecommendationMapper.selectMatchedCandidates(
                userId,
                startAt,
                endAt
        );
    }
}