package org.scoula.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.event.domain.EventAttendanceVO;
import org.scoula.event.domain.EventNormalVO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventGetAttendanceResponseDTO {

    private int eventId;
    private String eventName;
    private String eventDesc;
    private String eventType;
    private String eventStatus;
    private String eventImgName;
    private int eventTarget; // 이벤트 목표
    private int currentTargetCount; // 현재 진행 중인 달성 횟수
    private int eventLevel;
    private int eventDailyLimitCount; //하루 이벤트 참여 횟수
    private Date startAt;
    private Date endAt;

    private int rewardId;
    private int rewardPoint;
    private int rewardExp;

    private boolean completed;
    private boolean rewardReceived;
    private boolean todayAttendanceCompleted;

    public static EventGetAttendanceResponseDTO of(EventAttendanceVO eventAttendanceVO){
        return  eventAttendanceVO == null ? null : EventGetAttendanceResponseDTO.builder()
                .eventId(eventAttendanceVO.getEventId())
                .eventName(eventAttendanceVO.getEventName())
                .eventDesc(eventAttendanceVO.getEventDesc())
                .eventType(eventAttendanceVO.getEventType())
                .eventStatus(eventAttendanceVO.getEventStatus())
                .eventImgName(eventAttendanceVO.getEventImgName())// gpt 만들어서
                .eventTarget(eventAttendanceVO.getEventTarget())
                .currentTargetCount(eventAttendanceVO.getCurrentTargetCount())
                .eventLevel(eventAttendanceVO.getEventLevel())
                .eventDailyLimitCount(eventAttendanceVO.getEventDailyLimitCount())
                .startAt(eventAttendanceVO.getStartAt())
                .endAt(eventAttendanceVO.getEndAt())
                .rewardId(eventAttendanceVO.getRewardId())
                .rewardPoint(eventAttendanceVO.getRewardPoint())
                .rewardExp(eventAttendanceVO.getRewardExp())
                .completed(eventAttendanceVO.isCompleted())
                .rewardReceived(eventAttendanceVO.isRewardReceived())
                .todayAttendanceCompleted(eventAttendanceVO.isTodayAttendanceCompleted())
                .build();
    }

}
