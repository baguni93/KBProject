package org.scoula.pointwallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.scoula.pointwallet.dto.PointConversionRequestDTO;
import org.scoula.pointwallet.dto.PointConversionResultDTO;
import org.scoula.pointwallet.exception.PointWalletErrorCode;
import org.scoula.pointwallet.exception.PointWalletException;
import org.scoula.pointwallet.service.PointConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Api(tags = "포인트 전환 API")
@RestController
@RequestMapping("/api/point-conversions")
@RequiredArgsConstructor
public class PointConversionController {

    private final PointConversionService pointConversionService;

    @ApiOperation("포인트를 전자지갑으로 충전")
    @PostMapping
    public ResponseEntity<PointConversionResultDTO> convertPoints(
            @RequestBody PointConversionRequestDTO request
    ) {
        // TODO-AUTH: JWT 인증된 현재 사용자의 실제 user_tbl.user_id
        Integer temporaryUserId = 1;

        if (request == null) {
            throw new PointWalletException(
                    PointWalletErrorCode.INVALID_REQUEST,
                    "포인트 전환 요청값이 필요합니다."
            );
        }

        PointConversionResultDTO result =
                pointConversionService.convertPoints(
                        temporaryUserId,
                        request.getPointAmount()
                );

        return ResponseEntity.ok(result);
    }
}