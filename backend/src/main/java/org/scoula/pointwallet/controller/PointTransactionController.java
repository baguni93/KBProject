package org.scoula.pointwallet.controller;

import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.dto.PointTransactionDTO;
import org.scoula.pointwallet.service.PointWalletService;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "포인트 거래내역 API")
@RestController
@RequestMapping("/api/point-transactions")
@RequiredArgsConstructor
@Log4j2
public class PointTransactionController {

    private final PointWalletService pointWalletService;
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

    // 트랜잭션 TYPE으로, 적립, 사용, 만료, 취소, 복구
    @ApiOperation("포인트 이용 내역 조회")
    @GetMapping
    public ResponseEntity<List<PointTransactionDTO>> getTransactions(
            HttpServletRequest httpRequest,
            @RequestParam(
                    value = "type",
                    required = false
            ) String transactionType
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                pointWalletService.getTransactions(
                        userId,
                        transactionType
                )
        );
    }

    @ApiOperation("최근 포인트 거래내역 5건 조회")
    @GetMapping("/recent")
    public ResponseEntity<List<PointTransactionDTO>>
    getRecentTransactions(
            HttpServletRequest httpRequest
    ) {
        Integer userId = resolveUserId(httpRequest);

        return ResponseEntity.ok(
                pointWalletService.getRecentTransactions(
                        userId
                )
        );
    }
}
