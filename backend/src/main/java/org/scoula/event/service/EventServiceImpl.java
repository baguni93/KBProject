package org.scoula.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.event.domain.EventAttendanceVO;
import org.scoula.event.domain.EventNormalVO;
import org.scoula.event.domain.EventParticipationVO;
import org.scoula.event.domain.EventRewardReceiveVO;
import org.scoula.event.dto.*;
import org.scoula.event.mapper.EventMapper;
import org.scoula.pointwallet.dto.PointWalletDTO;
import org.scoula.pointwallet.service.PointWalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService{
    private final EventMapper eventMapper;
    private final PointWalletService pointWalletService;    // 포인트 조회용

    // 1. 이벤트 메인화면 데이터 조회
    @Override
    public EventMainDTO getEventMainPageData(Integer userId) {
        // 1-1. 포인트 조회
        PointWalletDTO wallet = pointWalletService.getWallet(userId);

        int currentPoint = (wallet != null) ? wallet.getPointBalance() : 0;

        // 1-2 이벤트 챌린지 조회
        UserChallengeDTO challenge = eventMapper.getUserChallengeStatus(userId);

        // 데이터 없을 경우를 위한 기본값 세팅
        if (challenge == null || challenge.getUserChallengeLevel() == null) {
            challenge = UserChallengeDTO.builder()
                    .userChallengeLevel(1) // 현재 이벤트 챌린지 레벨
                    .userChallengeExe(0) // 현재 이벤트 챌린지 경험치
                    .userChallengeMaxExe(1000) // 현재 이벤트 챌린지 목표 경험치
                    .status("PROCESS")
                    .challengeDDay("상시")
                    .build();
        } else {

        }

        // 1-3. 참여 가능 이벤트 목록 프리뷰
        List<EventResponseDTO> allActive = this.getActiveEventsProgress(userId);

        return EventMainDTO.builder()
                .userId(userId)
                .currentPoint(currentPoint)
                .userChallenge(challenge)
                .userChallengeLevel(challenge.getUserChallengeLevel())
                .userChallengeExe(challenge.getUserChallengeExe())
                .userChallengeMaxExe(challenge.getUserChallengeMaxExe())
                .challengeStartAt(challenge.getChallengeStartAt())
                .challengeEndAt(challenge.getChallengeEndAt())
                .activeEvents(allActive)
                .build();

    }

    // 2. 이벤트 리스트 조회 관련
    @Override
    public List<EventResponseDTO> getActiveEventsProgress(Integer userId) {
        List<EventResponseDTO> eventList = eventMapper.getActiveEventProgressList(userId);

        if (eventList == null) {
            return new java.util.ArrayList<>();
        }

        LocalDate today = LocalDate.now();

        for (EventResponseDTO event : eventList) {
            if (event == null) {
                continue;
            }

            String displayDday = "상시"; // 기본 디데이 값 초기화

            // 2-1. 출석체크나 상시 이벤트 타입 예외 처리
            if ("ATTENDANCE".equals(event.getEventType()) || "PERMANENT".equals(event.getEventType())) {
                displayDday = "매일";
            } else if (event.getEndAt() != null && !event.getEndAt().isEmpty()) {
                try {
                    String datePart = event.getEndAt().split(" ")[0];
                    LocalDate endDate = LocalDate.parse(datePart);

                    long days = java.time.temporal.ChronoUnit.DAYS.between(today, endDate);

                    if (days < 0)       displayDday = "종료";
                    else if (days == 0) displayDday = "D-Day";
                    else                displayDday = "D-" + days;
                } catch (Exception e) {
                    displayDday = "상시";
                }
            }

            event.setDDay(displayDday);
        }

        //이벤트 = 출석 이벤트  이벤트'



        return eventList;
    }
    // 3. 참여완료 이벤트 리스트
    @Override
    public List<EventResponseDTO> getJoinedEventsProgress(Integer userId, String yearMonth) {
        return eventMapper.getJoinedEventProgressList(userId, yearMonth);
    }

    // 4. 이벤트 참여 처리
    @Transactional
    @Override
    public boolean participateEvent(Integer userId, Integer eventId) {
        EventResponseDTO eventRewardInfo = eventMapper.getEventRewardInfoByEventId(eventId);
        if (eventRewardInfo == null) {
            return false;
        }

        // event_target 참여가능 횟수 체크
        int totalPartCount = eventMapper.getParticipationCount(eventId, userId);

        if (eventRewardInfo.getEventTarget() != null && totalPartCount >= eventRewardInfo.getEventTarget()) {
            log.warn("최종 목표를 달성한 이벤트입니다. userId: {}, eventId: {}", userId, eventId);
            return false;
        }

        // event_daily_limit_count 당일 참여가능 횟수 체크
        int todayPartCount = eventMapper.getTodayParticipationCount(eventId, userId, eventRewardInfo.getEventType());

        if (eventRewardInfo.getEventDailyLimitCount() != null && eventRewardInfo.getEventDailyLimitCount() > 0 && todayPartCount >= eventRewardInfo.getEventDailyLimitCount()) {
            log.warn("일일 참여 제한 횟수를 초과했습니다. userId: {}, eventId: {}", userId, eventId);
            return false;
        }

        // event_type(ATTENDANCE / etc...) 이벤트 유형에 따라 참여내역 생성
        int result = 0;
        if ("ATTENDANCE".equals(eventRewardInfo.getEventType())) {
            result = eventMapper.createAttendanceParticipation(eventId, userId);
        } else {
            EventParticipationVO vo = EventParticipationVO.builder()
                    .userId(userId)
                    .eventId(eventId)
                    .build();
            result = eventMapper.createParticipation(vo);
        }

        if (result < 1) {
            log.error("이벤트 참여이력 생성 실패. eventId: {}, userId: {}", eventId, userId);
            return false;
        }

        // 리워드 지급 체크(누적 참여 횟수가 req_count와 일치할 때 지급)
        if (eventRewardInfo.getRewardId() != null) {

            int currentPartCount = totalPartCount + 1;

            if (currentPartCount == eventRewardInfo.getReqCount()) {
                boolean isAlreadyReceived = eventMapper.checkRewardAlreadyReceived(eventId, userId, eventRewardInfo.getRewardId());

                if (!isAlreadyReceived) {
                    EventRewardReceiveVO rewardReceive = EventRewardReceiveVO.builder()
                            .userId(userId)
                            .eventId(eventId)
                            .rewardId(eventRewardInfo.getRewardId())
                            .build();
                    eventMapper.createEventRewardReceive(rewardReceive);

                    if (eventRewardInfo.getRewardPoint() != null && eventRewardInfo.getRewardPoint() > 0) {
                        eventMapper.updateUserPoint(userId, eventRewardInfo.getRewardPoint());
                        log.info("이벤트 보상 지급 완료 - 사용자: {}, 포인트: {}", userId, eventRewardInfo.getRewardPoint());
                    }
                }
            }
        }

        // 챌린지 경험치 자동 누적 처리
        UserChallengeDTO currentChallenge = eventMapper.getUserChallengeStatus(userId);
        if (currentChallenge != null) {
            log.info("챌린지 경험치 지급 처리 완료");
        }

        return true;
    }

    // 5. 이벤트 리워드 수령 처리
    @Transactional
    @Override
    public boolean receiveEventReward(Integer userId, Integer eventId) {
        EventResponseDTO eventRewardInfo = eventMapper.getEventRewardInfoByEventId(eventId);

        if (eventRewardInfo == null) {
            throw new IllegalArgumentException("해당 이벤트의 리워드 단계 정보가 존재하지 않습니다.");
        }

        EventRewardReceiveVO rewardReceive = EventRewardReceiveVO.builder()
                .userId(userId)
                .eventId(eventId)
                .rewardId(eventRewardInfo.getRewardId())
                .build();

        int receiveResult = eventMapper.createEventRewardReceive(rewardReceive);
        if (receiveResult < 1) {
            log.error("이벤트 리워드 수령이력 생성 실패");
            return false;
        }

        // 포인트 지급 처리
        Integer rewardPoint = eventRewardInfo.getRewardPoint();
        if (rewardPoint != null && rewardPoint > 0) {
            int pointResult = eventMapper.updateUserPoint(userId, rewardPoint);
            if (pointResult < 1) {
                throw new IllegalStateException("포인트 수령 처리 중 오류가 발생하였습니다.");
            }
        }

        return true;
    }

    // 6. 챌린지 참여 처리 및 리워드 수령 처리
    @Transactional
    @Override
    public boolean claimChallengeReward(Integer userId, Integer challengeId) {
        return true;
    }


    @Override
    public List<EventGetResponseDTO> getEventList(int userId){

        //모든 일회성이다
        //첫 피드 남기기 event level 1 -> 보상 테이블에서 이 이벤트레벨 머냐 물어보고 -> 해당 보상을 준다.
        //피드를 남긴 유저 있고 , 피드를 안남긴 유저 있다.
        //기간이 있다.

        List<EventNormalVO> eventNormalVOList =  eventMapper.getEvent(userId); //이거는 출석 제외 이벤트를 조회 합니다.

        Date now = new Date();

        //현재 시간 구분
        //이벤트 완료 구분
        //이벤트 보상 수령 구분
        //등록 순 정렬 완료
        eventNormalVOList.removeIf(event ->
                now.before(event.getStartAt()) || now.after(event.getEndAt())
        );

        log.info("출석 제외 이벤트 상태:" + eventNormalVOList);

        return eventNormalVOList.stream().map(EventGetResponseDTO::of).toList();
    }

    @Override
    public List<EventGetAttendanceResponseDTO> getAttendanceEventList(int userId){

        //출석 이벤트 조회

        List<EventAttendanceVO> eventAttendanceVOList =  eventMapper.getAttendanceEvent(userId); //이거는 출석 제외 이벤트를 조회 합니다.

        Date now = new Date();

        //현재 시간 구분
        //이벤트 완료 구분
        //이벤트 보상 수령 구분
        //오늘 출석 여부 구분
        //등록 순 정렬 완료
        eventAttendanceVOList.removeIf(event ->
                now.before(event.getStartAt()) || now.after(event.getEndAt())
        );

        log.info("출석 이벤트 상태:" + eventAttendanceVOList);

        return eventAttendanceVOList.stream().map(EventGetAttendanceResponseDTO::of).toList();

    }

    @Override
    @Transactional
    //이벤트 참여 버튼 눌렷을때
    public List<EventGetResponseDTO> joinEvent(int userId, int eventId){

        eventMapper.joinEvent(userId, eventId);

        //리펙토링 할 때
        //일반 이벤트 get return  EventGetResponseDTO
        //출석 이벤트 get return  EventGetAttendanceResponseDTO
        return getEventList(userId);
    }

    @Override
    @Transactional
    //출석 참여 버튼 눌렀을때
    public List<EventGetAttendanceResponseDTO> joinAttendanceEvent(int userId, int eventId){

        eventMapper.joinAttendanceEvent(userId, eventId);

        //리펙토링 할 때
        //일반 이벤트 get return  EventGetResponseDTO
        //출석 이벤트 get return  EventGetAttendanceResponseDTO
        return getAttendanceEventList(userId);
    }



}
