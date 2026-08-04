package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisAvailabilityDTO {

    // 1, 3, 12 (개월수 저장)
    private Integer period;

    // 최근 1개월, 최근 3개월, 최근 12개월
    private String periodLabel;

    // 실제 조회 시작일
    private String analysisStartDate;

    // 실제 조회 종료일
    private String analysisEndDate;

    // 선택 기간 안의 전체 정상 결제 거래 수
    private Integer totalPaymentCount;

    // 카테고리가 지정된 정상 결제 거래 수
    private Integer classifiedPaymentCount;

    // 카테고리가 지정되지 않은 정상 결제 거래 수
    private Integer unclassifiedPaymentCount;

    // 분석에 필요한 최소 거래 수
    private Integer requiredCount;

    // 추가로 분류해야 하는 거래 수
    private Integer remainingCount;

    // 분석 가능 여부
    private Boolean available;

    // 화면 표시 메시지
    private String message;
}