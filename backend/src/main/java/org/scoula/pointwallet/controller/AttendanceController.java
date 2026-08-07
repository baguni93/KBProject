package org.scoula.pointwallet.controller;

import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.dto.AttendanceResultDTO;
import org.scoula.pointwallet.dto.AttendanceStatusDTO;
import org.scoula.pointwallet.service.AttendanceService;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "출석 API")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Log4j2
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final JwtProcessor jwtProcessor;

    // 사용자 인증 토큰 처리
    private Integer resolveUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new CustomException(
                    ErrorCode.AUTHENTICATION_REQUIRED
            );
        }

        try {
            String token = authHeader.substring(7);
            Long userId = jwtProcessor.getUserId(token);
            return userId.intValue();
        } catch (Exception e) {
            log.warn("토큰에서 userId 추출 실패: {}", e.getMessage());
            throw new CustomException(
                    ErrorCode.AUTHENTICATION_REQUIRED
            );
        }
    }

    @ApiOperation("출석 체크")
    @PostMapping
    public ResponseEntity<AttendanceResultDTO> attend(
            HttpServletRequest httpRequest
    ) {
        Integer userId = resolveUserId(httpRequest);

        AttendanceResultDTO result =
                attendanceService.attend(
                        userId
                );

        return ResponseEntity.ok(result);
    }

    @ApiOperation("오늘 출석 여부 조회: 중복 출석 방지")
    @GetMapping("/today")
    public ResponseEntity<AttendanceStatusDTO>
    getTodayAttendanceStatus(
            HttpServletRequest httpRequest
    ) {
        Integer userId = resolveUserId(httpRequest);

        AttendanceStatusDTO status =
                attendanceService.getTodayAttendanceStatus(
                        userId
                );

        return ResponseEntity.ok(status);
    }
}
