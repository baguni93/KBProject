package org.scoula.pointwallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.pointwallet.dto.AttendanceResultDTO;
import org.scoula.pointwallet.dto.PointTransactionDTO;
import org.scoula.pointwallet.dto.PointWalletDTO;
import org.scoula.pointwallet.service.AttendanceService;
import org.scoula.pointwallet.service.PointWalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 스웨거 표기
@Api(tags = "포인트 지갑 API")
@RestController
@RequestMapping("/api/point-wallet")
@RequiredArgsConstructor
public class PointWalletController {

    // 포인트 지갑의 서비스들
    private final PointWalletService pointWalletService;

    // 포인트
    private final AttendanceService attendanceService;

    @ApiOperation("포인트 지갑 조회")
    @GetMapping
    public ResponseEntity<PointWalletDTO> getWallet() {

        // JWT 사용자 연결 전까지 사용하는 임시 사용자 ID
        // TODO-DATA: 요청한 사용자의 실제 포인트 지갑 ID를 DB에서 조회하도록 변경
        Integer temporaryUserId = 1;

        PointWalletDTO wallet =
                pointWalletService.getWallet(temporaryUserId);

        return ResponseEntity.ok(wallet);
    }
}