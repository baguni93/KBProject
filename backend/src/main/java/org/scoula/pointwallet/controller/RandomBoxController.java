package org.scoula.pointwallet.controller;

import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.dto.RandomBoxCountDTO;
import org.scoula.pointwallet.dto.RandomBoxOpenAllResultDTO;
import org.scoula.pointwallet.dto.RandomBoxOpenResultDTO;
import org.scoula.pointwallet.dto.UserRandomBoxDTO;
import org.scoula.pointwallet.service.RandomBoxService;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/random-boxes")
@Log4j2
public class RandomBoxController {

    private final RandomBoxService randomBoxService;
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

    /*
     * 사용자가 보유한 미개봉 랜덤박스 목록 조회
     */
    @GetMapping("/unopened")
    public ResponseEntity<List<UserRandomBoxDTO>> getUnopenedRandomBoxes(
            HttpServletRequest httpRequest
    ) {
        Integer userId = resolveUserId(httpRequest);

        List<UserRandomBoxDTO> randomBoxes =
                randomBoxService.getUnopenedRandomBoxes(
                        userId
                );

        return ResponseEntity.ok(randomBoxes);
    }

    /*
     * 사용자가 보유한 미개봉 랜덤박스 개수 조회
     */
    @GetMapping("/unopened/count")
    public ResponseEntity<RandomBoxCountDTO>
    getUnopenedRandomBoxCount(
            HttpServletRequest httpRequest
    ) {
        Integer userId = resolveUserId(httpRequest);

        int unopenedCount =
                randomBoxService
                        .getUnopenedRandomBoxCount(
                                userId
                        );

        RandomBoxCountDTO result =
                RandomBoxCountDTO.builder()
                        .unopenedCount(unopenedCount)
                        .build();

        return ResponseEntity.ok(result);
    }

    /*
     * 사용자가 선택한 랜덤박스 1개 개봉
     */
    @PostMapping("/{userRandomBoxId}/open")
    public ResponseEntity<RandomBoxOpenResultDTO> openRandomBox(
            HttpServletRequest httpRequest,
            @PathVariable("userRandomBoxId")
            Integer userRandomBoxId
    ) {
        Integer userId = resolveUserId(httpRequest);

        RandomBoxOpenResultDTO result =
                randomBoxService.openRandomBox(
                        userId,
                        userRandomBoxId
                );

        return ResponseEntity.ok(result);
    }

    /*
     * 사용자가 보유한 미개봉 랜덤박스 모두 개봉
     */
    @PostMapping("/open-all")
    public ResponseEntity<RandomBoxOpenAllResultDTO> openAllRandomBoxes(
            HttpServletRequest httpRequest
    ) {
        Integer userId = resolveUserId(httpRequest);

        RandomBoxOpenAllResultDTO result =
                randomBoxService.openAllRandomBoxes(
                        userId
                );

        return ResponseEntity.ok(result);
    }
}
