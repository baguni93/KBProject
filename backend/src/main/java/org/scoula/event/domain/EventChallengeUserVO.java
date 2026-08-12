package org.scoula.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventChallengeUserVO {
    private Integer userChallengeId;
    private Integer userId;
    private Integer challengeId;
    private Integer currentLevel;
    private Integer currentTarget;
    private Integer exp;
    private String status;
    private Date updatedAt;
    private Integer challengeLevelId;
    private Integer level;
    private Integer requiredExp;
    private Integer rewardPoint;
}
