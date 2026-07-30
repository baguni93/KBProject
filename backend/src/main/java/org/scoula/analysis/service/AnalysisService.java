package org.scoula.analysis.service;

import org.scoula.analysis.dto.AnalysisAvailabilityDTO;

public interface AnalysisService {

    // 소비분석 가능한 상태(10건 이상 카테고리 분류)인지 체크하기.
    AnalysisAvailabilityDTO getAnalysisAvailability(
            Integer userId,
            Integer period
    );
}