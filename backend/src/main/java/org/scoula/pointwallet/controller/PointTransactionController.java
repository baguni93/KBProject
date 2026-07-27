package org.scoula.pointwallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.pointwallet.dto.PointTransactionDTO;
import org.scoula.pointwallet.service.PointWalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "포인트 거래내역 API")
@RestController
@RequestMapping("/api/point-transactions")
@RequiredArgsConstructor
public class PointTransactionController {

    private final PointWalletService pointWalletService;

    // 트랜잭션 TYPE으로, 적립, 사용, 만료, 취소, 복구
    @ApiOperation("포인트 이용 내역 조회")
    @GetMapping
    public ResponseEntity<List<PointTransactionDTO>> getTransactions(
            @RequestParam(
                    value = "type",
                    required = false
            ) String transactionType
    ) {
        // TODO-AUTH: JWT 인증 정보에서 현재 로그인한 사용자의 userId를 조회하도록 변경
        Integer temporaryUserId = 1;

        return ResponseEntity.ok(
                pointWalletService.getTransactions(
                        temporaryUserId,
                        transactionType
                )
        );
    }

    @ApiOperation("최근 포인트 거래내역 5건 조회")
    @GetMapping("/recent")
    public ResponseEntity<List<PointTransactionDTO>>
    getRecentTransactions() {

        // TODO-AUTH: JWT 인증 정보에서 현재 로그인한 사용자의 user_tbl.user_id로 교체
        Integer temporaryUserId = 1;

        return ResponseEntity.ok(
                pointWalletService.getRecentTransactions(
                        temporaryUserId
                )
        );
    }



}
