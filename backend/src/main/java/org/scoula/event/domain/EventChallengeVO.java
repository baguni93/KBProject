package org.scoula.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
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
    private Date startDate;
    private Date endDate;
    private Date createdAt;
}
