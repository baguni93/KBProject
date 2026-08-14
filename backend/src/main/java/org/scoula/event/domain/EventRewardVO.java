package org.scoula.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRewardVO {
    private Integer rewardId;
    private Integer eventId;
    private String eventType;
    private Integer eventLevel;
    private Integer eventTarget;
    private Integer currentTargetCount;
    private Integer rewardPoint;
    private Integer rewardExp;
    private Integer reqCount;
    private String feedImgName;
    private String useYn;
    private Boolean joined;
    private Boolean rewardReceived;
}
