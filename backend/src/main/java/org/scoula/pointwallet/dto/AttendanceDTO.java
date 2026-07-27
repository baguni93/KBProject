package org.scoula.pointwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// DB에 출석을 저장하고, 생성된 PK를 받기위한 용도
public class AttendanceDTO {

    private Integer attendanceId;
    private Integer userId;
    private String attendanceDate;
}