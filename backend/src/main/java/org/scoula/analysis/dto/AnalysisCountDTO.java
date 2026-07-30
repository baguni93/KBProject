package org.scoula.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// DB에서 분석 대상 거래 건수를 조회할 때 사용
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisCountDTO {

    // 선택 기간 안의 전체 정상 결제 거래 수
    private Integer totalPaymentCount;

    // 소비 카테고리가 지정된 정상 결제 거래 수
    private Integer classifiedPaymentCount;
}