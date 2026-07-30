package org.scoula.pointwallet.mapper;

import org.scoula.pointwallet.domain.PointConversionVO;

public interface PointConversionMapper {

    // 포인트 전환 이력 저장
    int insertPointConversion(
            PointConversionVO pointConversion
    );

    // 생성된 포인트 전환 이력 조회
    PointConversionVO selectPointConversionById(
            Integer pointConversionId
    );
}