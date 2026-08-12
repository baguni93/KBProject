package org.scoula.event.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.event.dto.*;
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
    public ResponseEntity<EventMainDTO> getEventMainPage(@RequestParam(value = "userId") int userId) {
        EventMainDTO mainData = eventService.getEventMainPageData(userId);
        return ResponseEntity.ok(mainData);
    }

    // 2. 이벤트 리스트 - 참여 가능 이벤트 리스트 조회
    @ApiOperation("이벤트 리스트 - 참여 가능 이벤트 조회")
    @GetMapping("/list")
    public ResponseEntity<List<EventResponseDTO>> getActiveEventList( @RequestParam(value = "userId") int userId) {
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

    // 이벤트 조회
    @GetMapping("/eventList")
    public ResponseEntity<List<EventGetResponseDTO>> getEventList(
            @RequestParam(value = "userId") int userId) {
        return ResponseEntity.ok(eventService.getEventList(userId));
    }

    // 출석 이벤트 조회
    @GetMapping("/attendanceEventList")
    public ResponseEntity<List<EventGetAttendanceResponseDTO>> getAttendanceEventList(
            @RequestParam(value = "userId") int userId) {
        return ResponseEntity.ok(eventService.getAttendanceEventList(userId));
    }

    // 이벤트 참여
    @PostMapping("/joinEvent/{eventId}")
    public ResponseEntity<List<EventGetResponseDTO>>  joinEvent(
            @RequestParam(value = "userId") int userId,
            @PathVariable int eventId) {
        return ResponseEntity.ok(eventService.joinEvent(userId, eventId));
    }

    // 출석체크 참여
    @PostMapping("/joinAttendanceEvent/{eventId}")
    public ResponseEntity<List<EventGetAttendanceResponseDTO>>  joinAttendanceEvent(
            @RequestParam(value = "userId") int userId,
            @PathVariable int eventId) {
        return ResponseEntity.ok(eventService.joinAttendanceEvent(userId, eventId));
    }

    // 이벤트 참여이력 생성
    @PostMapping("/participateEvent/{eventId}")
    public ResponseEntity<List<EventGetResponseDTO>> createParticipation(
            @RequestParam(value = "userId") int userId,
            @PathVariable int eventId) {
        return ResponseEntity.ok(eventService.createParticipation(userId, eventId));
    }

    // 이벤트 리워드 수령 처리
    @PostMapping("/receiveEventReward/{eventId}/rewards/{rewardId}")
    public ResponseEntity<List<EventGetResponseDTO>> receiveEventReward(
            @RequestParam(value = "userId") int userId,
            @PathVariable int eventId,
            @PathVariable int rewardId) {

        List<EventGetResponseDTO> updatedList = eventService.receiveEventReward(userId, eventId, rewardId);
        return ResponseEntity.ok(updatedList);
    }

    // 출석체크 리워드 수령 처리
    @PostMapping("/receiveAttendanceEventReward/{eventId}/rewards/{rewardId}")
    public ResponseEntity<List<EventGetAttendanceResponseDTO>> receiveAttendanceReward(
            @RequestParam(value = "userId") int userId,
            @PathVariable int eventId,
            @PathVariable int rewardId) {

        List<EventGetAttendanceResponseDTO> updatedList = eventService.receiveAttendanceEventReward(userId, eventId, rewardId);
        return ResponseEntity.ok(updatedList);
    }

    // 이벤트 챌린지 보상 수령 처리
    @PostMapping("/challenge/claim/{challengeId}")
    public ResponseEntity<List<EventChallengeResponseDTO>> receiveChallengeReward(
            @RequestParam(value = "userId") int userId,
            @PathVariable int challengeId) {

        List<EventChallengeResponseDTO> challengeList = eventService.receiveChallengeReward(userId, challengeId);
        return ResponseEntity.ok(challengeList);
    }
}
