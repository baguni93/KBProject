package org.scoula.pointwallet.controller;

import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.dto.PointConversionRequestDTO;
import org.scoula.pointwallet.dto.PointConversionResultDTO;
import org.scoula.pointwallet.service.PointConversionService;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "포인트 전환 API")
@RestController
@RequestMapping("/api/point-conversions")
@RequiredArgsConstructor
@Log4j2
public class PointConversionController {

    private final PointConversionService pointConversionService;
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

    @ApiOperation("포인트를 전자지갑으로 충전")
    @PostMapping
    public ResponseEntity<PointConversionResultDTO> convertPoints(
            HttpServletRequest httpRequest,
            @RequestBody PointConversionRequestDTO request
    ) {
        Integer userId = resolveUserId(httpRequest);

        if (request == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        PointConversionResultDTO result =
                pointConversionService.convertPoints(
                        userId,
                        request.getPointAmount()
                );

        return ResponseEntity.ok(result);
    }
}
