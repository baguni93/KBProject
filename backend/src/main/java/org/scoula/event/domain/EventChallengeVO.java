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
public class EventChallengeVO {
    private Integer challengeId;
    private String challengeName;
    private Integer rewardPoint;
    private Integer maxLevel;
    private Integer maxTarget;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
}
