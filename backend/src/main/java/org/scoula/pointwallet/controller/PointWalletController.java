package org.scoula.pointwallet.controller;

import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.dto.PointWalletDTO;
import org.scoula.pointwallet.service.AttendanceService;
import org.scoula.pointwallet.service.PointWalletService;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

// 스웨거 표기
@Api(tags = "포인트 지갑 API")
@RestController
@RequestMapping("/api/point-wallet")
@RequiredArgsConstructor
@Log4j2
public class PointWalletController {

    // 포인트 지갑의 서비스들
    private final PointWalletService pointWalletService;

    // 포인트
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

    @ApiOperation("포인트 지갑 조회")
    @GetMapping
    public ResponseEntity<PointWalletDTO> getWallet(
            HttpServletRequest httpRequest
    ) {
        Integer userId = resolveUserId(httpRequest);

        PointWalletDTO wallet =
                pointWalletService.getWallet(userId);

        return ResponseEntity.ok(wallet);
    }
}
