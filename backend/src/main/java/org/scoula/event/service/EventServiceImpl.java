package org.scoula.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.Enum;
import org.scoula.event.domain.EventChallengeUserVO;
import org.scoula.event.domain.EventRewardVO;
import org.scoula.event.dto.*;
import org.scoula.event.mapper.EventMapper;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.pointwallet.common.PointReasonType;
import org.scoula.pointwallet.dto.PointWalletDTO;
import org.scoula.pointwallet.service.PointWalletService;
import org.scoula.task.service.TaskEventService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final String ATTENDANCE_TYPE = "ATTENDANCE";

    private final EventMapper eventMapper;
    private final PointWalletService pointWalletService;
    private final TaskEventService taskEventService;

    // 1. 이벤트 메인 페이지 불러오기
    @Override
    public EventMainDTO getEventMainPageData(int userId) {
        PointWalletDTO wallet = pointWalletService.getWallet(userId);

        return EventMainDTO.builder()
                .userId(userId)
                .currentPoint(wallet.getPointBalance())
                .userChallengeData(getEventChallengeUser(userId))
                .eventLists(getEventList(userId))
                .build();
    }

    // 2. 이벤트 첼린지 화면 가져오기.
    public List<EventChallengeResponseDTO> getEventChallengeUser(int userId) {
        List<EventChallengeResponseDTO> challengeList =
                eventMapper.getEventChallengeUser(userId);

        // 만약 참여하지 않았다면? 이벤트 첼린지 생성하기.
        if (challengeList.isEmpty()) {
            eventMapper.createEventChallengeParticipation(userId);
            challengeList = eventMapper.getEventChallengeUser(userId);
        }

        return challengeList;
    }

    // 진행중 이벤트 목록을 조회하고, 각 이벤트 화면에 뿌려주는 역할
    @Override
    public List<EventResponseDTO> getActiveEventsProgress(Integer userId) {
        List<EventResponseDTO> eventList =
                eventMapper.getActiveEventProgressList(userId);

        LocalDate today = LocalDate.now();


        for (EventResponseDTO event : eventList) {
            if (event == null) {
                continue;
            }
            // 날짜 타입이 insert안되어 있으면 상시로 표기하기.
            String displayDDay = "상시";

            // 이벤트 타입이 Attendance: 출석, Permant: 상시 인 경우, 매일로 표시
            if (ATTENDANCE_TYPE.equals(event.getEventType())
                    || "PERMANENT".equals(event.getEventType())) {
                displayDDay = "매일";
            } else if (event.getEndAt() != null && !event.getEndAt().isBlank()) {
                try {
                    LocalDate endDate = LocalDate.parse(
                            event.getEndAt().split(" ")[0]
                    );
                    // 현재 날짜와 종료일의 차이를 계산한다.
                    long days = ChronoUnit.DAYS.between(today, endDate);

                    // 종료일이 더 크면 종료 한다.  -> 이 코드는 기존 코드에 남아있던 방어코드로
                    // 결코 실행되지 않는다. xml에서 진행중 이벤트만 필터링해서 들고온다.
                    // 사실 필요없는 코드
                    if (days < 0) {
                        displayDDay = "종료";
                    } else if (days == 0) {
                        displayDDay = "D-Day";
                    } else {
                        displayDDay = "D-" + days;
                    }
                } catch (RuntimeException exception) {
                    displayDDay = "상시";
                }
            }

            event.setDDay(displayDDay);
        }

        return eventList;
    }

    //
    @Override
    public List<EventResponseDTO> getJoinedEventsProgress(
            Integer userId,
            String yearMonth
    ) {
        return eventMapper.getJoinedEventProgressList(userId, yearMonth);
    }

    @Override
    public List<EventGetResponseDTO> getEventList(int userId) {
        return eventMapper.getEvent(userId)
                .stream()
                .map(EventGetResponseDTO::of)
                .toList();
    }

    @Override
    public List<EventGetAttendanceResponseDTO> getAttendanceEventList(int userId) {
        return eventMapper.getAttendanceEvent(userId)
                .stream()
                .map(EventGetAttendanceResponseDTO::of)
                .toList();
    }

    @Override
    @Transactional
    public List<EventGetResponseDTO> joinEvent(int userId, int eventId) {
        eventMapper.joinEvent(userId, eventId);
        return getEventList(userId);
    }

    @Override
    @Transactional
    public List<EventGetAttendanceResponseDTO> joinAttendanceEvent(
            int userId,
            int eventId
    ) {
        eventMapper.joinAttendanceEvent(userId, eventId);

        if (eventMapper.lockEventUser(userId, eventId) == null) {
            throw new CustomException(ErrorCode.EVENT_NOT_FOUND);
        }

        eventMapper.createAttendanceParticipation(userId, eventId);
        return getAttendanceEventList(userId);
    }

    @Override
    @Transactional
    public int recordMissionProgress(int userId, String eventCategory) {
        if (eventCategory == null || eventCategory.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        int insertedCount = eventMapper.recordMissionProgress(
                userId,
                eventCategory.trim().toUpperCase()
        );

        if (insertedCount > 0) {
            log.info(
                    "이벤트 미션 진행도 반영 userId={}, category={}, eventCount={}",
                    userId,
                    eventCategory,
                    insertedCount
            );

            EventCompletionDTO completion =
                    eventMapper.getEventCompletion(
                            userId,
                            eventCategory
                    );

            if (completion != null && completion.getCompleted()) {

                log.info(
                        "이벤트 완료 userId={}, eventId={}, eventName={}",
                        userId,
                        completion.getEventId(),
                        completion.getEventName()
                );

                taskEventService.sendTaskEvent(userId , Enum.TaskType.EVENT_COMPLETE , completion.getEventName() + " 완료 \n 기다리던 보상을 확인해보세요!", null);
            }
        }

        return insertedCount;
    }

    @Override
    @Transactional
    public List<EventGetResponseDTO> receiveEventReward(
            int userId,
            int eventId,
            int rewardId
    ) {
        claimEventReward(userId, eventId, rewardId, false);
        return getEventList(userId);
    }

    @Override
    @Transactional
    public List<EventGetAttendanceResponseDTO> receiveAttendanceEventReward(
            int userId,
            int eventId,
            int rewardId
    ) {
        claimEventReward(userId, eventId, rewardId, true);
        return getAttendanceEventList(userId);
    }

    private void claimEventReward(
            int userId,
            int eventId,
            int rewardId,
            boolean attendanceReward
    ) {
        EventRewardVO reward = eventMapper.getRewardClaimInfo(
                userId,
                eventId,
                rewardId
        );

        if (reward == null
                || attendanceReward != ATTENDANCE_TYPE.equals(reward.getEventType())) {
            throw new CustomException(ErrorCode.EVENT_NOT_FOUND);
        }

        if (!Boolean.TRUE.equals(reward.getJoined())) {
            throw new CustomException(ErrorCode.EVENT_NOT_JOINED);
        }

        if (Boolean.TRUE.equals(reward.getRewardReceived())) {
            throw new CustomException(ErrorCode.EVENT_REWARD_ALREADY_RECEIVED);
        }

        if (reward.getCurrentTargetCount() == null
                || reward.getCurrentTargetCount() < reward.getEventTarget()) {
            throw new CustomException(ErrorCode.EVENT_NOT_COMPLETED);
        }

        try {
            eventMapper.createEventRewardReceive(userId, eventId, rewardId);
        } catch (DuplicateKeyException exception) {
            throw new CustomException(ErrorCode.EVENT_REWARD_ALREADY_RECEIVED);
        }

        if (reward.getRewardPoint() != null && reward.getRewardPoint() > 0) {
            pointWalletService.earnPoints(
                    userId,
                    reward.getRewardPoint(),
                    PointReasonType.EVENT
            );
        }

        if (reward.getRewardExp() != null && reward.getRewardExp() > 0) {
            getEventChallengeUser(userId);
            eventMapper.updateUserChallenge(userId, rewardId);
        }

        log.info(
                "이벤트 보상 수령 완료 userId={}, eventId={}, rewardId={}",
                userId,
                eventId,
                rewardId
        );
    }

    @Override
    @Transactional
    public List<EventChallengeResponseDTO> receiveChallengeReward(
            int userId,
            int challengeId
    ) {
        EventChallengeUserVO challenge =
                eventMapper.getChallengeClaimForUpdate(userId, challengeId);

        if (challenge == null
                || !"PROCESS".equals(challenge.getStatus())
                || challenge.getRequiredExp() == null
                || challenge.getExp() == null
                || challenge.getExp() < challenge.getRequiredExp()) {
            throw new CustomException(ErrorCode.EVENT_CHALLENGE_NOT_READY);
        }

        pointWalletService.earnPoints(
                userId,
                challenge.getRewardPoint(),
                PointReasonType.EVENT
        );

        int updatedCount;
        if (challenge.getCurrentLevel() >= challenge.getMaxLevel()) {
            updatedCount = eventMapper.completeChallenge(userId, challengeId);
        } else {
            updatedCount = eventMapper.updateUserLevel(userId, challengeId);
        }

        if (updatedCount != 1) {
            throw new CustomException(ErrorCode.EVENT_CHALLENGE_NOT_READY);
        }

        return getEventChallengeUser(userId);
    }
}
