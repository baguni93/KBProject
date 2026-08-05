package org.scoula.event.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.event.dto.EventMainDTO;
import org.scoula.event.dto.EventResponseDTO;
import org.scoula.event.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Log4j2
@RestController
@RequiredArgsConstructor
@Api(tags = "이벤트 조회 API")
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;

    // 1. 이벤트 메인
    @ApiOperation("이벤트 메인화면 조회")
    @GetMapping("/main")
    public ResponseEntity<EventMainDTO> getEventMainPage(@RequestParam(value = "userId") Integer userId) {
        EventMainDTO mainData = eventService.getEventMainPageData(userId);
        return ResponseEntity.ok(mainData);
    }

    // 2. 이벤트 리스트 - 참여 가능 이벤트 리스트 조회
    @ApiOperation("이벤트 리스트 - 참여 가능 이벤트 조회")
    @GetMapping("/list")
    public ResponseEntity<List<EventResponseDTO>> getActiveEventList( @RequestParam(value = "userId", required = false) Integer userId) {
        List<EventResponseDTO> activeEvents = eventService.getActiveEventsProgress(userId);
        return ResponseEntity.ok(activeEvents);
    }

    // 3. 이벤트 리스트 - 참여 완료 이벤트 리스트 조회
    @ApiOperation("이벤트 리스트 - 참여 완료 이벤트 조회")
    @GetMapping("/list/joined")
    public ResponseEntity<List<EventResponseDTO>> getJoinedEventList(
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "yearMonth", required = false) String yearMonth) {
        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = java.time.YearMonth.now().toString();
        }

        if (yearMonth.startsWith("-")) {
            yearMonth = yearMonth.substring(1);
        }

        List<EventResponseDTO> joinedEvents = eventService.getJoinedEventsProgress(userId, yearMonth);
        return ResponseEntity.ok(joinedEvents);
    }

    // 4. 이벤트 참여 처리 (우측 배지 버튼 클릭 시 처리할)
    @ApiOperation("이벤트 참여 처리")
    @PostMapping("/join/{eventId}")
    public ResponseEntity<Void> joinEvent(
            @PathVariable("eventId") Integer eventId,
            @RequestBody Map<String, Integer> body) {

        Integer userId = body.get("userId");

        eventService.participateEvent(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 5. 이벤트 리워드 수령 처리
    @ApiOperation("이벤트 리워드 수령 처리 (목표 달성 시 포인트 지급 및 수령 완료 처리)")
    @PostMapping("/{eventId}/reward")
    public ResponseEntity<Void> receiveEventReward(
            @PathVariable("eventId") Integer eventId,
            @RequestBody Map<String, Integer> body) {

        Integer userId = body.get("userId");
        eventService.receiveEventReward(userId, eventId);
        return ResponseEntity.ok().build();
    }

    // 6. 챌린지 리워드 수령 처리 (이벤트 상단 챌린지 바 관련 영역)
    @ApiOperation("챌린지 리워드 수령 처리 (상단 챌린지 게이지 바 리워드 수령)")
    @PostMapping("/challenges/{challengeId}/reward")
    public ResponseEntity<Void> claimChallengeReward(
            @PathVariable("challengeId") Integer challengeId,
            @RequestParam(value = "userId") Integer userId) {

        eventService.claimChallengeReward(userId, challengeId);
        return ResponseEntity.ok().build();
    }

}
