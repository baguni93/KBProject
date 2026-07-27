package org.scoula.pointwallet.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.pointwallet.dto.RandomBoxCountDTO;
import org.scoula.pointwallet.dto.RandomBoxOpenAllResultDTO;
import org.scoula.pointwallet.dto.RandomBoxOpenResultDTO;
import org.scoula.pointwallet.dto.UserRandomBoxDTO;
import org.scoula.pointwallet.service.RandomBoxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/random-boxes")
public class RandomBoxController {

    private final RandomBoxService randomBoxService;

    /*
     * 사용자가 보유한 미개봉 랜덤박스 목록 조회
     */
    @GetMapping("/unopened")
    public ResponseEntity<List<UserRandomBoxDTO>>
    getUnopenedRandomBoxes() {

        Integer userId = getCurrentUserId();

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
    getUnopenedRandomBoxCount() {

        Integer userId = getCurrentUserId();

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
    public ResponseEntity<RandomBoxOpenResultDTO>
    openRandomBox(
            @PathVariable("userRandomBoxId")
            Integer userRandomBoxId
    ) {
        Integer userId = getCurrentUserId();

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
    public ResponseEntity<RandomBoxOpenAllResultDTO>
    openAllRandomBoxes() {

        Integer userId = getCurrentUserId();

        RandomBoxOpenAllResultDTO result =
                randomBoxService.openAllRandomBoxes(
                        userId
                );

        return ResponseEntity.ok(result);
    }

    private Integer getCurrentUserId() {

        // TODO-AUTH: JWT 인증된 현재 사용자의 실제 user_tbl.user_id
        Integer temporaryUserId = 1;

        return temporaryUserId;
    }
}