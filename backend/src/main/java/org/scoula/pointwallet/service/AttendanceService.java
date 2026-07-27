package org.scoula.pointwallet.service;

import org.scoula.pointwallet.dto.AttendanceResultDTO;
import org.scoula.pointwallet.dto.AttendanceStatusDTO;

public interface AttendanceService {

    // 유저에게 출석 포인트, 랜덤박스, 출석기록 삽입 하기.
    AttendanceResultDTO attend(Integer userId);

    // 오늘 출석 여부 조회
    AttendanceStatusDTO getTodayAttendanceStatus(Integer userId);
}