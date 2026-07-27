package org.scoula.pointwallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.pointwallet.dto.AttendanceResultDTO;
import org.scoula.pointwallet.dto.AttendanceStatusDTO;
import org.scoula.pointwallet.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "출석 API")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;


    @ApiOperation("출석 체크")
    @PostMapping
    public ResponseEntity<AttendanceResultDTO> attend() {

        // TODO-AUTH: JWT에서 실제 user_tbl.user_id 조회로 변경해야한다.
        // 현재 유저 1번만 조회하는 상태.
        Integer temporaryUserId = 1;

        AttendanceResultDTO result =
                attendanceService.attend(
                        temporaryUserId
                );

        return ResponseEntity.ok(result);
    }

    @ApiOperation("오늘 출석 여부 조회: 중복 출석 방지")
    @GetMapping("/today")
    public ResponseEntity<AttendanceStatusDTO>
    getTodayAttendanceStatus() {

        // TODO-AUTH: JWT에서 실제 user_tbl.user_id 조회로 변경해야 함
        Integer temporaryUserId = 1;

        AttendanceStatusDTO status =
                attendanceService.getTodayAttendanceStatus(
                        temporaryUserId
                );

        return ResponseEntity.ok(status);
    }


}