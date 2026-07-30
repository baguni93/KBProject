package org.scoula.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.dto.AnalysisAvailabilityDTO;
import org.scoula.analysis.dto.AnalysisCountDTO;
import org.scoula.analysis.mapper.AnalysisMapper;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Log4j2
@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    // 분석에 필요한 최소 카테고리 분류 개수는 10개
    private static final int REQUIRED_TRANSACTION_COUNT = 10;

    // 매퍼
    private final AnalysisMapper analysisMapper;

    @Override
    public AnalysisAvailabilityDTO getAnalysisAvailability(
            Integer userId,
            Integer period
    ) {
        validatePeriod(period);

        /*
         * 예:
         * 오늘이 2026-07-30이고 period(개월)가 1이면
         *
         * 시작일: 2026-06-30
         * 종료일: 2026-07-30
         */
        LocalDate analysisEndDate = LocalDate.now();
        LocalDate analysisStartDate =
                analysisEndDate.minusMonths(period);

        LocalDateTime startAt =
                analysisStartDate.atStartOfDay();

        /*
         * 종료일의 모든 거래를 포함하기 위해
         * 다음 날 00:00 미만으로 조회한다.
         */
        LocalDateTime endAt =
                analysisEndDate
                        .plusDays(1)
                        .atStartOfDay();

        AnalysisCountDTO count =
                analysisMapper.selectAnalysisCount(
                        userId,
                        startAt,
                        endAt
                );

        // 결과 DTO로 부터, 가져오고 없으면 0으로 표기
        int totalPaymentCount =
                count == null
                        || count.getTotalPaymentCount() == null
                        ? 0
                        : count.getTotalPaymentCount();

        int classifiedPaymentCount =
                count == null
                        || count.getClassifiedPaymentCount() == null
                        ? 0
                        : count.getClassifiedPaymentCount();

        int unclassifiedPaymentCount =
                totalPaymentCount - classifiedPaymentCount;

        int remainingCount =
                Math.max(
                        REQUIRED_TRANSACTION_COUNT
                                - classifiedPaymentCount,
                        0
                );

        boolean available =
                classifiedPaymentCount
                        >= REQUIRED_TRANSACTION_COUNT;

        String message;

        if (available) {
            message = "소비 분석이 가능합니다.";
        } else {
            message =
                    "분류 완료된 결제 거래가 "
                            + remainingCount
                            + "건 더 필요합니다.";
        }

        log.info(
                "소비 분석 가능 여부 조회 userId={}, period={}, "
                        + "totalCount={}, classifiedCount={}, available={}",
                userId,
                period,
                totalPaymentCount,
                classifiedPaymentCount,
                available
        );

        return AnalysisAvailabilityDTO.builder()
                .period(period)
                .periodLabel(createPeriodLabel(period))
                .analysisStartDate(analysisStartDate.toString())
                .analysisEndDate(analysisEndDate.toString())
                .totalPaymentCount(totalPaymentCount)
                .classifiedPaymentCount(classifiedPaymentCount)
                .unclassifiedPaymentCount(unclassifiedPaymentCount)
                .requiredCount(REQUIRED_TRANSACTION_COUNT)
                .remainingCount(remainingCount)
                .available(available)
                .message(message)
                .build();
    }

    // 1개월, 3개월, 12개월 인지 검증하기. ->DB에도 1,3,12 개월로 정해져있음.
    private void validatePeriod(Integer period) {
        if (period == null
                || (period != 1
                && period != 3
                && period != 12)) {

            throw new CustomException(
                    ErrorCode.INVALID_ANALYSIS_PERIOD
            );
        }
    }

    private String createPeriodLabel(Integer period) {
        return "최근 " + period + "개월";
    }
}