package org.scoula.analysis.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.analysis.dto.AnalysisCountDTO;

import java.time.LocalDateTime;

public interface AnalysisMapper {

    // 선택한 기간 안에 존재하는 정상 결제 거래수와,
    // 카테고리 분류 완료 거래수를 조회한다. + 시간은 고려 X
    AnalysisCountDTO selectAnalysisCount(
            @Param("userId") Integer userId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );


}