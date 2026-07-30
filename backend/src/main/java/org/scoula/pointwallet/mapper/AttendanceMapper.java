package org.scoula.pointwallet.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.pointwallet.domain.AttendanceVO;
import org.scoula.pointwallet.dto.AttendanceDTO;

public interface AttendanceMapper {

    // 오늘 출석 했는지 확인하기.
    int countTodayAttendance(
            @Param("userId") Integer userId
    );

    // 출석 기록 저장
    int insertTodayAttendance(
            AttendanceVO attendanceVO
    );
}
