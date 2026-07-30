package org.scoula.pointwallet.service;

import org.scoula.pointwallet.dto.PointConversionResultDTO;

public interface PointConversionService {

    // 포인트를 전자지갑 잔액으로 전환한다.
    PointConversionResultDTO convertPoints(
            Integer userId,
            Integer pointAmount
    );
}