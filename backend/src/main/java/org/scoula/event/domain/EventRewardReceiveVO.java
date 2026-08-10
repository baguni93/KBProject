package org.scoula.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRewardReceiveVO {
    private Integer recvId;
    private Integer eventId;
    private String eventName;
    private String eventType;
    private String eventImgName;
    private Integer rewardId;
    private Integer userId;
    private Integer rewardPoint;
    private Integer rewardExe;
    private LocalDateTime receivedAt;
}
