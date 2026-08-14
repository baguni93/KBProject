package org.scoula.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventNormalVO {

    private int eventId;
    private String eventName;
    private String eventDesc;
    private String eventType;
    private String eventStatus;
    private String eventCategory;
    private String eventImgName;
    private int eventTarget; // 이벤트 목표
    private int eventLevel;
    private int eventDailyLimitCount; //하루 이벤트 참여 횟수
    private int currentTargetCount; // 현재 진행 중인 달성 횟수
    private Date startAt;
    private Date endAt;

    private int rewardId;
    private int rewardPoint;
    private int rewardExp;

    private boolean completed;
    private boolean rewardReceived;
    private boolean joined;
}
