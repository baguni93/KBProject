package org.scoula.pointwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 오늘의 출석 여부 조회 API
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStatusDTO {

    // 오늘 출석 완료 여부
    private Boolean attendedToday;

    // 오늘 날짜
    private String attendanceDate;

    // 출석 완료 시 지급되는 포인트
    private Integer rewardPoint;

    // 출석 완료 시 지급되는 랜덤박스 수
    private Integer randomBoxCount;

    // 프론트 표시 메시지
    private String message;
}