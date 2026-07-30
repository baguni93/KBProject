package org.scoula.pointwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 출석 API결과를 프론트에 보내는 응답객체.
public class AttendanceResultDTO {

    // 유저 ID
    private Integer userId;
    // 출석 일자
    private String attendanceDate;
    // 적립 포인트
    private Integer rewardPoint;
    // 포인트 잔액
    private Integer pointBalance;
    // 출석 완료 메세지
    private String message;

    // 랜덤박스 지급 사유
    private Boolean randomBoxIssued;
    // 랜덤박스 ID
    private Integer userRandomBoxId;
}