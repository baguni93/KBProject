package org.scoula.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.event.domain.EventNormalVO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventGetResponseDTO {

    private int eventId;
    private String eventName;
    private String eventDesc;
    private String eventType;
    private String eventStatus;
    private String eventCategory;
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
    private boolean joined;
    private boolean rewardReceived;

    public static EventGetResponseDTO of(EventNormalVO eventNormalVO){
        return  eventNormalVO == null ? null : EventGetResponseDTO.builder()
                .eventId(eventNormalVO.getEventId())
                .eventName(eventNormalVO.getEventName())
                .eventDesc(eventNormalVO.getEventDesc())
                .eventType(eventNormalVO.getEventType())
                .eventCategory(eventNormalVO.getEventCategory())
                .eventStatus(eventNormalVO.getEventStatus())
                .eventImgName(eventNormalVO.getEventImgName())// gpt 만들어서
                .eventTarget(eventNormalVO.getEventTarget())
                .currentTargetCount(eventNormalVO.getCurrentTargetCount())
                .eventLevel(eventNormalVO.getEventLevel())
                .eventDailyLimitCount(eventNormalVO.getEventDailyLimitCount())
                .startAt(eventNormalVO.getStartAt())
                .endAt(eventNormalVO.getEndAt())
                .rewardId(eventNormalVO.getRewardId())
                .rewardPoint(eventNormalVO.getRewardPoint())
                .rewardExp(eventNormalVO.getRewardExp())
                .completed(eventNormalVO.isCompleted())
                .joined(eventNormalVO.isJoined())
                .rewardReceived(eventNormalVO.isRewardReceived())
                .build();

    }
}
