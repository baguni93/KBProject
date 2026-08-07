package org.scoula.insurancerecommendation.mapper;

import org.scoula.insurancerecommendation.domain.InsuranceRecommendationCandidateVO;

import java.time.LocalDateTime;
import java.util.List;

public interface InsuranceRecommendationMapper {

    List<InsuranceRecommendationCandidateVO> selectMatchedCandidates(
            @Param("userId") Integer userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}