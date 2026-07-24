package org.scoula.pointwallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.pointwallet.dto.PointWalletDTO;
import org.scoula.pointwallet.service.PointWalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 스웨거 표기
@Api(tags = "포인트 지갑 API")
@RestController
@RequestMapping("/api/point-wallet")
@RequiredArgsConstructor
public class PointWalletController {

    private final PointWalletService pointWalletService;

    @ApiOperation("포인트 지갑 조회")
    @GetMapping
    public ResponseEntity<PointWalletDTO> getWallet() {

        // JWT 사용자 연결 전까지 사용하는 임시 사용자 ID
        Integer temporaryUserId = 1;

        PointWalletDTO wallet =
                pointWalletService.getWallet(temporaryUserId);

        return ResponseEntity.ok(wallet);
    }
}