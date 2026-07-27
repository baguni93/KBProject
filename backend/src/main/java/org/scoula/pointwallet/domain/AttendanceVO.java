package org.scoula.pointwallet.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceVO {

    private Integer attendanceId;
    private Integer userId;
    private String attendanceDate;
}